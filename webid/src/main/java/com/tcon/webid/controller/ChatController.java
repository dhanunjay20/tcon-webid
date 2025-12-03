package com.tcon.webid.controller;

import com.tcon.webid.dto.ChatNotification;
import com.tcon.webid.dto.TypingStatus;
import com.tcon.webid.dto.UserStatus;
import com.tcon.webid.entity.ChatMessage;
import com.tcon.webid.service.ChatService;
import com.tcon.webid.service.ChatNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatNotificationService chatNotificationService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        try {
            log.info("Processing message from {} to {}",
                    chatMessage.getSenderId(), chatMessage.getRecipientId());

            ChatMessage savedMessage = chatService.save(chatMessage);

            chatNotificationService.updateChatNotification(savedMessage);

            ChatNotification notification = ChatNotification.builder()
                    .id(savedMessage.getId())
                    .senderId(savedMessage.getSenderId())
                    .content(savedMessage.getContent())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    chatMessage.getRecipientId(),
                    "/queue/messages",
                    notification
            );

            log.info("Message sent successfully to user: {}", chatMessage.getRecipientId());
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/typing")
    public void handleTyping(@Payload TypingStatus typingStatus) {
        try {
            log.info("User {} typing status: {} to {}",
                    typingStatus.getSenderId(), typingStatus.isTyping(), typingStatus.getRecipientId());

            chatNotificationService.updateTypingStatus(
                    typingStatus.getSenderId(),
                    typingStatus.getRecipientId(),
                    typingStatus.isTyping()
            );

            messagingTemplate.convertAndSendToUser(
                    typingStatus.getRecipientId(),
                    "/queue/typing",
                    typingStatus
            );
        } catch (Exception e) {
            log.error("Error handling typing indicator: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/read")
    public void handleReadReceipt(@Payload ChatNotification readReceipt) {
        try {
            log.info("Marking messages as read from {} to {}",
                    readReceipt.getSenderId(), readReceipt.getId());

            int count = chatService.markMessagesAsRead(readReceipt.getSenderId(), readReceipt.getId());

            chatNotificationService.markChatAsRead(readReceipt.getId(), readReceipt.getSenderId());

            ChatNotification notification = ChatNotification.builder()
                    .id(readReceipt.getId())
                    .senderId(readReceipt.getSenderId())
                    .content("READ")
                    .build();

            messagingTemplate.convertAndSendToUser(
                    readReceipt.getSenderId(),
                    "/queue/read",
                    notification
            );

            log.info("Read receipt sent to user: {}", readReceipt.getSenderId());
        } catch (Exception e) {
            log.error("Error handling read receipt: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/status")
    public void handleUserStatus(@Payload UserStatus userStatus) {
        try {
            log.info("User {} status: {}", userStatus.getUserId(), userStatus.getStatus());

            chatNotificationService.updateOnlineStatus(userStatus.getUserId(), userStatus.getStatus());

            messagingTemplate.convertAndSend("/topic/status", userStatus);
        } catch (Exception e) {
            log.error("Error handling user status: {}", e.getMessage(), e);
        }
    }
}

