# Chat Module Documentation

## Overview

This document describes the real-time chat module implementation for the WebID application. The chat module provides WhatsApp-like messaging functionality between **Clients (Users)** and **Vendors** with the following features:

- **Real-time messaging** via WebSocket (STOMP over SockJS)
- **Message status tracking**: SENDING → SENT → DELIVERED → READ (or FAILED)
- **Online/Offline status** with automatic detection
- **Typing indicators** with debouncing
- **Last seen timestamps**
- **Unread message counts**
- **Chat list (inbox)** with conversation previews

---

## Architecture

### Technology Stack
- **Backend**: Spring Boot 3.x with WebSocket (STOMP)
- **Database**: MongoDB
- **Real-time Communication**: WebSocket with SockJS fallback
- **Authentication**: JWT tokens

### Components

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ Client App  │  │ Vendor App  │  │  SockJS     │              │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
└─────────┼────────────────┼────────────────┼─────────────────────┘
          │                │                │
          ▼                ▼                ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Backend                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                  WebSocket Layer                            │ │
│  │  ┌─────────────────┐  ┌─────────────────┐                  │ │
│  │  │ ChatWebSocket   │  │ WebSocketEvent  │                  │ │
│  │  │ Controller      │  │ Listener        │                  │ │
│  │  └────────┬────────┘  └────────┬────────┘                  │ │
│  └───────────┼────────────────────┼───────────────────────────┘ │
│              │                    │                              │
│              ▼                    ▼                              │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    ChatService                              │ │
│  │  • Message handling    • Presence management                │ │
│  │  • Status updates      • Typing indicators                  │ │
│  │  • Chat list           • Unread counts                      │ │
│  └────────────────────────────────────────────────────────────┘ │
│              │                                                   │
│              ▼                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    MongoDB                                  │ │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐              │ │
│  │  │ ChatMessage│ │ ChatRoom   │ │UserPresence│              │ │
│  │  └────────────┘ └────────────┘ └────────────┘              │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## Data Models

### 1. ChatMessage (MongoDB Collection: `chat_messages`)

```java
{
  "_id": "ObjectId",
  "chatId": "userId1_userId2",      // Alphabetically sorted
  "senderId": "ObjectId",
  "senderType": "USER" | "VENDOR",
  "senderName": "John Doe",
  "recipientId": "ObjectId",
  "recipientType": "USER" | "VENDOR",
  "recipientName": "Vendor Name",
  "content": "Message text",
  "messageType": "TEXT" | "IMAGE" | "FILE" | "SYSTEM",
  "mediaUrl": "https://...",        // Optional
  "fileName": "document.pdf",       // Optional
  "status": "SENDING" | "SENT" | "DELIVERED" | "READ" | "FAILED",
  "timestamp": "2025-12-11T10:30:00Z",
  "sentAt": "2025-12-11T10:30:00Z",
  "deliveredAt": "2025-12-11T10:30:05Z",
  "readAt": "2025-12-11T10:31:00Z",
  "tempId": "client-uuid",          // Client-generated for optimistic UI
  "deleted": false,
  "deletedAt": null
}
```

### 2. ChatRoom (MongoDB Collection: `chat_rooms`)

```java
{
  "_id": "ObjectId",
  "chatId": "userId1_userId2",
  
  // Participant 1
  "participant1Id": "ObjectId",
  "participant1Type": "USER",
  "participant1Name": "John Doe",
  "participant1ProfileUrl": "https://...",
  "participant1UnreadCount": 0,
  "participant1Online": true,
  "participant1Typing": false,
  "participant1LastSeen": "2025-12-11T10:00:00Z",
  
  // Participant 2
  "participant2Id": "ObjectId",
  "participant2Type": "VENDOR",
  "participant2Name": "Vendor Name",
  "participant2ProfileUrl": null,
  "participant2UnreadCount": 3,
  "participant2Online": false,
  "participant2Typing": false,
  "participant2LastSeen": "2025-12-11T09:45:00Z",
  
  // Last Message
  "lastMessageContent": "Hello, I have a question...",
  "lastMessageSenderId": "ObjectId",
  "lastMessageTimestamp": "2025-12-11T10:30:00Z",
  "lastMessageStatus": "DELIVERED",
  
  // Metadata
  "createdAt": "2025-12-10T15:00:00Z",
  "updatedAt": "2025-12-11T10:30:00Z",
  "active": true
}
```

