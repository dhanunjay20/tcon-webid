package com.tcon.webid.controller;

import com.tcon.webid.service.ChatKeyProvider;
import com.tcon.webid.util.MessageEncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for managing chat encryption keys
 * In production, use a secure key management service (KMS, Vault, etc.)
 * For true end-to-end encryption, keys should be generated and stored client-side
 */
@Slf4j
@RestController
@RequestMapping("/api/chat/encryption")
public class ChatEncryptionController {

    @Autowired
    private MessageEncryptionUtil encryptionUtil;

    @Autowired
    private ChatKeyProvider keyProvider;

    /**
     * Get or generate encryption key for a chat
     * @param chatId The chat ID (combination of sender and recipient IDs)
     * @return Encryption key (Base64-encoded)
     */
    @GetMapping("/key/{chatId}")
    public ResponseEntity<Map<String, String>> getChatKey(@PathVariable String chatId) {
        try {
            String key = keyProvider.getOrCreateKeyForChat(chatId).orElseGet(() -> {
                String newKey = encryptionUtil.generateKey();
                log.info("Generated new encryption key for chat: {}", chatId);
                return newKey;
            });

            Map<String, String> response = new HashMap<>();
            response.put("chatId", chatId);
            response.put("key", key);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting chat encryption key", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test encryption endpoint (for debugging only - remove in production)
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testEncryption(
            @RequestParam String message,
            @RequestParam(required = false) String key) {
        try {
            String encKey = key != null ? key : encryptionUtil.generateKey();
            String encrypted = encryptionUtil.encrypt(message, encKey);
            String decrypted = encryptionUtil.decrypt(encrypted, encKey);

            Map<String, String> response = new HashMap<>();
            response.put("original", message);
            response.put("key", encKey);
            response.put("encrypted", encrypted);
            response.put("decrypted", decrypted);
            response.put("match", String.valueOf(message.equals(decrypted)));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error testing encryption", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
