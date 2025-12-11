package com.tcon.webid.repository;

import com.tcon.webid.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ChatRoom entity
 */
@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    /**
     * Find chat room by unique chat ID
     */
    Optional<ChatRoom> findByChatId(String chatId);

    /**
     * Find all chat rooms for a participant (as participant1 or participant2)
     * Ordered by last message timestamp descending
     */
    @Query("{'$or': [{'participant1Id': ?0}, {'participant2Id': ?0}], 'active': true}")
    List<ChatRoom> findByParticipantId(String participantId);

    /**
     * Find all chat rooms for participant1, ordered by last message
     */
    List<ChatRoom> findByParticipant1IdAndActiveTrueOrderByLastMessageTimestampDesc(String participant1Id);

    /**
     * Find all chat rooms for participant2, ordered by last message
     */
    List<ChatRoom> findByParticipant2IdAndActiveTrueOrderByLastMessageTimestampDesc(String participant2Id);

    /**
     * Find chat room between two specific participants
     */
    @Query("{'$or': [" +
           "{'participant1Id': ?0, 'participant2Id': ?1}, " +
           "{'participant1Id': ?1, 'participant2Id': ?0}" +
           "]}")
    Optional<ChatRoom> findByParticipants(String participant1Id, String participant2Id);

    /**
     * Check if chat room exists by chat ID
     */
    boolean existsByChatId(String chatId);

    /**
     * Find all chat rooms where a participant has unread messages
     */
    @Query("{'$or': [" +
           "{'participant1Id': ?0, 'participant1UnreadCount': {$gt: 0}}, " +
           "{'participant2Id': ?0, 'participant2UnreadCount': {$gt: 0}}" +
           "], 'active': true}")
    List<ChatRoom> findChatRoomsWithUnreadMessages(String participantId);

    /**
     * Count total unread messages for a participant
     */
    @Query(value = "{'participant1Id': ?0, 'participant1UnreadCount': {$gt: 0}, 'active': true}")
    List<ChatRoom> findUnreadAsParticipant1(String participantId);

    @Query(value = "{'participant2Id': ?0, 'participant2UnreadCount': {$gt: 0}, 'active': true}")
    List<ChatRoom> findUnreadAsParticipant2(String participantId);

    /**
     * Delete chat room by chat ID
     */
    void deleteByChatId(String chatId);

    /**
     * Find all chat rooms where participant is online
     */
    @Query("{'$or': [" +
           "{'participant1Id': ?0, 'participant1Online': true}, " +
           "{'participant2Id': ?0, 'participant2Online': true}" +
           "]}")
    List<ChatRoom> findOnlineChatRooms(String participantId);

    /**
     * Find all chat rooms for a specific user type
     */
    @Query("{'$or': [" +
           "{'participant1Id': ?0, 'participant1Type': ?1}, " +
           "{'participant2Id': ?0, 'participant2Type': ?1}" +
           "]}")
    List<ChatRoom> findByParticipantIdAndType(String participantId, String participantType);
}