### 3. UserPresence (MongoDB Collection: `user_presence`)

```java
{
  "_id": "ObjectId",
  "userId": "ObjectId",
  "userType": "USER" | "VENDOR",
  "displayName": "John Doe",
  "profileUrl": "https://...",
  "status": "ONLINE" | "OFFLINE" | "AWAY" | "BUSY",
  "lastSeen": "2025-12-11T10:00:00Z",
  "lastActivity": "2025-12-11T10:30:00Z",
  "activeConnections": 1,            // Multi-device support
  "statusMessage": "Available",      // Optional
  "createdAt": "2025-12-01T00:00:00Z",
  "updatedAt": "2025-12-11T10:30:00Z"
}
```

---

## WebSocket Configuration

### Connection Endpoint
```
ws://your-domain/ws
```
With SockJS fallback: `http://your-domain/ws`

### STOMP Destinations

#### Application Destinations (Client → Server)
| Destination | Description | Payload |
|-------------|-------------|---------|
| `/app/chat.send` | Send a new message | `ChatMessageRequestDto` |
| `/app/chat.typing` | Send typing status | `TypingStatusDto` |
| `/app/chat.read` | Mark messages as read | `MessageReadReceiptDto` |
| `/app/chat.online` | Notify going online | `UserPresenceDto` |
| `/app/chat.offline` | Notify going offline | `UserPresenceDto` |
| `/app/chat.open` | Notify chat opened | `MessageReadReceiptDto` |

#### User Destinations (Server → Client)
| Destination | Description | Payload |
|-------------|-------------|---------|
| `/user/{userId}/queue/chat-events` | All chat events | `ChatEventDto` |
| `/user/{userId}/queue/chat-list` | Chat list updates | `List<ChatListItemDto>` |

#### Topic Destinations (Broadcast)
| Destination | Description |
|-------------|-------------|
| `/topic/chat-events` | Global chat events (admin) |
| `/topic/vendor/{vendorId}/chat-events` | Vendor-specific events |

---

## Event Types (ChatEventDto.EventType)

### Message Events
| Event | Description | When Triggered |
|-------|-------------|----------------|
| `MESSAGE_NEW` | New message received | When recipient receives a message |
| `MESSAGE_SENT` | Message sent confirmation | After server saves message |
| `MESSAGE_DELIVERED` | Message delivered | When recipient comes online |
| `MESSAGE_READ` | Message read | When recipient opens chat |
| `MESSAGE_FAILED` | Message failed | On delivery error |
| `MESSAGE_DELETED` | Message deleted | When message is deleted |

### Status Events
| Event | Description |
|-------|-------------|
| `TYPING_START` | User started typing |
| `TYPING_STOP` | User stopped typing |
| `USER_ONLINE` | User came online |
| `USER_OFFLINE` | User went offline |
| `USER_AWAY` | User is away/idle |

### System Events
| Event | Description |
|-------|-------------|
| `UNREAD_COUNT_UPDATE` | Unread count changed |
| `CONNECTION_ACK` | WebSocket connection acknowledged |
| `ERROR` | Error occurred |
| `HEARTBEAT` | Keep-alive heartbeat |

---

## REST API Endpoints

### Base URL: `/api/chat`

### Message Endpoints

