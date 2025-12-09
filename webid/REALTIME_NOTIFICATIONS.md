# Real-Time Notifications for Bids and Orders

This document explains the real-time notification system implemented for the Event Bidding application using WebSockets.

## Overview

The application now pushes real-time updates to the frontend without requiring page refresh for all CRUD operations on **Bids** and **Orders**.

## Architecture

### Components

1. **WebSocket Configuration** (`WebSocketConfig.java`)
   - Already configured with STOMP over WebSocket
   - Endpoints: `/ws` with SockJS fallback
   - Message brokers: `/topic` (broadcast), `/queue` (user-specific)
   - Application prefix: `/app`

2. **Real-Time Notification DTOs**
   - `BidUpdateNotification.java` - For bid-related updates
   - `OrderUpdateNotification.java` - For order-related updates

3. **Real-Time Notification Service**
   - `RealTimeNotificationService.java` - Interface
   - `RealTimeNotificationServiceImpl.java` - Implementation using `SimpMessagingTemplate`

4. **Enhanced Service Layer**
   - `BidServiceImpl.java` - Sends WebSocket notifications for all bid operations
   - `OrderServiceImpl.java` - Sends WebSocket notifications for all order operations

## WebSocket Topics/Queues

### For Bids

1. **Broadcast to all** (public updates):
   - Topic: `/topic/bids`
   - All bid updates are broadcast here

2. **User-specific** (private to customer):
   - Queue: `/user/{userId}/queue/bids`
   - Customer receives updates about their order bids

3. **Vendor-specific** (private to vendor):
   - Topic: `/topic/vendor/{vendorOrganizationId}/bids`
   - Vendor receives updates about their bids

### For Orders

1. **Broadcast to all** (public updates):
   - Topic: `/topic/orders`
   - All order updates are broadcast here

2. **User-specific** (private to customer):
   - Queue: `/user/{userId}/queue/orders`
   - Customer receives updates about their orders

3. **Vendor-specific** (private to vendor):
   - Topic: `/topic/vendor/{vendorOrganizationId}/orders`
   - Vendor receives updates about orders they're involved in

## Event Types

### Bid Events

- `BID_CREATED` - New bid request sent to vendor
- `BID_QUOTED` - Vendor submitted a quote
- `BID_ACCEPTED` - Customer accepted a bid
- `BID_REJECTED` - Bid was rejected
- `BID_DELETED` - Bid was deleted

### Order Events

- `ORDER_CREATED` - New order created
- `ORDER_STATUS_CHANGED` - Order status updated
- `ORDER_DELETED` - Order deleted

## CRUD Operations Coverage

### Bids

✅ **Create**: When order is created, bids are created for selected vendors
- Notification sent to each vendor via `/topic/vendor/{vendorId}/bids`
- Event type: `BID_CREATED`

✅ **Update (Submit Quote)**: Vendor submits a quote
- Notification sent to customer via `/user/{userId}/queue/bids`
- Event type: `BID_QUOTED`
- Order total price is also updated

✅ **Update (Accept Bid)**: Customer accepts a bid
- Notification sent to accepted vendor via `/topic/vendor/{vendorId}/bids`
- Event type: `BID_ACCEPTED`
- Other bids are automatically rejected with notifications

✅ **Update (Reject Bid)**: Customer/system rejects a bid
- Notification sent to vendor via `/topic/vendor/{vendorId}/bids`
- Event type: `BID_REJECTED`

✅ **Delete**: Bid is deleted
- Notification sent to vendor via `/topic/vendor/{vendorId}/bids`
- Event type: `BID_DELETED`

### Orders

✅ **Create**: Customer creates an order
- Notification sent to customer via `/user/{userId}/queue/orders`
- Broadcast to all via `/topic/orders`
- Event type: `ORDER_CREATED`

