package com.tcon.webid.repository;

import com.tcon.webid.entity.ChatNotificationMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ChatNotificationMetadata entity
 */
@Repository
public interface ChatNotificationMetadataRepository extends MongoRepository<ChatNotificationMetadata, String> {

    /**
     * Find all chat notifications for a user, ordered by last message timestamp
     *
     * @param userId MongoDB ObjectId of the user
     * @return List of chat notification metadata
     */
    List<ChatNotificationMetadata> findByUserIdOrderByLastMessageTimestampDesc(String userId);

    /**
     * Find a specific chat notification between two users
     *
     * @param userId               MongoDB ObjectId of the user
     * @param otherParticipantId   MongoDB ObjectId of the other participant
     * @return Optional ChatNotificationMetadata
     */
    Optional<ChatNotificationMetadata> findByUserIdAndOtherParticipantId(String userId, String otherParticipantId);

    /**
     * Find all chats with unread messages for a user
     *
     * @param userId MongoDB ObjectId of the user
     * @return List of chat notification metadata with unread messages
     */
    List<ChatNotificationMetadata> findByUserIdAndUnreadCountGreaterThan(String userId, int count);

    /**
     * Delete chat notification metadata
     *
     * @param userId               MongoDB ObjectId of the user
     * @param otherParticipantId   MongoDB ObjectId of the other participant
     */
    void deleteByUserIdAndOtherParticipantId(String userId, String otherParticipantId);

    /**
     * Check if a chat notification exists
     *
     * @param userId               MongoDB ObjectId of the user
     * @param otherParticipantId   MongoDB ObjectId of the other participant
     * @return true if exists
     */
    boolean existsByUserIdAndOtherParticipantId(String userId, String otherParticipantId);
}