#### Send Message
```http
POST /api/chat/messages
Headers:
  X-User-Id: {userId}
  X-User-Type: USER|VENDOR
Body:
{
  "recipientId": "string",
  "recipientType": "USER|VENDOR",
  "content": "string",
  "messageType": "TEXT|IMAGE|FILE",
  "mediaUrl": "string (optional)",
  "fileName": "string (optional)",
  "tempId": "string (client-generated UUID)"
}
Response: ChatMessageResponseDto
```

#### Get Chat History
```http
GET /api/chat/messages/{otherParticipantId}
Headers:
  X-User-Id: {userId}
Response: List<ChatMessageResponseDto>
```

#### Get Chat History (Paginated)
```http
GET /api/chat/messages/{otherParticipantId}/paginated?page=0&size=50
Headers:
  X-User-Id: {userId}
Response: Page<ChatMessageResponseDto>
```

#### Mark Messages as Read
```http
PUT /api/chat/messages/read/{otherParticipantId}
Headers:
  X-User-Id: {userId}
Response: { "markedAsRead": number }
```

#### Mark Messages as Delivered
```http
PUT /api/chat/messages/delivered
Headers:
  X-User-Id: {userId}
Response: { "markedAsDelivered": number }
```

### Chat List Endpoints

#### Get Chat List (Inbox)
```http
GET /api/chat/list
Headers:
  X-User-Id: {userId}
Response: List<ChatListItemDto>
```

#### Get Unread Count
```http
GET /api/chat/unread-count
Headers:
  X-User-Id: {userId}
Response: UnreadCountDto
```

### Presence Endpoints

#### Go Online
```http
PUT /api/chat/presence/online
Headers:
  X-User-Id: {userId}
  X-User-Type: USER|VENDOR
Response: 200 OK
```

#### Go Offline
```http
PUT /api/chat/presence/offline
Headers:
  X-User-Id: {userId}
  X-User-Type: USER|VENDOR
Response: 200 OK
```

#### Get User Presence
```http
GET /api/chat/presence/{userId}
Response: UserPresenceDto
```

### Utility Endpoints

#### Get Chat ID
```http
GET /api/chat/id/{otherParticipantId}
Headers:
  X-User-Id: {userId}
Response: { "chatId": "string" }
```

#### Delete Chat
```http
DELETE /api/chat/{otherParticipantId}
Headers:
  X-User-Id: {userId}
Response: 200 OK
```

### Vendor-Specific Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/chat/vendor/list` | GET | Get vendor's chat list |
| `/api/chat/vendor/messages/{clientId}` | GET | Get chat with client |
| `/api/chat/vendor/messages` | POST | Send message to client |
| `/api/chat/vendor/unread-count` | GET | Get vendor's unread count |

---

## Frontend Integration Guide

### 1. WebSocket Connection

```javascript
// Using SockJS and STOMP
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const client = new Client({
  webSocketFactory: () => new SockJS('http://your-api/ws'),
  connectHeaders: {
    'Authorization': `Bearer ${jwtToken}`,
    'X-User-Id': userId,
    'X-User-Type': 'USER' // or 'VENDOR'
  },
  onConnect: () => {
    console.log('Connected to WebSocket');
    
    // Subscribe to chat events
    client.subscribe(`/user/${userId}/queue/chat-events`, (message) => {
      const event = JSON.parse(message.body);
      handleChatEvent(event);
    });
    
    // Subscribe to chat list updates
    client.subscribe(`/user/${userId}/queue/chat-list`, (message) => {
      const chatList = JSON.parse(message.body);
      updateChatList(chatList);
    });
    
    // Notify server that user is online
    client.publish({
      destination: '/app/chat.online',
      body: JSON.stringify({ userId, userType: 'USER' })
    });
  },
  onDisconnect: () => {
    console.log('Disconnected from WebSocket');
  }
});

client.activate();
```

### 2. Sending Messages

