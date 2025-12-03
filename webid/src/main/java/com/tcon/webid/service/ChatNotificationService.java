package com.tcon.webid.service;

import com.tcon.webid.dto.ChatListItemDto;
import com.tcon.webid.dto.UnreadCountDto;
import com.tcon.webid.entity.ChatMessage;
import com.tcon.webid.entity.ChatNotificationMetadata;
import com.tcon.webid.entity.User;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.repository.ChatNotificationMetadataRepository;
import com.tcon.webid.repository.UserRepository;
import com.tcon.webid.repository.VendorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for handling chat notifications and metadata
 */
@Slf4j
@Service
public class ChatNotificationService {

    @Autowired
    private ChatNotificationMetadataRepository notificationMetadataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    /**
     * Update or create chat notification metadata when a message is sent
     *
     * @param message The chat message that was sent
     */
    public void updateChatNotification(ChatMessage message) {
        try {
            // Update metadata for both sender and recipient
            updateMetadataForParticipant(
                    message.getSenderId(),
                    message.getRecipientId(),
                    message.getContent(),
                    message.getTimestamp(),
                    false // sender doesn't get unread count increase
            );

            updateMetadataForParticipant(
                    message.getRecipientId(),
                    message.getSenderId(),
                    message.getContent(),
                    message.getTimestamp(),
                    true // recipient gets unread count increase
            );

            log.info("Updated chat notifications for message: {}", message.getId());
        } catch (Exception e) {
            log.error("Error updating chat notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Update metadata for a single participant
     */
    private void updateMetadataForParticipant(String userId, String otherParticipantId,
                                               String lastMessage, String timestamp, boolean incrementUnread) {
        Optional<ChatNotificationMetadata> existingOpt =
                notificationMetadataRepository.findByUserIdAndOtherParticipantId(userId, otherParticipantId);

        ChatNotificationMetadata metadata;
        if (existingOpt.isPresent()) {
            metadata = existingOpt.get();
            metadata.setUpdatedAt(Instant.now().toString());
        } else {
            metadata = ChatNotificationMetadata.builder()
                    .userId(userId)
                    .otherParticipantId(otherParticipantId)
                    .chatId(generateChatId(userId, otherParticipantId))
                    .unreadCount(0)
                    .onlineStatus("OFFLINE")
                    .isTyping(false)
                    .createdAt(Instant.now().toString())
                    .updatedAt(Instant.now().toString())
                    .build();

            // Fetch participant info
            updateParticipantInfo(metadata, otherParticipantId);
        }

        // Update last message info
        metadata.setLastMessageContent(lastMessage);
        metadata.setLastMessageSenderId(otherParticipantId.equals(metadata.getOtherParticipantId()) ?
                otherParticipantId : userId);
        metadata.setLastMessageTimestamp(timestamp);

        // Increment unread count if this is for the recipient
        if (incrementUnread) {
            metadata.setUnreadCount(metadata.getUnreadCount() + 1);
        }

        notificationMetadataRepository.save(metadata);
    }

    /**
     * Fetch and update participant info from User or Vendor collection
     */
    private void updateParticipantInfo(ChatNotificationMetadata metadata, String participantId) {
        // Try to find as User first
        Optional<User> userOpt = userRepository.findById(participantId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            metadata.setOtherParticipantName(user.getFirstName() + " " + user.getLastName());
            metadata.setOtherParticipantType("USER");
            metadata.setOtherParticipantProfileUrl(user.getProfileUrl());
            return;
        }

        // Try to find as Vendor
        Optional<Vendor> vendorOpt = vendorRepository.findById(participantId);
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();
            metadata.setOtherParticipantName(vendor.getBusinessName());
            metadata.setOtherParticipantType("VENDOR");
            metadata.setOtherParticipantProfileUrl(null); // Vendors don't have profile URL
        }
    }

    /**
     * Generate deterministic chat ID
     */
    private String generateChatId(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + "_" + id2;
        } else {
            return id2 + "_" + id1;
        }
    }

    /**
     * Get all chat list items for a user/vendor
     *
     * @param userId MongoDB ObjectId of the user/vendor
     * @return List of chat list items
     */
    public List<ChatListItemDto> getChatList(String userId) {
        try {
            List<ChatNotificationMetadata> metadataList =
                    notificationMetadataRepository.findByUserIdOrderByLastMessageTimestampDesc(userId);

            return metadataList.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting chat list for user {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get unread message count for a user/vendor
     *
     * @param userId MongoDB ObjectId of the user/vendor
     * @return UnreadCountDto
     */
    public UnreadCountDto getUnreadCount(String userId) {
        try {
            List<ChatNotificationMetadata> unreadChats =
                    notificationMetadataRepository.findByUserIdAndUnreadCountGreaterThan(userId, 0);

            int totalUnread = unreadChats.stream()
                    .mapToInt(ChatNotificationMetadata::getUnreadCount)
                    .sum();

            return UnreadCountDto.builder()
                    .userId(userId)
                    .totalUnreadCount(totalUnread)
                    .unreadChatsCount(unreadChats.size())
                    .build();
        } catch (Exception e) {
            log.error("Error getting unread count for user {}: {}", userId, e.getMessage(), e);
            return UnreadCountDto.builder()
                    .userId(userId)
                    .totalUnreadCount(0)
                    .unreadChatsCount(0)
                    .build();
        }
    }

    /**
     * Mark all messages in a chat as read (reset unread count)
     *
     * @param userId             MongoDB ObjectId of the user
     * @param otherParticipantId MongoDB ObjectId of the other participant
     */
    public void markChatAsRead(String userId, String otherParticipantId) {
        try {
            Optional<ChatNotificationMetadata> metadataOpt =
                    notificationMetadataRepository.findByUserIdAndOtherParticipantId(userId, otherParticipantId);

            if (metadataOpt.isPresent()) {
                ChatNotificationMetadata metadata = metadataOpt.get();
                metadata.setUnreadCount(0);
                metadata.setUpdatedAt(Instant.now().toString());
                notificationMetadataRepository.save(metadata);
                log.info("Marked chat as read for user {} with participant {}", userId, otherParticipantId);
            }
        } catch (Exception e) {
            log.error("Error marking chat as read: {}", e.getMessage(), e);
        }
    }

    /**
     * Update online status for a user/vendor
     *
     * @param userId MongoDB ObjectId of the user
     * @param status "ONLINE" or "OFFLINE"
     */
    public void updateOnlineStatus(String userId, String status) {
        try {
            // Find all chats where this user is the other participant
            List<ChatNotificationMetadata> allMetadata = notificationMetadataRepository.findAll();

            for (ChatNotificationMetadata metadata : allMetadata) {
                if (metadata.getOtherParticipantId().equals(userId)) {
                    metadata.setOnlineStatus(status);
                    metadata.setUpdatedAt(Instant.now().toString());
                    notificationMetadataRepository.save(metadata);
                }
            }

            log.info("Updated online status to {} for user {}", status, userId);
        } catch (Exception e) {
            log.error("Error updating online status: {}", e.getMessage(), e);
        }
    }

    /**
     * Update typing status for a user/vendor
     *
     * @param userId             MongoDB ObjectId of the user who is typing
     * @param otherParticipantId MongoDB ObjectId of the recipient
     * @param isTyping           true if typing, false otherwise
     */
    public void updateTypingStatus(String userId, String otherParticipantId, boolean isTyping) {
        try {
            // Update the recipient's metadata to show the sender is typing
            Optional<ChatNotificationMetadata> metadataOpt =
                    notificationMetadataRepository.findByUserIdAndOtherParticipantId(otherParticipantId, userId);

            if (metadataOpt.isPresent()) {
                ChatNotificationMetadata metadata = metadataOpt.get();
                metadata.setTyping(isTyping);
                metadata.setUpdatedAt(Instant.now().toString());
                notificationMetadataRepository.save(metadata);
                log.debug("Updated typing status to {} for user {} in chat with {}",
                        isTyping, userId, otherParticipantId);
            }
        } catch (Exception e) {
            log.error("Error updating typing status: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete a chat (clear notification metadata)
     *
     * @param userId             MongoDB ObjectId of the user
     * @param otherParticipantId MongoDB ObjectId of the other participant
     */
    public void deleteChat(String userId, String otherParticipantId) {
        try {
            notificationMetadataRepository.deleteByUserIdAndOtherParticipantId(userId, otherParticipantId);
            log.info("Deleted chat notification for user {} with participant {}", userId, otherParticipantId);
        } catch (Exception e) {
            log.error("Error deleting chat: {}", e.getMessage(), e);
        }
    }

    /**
     * Convert ChatNotificationMetadata to ChatListItemDto
     */
    private ChatListItemDto convertToDto(ChatNotificationMetadata metadata) {
        return ChatListItemDto.builder()
                .participantId(metadata.getOtherParticipantId())
                .participantName(metadata.getOtherParticipantName())
                .participantType(metadata.getOtherParticipantType())
                .participantProfileUrl(metadata.getOtherParticipantProfileUrl())
                .chatId(metadata.getChatId())
                .lastMessage(metadata.getLastMessageContent())
                .lastMessageSenderId(metadata.getLastMessageSenderId())
                .lastMessageTimestamp(metadata.getLastMessageTimestamp())
                .unreadCount(metadata.getUnreadCount())
                .onlineStatus(metadata.getOnlineStatus())
                .isTyping(metadata.isTyping())
                .build();
    }

    /**
     * Refresh participant info in chat notifications (useful when user/vendor updates profile)
     *
     * @param participantId MongoDB ObjectId of the user/vendor whose info changed
     */
    public void refreshParticipantInfo(String participantId) {
        try {
            List<ChatNotificationMetadata> allMetadata = notificationMetadataRepository.findAll();

            for (ChatNotificationMetadata metadata : allMetadata) {
                if (metadata.getOtherParticipantId().equals(participantId)) {
                    updateParticipantInfo(metadata, participantId);
                    metadata.setUpdatedAt(Instant.now().toString());
                    notificationMetadataRepository.save(metadata);
                }
            }

            log.info("Refreshed participant info for {}", participantId);
        } catch (Exception e) {
            log.error("Error refreshing participant info: {}", e.getMessage(), e);
        }
    }
}

