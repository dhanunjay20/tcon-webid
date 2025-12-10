package com.tcon.webid.controller;

import com.tcon.webid.entity.ChatMessage;
import com.tcon.webid.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class ChatRestController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String senderId,
            @PathVariable String recipientId) {
        try {
            log.info("Fetching chat history between {} and {}", senderId, recipientId);
            List<ChatMessage> messages = chatService.findChatMessages(senderId, recipientId);
            log.info("Retrieved {} messages", messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error retrieving chat history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/chatId/{senderId}/{recipientId}")
    public ResponseEntity<String> getChatId(
            @PathVariable String senderId,
            @PathVariable String recipientId) {
        try {
            String chatId = chatService.getChatId(senderId, recipientId);
            return ResponseEntity.ok(chatId);
        } catch (Exception e) {
            log.error("Error generating chat ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/delivered/{senderId}/{recipientId}")
    public ResponseEntity<Integer> markAsDelivered(
            @PathVariable String senderId,
            @PathVariable String recipientId) {
        try {
            log.info("Marking messages as delivered from {} to {}", senderId, recipientId);
            int count = chatService.markMessagesAsDelivered(senderId, recipientId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error marking messages as delivered: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/read/{senderId}/{recipientId}")
    public ResponseEntity<Integer> markAsRead(
            @PathVariable String senderId,
            @PathVariable String recipientId) {
        try {
            log.info("Marking messages as read from {} to {}", senderId, recipientId);
            int count = chatService.markMessagesAsRead(senderId, recipientId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error marking messages as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