```javascript
function sendMessage(recipientId, recipientType, content) {
  const tempId = generateUUID(); // Client-generated ID for optimistic UI
  
  // Optimistically add message to UI
  addMessageToUI({
    tempId,
    content,
    status: 'SENDING',
    timestamp: new Date().toISOString()
  });
  
  // Send via WebSocket
  client.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({
      recipientId,
      recipientType,
      content,
      messageType: 'TEXT',
      tempId
    })
  });
}
```

### 3. Handling Chat Events

```javascript
function handleChatEvent(event) {
  switch (event.eventType) {
    case 'MESSAGE_NEW':
      // New message received
      addMessageToChat(event);
      if (currentChatId === event.chatId) {
        markAsRead(event.chatId);
      }
      break;
      
    case 'MESSAGE_SENT':
      // Our message was saved - update tempId with real ID
      updateMessageId(event.tempId, event.messageId);
      updateMessageStatus(event.messageId, 'SENT');
      break;
      
    case 'MESSAGE_DELIVERED':
      // Our message was delivered
      updateMessageStatus(event.messageId, 'DELIVERED');
      break;
      
    case 'MESSAGE_READ':
      // Our message was read
      updateMessageStatus(event.messageId, 'READ');
      break;
      
    case 'MESSAGE_FAILED':
      // Message failed to send
      updateMessageStatus(event.tempId, 'FAILED', event.errorMessage);
      break;
      
    case 'TYPING_START':
      showTypingIndicator(event.senderId);
      break;
      
    case 'TYPING_STOP':
      hideTypingIndicator(event.senderId);
      break;
      
    case 'USER_ONLINE':
      updateOnlineStatus(event.senderId, true);
      break;
      
    case 'USER_OFFLINE':
      updateOnlineStatus(event.senderId, false, event.lastSeen);
      break;
      
    case 'UNREAD_COUNT_UPDATE':
      updateUnreadBadge(event.totalUnreadCount);
      break;
  }
}
```

### 4. Typing Indicators

```javascript
let typingTimeout = null;

function handleTyping(recipientId) {
  // Send typing start
  if (!typingTimeout) {
    client.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify({
        recipientId,
        typing: true
      })
    });
  }
  
  // Clear existing timeout
  clearTimeout(typingTimeout);
  
  // Set timeout to send typing stop
  typingTimeout = setTimeout(() => {
    client.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify({
        recipientId,
        typing: false
      })
    });
    typingTimeout = null;
  }, 2000);
}
```

### 5. Read Receipts

```javascript
function markAsRead(chatId) {
  client.publish({
    destination: '/app/chat.read',
    body: JSON.stringify({ chatId })
  });
}

// Call when opening a chat
function openChat(otherParticipantId) {
  const chatId = getChatId(userId, otherParticipantId);
  
  client.publish({
    destination: '/app/chat.open',
    body: JSON.stringify({ chatId })
  });
}
```

---

## Message Status Flow

```
┌──────────┐     ┌──────────┐     ┌───────────┐     ┌──────────┐
│ SENDING  │ ──► │  SENT    │ ──► │ DELIVERED │ ──► │  READ    │
│(client)  │     │(server)  │     │(recipient │     │(recipient│
│          │     │          │     │  online)  │     │  opened) │
└──────────┘     └──────────┘     └───────────┘     └──────────┘
     │                                                    
     │           ┌──────────┐                             
     └─────────► │  FAILED  │                             
                 │(on error)│                             
                 └──────────┘                             
```

### Status Indicators (WhatsApp-style)
- **SENDING**: ⏳ (clock icon)
- **SENT**: ✓ (single gray tick)
- **DELIVERED**: ✓✓ (double gray ticks)
- **READ**: ✓✓ (double blue ticks)
- **FAILED**: ❌ (red error icon with retry option)

---

## Best Practices

### 1. Optimistic UI Updates
- Generate a `tempId` for each message before sending
- Show the message immediately with "SENDING" status
- Update with server ID when `MESSAGE_SENT` is received

