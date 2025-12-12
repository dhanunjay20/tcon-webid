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

            // Save message to database
            ChatMessage savedMessage = chatService.save(chatMessage);

            // Update chat notification metadata (unread counts, last message, etc.)
            chatNotificationService.updateChatNotification(savedMessage);

            // Send the FULL saved message to recipient (real-time delivery)
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getRecipientId(),
                    "/queue/messages",
                    savedMessage
            );

            // Send the FULL saved message back to sender (confirmation + real-time update)
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getSenderId(),
                    "/queue/messages",
                    savedMessage
            );

            // Send chat list update notification to recipient
            chatNotificationService.sendChatListUpdate(savedMessage.getRecipientId());

            log.info("Message sent successfully - ID: {}, From: {} To: {}",
                    savedMessage.getId(), savedMessage.getSenderId(), savedMessage.getRecipientId());
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/typing")
    public void handleTyping(@Payload TypingStatus typingStatus) {
        try {
            if (typingStatus == null) {
                log.debug("Received null TypingStatus, ignoring");
                return;
            }

            String senderId = typingStatus.getSenderId();
            String recipientId = typingStatus.getRecipientId();
            String vendorId = typingStatus.getVendorId();

            if (senderId == null || senderId.isBlank() || recipientId == null || recipientId.isBlank()) {
                log.warn("TypingStatus missing sender or recipient: {}", typingStatus);
                return;
            }

            if (vendorId == null || vendorId.isBlank()) {
                log.warn("TypingStatus missing vendorId - frontend must send vendorId in payload: {}", typingStatus);
                return;
            }

            // Normalize nullable Boolean into a non-null boolean for logging
            boolean isTyping = Boolean.TRUE.equals(typingStatus.isTyping());

            log.info("User {} typing status: {} to {} (vendorId: {})", senderId, isTyping, recipientId, vendorId);

            // Delegate to service which now handles dedupe, persistence and notifying the recipient
            chatNotificationService.updateTypingStatus(senderId, recipientId, typingStatus.isTyping());

            // Do NOT send the typing payload here - service will push it when appropriate.
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
