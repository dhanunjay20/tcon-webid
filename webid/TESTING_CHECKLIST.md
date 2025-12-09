# ✅ Real-Time Notifications - Testing Checklist

## Pre-Testing Setup

- [ ] Spring Boot application is running on `http://localhost:8080`
- [ ] MongoDB is connected and accessible
- [ ] CORS is properly configured for your frontend domain

## Phase 1: WebSocket Connection Test

### Using websocket-test.html

1. [ ] Open `websocket-test.html` in your browser
2. [ ] Click "Connect" button
3. [ ] Verify status shows "Connected ✓"
4. [ ] Check browser console for connection message

### Expected Result:
```
Connected: CONNECTED
```

## Phase 2: Subscription Tests

### Test Broadcast Subscriptions

1. [ ] Click "Subscribe to All Bids"
2. [ ] Click "Subscribe to All Orders"
3. [ ] Verify subscriptions appear in "Active Subscriptions" section

### Test User-Specific Subscriptions

1. [ ] Enter a user ID (e.g., `user123`)
2. [ ] Click "Subscribe to User Bids"
3. [ ] Click "Subscribe to User Orders"
4. [ ] Verify subscriptions are added

### Test Vendor-Specific Subscriptions

1. [ ] Enter a vendor organization ID (e.g., `VENDOR_ORG_123`)
2. [ ] Click "Subscribe to Vendor Bids"
3. [ ] Click "Subscribe to Vendor Orders"
4. [ ] Verify subscriptions are added

## Phase 3: Test Notification Delivery

### Test Broadcast Notifications

1. [ ] Click "Test Bid Broadcast" button
2. [ ] Verify notification appears in "Received Notifications" section
3. [ ] Verify notification contains bid data (JSON)
4. [ ] Click "Test Order Broadcast" button
5. [ ] Verify order notification appears

### Test User-Specific Notifications

1. [ ] Enter user ID matching your subscription
2. [ ] Click "Send Bid to User"
3. [ ] Verify notification received
4. [ ] Click "Send Order to User"
5. [ ] Verify notification received

### Test Vendor-Specific Notifications

1. [ ] Enter vendor org ID matching your subscription
2. [ ] Click "Send Bid to Vendor"
3. [ ] Verify notification received
4. [ ] Click "Send Order to Vendor"
5. [ ] Verify notification received

## Phase 4: Real CRUD Operation Tests

### Test Order Creation (Real-Time)

1. [ ] Keep WebSocket connection open in browser
2. [ ] Subscribe to user orders (use your actual user ID)
3. [ ] Create a new order using Postman/REST client:
   ```
   POST http://localhost:8080/api/user/{userId}/orders
   ```
4. [ ] Verify `ORDER_CREATED` notification received
5. [ ] Verify vendor subscriptions receive `BID_CREATED` notifications

### Test Bid Quote Submission (Real-Time)

1. [ ] Subscribe to user bids (customer view)
2. [ ] Submit a bid quote using Postman:
   ```
   PUT http://localhost:8080/api/vendor/{vendorOrgId}/bids/{bidId}/quote
   ```
3. [ ] Verify `BID_QUOTED` notification received by customer
4. [ ] Verify notification shows proposed price

### Test Bid Acceptance (Real-Time)

1. [ ] Subscribe to vendor bids (vendor view)
2. [ ] Accept a bid using Postman:
   ```
   PUT http://localhost:8080/api/user/{userId}/bids/{bidId}/accept
   ```
3. [ ] Verify `BID_ACCEPTED` notification received by accepted vendor
4. [ ] Verify `BID_REJECTED` notifications received by other vendors

### Test Order Status Update (Real-Time)

1. [ ] Subscribe to both customer and vendor topics
2. [ ] Update order status using Postman:
   ```
   PUT http://localhost:8080/api/vendor/{vendorOrgId}/orders/{orderId}/status?status=in_progress
   ```
3. [ ] Verify `ORDER_STATUS_CHANGED` notification received by customer
4. [ ] Verify notification received by all vendors with bids

### Test Order Deletion (Real-Time)

1. [ ] Subscribe to user orders
2. [ ] Delete an order using Postman:
   ```
   DELETE http://localhost:8080/api/user/{userId}/orders/{orderId}
   ```
3. [ ] Verify `ORDER_DELETED` notification received
4. [ ] Verify all vendors with bids receive notification

## Phase 5: Multi-User Scenario Testing

### Scenario: Customer-Vendor Interaction

1. [ ] Open two browser windows/tabs
2. [ ] Window 1: Subscribe as customer (userId)
3. [ ] Window 2: Subscribe as vendor (vendorOrgId)
4. [ ] Create order in Window 1
5. [ ] Verify Window 2 receives bid request notification
6. [ ] Submit quote from vendor API
7. [ ] Verify Window 1 receives quote notification
8. [ ] Accept bid in Window 1
9. [ ] Verify Window 2 receives acceptance notification

### Scenario: Multiple Vendors

1. [ ] Open three browser windows
2. [ ] Window 1: Customer view
3. [ ] Window 2: Vendor 1 view
4. [ ] Window 3: Vendor 2 view
5. [ ] Create order for both vendors
6. [ ] Both vendor windows receive notifications
7. [ ] Both vendors submit quotes
8. [ ] Customer window receives both quote notifications
9. [ ] Accept one bid
10. [ ] Accepted vendor receives acceptance
11. [ ] Rejected vendor receives rejection

## Phase 6: Error Handling Tests

### Connection Loss

1. [ ] Establish connection
2. [ ] Stop the Spring Boot server
3. [ ] Verify browser shows disconnected
4. [ ] Restart server
5. [ ] Click "Connect" again
6. [ ] Verify reconnection works

### Invalid Subscriptions

1. [ ] Try subscribing with empty user ID
2. [ ] Verify graceful handling
3. [ ] Try subscribing without connection
4. [ ] Verify appropriate error message

## Phase 7: Performance Tests

### Load Testing

1. [ ] Subscribe to multiple topics simultaneously
2. [ ] Send 10+ rapid notifications
3. [ ] Verify all notifications received
4. [ ] Check for memory leaks in browser console

### Concurrent Users

1. [ ] Open 5+ browser windows
2. [ ] Connect all to WebSocket
3. [ ] Send broadcast notification
4. [ ] Verify all windows receive notification

## Troubleshooting

### If connection fails:

- [ ] Check if Spring Boot is running
- [ ] Verify port 8080 is accessible
- [ ] Check browser console for errors
- [ ] Verify CORS settings in SecurityConfig.java

### If notifications not received:

- [ ] Verify subscription is active
- [ ] Check topic/queue name matches exactly
- [ ] Verify user ID / vendor org ID is correct
- [ ] Check backend logs for errors

### If compilation errors:

- [ ] Run `./mvnw clean compile`
- [ ] Check all dependencies are resolved
- [ ] Verify Java version (17+)
- [ ] Check Lombok is properly configured

## Success Criteria

✅ All tests pass
✅ Notifications received in real-time (< 1 second)
✅ No errors in browser console
✅ No errors in backend logs
✅ Multiple users can connect simultaneously
✅ Reconnection works after disconnect

## Next Steps After Testing

1. [ ] Integrate WebSocket connection in your frontend app
2. [ ] Add notification UI components (toasts, badges)
3. [ ] Implement notification sound/visual effects
4. [ ] Add user notification preferences
5. [ ] Consider adding JWT authentication to WebSocket
6. [ ] Remove or secure WebSocketTestController in production

---

**Last Updated**: December 9, 2025