✅ **Update (Status Change)**: Order status changes
- Notification sent to customer via `/user/{userId}/queue/orders`
- Notification sent to all vendors with bids via `/topic/vendor/{vendorId}/orders`
- Broadcast to all via `/topic/orders`
- Event type: `ORDER_STATUS_CHANGED`

✅ **Delete**: Order is deleted
- Notification sent to customer via `/user/{userId}/queue/orders`
- Notification sent to all vendors with bids via `/topic/vendor/{vendorId}/orders`
- Broadcast to all via `/topic/orders`
- Event type: `ORDER_DELETED`

## Frontend Integration

### 1. Connect to WebSocket

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
  console.log('Connected: ' + frame);
  
  // Subscribe to topics
  subscribeToNotifications();
});
```

### 2. Subscribe to Bid Updates (Customer)

```javascript
// For customers to receive bid updates for their orders
stompClient.subscribe('/user/queue/bids', (message) => {
  const bidUpdate = JSON.parse(message.body);
  console.log('Bid update:', bidUpdate);
  
  // Update UI based on event type
  switch(bidUpdate.eventType) {
    case 'BID_QUOTED':
      // Show notification: "A vendor submitted a quote"
      // Update bids list for the order
      break;
    case 'BID_ACCEPTED':
      // Show notification: "Your bid was accepted"
      break;
    case 'BID_REJECTED':
      // Show notification: "Bid was rejected"
      break;
  }
});

// Also subscribe to broadcast for general updates
stompClient.subscribe('/topic/bids', (message) => {
  const bidUpdate = JSON.parse(message.body);
  // Handle broadcast updates
});
```

### 3. Subscribe to Bid Updates (Vendor)

```javascript
// For vendors to receive updates about their bids
const vendorOrgId = 'VENDOR_ORG_123'; // Get from auth context

stompClient.subscribe(`/topic/vendor/${vendorOrgId}/bids`, (message) => {
  const bidUpdate = JSON.parse(message.body);
  console.log('Vendor bid update:', bidUpdate);
  
  switch(bidUpdate.eventType) {
    case 'BID_CREATED':
      // Show notification: "New bid request received"
      // Add to bid requests list
      break;
    case 'BID_ACCEPTED':
      // Show notification: "Your quote was accepted!"
      break;
    case 'BID_REJECTED':
      // Show notification: "Your quote was not selected"
      break;
  }
});
```

### 4. Subscribe to Order Updates (Customer)

```javascript
// For customers to receive updates about their orders
stompClient.subscribe('/user/queue/orders', (message) => {
  const orderUpdate = JSON.parse(message.body);
  console.log('Order update:', orderUpdate);
  
  switch(orderUpdate.eventType) {
    case 'ORDER_CREATED':
      // Add order to list
      break;
    case 'ORDER_STATUS_CHANGED':
      // Update order status in UI
      // Show notification with new status
      break;
    case 'ORDER_DELETED':
      // Remove order from list
      break;
  }
});
```

### 5. Subscribe to Order Updates (Vendor)

```javascript
// For vendors to receive updates about orders they're bidding on
const vendorOrgId = 'VENDOR_ORG_123'; // Get from auth context

stompClient.subscribe(`/topic/vendor/${vendorOrgId}/orders`, (message) => {
  const orderUpdate = JSON.parse(message.body);
  console.log('Vendor order update:', orderUpdate);
  
  switch(orderUpdate.eventType) {
    case 'ORDER_STATUS_CHANGED':
      // Update order status in vendor's view
      break;
    case 'ORDER_DELETED':
      // Remove order/bid from list
      break;
  }
});
```

### 6. React/Vue Example with State Management

```javascript
// React example with hooks
import { useEffect, useState } from 'react';

