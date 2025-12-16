package com.tcon.webid.config;

import com.tcon.webid.dto.TypingStatus;
import com.tcon.webid.service.ChatNotificationService;
import com.tcon.webid.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class WebSocketEventListener {

    private final ChatNotificationService chatNotificationService;
    private final JwtUtil jwtUtil;
    private final SimpMessagingTemplate messagingTemplate;

    // Map sessionId -> userId (subject from JWT or principal name)
    private final ConcurrentMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    public WebSocketEventListener(ChatNotificationService chatNotificationService,
                                  JwtUtil jwtUtil,
                                  SimpMessagingTemplate messagingTemplate) {
        this.chatNotificationService = chatNotificationService;
        this.jwtUtil = jwtUtil;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            Principal principal = accessor.getUser();
            String userId = null;

            if (principal != null) {
                userId = principal.getName();
            } else {
                // Try to read Authorization header from CONNECT frame
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader == null) {
                    authHeader = accessor.getFirstNativeHeader("authorization");
                }
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    Claims claims = jwtUtil.validateToken(token);
                    if (claims != null) {
                        final String subject = claims.getSubject();
                        userId = subject;
                        // Create and set a Principal so Spring can map this session to the user
                        Principal created = new Principal() {
                            private final String name = subject;
                            @Override
                            public String getName() {
                                return name;
                            }
                        };
                        accessor.setUser(created);
                    }
                }
            }

            String sessionId = accessor.getSessionId();
            if (userId != null) {
                sessionUserMap.put(sessionId, userId);
                chatNotificationService.updateOnlineStatus(userId, "ONLINE");
                log.info("WebSocket connected - session={} user={}", sessionId, userId);
            } else {
                log.debug("WebSocket connected without authenticated user - session={}", sessionId);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket connect event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = accessor.getSessionId();
            String userId = null;
            Principal principal = accessor.getUser();
            if (principal != null) userId = principal.getName();
            if (userId == null) userId = sessionUserMap.remove(sessionId);

            if (userId != null) {
                chatNotificationService.updateOnlineStatus(userId, "OFFLINE");
                log.info("WebSocket disconnected - session={} user={}", sessionId, userId);
            } else {
                log.debug("WebSocket disconnected - no mapped user for session={}", sessionId);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket disconnect event: {}", e.getMessage(), e);
        }
    }

    public Map<String, String> getSessionUserMap() {
        return Collections.unmodifiableMap(sessionUserMap);
    }
}
