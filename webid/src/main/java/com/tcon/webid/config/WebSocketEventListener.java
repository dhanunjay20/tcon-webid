package com.tcon.webid.config;

import com.tcon.webid.service.ChatNotificationService;
import com.tcon.webid.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class WebSocketEventListener {

    private final ChatNotificationService chatNotificationService;
    private final JwtUtil jwtUtil;

    // Map sessionId -> userId (subject from JWT or principal name)
    private final ConcurrentMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @Autowired
    public WebSocketEventListener(ChatNotificationService chatNotificationService, JwtUtil jwtUtil) {
        this.chatNotificationService = chatNotificationService;
        this.jwtUtil = jwtUtil;
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
                        userId = claims.getSubject();
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
}