function useBidNotifications(userId) {
  const [bids, setBids] = useState([]);
  const [stompClient, setStompClient] = useState(null);

  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    const client = Stomp.over(socket);

    client.connect({}, () => {
      // Subscribe to user-specific bid updates
      client.subscribe('/user/queue/bids', (message) => {
        const bidUpdate = JSON.parse(message.body);
        
        setBids(prevBids => {
          switch(bidUpdate.eventType) {
            case 'BID_QUOTED':
              // Update existing bid or add new
              return prevBids.map(bid => 
                bid.bidId === bidUpdate.bidId 
                  ? { ...bid, ...bidUpdate }
                  : bid
              );
            case 'BID_DELETED':
              // Remove bid
              return prevBids.filter(bid => bid.bidId !== bidUpdate.bidId);
            default:
              return prevBids;
          }
        });

        // Show toast notification
        toast.info(bidUpdate.message);
      });
    });

    setStompClient(client);

    return () => {
      if (client && client.connected) {
        client.disconnect();
      }
    };
  }, [userId]);

  return { bids, stompClient };
}
```

## Message Format

### BidUpdateNotification

```json
{
  "bidId": "bid123",
  "orderId": "order456",
  "vendorOrganizationId": "VENDOR_ORG_789",
  "status": "quoted",
  "eventType": "BID_QUOTED",
  "message": "A vendor has submitted a quote for your event: Birthday Party",
  "proposedTotalPrice": 15000.0,
  "customerName": "John Doe",
  "vendorBusinessName": "Premium Caterers",
  "eventName": "Birthday Party",
  "timestamp": "2025-12-09T14:30:00Z"
}
```

### OrderUpdateNotification

```json
{
  "orderId": "order456",
  "customerId": "user123",
  "vendorOrganizationId": "VENDOR_ORG_789",
  "eventName": "Birthday Party",
  "eventDate": "2025-12-25",
  "eventLocation": "Central Park",
  "guestCount": 50,
  "status": "confirmed",
  "totalPrice": 15000.0,
  "eventType": "ORDER_STATUS_CHANGED",
  "message": "Order Birthday Party status changed from pending to confirmed",
  "timestamp": "2025-12-09T14:30:00Z"
}
```

## Testing

### Using Browser Console

```javascript
// Connect to WebSocket
var socket = new SockJS('http://localhost:8080/ws');
var stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to all bids
    stompClient.subscribe('/topic/bids', function(message) {
        console.log('Bid update:', JSON.parse(message.body));
    });
    
    // Subscribe to all orders
    stompClient.subscribe('/topic/orders', function(message) {
        console.log('Order update:', JSON.parse(message.body));
    });
});
```

### Using Postman/Insomnia

1. Create a WebSocket request to: `ws://localhost:8080/ws`
2. After connecting, send STOMP CONNECT frame
3. Subscribe to topics using STOMP SUBSCRIBE frames
4. Perform CRUD operations via REST API
5. Watch for real-time notifications in WebSocket connection

## Implementation References

This implementation follows best practices from:

1. **Spring WebSocket Documentation**
   - https://spring.io/guides/gs/messaging-stomp-websocket/

2. **SockJS + STOMP**
   - Standard WebSocket protocol with fallback support

3. **Real-time Notification Patterns**
   - Broadcast pattern for public updates
   - User-specific queues for private notifications
   - Topic-based subscriptions for filtered updates

## Security Considerations

1. **Authentication**: Integrate JWT authentication with WebSocket connections
2. **Authorization**: Validate user permissions before sending notifications
3. **Rate Limiting**: Implement rate limiting to prevent notification spam
4. **Data Privacy**: Ensure sensitive data is only sent to authorized recipients

## Performance Optimization

1. **Connection Pooling**: Reuse WebSocket connections
2. **Message Batching**: Batch multiple updates if needed
3. **Compression**: Enable message compression for large payloads
4. **Heartbeat**: Configure STOMP heartbeat to maintain connections

## Future Enhancements

1. Add notification preferences per user
2. Implement notification history/persistence
3. Add read/unread status for notifications
4. Support for push notifications (FCM/APNS)
5. Add notification sound/visual preferences

---

**Last Updated**: December 9, 2025
**Version**: 1.0.0

