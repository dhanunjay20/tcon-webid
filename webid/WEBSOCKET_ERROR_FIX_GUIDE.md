# WebSocket STOMP Error Fix Guide

## Error Description

```
org.springframework.messaging.simp.stomp.StompConversionException: 
Illegal header: '// Subscribe to incoming messages for notifications'. 
A header must be of the form <name>:[<value>].
```

## Root Cause

This error occurs when the **frontend client** sends malformed STOMP headers to the WebSocket server. The error message shows that a **JavaScript comment** is being sent as a header, which violates the STOMP protocol.

### What's Happening:
- The client is sending: `// Subscribe to incoming messages for notifications` as a header
- STOMP expects headers in the format: `headerName:headerValue`
- Comments and plain text are not valid STOMP headers

## ❌ Incorrect Frontend Code (Causing the Error)

```javascript
// WRONG - This will cause the error
stompClient.subscribe('/user/queue/messages', function(message) {
    // Subscribe to incoming messages for notifications  ← This comment is sent as header!
    console.log(message);
}, {
    // Subscribe to incoming messages for notifications  ← Or this!
});
```

Or:

```javascript
// WRONG - Malformed headers object
const headers = {
    'Authorization': 'Bearer ' + token,
    '// Subscribe to incoming messages for notifications': ''  ← Invalid!
};
```

## ✅ Correct Frontend Code

### Option 1: Using @stomp/stompjs (Recommended)

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
    // WebSocket factory
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    
    // Connection headers (sent during CONNECT)
    connectHeaders: {
        'X-User-Id': userId,
        'X-User-Type': userType  // 'USER' or 'VENDOR'
    },
    
    // Debug function
    debug: function (str) {
        console.log('STOMP: ' + str);
    },
    
    // Reconnect settings
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000
});

// Set up callbacks BEFORE activating
client.onConnect = function (frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to chat events (NO comments in headers!)
    client.subscribe(`/user/${userId}/queue/chat-events`, function (message) {
        const event = JSON.parse(message.body);
        handleChatEvent(event);
    });
    
    // Subscribe to chat list updates
    client.subscribe(`/user/${userId}/queue/chat-list`, function (message) {
        const chatList = JSON.parse(message.body);
        updateChatList(chatList);
    });
};

client.onStompError = function (frame) {
    console.error('STOMP error: ' + frame.headers['message']);
    console.error('Details: ' + frame.body);
};

// Activate the client
client.activate();
```

### Option 2: Using Vanilla SockJS + STOMP (Legacy)

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// Connection headers
const headers = {
    'X-User-Id': userId,
    'X-User-Type': userType
};

// Connect
stompClient.connect(headers, function (frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe - headers parameter is OPTIONAL, use empty object if needed
    stompClient.subscribe(`/user/${userId}/queue/chat-events`, function (message) {
        const event = JSON.parse(message.body);
        handleChatEvent(event);
    }, {}); // Empty headers object, NOT comments!
    
}, function (error) {
    console.error('Connection error: ' + error);
});
```

## Common Mistakes to Avoid

### ❌ Mistake 1: Comments in Headers Object
```javascript
// WRONG
const headers = {
    'Authorization': 'Bearer token',
    // This is a comment  ← Will be sent as header key!
};
```

### ❌ Mistake 2: Multiline Strings as Headers
```javascript
// WRONG
const headers = {
    'Description': `
        Subscribe to incoming 
        messages for notifications
    `  ← Newlines will break STOMP protocol
};
```

### ❌ Mistake 3: Invalid Header Names
```javascript
// WRONG
const headers = {
    '//comment': 'value',           ← Starts with //
    'key with spaces': 'value',     ← Spaces in key
    'key:with:colons': 'value'      ← Colons in key
};
```

### ✅ Correct: Clean Headers Object
```javascript
// CORRECT
const headers = {
    'X-User-Id': userId,
    'X-User-Type': userType,
    'Authorization': 'Bearer ' + token
};
```

## Testing Your Frontend

### Step 1: Use the Provided Test Client

1. **Start your Spring Boot application**
   ```bash
   mvnw spring-boot:run
   ```

2. **Open the test client in your browser**
   ```
   http://localhost:8080/chat-test.html
   ```

3. **Enter connection details**
   - Server URL: `http://localhost:8080/ws`
   - User ID: `user123`
   - User Type: `USER` or `VENDOR`

4. **Click "Connect"**
   - If successful, status will show "Connected"
   - Check the Events Log for connection events

5. **Test messaging**
   - Enter recipient ID
   - Type a message
   - Click "Send"

### Step 2: Check Browser Console

Open browser DevTools (F12) and check:

```javascript
// You should see:
STOMP: >>> CONNECT
STOMP: connected to server ...
STOMP: <<< CONNECTED

// You should NOT see:
STOMP: ERROR ...
Illegal header: ...
```

