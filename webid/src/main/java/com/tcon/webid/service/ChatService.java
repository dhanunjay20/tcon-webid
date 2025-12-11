package com.tcon.webid.service;

import com.tcon.webid.dto.*;
import com.tcon.webid.entity.ChatMessage;
import com.tcon.webid.entity.ChatMessage.MessageStatus;
import com.tcon.webid.entity.ChatMessage.MessageType;
import com.tcon.webid.entity.ChatRoom;
import com.tcon.webid.entity.User;
import com.tcon.webid.entity.UserPresence;
import com.tcon.webid.entity.UserPresence.PresenceStatus;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.repository.ChatMessageRepository;
import com.tcon.webid.repository.ChatRoomRepository;
import com.tcon.webid.repository.UserPresenceRepository;
import com.tcon.webid.repository.UserRepository;
import com.tcon.webid.repository.VendorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Main service for handling all chat operations.
 * Provides real-time messaging with WhatsApp-like features:
 * - Message status tracking (SENT, DELIVERED, READ, FAILED)
 * - Online/Offline status
 * - Typing indicators
 * - Last seen tracking
 * - Unread message counts
 */
@Slf4j
@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserPresenceRepository presenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Cache for typing status with auto-expiry (userId -> timestamp)
    private final ConcurrentHashMap<String, Long> typingCache = new ConcurrentHashMap<>();
    private static final long TYPING_TIMEOUT_MS = 5000; // 5 seconds

    // ==================== MESSAGE OPERATIONS ====================

    /**
     * Send a new chat message
     */
    @Transactional
    public ChatMessageResponseDto sendMessage(String senderId, String senderType, ChatMessageRequestDto request) {
        try {
            log.info("Processing message from {} ({}) to {} ({})",
                    senderId, senderType, request.getRecipientId(), request.getRecipientType());

            // Validate sender and recipient
            String senderName = getParticipantName(senderId, senderType);
            String recipientName = getParticipantName(request.getRecipientId(), request.getRecipientType());

            if (senderName == null) {
                throw new IllegalArgumentException("Sender not found: " + senderId);
            }
            if (recipientName == null) {
                throw new IllegalArgumentException("Recipient not found: " + request.getRecipientId());
            }

            // Generate chat ID
            String chatId = ChatMessage.generateChatId(senderId, request.getRecipientId());

            // Create message entity
            ChatMessage message = ChatMessage.builder()
                    .chatId(chatId)
                    .senderId(senderId)
                    .senderType(senderType)
                    .senderName(senderName)
                    .recipientId(request.getRecipientId())
                    .recipientType(request.getRecipientType())
                    .recipientName(recipientName)
                    .content(request.getContent())
                    .messageType(parseMessageType(request.getMessageType()))
                    .mediaUrl(request.getMediaUrl())
                    .fileName(request.getFileName())
                    .tempId(request.getTempId())
                    .status(MessageStatus.SENT)
                    .timestamp(Instant.now().toString())
                    .sentAt(Instant.now().toString())
                    .build();

            // Save message
            ChatMessage savedMessage = messageRepository.save(message);
            log.info("Message saved with ID: {}", savedMessage.getId());

            // Update or create chat room
            updateChatRoom(savedMessage);

            // Check if recipient is online and mark as delivered
            if (isUserOnline(request.getRecipientId())) {
                savedMessage.setStatus(MessageStatus.DELIVERED);
                savedMessage.setDeliveredAt(Instant.now().toString());
                savedMessage = messageRepository.save(savedMessage);
                log.info("Message {} marked as DELIVERED (recipient online)", savedMessage.getId());
            }

            // Convert to response DTO
            ChatMessageResponseDto response = convertToResponseDto(savedMessage, senderId);

            // Send real-time notifications
            sendMessageNotifications(savedMessage);

            return response;

        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);

            // Send failure notification to sender
            ChatEventDto failureEvent = ChatEventDto.builder()
                    .eventType(ChatEventDto.EventType.MESSAGE_FAILED)
                    .tempId(request.getTempId())
                    .senderId(senderId)
                    .recipientId(request.getRecipientId())
                    .errorMessage(e.getMessage())
                    .build();
            sendToUser(senderId, "/queue/chat-events", failureEvent);

            throw new RuntimeException("Failed to send message: " + e.getMessage(), e);
        }
    }

    /**
     * Get chat history between two participants
     */
    public List<ChatMessageResponseDto> getChatHistory(String userId, String otherParticipantId) {
        String chatId = ChatMessage.generateChatId(userId, otherParticipantId);
        List<ChatMessage> messages = messageRepository.findByChatIdAndDeletedFalseOrderByTimestampAsc(chatId);

        return messages.stream()
                .map(msg -> convertToResponseDto(msg, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get chat history with pagination
     */
    public Page<ChatMessageResponseDto> getChatHistoryPaginated(String userId, String otherParticipantId,
                                                                 int page, int size) {
        String chatId = ChatMessage.generateChatId(userId, otherParticipantId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = messageRepository.findByChatIdAndDeletedFalseOrderByTimestampDesc(chatId, pageable);

        return messages.map(msg -> convertToResponseDto(msg, userId));
    }

    /**
     * Mark messages as delivered
     */
    @Transactional
    public int markMessagesAsDelivered(String recipientId) {
        List<ChatMessage> undeliveredMessages = messageRepository.findUndeliveredMessagesForRecipient(recipientId);
        String now = Instant.now().toString();
        int count = 0;

        for (ChatMessage message : undeliveredMessages) {
            message.setStatus(MessageStatus.DELIVERED);
            message.setDeliveredAt(now);
            messageRepository.save(message);
            count++;

            // Notify sender about delivery
            ChatEventDto deliveryEvent = ChatEventDto.messageDelivered(
                    message.getChatId(), message.getId(), message.getSenderId());
            sendToUser(message.getSenderId(), "/queue/chat-events", deliveryEvent);
        }

        log.info("Marked {} messages as DELIVERED for recipient {}", count, recipientId);
        return count;
    }

    /**
     * Mark messages as read in a specific chat
     */
    @Transactional
    public int markMessagesAsRead(String chatId, String readerId) {
        List<ChatMessage> unreadMessages = messageRepository.findUnreadMessagesInChat(chatId, readerId);
        String now = Instant.now().toString();
        int count = 0;
        Set<String> notifiedSenders = new HashSet<>();

        for (ChatMessage message : unreadMessages) {
            message.setStatus(MessageStatus.READ);
            message.setReadAt(now);
            messageRepository.save(message);
            count++;

            // Notify sender about read receipt (only once per sender)
            if (!notifiedSenders.contains(message.getSenderId())) {
                ChatEventDto readEvent = ChatEventDto.messageRead(
                        message.getChatId(), message.getId(), message.getSenderId());
                sendToUser(message.getSenderId(), "/queue/chat-events", readEvent);
                notifiedSenders.add(message.getSenderId());
            }
        }

        // Reset unread count in chat room
        chatRoomRepository.findByChatId(chatId).ifPresent(room -> {
            room.resetUnreadCountFor(readerId);
            room.setUpdatedAt(now);
            chatRoomRepository.save(room);
        });

        // Send unread count update to reader
        sendUnreadCountUpdate(readerId);

        log.info("Marked {} messages as READ in chat {} for reader {}", count, chatId, readerId);
        return count;
    }

    // ==================== CHAT LIST OPERATIONS ====================

    /**
     * Get chat list for a user (inbox view)
     */
    public List<ChatListItemDto> getChatList(String userId) {
        List<ChatRoom> rooms1 = chatRoomRepository.findByParticipant1IdAndActiveTrueOrderByLastMessageTimestampDesc(userId);
        List<ChatRoom> rooms2 = chatRoomRepository.findByParticipant2IdAndActiveTrueOrderByLastMessageTimestampDesc(userId);

        // Combine and sort
        List<ChatRoom> allRooms = new ArrayList<>();
        allRooms.addAll(rooms1);
        allRooms.addAll(rooms2);
        allRooms.sort((a, b) -> {
            String tsA = a.getLastMessageTimestamp();
            String tsB = b.getLastMessageTimestamp();
            if (tsA == null && tsB == null) return 0;
            if (tsA == null) return 1;
            if (tsB == null) return -1;
            return tsB.compareTo(tsA);
        });

        return allRooms.stream()
                .map(room -> convertToChatListItem(room, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get unread message count for a user
     */
    public UnreadCountDto getUnreadCount(String userId) {
        List<ChatRoom> unread1 = chatRoomRepository.findUnreadAsParticipant1(userId);
        List<ChatRoom> unread2 = chatRoomRepository.findUnreadAsParticipant2(userId);

        int totalUnread = 0;
        int unreadChats = 0;

        for (ChatRoom room : unread1) {
            totalUnread += room.getParticipant1UnreadCount();
            if (room.getParticipant1UnreadCount() > 0) unreadChats++;
        }
        for (ChatRoom room : unread2) {
            totalUnread += room.getParticipant2UnreadCount();
            if (room.getParticipant2UnreadCount() > 0) unreadChats++;
        }

        return UnreadCountDto.builder()
                .userId(userId)
                .totalUnreadCount(totalUnread)
                .unreadChatsCount(unreadChats)
                .build();
    }

    // ==================== PRESENCE OPERATIONS ====================

    /**
     * Update user online status
     */
    @Transactional
    public void updateOnlineStatus(String userId, String userType, boolean online) {
        String now = Instant.now().toString();

        // Update or create presence record
        UserPresence presence = presenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPresence.builder()
                        .userId(userId)
                        .userType(userType)
                        .displayName(getParticipantName(userId, userType))
                        .createdAt(now)
                        .build());

        if (online) {
            presence.goOnline();
        } else {
            presence.goOffline();
        }
        presenceRepository.save(presence);

        // Update all chat rooms where this user is a participant
        updateChatRoomsOnlineStatus(userId, online, now);

        // Broadcast status change to relevant users
        broadcastPresenceUpdate(userId, online, presence.getLastSeen());

        log.info("User {} ({}) is now {}", userId, userType, online ? "ONLINE" : "OFFLINE");
    }

    /**
     * Check if user is online
     */
    public boolean isUserOnline(String userId) {
        return presenceRepository.findByUserId(userId)
                .map(UserPresence::isTrulyOnline)
                .orElse(false);
    }

    /**
     * Get user presence status
     */
    public UserPresenceDto getUserPresence(String userId) {
        return presenceRepository.findByUserId(userId)
                .map(this::convertToPresenceDto)
                .orElse(UserPresenceDto.builder()
                        .userId(userId)
                        .status("OFFLINE")
                        .build());
    }

    // ==================== TYPING OPERATIONS ====================

    /**
     * Update typing status
     */
    public void updateTypingStatus(String senderId, String senderType, String recipientId, boolean typing) {
        String chatId = ChatMessage.generateChatId(senderId, recipientId);
        String cacheKey = senderId + "_" + recipientId;

        // Debounce typing notifications
        if (typing) {
            Long lastTyping = typingCache.get(cacheKey);
            if (lastTyping != null && System.currentTimeMillis() - lastTyping < 2000) {
                return; // Skip if typed recently
            }
            typingCache.put(cacheKey, System.currentTimeMillis());
        } else {
            typingCache.remove(cacheKey);
        }

        // Update chat room
        chatRoomRepository.findByChatId(chatId).ifPresent(room -> {
            room.setTypingStatus(senderId, typing);
            chatRoomRepository.save(room);
        });

        // Send typing notification to recipient
        ChatEventDto typingEvent = ChatEventDto.typingStatus(chatId, senderId, recipientId, typing);
        sendToUser(recipientId, "/queue/chat-events", typingEvent);

        log.debug("Typing status: {} is {} typing to {}", senderId, typing ? "" : "not", recipientId);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Update or create chat room when a message is sent
     */
    private void updateChatRoom(ChatMessage message) {
        String chatId = message.getChatId();
        String now = Instant.now().toString();

        ChatRoom room = chatRoomRepository.findByChatId(chatId)
                .orElseGet(() -> createNewChatRoom(message));

        // Update last message info
        room.setLastMessageContent(truncateMessage(message.getContent()));
        room.setLastMessageSenderId(message.getSenderId());
        room.setLastMessageTimestamp(message.getTimestamp());
        room.setLastMessageStatus(message.getStatus().toString());
        room.setUpdatedAt(now);

        // Increment unread count for recipient
        room.incrementUnreadCountFor(message.getRecipientId());

        chatRoomRepository.save(room);
    }

    /**
     * Create a new chat room
     */
    private ChatRoom createNewChatRoom(ChatMessage message) {
        String now = Instant.now().toString();

        // Determine participant order (alphabetically)
        String p1Id, p1Type, p1Name, p1ProfileUrl;
        String p2Id, p2Type, p2Name, p2ProfileUrl;

        if (message.getSenderId().compareTo(message.getRecipientId()) < 0) {
            p1Id = message.getSenderId();
            p1Type = message.getSenderType();
            p1Name = message.getSenderName();
            p1ProfileUrl = getParticipantProfileUrl(p1Id, p1Type);
            p2Id = message.getRecipientId();
            p2Type = message.getRecipientType();
            p2Name = message.getRecipientName();
            p2ProfileUrl = getParticipantProfileUrl(p2Id, p2Type);
        } else {
            p1Id = message.getRecipientId();
            p1Type = message.getRecipientType();
            p1Name = message.getRecipientName();
            p1ProfileUrl = getParticipantProfileUrl(p1Id, p1Type);
            p2Id = message.getSenderId();
            p2Type = message.getSenderType();
            p2Name = message.getSenderName();
            p2ProfileUrl = getParticipantProfileUrl(p2Id, p2Type);
        }

        // Check online status
        boolean p1Online = isUserOnline(p1Id);
        boolean p2Online = isUserOnline(p2Id);

        return ChatRoom.builder()
                .chatId(message.getChatId())
                .participant1Id(p1Id)
                .participant1Type(p1Type)
                .participant1Name(p1Name)
                .participant1ProfileUrl(p1ProfileUrl)
                .participant1Online(p1Online)
                .participant2Id(p2Id)
                .participant2Type(p2Type)
                .participant2Name(p2Name)
                .participant2ProfileUrl(p2ProfileUrl)
                .participant2Online(p2Online)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Get participant name from User or Vendor collection
     */
    private String getParticipantName(String participantId, String participantType) {
        if ("VENDOR".equalsIgnoreCase(participantType)) {
            return vendorRepository.findById(participantId)
                    .map(Vendor::getBusinessName)
                    .orElse(null);
        } else {
            return userRepository.findById(participantId)
                    .map(user -> user.getFirstName() + " " + user.getLastName())
                    .orElse(null);
        }
    }

    /**
     * Get participant profile URL
     */
    private String getParticipantProfileUrl(String participantId, String participantType) {
        if ("USER".equalsIgnoreCase(participantType)) {
            return userRepository.findById(participantId)
                    .map(User::getProfileUrl)
                    .orElse(null);
        }
        return null; // Vendors don't have profile URLs in current schema
    }

    /**
     * Parse message type string to enum
     */
    private MessageType parseMessageType(String type) {
        if (type == null || type.isEmpty()) {
            return MessageType.TEXT;
        }
        try {
            return MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MessageType.TEXT;
        }
    }

    /**
     * Truncate message for preview
     */
    private String truncateMessage(String message) {
        if (message == null) return "";
        return message.length() > 100 ? message.substring(0, 100) + "..." : message;
    }

    /**
     * Convert ChatMessage entity to response DTO
     */
    private ChatMessageResponseDto convertToResponseDto(ChatMessage message, String viewerId) {
        return ChatMessageResponseDto.builder()
                .id(message.getId())
                .chatId(message.getChatId())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType())
                .senderName(message.getSenderName())
                .recipientId(message.getRecipientId())
                .recipientType(message.getRecipientType())
                .recipientName(message.getRecipientName())
                .content(message.getContent())
                .messageType(message.getMessageType().toString())
                .mediaUrl(message.getMediaUrl())
                .fileName(message.getFileName())
                .status(message.getStatus().toString())
                .timestamp(message.getTimestamp())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .errorMessage(message.getErrorMessage())
                .tempId(message.getTempId())
                .isOwnMessage(message.getSenderId().equals(viewerId))
                .build();
    }

    /**
     * Convert ChatRoom to ChatListItemDto
     */
    private ChatListItemDto convertToChatListItem(ChatRoom room, String viewerId) {
        ChatRoom.ParticipantInfo other = room.getOtherParticipantInfo(viewerId);

        return ChatListItemDto.builder()
                .chatId(room.getChatId())
                .participantId(other.getParticipantId())
                .participantType(other.getParticipantType())
                .participantName(other.getParticipantName())
                .participantProfileUrl(other.getParticipantProfileUrl())
                .lastMessage(room.getLastMessageContent())
                .lastMessageSenderId(room.getLastMessageSenderId())
                .lastMessageTimestamp(room.getLastMessageTimestamp())
                .lastMessageStatus(room.getLastMessageStatus())
                .unreadCount(room.getUnreadCountFor(viewerId))
                .online(other.isOnline())
                .typing(other.isTyping())
                .lastSeen(other.getLastSeen())
                .build();
    }

    /**
     * Convert UserPresence to DTO
     */
    private UserPresenceDto convertToPresenceDto(UserPresence presence) {
        return UserPresenceDto.builder()
                .userId(presence.getUserId())
                .userType(presence.getUserType())
                .displayName(presence.getDisplayName())
                .status(presence.getStatus().toString())
                .lastSeen(presence.getLastSeen())
                .lastActivity(presence.getLastActivity())
                .statusMessage(presence.getStatusMessage())
                .build();
    }

    /**
     * Send message notifications via WebSocket
     */
    private void sendMessageNotifications(ChatMessage message) {
        // Create event for new message
        ChatEventDto newMessageEvent = ChatEventDto.builder()
                .eventType(ChatEventDto.EventType.MESSAGE_NEW)
                .chatId(message.getChatId())
                .messageId(message.getId())
                .tempId(message.getTempId())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType())
                .senderName(message.getSenderName())
                .recipientId(message.getRecipientId())
                .recipientType(message.getRecipientType())
                .recipientName(message.getRecipientName())
                .content(message.getContent())
                .messageType(message.getMessageType().toString())
                .messageStatus(message.getStatus().toString())
                .mediaUrl(message.getMediaUrl())
                .timestamp(message.getTimestamp())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .build();

        // Send to recipient
        sendToUser(message.getRecipientId(), "/queue/chat-events", newMessageEvent);

        // Send confirmation to sender with server-generated ID
        ChatEventDto sentConfirmation = ChatEventDto.builder()
                .eventType(ChatEventDto.EventType.MESSAGE_SENT)
                .chatId(message.getChatId())
                .messageId(message.getId())
                .tempId(message.getTempId())
                .senderId(message.getSenderId())
                .recipientId(message.getRecipientId())
                .messageStatus(message.getStatus().toString())
                .timestamp(message.getTimestamp())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .build();
        sendToUser(message.getSenderId(), "/queue/chat-events", sentConfirmation);

        // Send chat list update to recipient
        sendChatListUpdate(message.getRecipientId());
        sendUnreadCountUpdate(message.getRecipientId());
    }

    /**
     * Update online status in all relevant chat rooms
     */
    private void updateChatRoomsOnlineStatus(String userId, boolean online, String timestamp) {
        List<ChatRoom> rooms = chatRoomRepository.findByParticipantId(userId);
        for (ChatRoom room : rooms) {
            room.setOnlineStatus(userId, online);
            if (!online) {
                room.setTypingStatus(userId, false); // Clear typing when going offline
            }
            chatRoomRepository.save(room);
        }
    }

    /**
     * Broadcast presence update to all chat partners
     */
    private void broadcastPresenceUpdate(String userId, boolean online, String lastSeen) {
        List<ChatRoom> rooms = chatRoomRepository.findByParticipantId(userId);
        Set<String> notifiedUsers = new HashSet<>();

        ChatEventDto presenceEvent = ChatEventDto.onlineStatus(userId, online, lastSeen);

        for (ChatRoom room : rooms) {
            String partnerId = room.getParticipant1Id().equals(userId)
                    ? room.getParticipant2Id()
                    : room.getParticipant1Id();

            if (!notifiedUsers.contains(partnerId)) {
                sendToUser(partnerId, "/queue/chat-events", presenceEvent);
                notifiedUsers.add(partnerId);
            }
        }
    }

    /**
     * Send chat list update notification
     */
    private void sendChatListUpdate(String userId) {
        try {
            List<ChatListItemDto> chatList = getChatList(userId);
            messagingTemplate.convertAndSendToUser(userId, "/queue/chat-list", chatList);
        } catch (Exception e) {
            log.warn("Failed to send chat list update to {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Send unread count update notification
     */
    private void sendUnreadCountUpdate(String userId) {
        try {
            UnreadCountDto unreadCount = getUnreadCount(userId);
            ChatEventDto event = ChatEventDto.unreadCountUpdate(
                    userId,
                    unreadCount.getTotalUnreadCount(),
                    unreadCount.getUnreadChatsCount()
            );
            sendToUser(userId, "/queue/chat-events", event);
        } catch (Exception e) {
            log.warn("Failed to send unread count update to {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Send message to a specific user via WebSocket
     */
    private void sendToUser(String userId, String destination, Object payload) {
        try {
            messagingTemplate.convertAndSendToUser(userId, destination, payload);
        } catch (Exception e) {
            log.warn("Failed to send WebSocket message to {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Get chat ID for two participants
     */
    public String getChatId(String userId1, String userId2) {
        return ChatMessage.generateChatId(userId1, userId2);
    }

    /**
     * Delete a chat (soft delete messages, deactivate room)
     */
    @Transactional
    public void deleteChat(String userId, String otherParticipantId) {
        String chatId = ChatMessage.generateChatId(userId, otherParticipantId);

        // Soft delete all messages
        List<ChatMessage> messages = messageRepository.findAllByChatId(chatId);
        String now = Instant.now().toString();
        for (ChatMessage message : messages) {
            message.setDeleted(true);
            message.setDeletedAt(now);
            messageRepository.save(message);
        }

        // Deactivate chat room
        chatRoomRepository.findByChatId(chatId).ifPresent(room -> {
            room.setActive(false);
            room.setUpdatedAt(now);
            chatRoomRepository.save(room);
        });

        log.info("Deleted chat {} for user {}", chatId, userId);
    }
}

