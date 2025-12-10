package com.tcon.webid.repository;

import com.tcon.webid.entity.ChatMessage;
import com.tcon.webid.entity.ChatMessage.MessageStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ChatMessage entity
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * Find all messages for a specific chat, ordered by timestamp
     *
     * @param chatId The unique chat identifier
     * @return List of chat messages ordered by timestamp
     */
    List<ChatMessage> findByChatIdOrderByTimestampAsc(String chatId);

    /**
     * Find messages by sender, recipient, and status
     *
     * @param senderId    ID of the sender
     * @param recipientId ID of the recipient
     * @param status      Message status
     * @return List of matching chat messages
     */
    List<ChatMessage> findBySenderIdAndRecipientIdAndStatus(String senderId, String recipientId, MessageStatus status);
}