### 2. Connection Management
- Implement reconnection logic with exponential backoff
- Send heartbeat messages to detect connection issues
- Queue messages when disconnected and send when reconnected

### 3. Typing Indicators
- Debounce typing events (2-3 seconds)
- Auto-clear typing status after timeout
- Don't show your own typing indicator

### 4. Read Receipts
- Automatically mark messages as read when chat is visible
- Use visibility API to detect if user is viewing the chat
- Batch read receipts for performance

### 5. Performance
- Implement message pagination (load 50 messages initially)
- Use virtual scrolling for large chat histories
- Cache chat list locally and update incrementally

---

## Error Handling

### Common Errors

| Error Code | Description | Solution |
|------------|-------------|----------|
| `AUTH_FAILED` | JWT token invalid/expired | Refresh token and reconnect |
| `RECIPIENT_NOT_FOUND` | Invalid recipient ID | Validate IDs before sending |
| `MESSAGE_TOO_LONG` | Content exceeds limit | Limit to 5000 characters |
| `RATE_LIMITED` | Too many messages | Implement client-side rate limiting |
| `CONNECTION_LOST` | WebSocket disconnected | Auto-reconnect with backoff |

### Error Event Handling
```javascript
function handleError(event) {
  if (event.eventType === 'ERROR') {
    switch (event.errorCode) {
      case 'AUTH_FAILED':
        refreshTokenAndReconnect();
        break;
      case 'MESSAGE_TOO_LONG':
        showError('Message is too long');
        break;
      default:
        showError(event.errorMessage);
    }
  }
}
```

---

## Testing

### Test Endpoints
The following test endpoints are available at `/api/test/notifications/`:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/chat/broadcast` | POST | Broadcast test chat event |
| `/chat/user/{userId}` | POST | Send test event to user |
| `/chat/vendor/{vendorId}` | POST | Send test event to vendor |
| `/health` | GET | Health check |

### Testing with WebSocket Client
1. Connect to `/ws` endpoint with valid JWT
2. Subscribe to `/user/{userId}/queue/chat-events`
3. Use test endpoints to trigger events
4. Verify events are received

---

## File Structure

```
src/main/java/com/tcon/webid/
├── config/
│   ├── WebSocketConfig.java          # WebSocket configuration
│   └── WebSocketEventListener.java   # Connection lifecycle handling
├── controller/
│   ├── ChatWebSocketController.java  # WebSocket message handlers
│   └── ChatRestController.java       # REST API endpoints
├── dto/
│   ├── ChatMessageRequestDto.java    # Message send request
│   ├── ChatMessageResponseDto.java   # Message response
│   ├── ChatListItemDto.java          # Chat list item
│   ├── ChatEventDto.java             # WebSocket event payload
│   ├── TypingStatusDto.java          # Typing indicator
│   ├── UserPresenceDto.java          # Online status
│   ├── UnreadCountDto.java           # Unread count
│   └── MessageReadReceiptDto.java    # Read receipt
├── entity/
│   ├── ChatMessage.java              # Message entity
│   ├── ChatRoom.java                 # Chat room entity
│   └── UserPresence.java             # Presence entity
├── repository/
│   ├── ChatMessageRepository.java    # Message queries
│   ├── ChatRoomRepository.java       # Chat room queries
│   └── UserPresenceRepository.java   # Presence queries
└── service/
    └── ChatService.java              # Main chat service
```

---

## Security Considerations

1. **Authentication**: All WebSocket connections require valid JWT
2. **Authorization**: Users can only access their own chats
3. **Input Validation**: All message content is validated
4. **Rate Limiting**: Implement rate limits for message sending
5. **XSS Prevention**: Sanitize message content before display

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 2.0.0 | 2025-12-11 | Complete rewrite with new architecture |
| | | - Separated ChatRoom from ChatMessage |
| | | - Added UserPresence for better status tracking |
| | | - Improved typing indicator with debouncing |
| | | - Added comprehensive event types |
| | | - Better error handling |

