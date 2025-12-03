package com.tcon.webid.service;

import com.tcon.webid.entity.ChatMessage;
import com.tcon.webid.entity.ChatMessage.MessageStatus;
import com.tcon.webid.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling chat message operations
 */
@Slf4j
@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * Save a chat message with initial status SENT
     *
     * @param chatMessage The message to save
     * @return The saved message
     */
    public ChatMessage save(ChatMessage chatMessage) {
        try {
            // Set status to SENT
            chatMessage.setStatus(MessageStatus.SENT);

            // Generate chatId
            chatMessage.setChatId(getChatId(chatMessage.getSenderId(), chatMessage.getRecipientId()));

            // Set timestamp
            chatMessage.setTimestamp(Instant.now().toString());

            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            log.info("Saved chat message with id: {} from: {} to: {}",
                    savedMessage.getId(), savedMessage.getSenderId(), savedMessage.getRecipientId());

            return savedMessage;
        } catch (Exception e) {
            log.error("Error saving chat message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save chat message", e);
        }
    }

    /**
     * Find all messages between two users
     *
     * @param senderId    ID of the sender
     * @param recipientId ID of the recipient
     * @return List of chat messages
     */
    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        try {
            String chatId = getChatId(senderId, recipientId);
            List<ChatMessage> messages = chatMessageRepository.findByChatIdOrderByTimestampAsc(chatId);
            log.info("Retrieved {} messages for chat: {}", messages.size(), chatId);
            return messages;
        } catch (Exception e) {
            log.error("Error finding chat messages: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Generate a unique chat ID from two user IDs
     * The chat ID is deterministic (same for both users)
     *
     * @param senderId    First user ID
     * @param recipientId Second user ID
     * @return Chat ID in format "id1_id2" (alphabetically sorted)
     */
    public String getChatId(String senderId, String recipientId) {
        // Sort alphabetically to ensure same chatId regardless of who sends first
        if (senderId.compareTo(recipientId) < 0) {
            return senderId + "_" + recipientId;
        } else {
            return recipientId + "_" + senderId;
        }
    }

    /**
     * Mark all messages from a sender to recipient as READ
     *
     * @param senderId    ID of the original sender
     * @param recipientId ID of the recipient (who is reading)
     * @return Number of messages marked as read
     */
    public int markMessagesAsRead(String senderId, String recipientId) {
        try {
            // Find all messages sent by senderId to recipientId that are not yet READ
            List<ChatMessage> unreadMessages = chatMessageRepository
                    .findBySenderIdAndRecipientIdAndStatus(senderId, recipientId, MessageStatus.DELIVERED);

            // Also mark SENT messages as READ
            List<ChatMessage> sentMessages = chatMessageRepository
                    .findBySenderIdAndRecipientIdAndStatus(senderId, recipientId, MessageStatus.SENT);

            unreadMessages.addAll(sentMessages);

            // Update status to READ
            for (ChatMessage message : unreadMessages) {
                message.setStatus(MessageStatus.READ);
                chatMessageRepository.save(message);
            }

            log.info("Marked {} messages as READ from {} to {}",
                    unreadMessages.size(), senderId, recipientId);

            return unreadMessages.size();
        } catch (Exception e) {
            log.error("Error marking messages as read: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Mark messages as DELIVERED when recipient is online
     *
     * @param senderId    ID of the original sender
     * @param recipientId ID of the recipient
     * @return Number of messages marked as delivered
     */
    public int markMessagesAsDelivered(String senderId, String recipientId) {
        try {
            List<ChatMessage> sentMessages = chatMessageRepository
                    .findBySenderIdAndRecipientIdAndStatus(senderId, recipientId, MessageStatus.SENT);

            for (ChatMessage message : sentMessages) {
                message.setStatus(MessageStatus.DELIVERED);
                chatMessageRepository.save(message);
            }

            log.info("Marked {} messages as DELIVERED from {} to {}",
                    sentMessages.size(), senderId, recipientId);

            return sentMessages.size();
        } catch (Exception e) {
            log.error("Error marking messages as delivered: {}", e.getMessage(), e);
            return 0;
        }
    }
}