### Step 3: Verify No Errors in Backend Logs

Your Spring Boot logs should show:

```
✅ WebSocket connected - session=xxx userId=user123 userType=USER
✅ User user123 (USER) is now ONLINE
```

NOT:

```
❌ Failed to parse TextMessage payload
❌ Illegal header: ...
❌ Sending STOMP ERROR to client
```

## Debugging Tips

### 1. Enable STOMP Debug Logging

```javascript
const client = new Client({
    // ... other config ...
    debug: function (str) {
        console.log('STOMP DEBUG: ' + str);
    }
});
```

### 2. Check Network Tab

1. Open DevTools → Network tab
2. Filter by "WS" (WebSocket)
3. Click on the WebSocket connection
4. Check "Messages" tab
5. Look for STOMP frames

**Example of CORRECT CONNECT frame:**
```
CONNECT
X-User-Id:user123
X-User-Type:USER
accept-version:1.1,1.0
heart-beat:10000,10000

^@
```

**Example of INCORRECT frame (with comments):**
```
CONNECT
X-User-Id:user123
// Subscribe to notifications  ← This should NOT be here!
accept-version:1.1,1.0

^@
```

### 3. Validate Your Code

Check your frontend code for:
- ✅ No comments inside header objects
- ✅ Header keys contain only alphanumeric and hyphens
- ✅ Header values are simple strings (no newlines)
- ✅ Using proper STOMP client library
- ✅ Headers object is plain JavaScript object

## Frontend Framework Examples

### React Example

```jsx
import React, { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

function ChatComponent({ userId, userType }) {
    const [stompClient, setStompClient] = useState(null);
    
    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            connectHeaders: {
                'X-User-Id': userId,
                'X-User-Type': userType
            },
            onConnect: () => {
                console.log('Connected');
                
                client.subscribe(`/user/${userId}/queue/chat-events`, (message) => {
                    const event = JSON.parse(message.body);
                    handleEvent(event);
                });
            }
        });
        
        client.activate();
        setStompClient(client);
        
        return () => {
            client.deactivate();
        };
    }, [userId, userType]);
    
    const sendMessage = (recipientId, content) => {
        if (stompClient && stompClient.connected) {
            stompClient.publish({
                destination: '/app/chat.send',
                body: JSON.stringify({
                    recipientId,
                    recipientType: 'VENDOR',
                    content,
                    messageType: 'TEXT'
                })
            });
        }
    };
    
    // ... rest of component
}
```

### Vue.js Example

```vue
<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const props = defineProps(['userId', 'userType']);
const stompClient = ref(null);

onMounted(() => {
    const client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
        connectHeaders: {
            'X-User-Id': props.userId,
            'X-User-Type': props.userType
        },
        onConnect: () => {
            client.subscribe(`/user/${props.userId}/queue/chat-events`, (message) => {
                const event = JSON.parse(message.body);
                handleEvent(event);
            });
        }
    });
    
    client.activate();
    stompClient.value = client;
});

onUnmounted(() => {
    if (stompClient.value) {
        stompClient.value.deactivate();
    }
});
</script>
```

### Angular Example

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Client, StompConfig } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html'
})
export class ChatComponent implements OnInit, OnDestroy {
  private stompClient: Client;

  ngOnInit() {
    const config: StompConfig = {
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {
        'X-User-Id': this.userId,
        'X-User-Type': this.userType
      },
      onConnect: () => {
        this.stompClient.subscribe(`/user/${this.userId}/queue/chat-events`, (message) => {
          const event = JSON.parse(message.body);
          this.handleEvent(event);
        });
      }
    };
    
    this.stompClient = new Client(config);
    this.stompClient.activate();
  }

  ngOnDestroy() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
}
```

## Quick Fix Checklist

- [ ] Remove ALL comments from inside header objects
- [ ] Use plain object for headers: `{ 'key': 'value' }`
- [ ] Header keys: alphanumeric + hyphens only
- [ ] Header values: simple strings, no newlines
- [ ] Test with provided `chat-test.html`
- [ ] Check browser console for STOMP errors
- [ ] Verify backend logs show successful connection
- [ ] Use modern STOMP library (@stomp/stompjs v7+)

## Still Having Issues?

1. **Clear browser cache** and reload
2. **Use the test client** (`chat-test.html`) to verify backend works
3. **Compare your code** with the examples above
4. **Check browser console** for JavaScript errors
5. **Enable STOMP debug logging** to see exact frames
6. **Verify backend is running** on correct port
7. **Check firewall/proxy** settings

## Success Indicators

✅ No errors in backend logs  
✅ No errors in browser console  
✅ Status shows "Connected"  
✅ Can send and receive messages  
✅ Events appear in the log  
✅ Typing indicators work  
✅ Online/offline status updates  

---

**Backend is working correctly!** The error is from malformed client requests. Use the provided test client to verify, then fix your frontend code following the examples above.

