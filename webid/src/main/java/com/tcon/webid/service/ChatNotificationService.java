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
import com.tcon.webid.dto.TypingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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

            // Also update Vendor's online status if this is a vendor
            try {
                Vendor vendor = vendorRepository.findById(userId).orElse(null);
                if (vendor != null) {
                    vendor.setIsOnline("ONLINE".equalsIgnoreCase(status));
                    vendor.setLastSeenAt(Instant.now().toString());
                    vendorRepository.save(vendor);
                    log.info("Updated vendor online status: {} for vendor {}", status, userId);
                }
            } catch (Exception ve) {
                log.debug("Not a vendor ID or vendor update failed: {}", userId);
            }

            // Also update User's online status if this is a user
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    // Users may not have isOnline field, so just log the update
                    log.debug("Updated user status to {} for user {}", status, userId);
                }
            } catch (Exception ue) {
                log.debug("Not a user ID or user update failed: {}", userId);
            }

            log.info("Updated online status to {} for user {}", status, userId);
        } catch (Exception e) {
            log.error("Error updating online status: {}", e.getMessage(), e);
        }
    }

    /**
     * Update typing status for a user/vendor
     *
     * @param userId MongoDB ObjectId of the user who is typing (sender)
     * @param otherParticipantId MongoDB ObjectId of the recipient
     * @param isTypingObj        true if typing, false otherwise (nullable; null treated as false)
     */
    public void updateTypingStatus(String userId, String otherParticipantId, Boolean isTypingObj) {
        try {
            if (userId == null || otherParticipantId == null) {
                log.debug("updateTypingStatus called with null userId/otherParticipantId: {}, {}", userId, otherParticipantId);
                return;
            }

            boolean newTyping = Boolean.TRUE.equals(isTypingObj);

            // Update the recipient's metadata to show the sender is typing.
            // We look up the metadata record for the recipient where the other participant is the sender.
            Optional<ChatNotificationMetadata> metadataOpt =
                    notificationMetadataRepository.findByUserIdAndOtherParticipantId(otherParticipantId, userId);

            if (metadataOpt.isPresent()) {
                ChatNotificationMetadata metadata = metadataOpt.get();

                // If there is no change in typing state, do nothing (avoid redundant DB writes & notifications)
                boolean currentTyping = metadata.isTyping();
                if (currentTyping == newTyping) {
                    log.debug("No typing state change for chat {} (participant {} -> {}), still {}",
                            metadata.getChatId(), userId, otherParticipantId, newTyping);
                    return;
                }

                metadata.setTyping(newTyping);
                metadata.setUpdatedAt(Instant.now().toString());
                notificationMetadataRepository.save(metadata);

                log.info("Updated typing status to {} for user {} ({}) in chat with {}",
                        newTyping, userId, metadata.getOtherParticipantType(), otherParticipantId);

                // Determine sender type based on metadata
                String senderType = "USER".equals(metadata.getOtherParticipantType()) ? "VENDOR" : "USER";

                // Send a compact typing DTO to the recipient to update their UI.
                // Keep the payload minimal and deterministic.
                TypingStatus outbound = TypingStatus.builder()
                        .senderId(userId)
                        .recipientId(otherParticipantId)
                        .senderType(senderType)
                        .typing(newTyping)
                        .build();

                try {
                    messagingTemplate.convertAndSendToUser(
                            otherParticipantId,
                            "/queue/typing",
                            outbound
                    );
                    log.debug("Sent typing notification to user {}: sender={}, senderType={}, typing={}",
                            otherParticipantId, userId, senderType, newTyping);
                } catch (Exception me) {
                    log.warn("Failed to send typing notification to user {}: {}", otherParticipantId, me.getMessage());
                }
            } else {
                // No metadata existed. We avoid creating a metadata record just for typing.
                // Optionally, you can create a lightweight metadata record if you want typing to work
                // even before a prior message exists.
                log.debug("No ChatNotificationMetadata found for user {} with participant {} — skipping typing update", otherParticipantId, userId);
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

    /**
     * Send real-time chat list update to a user via WebSocket
     * This pushes the updated unread count and chat list to the user
     *
     * @param userId MongoDB ObjectId of the user to notify
     */
    public void sendChatListUpdate(String userId) {
        try {
            // Get updated unread count
            UnreadCountDto unreadCount = getUnreadCount(userId);

            // Send unread count update via WebSocket
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/chat-updates",
                    unreadCount
            );

            log.info("Sent chat list update to user: {} with {} unread messages",
                    userId, unreadCount.getTotalUnreadCount());
        } catch (Exception e) {
            log.error("Error sending chat list update: {}", e.getMessage(), e);
        }
    }
}
