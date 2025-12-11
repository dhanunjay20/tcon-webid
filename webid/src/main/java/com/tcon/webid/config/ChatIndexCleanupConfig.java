package com.tcon.webid.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration to clean up old chat module indexes and create new ones.
 * This prevents index conflicts when migrating from the old chat module to the new one.
 *
 * Note: This runs BEFORE other CommandLineRunners (Order = 1) to ensure indexes are ready.
 */
@Slf4j
@Configuration
public class ChatIndexCleanupConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Bean
    @Order(1) // Run early, before other initializers
    public CommandLineRunner cleanupAndCreateChatIndexes() {
        return args -> {
            try {
                log.info("=== Starting chat index cleanup and creation ===");

                MongoDatabase database = mongoTemplate.getDb();

                // Step 1: Clean up old indexes
                cleanupOldIndexes(database);

                // Step 2: Drop old collections
                dropOldCollection(database, "chat_notifications");

                // Step 3: Create new indexes
                createNewIndexes(database);

                log.info("=== Chat index setup completed successfully ===");

            } catch (Exception e) {
                log.error("Error during chat index setup: {}", e.getMessage(), e);
                throw e; // Re-throw to prevent application startup with broken indexes
            }
        };
    }

    private void cleanupOldIndexes(MongoDatabase database) {
        log.info("Cleaning up old chat indexes...");

        // Clean up chat_messages collection
        cleanupCollectionIndexes(database, "chat_messages", List.of(
            "chat_idx",
            "chat_timestamp_idx",
            "sender_recipient_idx",
            "recipient_status_idx"
        ));

        // Clean up chat_rooms collection
        cleanupCollectionIndexes(database, "chat_rooms", List.of(
            "participant1_idx",
            "participant2_idx",
            "chat_id_unique_idx"
        ));

        // Clean up user_presence collection
        cleanupCollectionIndexes(database, "user_presence", List.of(
            "user_type_idx"
        ));
    }

    private void cleanupCollectionIndexes(MongoDatabase database, String collectionName, List<String> indexesToDrop) {
        try {
            MongoCollection<Document> collection = database.getCollection(collectionName);

            // Get all existing indexes
            List<String> existingIndexNames = new ArrayList<>();
            for (Document index : collection.listIndexes()) {
                String name = index.getString("name");
                if (name != null && !name.equals("_id_")) {
                    existingIndexNames.add(name);
                }
            }

            if (!existingIndexNames.isEmpty()) {
                log.info("Collection '{}' has {} existing indexes", collectionName, existingIndexNames.size());

                for (String indexName : indexesToDrop) {
                    if (existingIndexNames.contains(indexName)) {
                        try {
                            collection.dropIndex(indexName);
                            log.info("  Dropped old index: {}", indexName);
                        } catch (Exception e) {
                            log.debug("  Could not drop index {}: {}", indexName, e.getMessage());
                        }
                    }
                }
            } else {
                log.debug("Collection '{}' has no indexes", collectionName);
            }

        } catch (Exception e) {
            log.debug("Collection '{}' does not exist yet: {}", collectionName, e.getMessage());
        }
    }

    private void dropOldCollection(MongoDatabase database, String collectionName) {
        try {
            boolean exists = false;
            for (String name : database.listCollectionNames()) {
                if (name.equals(collectionName)) {
                    exists = true;
                    break;
                }
            }

            if (exists) {
                database.getCollection(collectionName).drop();
                log.info("Dropped old collection: {}", collectionName);
            }

        } catch (Exception e) {
            log.debug("Error checking/dropping collection {}: {}", collectionName, e.getMessage());
        }
    }

    private void createNewIndexes(MongoDatabase database) {
        log.info("Creating new chat indexes...");

        // Create indexes for chat_messages
        createChatMessagesIndexes(database);

        // Create indexes for chat_rooms
        createChatRoomsIndexes(database);

        // Create indexes for user_presence
        createUserPresenceIndexes(database);
    }

    private void createChatMessagesIndexes(MongoDatabase database) {
        try {
            MongoCollection<Document> collection = database.getCollection("chat_messages");

            // Index 1: chat_v2_idx
            collection.createIndex(
                Indexes.compoundIndex(
                    Indexes.ascending("chatId"),
                    Indexes.ascending("timestamp"),
                    Indexes.ascending("deleted")
                ),
                new IndexOptions().name("chat_v2_idx")
            );
            log.info("  Created index: chat_v2_idx");

            // Index 2: sender_recipient_v2_idx
            collection.createIndex(
                Indexes.compoundIndex(
                    Indexes.ascending("senderId"),
                    Indexes.ascending("recipientId"),
                    Indexes.ascending("status"),
                    Indexes.ascending("deleted")
                ),
                new IndexOptions().name("sender_recipient_v2_idx")
            );
            log.info("  Created index: sender_recipient_v2_idx");

            // Index 3: recipient_status_v2_idx
            collection.createIndex(
                Indexes.compoundIndex(
                    Indexes.ascending("recipientId"),
                    Indexes.ascending("status"),
                    Indexes.ascending("deleted")
                ),
                new IndexOptions().name("recipient_status_v2_idx")
            );
            log.info("  Created index: recipient_status_v2_idx");

        } catch (Exception e) {
            log.error("Error creating chat_messages indexes: {}", e.getMessage(), e);
        }
    }

    private void createChatRoomsIndexes(MongoDatabase database) {
        try {
            MongoCollection<Document> collection = database.getCollection("chat_rooms");

            // Index 1: participant1_v2_idx
            collection.createIndex(
                Indexes.compoundIndex(
                    Indexes.ascending("participant1Id"),
                    Indexes.descending("lastMessageTimestamp"),
                    Indexes.ascending("active")
                ),
                new IndexOptions().name("participant1_v2_idx")
            );
            log.info("  Created index: participant1_v2_idx");

            // Index 2: participant2_v2_idx
            collection.createIndex(
                Indexes.compoundIndex(
                    Indexes.ascending("participant2Id"),
                    Indexes.descending("lastMessageTimestamp"),
                    Indexes.ascending("active")
                ),
                new IndexOptions().name("participant2_v2_idx")
            );
            log.info("  Created index: participant2_v2_idx");

            // Index 3: chat_id_v2_unique_idx (unique)
            collection.createIndex(
                Indexes.ascending("chatId"),
                new IndexOptions().name("chat_id_v2_unique_idx").unique(true)
            );
            log.info("  Created index: chat_id_v2_unique_idx (unique)");

        } catch (Exception e) {
            log.error("Error creating chat_rooms indexes: {}", e.getMessage(), e);
        }
    }

    private void createUserPresenceIndexes(MongoDatabase database) {
        try {
            MongoCollection<Document> collection = database.getCollection("user_presence");

            // Index 1: userId unique
            collection.createIndex(
                Indexes.ascending("userId"),
                new IndexOptions().name("userId_unique_idx").unique(true)
            );
            log.info("  Created index: userId_unique_idx (unique)");

            // Index 2: user_type_v2_idx
            collection.createIndex(
                Indexes.compoundIndex(
                    Indexes.ascending("userId"),
                    Indexes.ascending("userType")
                ),
                new IndexOptions().name("user_type_v2_idx").unique(true)
            );
            log.info("  Created index: user_type_v2_idx (unique)");

            // Index 3: status index for queries
            collection.createIndex(
                Indexes.ascending("status"),
                new IndexOptions().name("status_idx")
            );
            log.info("  Created index: status_idx");

        } catch (Exception e) {
            log.error("Error creating user_presence indexes: {}", e.getMessage(), e);
        }
    }
}

