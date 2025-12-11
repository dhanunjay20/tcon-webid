package com.tcon.webid.repository;

import com.tcon.webid.entity.UserPresence;
import com.tcon.webid.entity.UserPresence.PresenceStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserPresence entity
 */
@Repository
public interface UserPresenceRepository extends MongoRepository<UserPresence, String> {

    /**
     * Find presence by user ID
     */
    Optional<UserPresence> findByUserId(String userId);

    /**
     * Find presence by user ID and type
     */
    Optional<UserPresence> findByUserIdAndUserType(String userId, String userType);

    /**
     * Check if user exists in presence collection
     */
    boolean existsByUserId(String userId);

    /**
     * Find all online users
     */
    List<UserPresence> findByStatus(PresenceStatus status);

    /**
     * Find all online users of a specific type
     */
    List<UserPresence> findByStatusAndUserType(PresenceStatus status, String userType);

    /**
     * Find presence for multiple users
     */
    List<UserPresence> findByUserIdIn(List<String> userIds);

    /**
     * Find all users with active connections
     */
    @Query("{'activeConnections': {$gt: 0}}")
    List<UserPresence> findAllWithActiveConnections();

    /**
     * Delete presence by user ID
     */
    void deleteByUserId(String userId);

    /**
     * Count online users
     */
    long countByStatus(PresenceStatus status);

    /**
     * Count online users of a specific type
     */
    long countByStatusAndUserType(PresenceStatus status, String userType);
}

