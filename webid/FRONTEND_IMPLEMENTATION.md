# Frontend Implementation Guide

## Typing Status WebSocket Update

### Updated Payload Structure
The frontend must now send `vendorId` in the typing status payload. The backend will no longer auto-generate it.

**Old Payload (Will Now Fail)**:
```json
{
  "senderId": "user123",
  "recipientId": "user456",
  "isTyping": true
}
```

**New Payload (Required)**:
```json
{
  "senderId": "user123",
  "recipientId": "user456",
  "vendorId": "vendor789",
  "isTyping": true
}
```

### Implementation Example

**JavaScript/TypeScript**:
```typescript
// When sending typing status
const typingStatus = {
  senderId: currentUserId,
  recipientId: otherUserId,
  vendorId: currentVendorId, // REQUIRED - must be provided
  isTyping: true
};

stompClient.send('/app/typing', {}, JSON.stringify(typingStatus));
```

**Error Handling**:
- If `vendorId` is null or blank, the backend will return a warning log:
  ```
  WARN: TypingStatus missing vendorId - frontend must send vendorId in payload
  ```
- The message will be ignored by the server

---

## Vendor Online Status Display

### Updated Response Fields
Vendor responses now include two new fields:

```json
{
  "id": "vendor-mongo-id",
  "vendorOrganizationId": "ORG123",
  "businessName": "ABC Catering",
  "contactName": "John Doe",
  "email": "john@abc.com",
  "mobile": "+919876543210",
  "isOnline": true,
  "lastSeenAt": "2025-12-10T20:15:40Z"
}
```

### Field Descriptions
- **`isOnline`** (Boolean): 
  - `true` = Vendor is currently online
  - `false` = Vendor is offline
  - `null` = Status not yet tracked (for legacy vendors)

- **`lastSeenAt`** (String - ISO 8601):
  - Timestamp when the vendor was last seen online
  - Format: `2025-12-10T20:15:40Z`
  - Updated whenever online status changes

### Usage in UI
```typescript
// Display vendor online status
if (vendor.isOnline) {
  displayBadge("ONLINE", "green");
} else {
  displayBadge("OFFLINE", "gray");
  if (vendor.lastSeenAt) {
    displayLastSeen(`Last seen: ${formatTime(vendor.lastSeenAt)}`);
  }
}
```

---

## WebSocket Status Update

### Sending Online/Offline Status
```typescript
// When user goes online
const userStatus = {
  userId: currentUserId,
  status: "ONLINE"
};
stompClient.send('/app/status', {}, JSON.stringify(userStatus));

// When user goes offline
const userStatus = {
  userId: currentUserId,
  status: "OFFLINE"
};
stompClient.send('/app/status', {}, JSON.stringify(userStatus));
```

### Listening for Status Updates
```typescript
stompClient.subscribe('/topic/status', (message) => {
  const userStatus = JSON.parse(message.body);
  console.log(`${userStatus.userId} is now ${userStatus.status}`);
  // Update UI with new status
});
```

---

## Vendor Endpoints

### Get Single Vendor
```
GET /api/vendor/{id}

Response:
{
  "id": "...",
  "vendorOrganizationId": "...",
  "businessName": "...",
  "contactName": "...",
  "email": "...",
  "mobile": "...",
  "addresses": [...],
  "licenseDocuments": [...],
  "isOnline": true,
  "lastSeenAt": "2025-12-10T20:15:40Z"
}
```

### Get All Vendors
```
GET /api/vendor

Response:
[
  {
    "id": "...",
    "vendorOrganizationId": "...",
    "businessName": "...",
    "contactName": "...",
    "email": "...",
    "mobile": "...",
    "addresses": [...],
    "licenseDocuments": [...],
    "isOnline": true,
    "lastSeenAt": "2025-12-10T20:15:40Z"
  },
  ...
]
```

---

## Migration Notes

### For Existing Vendors
- Vendors in the database may have `isOnline = null` initially
- First login will trigger online status update
- Display as `OFFLINE` if `isOnline` is null

### For New Vendors
- New registrations will have `isOnline = false`
- Status updates to `true` on first login via WebSocket

---

## Troubleshooting

### Issue: "TypingStatus missing vendorId"
**Solution**: Ensure frontend is sending vendorId in the typing payload

### Issue: Vendor isOnline still shows as false
**Solution**: 
1. Ensure vendor has connected to WebSocket and sent status update
2. Check that the /app/status WebSocket endpoint is being called
3. Verify vendorId is correct and matches database ID

### Issue: lastSeenAt not updating
**Solution**:
1. This field only updates when online status changes
2. Will not update on every message
3. For real-time last activity, track separately on frontend

---

## Validation Rules

### TypingStatus Payload Validation
```
✓ senderId: Required, non-empty string
✓ recipientId: Required, non-empty string
✓ vendorId: Required, non-empty string (NEW)
✓ isTyping: Required, boolean (null treated as false)
```

If any required field is missing, the request will be logged as a warning and ignored.

