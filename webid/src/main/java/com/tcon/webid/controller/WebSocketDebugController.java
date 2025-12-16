package com.tcon.webid.controller;

import com.tcon.webid.config.WebSocketEventListener;
import com.tcon.webid.dto.TypingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/ws-debug")
@RequiredArgsConstructor
public class WebSocketDebugController {

    private final WebSocketEventListener wsListener;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/sessions")
    public ResponseEntity<Map<String, String>> listSessions() {
        return ResponseEntity.ok(wsListener.getSessionUserMap());
    }

    @PostMapping("/send-typing")
    public ResponseEntity<String> sendTypingTest(@RequestBody TypingStatus payload) {
        // send to user destination (simulate the server sending typing status)
        messagingTemplate.convertAndSendToUser(payload.getRecipientId(), "/queue/typing", payload);
        return ResponseEntity.ok("Sent");
    }
}

