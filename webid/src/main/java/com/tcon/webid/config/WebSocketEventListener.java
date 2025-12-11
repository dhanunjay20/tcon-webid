package com.tcon.webid.config;

import com.tcon.webid.service.ChatService;
import com.tcon.webid.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WebSocket event listener for handling connection lifecycle events.
 * Manages user presence (online/offline status) based on WebSocket connections.
 */
@Slf4j
@Component
public class WebSocketEventListener {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    // Map sessionId -> UserSession (contains userId and userType)
    private final ConcurrentMap<String, UserSession> sessionUserMap = new ConcurrentHashMap<>();

    @Autowired
    public WebSocketEventListener(ChatService chatService, JwtUtil jwtUtil) {
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Handle WebSocket connection event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = accessor.getSessionId();

            UserSession userSession = extractUserSession(accessor);

            if (userSession != null) {
                sessionUserMap.put(sessionId, userSession);

                // Store user info in session attributes for later use
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    sessionAttributes.put("userId", userSession.getUserId());
                    sessionAttributes.put("userType", userSession.getUserType());
                }

                // Update online status
                chatService.updateOnlineStatus(userSession.getUserId(), userSession.getUserType(), true);

                // Mark any pending messages as delivered
                chatService.markMessagesAsDelivered(userSession.getUserId());

                log.info("WebSocket connected - session={} userId={} userType={}",
                        sessionId, userSession.getUserId(), userSession.getUserType());
            } else {
                log.debug("WebSocket connected without authenticated user - session={}", sessionId);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket connect event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle WebSocket disconnection event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = accessor.getSessionId();

            UserSession userSession = sessionUserMap.remove(sessionId);

            // Try to get from principal if not in map
            if (userSession == null) {
                Principal principal = accessor.getUser();
                if (principal != null) {
                    userSession = new UserSession(principal.getName(), "USER");
                }
            }

            if (userSession != null) {
                // Update offline status
                chatService.updateOnlineStatus(userSession.getUserId(), userSession.getUserType(), false);

                log.info("WebSocket disconnected - session={} userId={} userType={}",
                        sessionId, userSession.getUserId(), userSession.getUserType());
            } else {
                log.debug("WebSocket disconnected - no mapped user for session={}", sessionId);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket disconnect event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle WebSocket subscription event (for logging/debugging)
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = accessor.getSessionId();
            String destination = accessor.getDestination();

            UserSession userSession = sessionUserMap.get(sessionId);

            if (userSession != null) {
                log.debug("WebSocket subscription - session={} userId={} destination={}",
                        sessionId, userSession.getUserId(), destination);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket subscribe event: {}", e.getMessage(), e);
        }
    }

    /**
     * Extract user session information from WebSocket headers
     */
    private UserSession extractUserSession(StompHeaderAccessor accessor) {
        String userId = null;
        String userType = "USER"; // Default to USER

        // Try to get from principal first
        Principal principal = accessor.getUser();
        if (principal != null) {
            userId = principal.getName();
        }

        // Try to get from Authorization header if no principal
        if (userId == null) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null) {
                authHeader = accessor.getFirstNativeHeader("authorization");
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Claims claims = jwtUtil.validateToken(token);
                    if (claims != null) {
                        userId = claims.getSubject();
                        // Try to get user type from claims
                        Object typeObj = claims.get("userType");
                        if (typeObj != null) {
                            userType = typeObj.toString();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to validate JWT token: {}", e.getMessage());
                }
            }
        }

        // Also check for custom headers
        String userIdHeader = accessor.getFirstNativeHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            userId = userIdHeader;
        }

        String userTypeHeader = accessor.getFirstNativeHeader("X-User-Type");
        if (userTypeHeader != null && !userTypeHeader.isEmpty()) {
            userType = userTypeHeader;
        }

        // Check for vendor header
        String vendorIdHeader = accessor.getFirstNativeHeader("X-Vendor-Id");
        if (vendorIdHeader != null && !vendorIdHeader.isEmpty()) {
            userId = vendorIdHeader;
            userType = "VENDOR";
        }

        if (userId != null) {
            return new UserSession(userId, userType);
        }

        return null;
    }

    /**
     * Get user ID for a session
     */
    public String getUserIdForSession(String sessionId) {
        UserSession session = sessionUserMap.get(sessionId);
        return session != null ? session.getUserId() : null;
    }

    /**
     * Get user type for a session
     */
    public String getUserTypeForSession(String sessionId) {
        UserSession session = sessionUserMap.get(sessionId);
        return session != null ? session.getUserType() : null;
    }

    /**
     * Check if a user has any active sessions
     */
    public boolean hasActiveSessions(String userId) {
        return sessionUserMap.values().stream()
                .anyMatch(session -> session.getUserId().equals(userId));
    }

    /**
     * Get count of active sessions for a user
     */
    public int getActiveSessionCount(String userId) {
        return (int) sessionUserMap.values().stream()
                .filter(session -> session.getUserId().equals(userId))
                .count();
    }

    /**
     * Inner class to hold user session information
     */
    private static class UserSession {
        private final String userId;
        private final String userType;

        public UserSession(String userId, String userType) {
            this.userId = userId;
            this.userType = userType;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserType() {
            return userType;
        }
    }
}

