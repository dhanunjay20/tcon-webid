package com.tcon.webid.repository;

import com.tcon.webid.entity.ChatMessage;
import com.tcon.webid.entity.ChatMessage.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ChatMessage entity with comprehensive query methods
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * Find all messages for a specific chat, ordered by timestamp ascending
     */
    List<ChatMessage> findByChatIdAndDeletedFalseOrderByTimestampAsc(String chatId);

    /**
     * Find messages for a specific chat with pagination (for loading history)
     */
    Page<ChatMessage> findByChatIdAndDeletedFalseOrderByTimestampDesc(String chatId, Pageable pageable);

    /**
     * Find messages by sender, recipient, and status
     */
    List<ChatMessage> findBySenderIdAndRecipientIdAndStatusAndDeletedFalse(
            String senderId, String recipientId, MessageStatus status);

    /**
     * Find all undelivered messages for a recipient
     */
    List<ChatMessage> findByRecipientIdAndStatusAndDeletedFalse(String recipientId, MessageStatus status);

    /**
     * Find all messages that need to be marked as delivered for a recipient
     */
    @Query("{'recipientId': ?0, 'status': 'SENT', 'deleted': false}")
    List<ChatMessage> findUndeliveredMessagesForRecipient(String recipientId);

    /**
     * Find all messages that need to be marked as read for a specific chat
     */
    @Query("{'chatId': ?0, 'recipientId': ?1, 'status': {$in: ['SENT', 'DELIVERED']}, 'deleted': false}")
    List<ChatMessage> findUnreadMessagesInChat(String chatId, String recipientId);

    /**
     * Count unread messages for a recipient in a specific chat
     */
    @Query(value = "{'chatId': ?0, 'recipientId': ?1, 'status': {$in: ['SENT', 'DELIVERED']}, 'deleted': false}", count = true)
    long countUnreadMessagesInChat(String chatId, String recipientId);

    /**
     * Find the last message in a chat
     */
    Optional<ChatMessage> findFirstByChatIdAndDeletedFalseOrderByTimestampDesc(String chatId);

    /**
     * Find messages by tempId (for client-side correlation)
     */
    Optional<ChatMessage> findByTempIdAndSenderId(String tempId, String senderId);

    /**
     * Find all chats for a user (distinct chat IDs where user is sender or recipient)
     */
    @Query(value = "{'$or': [{'senderId': ?0}, {'recipientId': ?0}], 'deleted': false}", fields = "{'chatId': 1}")
    List<ChatMessage> findDistinctChatIdsByUserId(String userId);

    /**
     * Delete all messages in a chat (soft delete)
     */
    @Query("{'chatId': ?0}")
    List<ChatMessage> findAllByChatId(String chatId);

    /**
     * Find messages sent after a specific timestamp
     */
    List<ChatMessage> findByChatIdAndTimestampGreaterThanAndDeletedFalseOrderByTimestampAsc(
            String chatId, String timestamp);

    /**
     * Count total messages in a chat
     */
    long countByChatIdAndDeletedFalse(String chatId);
}

