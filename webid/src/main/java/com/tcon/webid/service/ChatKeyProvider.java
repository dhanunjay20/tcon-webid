package com.tcon.webid.service;

import com.tcon.webid.util.MessageEncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple provider for chat encryption keys.
 * Stores per-chat keys in-memory (demo). Falls back to a master key if configured.
 */
@Slf4j
@Component
public class ChatKeyProvider {

    private final MessageEncryptionUtil encryptionUtil;
    private final ConcurrentHashMap<String, String> chatKeys = new ConcurrentHashMap<>();
    private final String masterKey;

    public ChatKeyProvider(MessageEncryptionUtil encryptionUtil,
                           @Value("${chat.encryption.master-key:}") String masterKey) {
        this.encryptionUtil = encryptionUtil;
        this.masterKey = masterKey;
    }

    public Optional<String> getKeyForChat(String chatId) {
        return Optional.ofNullable(chatKeys.get(chatId));
    }

    public Optional<String> getOrCreateKeyForChat(String chatId) {
        return Optional.ofNullable(chatKeys.computeIfAbsent(chatId, id -> encryptionUtil.generateKey()));
    }

    public String createKeyForChat(String chatId) {
        String key = encryptionUtil.generateKey();
        chatKeys.put(chatId, key);
        log.info("Created encryption key for chatId={}", chatId);
        return key;
    }

    public Optional<String> getMasterKey() {
        if (masterKey == null || masterKey.isBlank()) return Optional.empty();
        return Optional.of(masterKey);
    }
}

