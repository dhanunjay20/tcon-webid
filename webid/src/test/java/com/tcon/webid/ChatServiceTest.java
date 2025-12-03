package com.tcon.webid;

import com.tcon.webid.entity.ChatMessage;
import com.tcon.webid.entity.ChatMessage.MessageStatus;
import com.tcon.webid.repository.ChatMessageRepository;
import com.tcon.webid.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for Chat functionality
 */
@SpringBootTest
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @MockBean
    private ChatMessageRepository chatMessageRepository;

    @Test
    void testGetChatId_ShouldReturnAlphabeticallySortedId() {
        // Test that chatId is consistent regardless of order
        String chatId1 = chatService.getChatId("user123", "vendor456");
        String chatId2 = chatService.getChatId("vendor456", "user123");

        assertEquals(chatId1, chatId2, "ChatId should be same regardless of parameter order");
        assertTrue(chatId1.equals("user123_vendor456"), "ChatId should be alphabetically sorted");
    }

    @Test
    void testSaveMessage_ShouldSetStatusAndTimestamp() {
        // Arrange
        ChatMessage message = ChatMessage.builder()
                .senderId("user123")
                .recipientId("vendor456")
                .content("Hello")
                .build();

        ChatMessage savedMessage = ChatMessage.builder()
                .id("msg123")
                .senderId("user123")
                .recipientId("vendor456")
                .content("Hello")
                .chatId("user123_vendor456")
                .status(MessageStatus.SENT)
                .timestamp("2025-12-03T10:00:00Z")
                .build();

        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        // Act
        ChatMessage result = chatService.save(message);

        // Assert
        assertNotNull(result);
        assertEquals(MessageStatus.SENT, result.getStatus());
        assertNotNull(result.getChatId());
        assertNotNull(result.getTimestamp());
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void testFindChatMessages_ShouldReturnMessagesForChatId() {
        // Arrange
        String senderId = "user123";
        String recipientId = "vendor456";
        String chatId = "user123_vendor456";

        List<ChatMessage> mockMessages = Arrays.asList(
                ChatMessage.builder()
                        .id("msg1")
                        .chatId(chatId)
                        .senderId(senderId)
                        .recipientId(recipientId)
                        .content("Message 1")
                        .status(MessageStatus.READ)
                        .timestamp("2025-12-03T10:00:00Z")
                        .build(),
                ChatMessage.builder()
                        .id("msg2")
                        .chatId(chatId)
                        .senderId(recipientId)
                        .recipientId(senderId)
                        .content("Message 2")
                        .status(MessageStatus.SENT)
                        .timestamp("2025-12-03T10:01:00Z")
                        .build()
        );

        when(chatMessageRepository.findByChatIdOrderByTimestampAsc(chatId))
                .thenReturn(mockMessages);

        // Act
        List<ChatMessage> result = chatService.findChatMessages(senderId, recipientId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(chatMessageRepository, times(1)).findByChatIdOrderByTimestampAsc(chatId);
    }

    @Test
    void testMarkMessagesAsRead_ShouldUpdateStatus() {
        // Arrange
        String senderId = "user123";
        String recipientId = "vendor456";

        List<ChatMessage> deliveredMessages = Arrays.asList(
                ChatMessage.builder()
                        .id("msg1")
                        .senderId(senderId)
                        .recipientId(recipientId)
                        .status(MessageStatus.DELIVERED)
                        .build()
        );

        List<ChatMessage> sentMessages = Arrays.asList(
                ChatMessage.builder()
                        .id("msg2")
                        .senderId(senderId)
                        .recipientId(recipientId)
                        .status(MessageStatus.SENT)
                        .build()
        );

        when(chatMessageRepository.findBySenderIdAndRecipientIdAndStatus(
                senderId, recipientId, MessageStatus.DELIVERED))
                .thenReturn(deliveredMessages);

        when(chatMessageRepository.findBySenderIdAndRecipientIdAndStatus(
                senderId, recipientId, MessageStatus.SENT))
                .thenReturn(sentMessages);

        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        int count = chatService.markMessagesAsRead(senderId, recipientId);

        // Assert
        assertEquals(2, count);
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
    }
}

