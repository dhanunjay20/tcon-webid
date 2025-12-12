# Quick API Reference - Service Details & Vendor Updates

## 🔑 Service Details Endpoints

### Create/Update Service Details
```http
POST /api/service-details/{vendorId}
PUT /api/service-details/{vendorId}
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "cuisineSpecialties": ["Indian", "Chinese", "Continental"],
  "dietaryOptions": ["Vegetarian", "Vegan", "Gluten-Free"],
  "serviceTypes": ["Wedding Catering", "Corporate Events"],
  "maximumCapacity": 500,
  "minimumCapacity": 50,
  "serviceArea": ["Mumbai", "Navi Mumbai"],
  "startingPricePerPerson": 500.00,
  "pricingModel": "Per Person",
  "specialServices": ["Live Cooking", "Bartending"],
  "equipment": ["Tables", "Chairs", "Tents"],
  "website": "https://example.com",
  "yearsInBusiness": 10,
  "aboutBusiness": "We provide premium catering...",
  "certifications": ["FSSAI", "ISO 9001"],
  "portfolioImages": ["https://...", "https://..."],
  "availableForBooking": true,
  "advanceBookingDays": 7
}
```

### Get Service Details
```http
GET /api/service-details/vendor/{vendorId}
GET /api/service-details/org/{vendorOrgId}
GET /api/service-details
```

### Search Service Details
```http
GET /api/service-details/search/service-type?type=Wedding Catering
GET /api/service-details/search/cuisine?cuisine=Indian
GET /api/service-details/search/area?area=Mumbai
```

### Delete Service Details
```http
DELETE /api/service-details/vendor/{vendorId}
Authorization: Bearer {jwt_token}
```

---

## 👤 Vendor Update Endpoint

### Update Vendor Profile
```http
PUT /api/vendor/{vendorId}
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "businessName": "ABC Catering Services",
  "contactName": "John Doe",
  "email": "john@abccatering.com",
  "mobile": "+919876543210",
  "password": "NewPassword123!",  // Optional
  "addresses": [
    {
      "street": "123 Main St",
      "city": "Mumbai",
      "state": "Maharashtra",
      "zipCode": "400001",
      "country": "India"
    }
  ],
  "licenseDocuments": [
    {
      "country": "India",
      "formatType": "FSSAI",
      "licenseNumber": "12345678901234",
      "issuingAuthority": "FSSAI",
      "validFrom": "2020-01-01",
      "validTill": "2025-12-31",
      "docFileUrl": "https://..."
    }
  ],
  "website": "https://www.abccatering.com",
  "yearsInBusiness": 10,
  "aboutBusiness": "Premium catering service with 10+ years of experience"
}
```

---

## 📧 Email Templates (Automatic)

All emails now automatically use modern HTML templates:

### 1. OTP Email
**Trigger:** POST `/api/auth/forgot-password`
**Contains:** Large OTP code, expiration warning, security tips

### 2. Vendor Registration Email
**Trigger:** POST `/api/vendor/register`
**Contains:** Welcome message, Vendor Organization ID, account details, next steps

### 3. Username Recovery Email
**Trigger:** POST `/api/auth/forgot-username`
**Contains:** Username/Organization ID, login button, security notice

### 4. Password Reset Confirmation
**Trigger:** POST `/api/auth/reset-password`
**Contains:** Success message, security tips, login button

---

## 🎯 Common Workflows

### Workflow 1: Complete Vendor Profile Setup
```bash
# Step 1: Register vendor
POST /api/vendor/register
# Vendor receives registration email with Organization ID

# Step 2: Login
POST /api/vendor/login
# Returns JWT token

# Step 3: Update basic profile
PUT /api/vendor/{vendorId}
# Add website, years in business, about business

# Step 4: Add service details
POST /api/service-details/{vendorId}
# Add all service information
```

### Workflow 2: Search for Vendors
```bash
# Search by cuisine
GET /api/service-details/search/cuisine?cuisine=Indian

# Search by service type
GET /api/service-details/search/service-type?type=Wedding Catering

# Search by area
GET /api/service-details/search/area?area=Mumbai

# Get all vendors
GET /api/vendor
```

### Workflow 3: Update Service Details
```bash
# Get current details
GET /api/service-details/vendor/{vendorId}

# Update specific fields
PUT /api/service-details/{vendorId}
{
  "availableForBooking": false,
  "startingPricePerPerson": 600.00
}
```

---

## 🔐 Authentication

All update/create/delete endpoints require JWT authentication:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

Get JWT token from:
- `POST /api/auth/login` (for users)
- `POST /api/vendor/login` (for vendors)

---

## ✅ Response Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid token) |
| 404 | Not Found |
| 500 | Internal Server Error |

---

## 📊 Example Responses

### Service Details Response
```json
{
  "id": "675e1234567890abcdef1234",
  "vendorId": "6739265ee089e4aae58f4d8",
  "vendorOrganizationId": "ORG123",
  "cuisineSpecialties": ["Indian", "Chinese"],
  "dietaryOptions": ["Vegetarian", "Vegan"],
  "serviceTypes": ["Wedding Catering"],
  "maximumCapacity": 500,
  "minimumCapacity": 50,
  "serviceArea": ["Mumbai", "Navi Mumbai"],
  "startingPricePerPerson": 500.00,
  "pricingModel": "Per Person",
  "specialServices": ["Live Cooking"],
  "equipment": ["Tables", "Chairs"],
  "website": "https://example.com",
  "yearsInBusiness": 10,
  "aboutBusiness": "Premium catering...",
  "certifications": ["FSSAI"],
  "portfolioImages": ["https://..."],
  "availableForBooking": true,
  "advanceBookingDays": 7,
  "createdAt": "2025-12-10T10:00:00Z",
  "updatedAt": "2025-12-10T15:30:00Z"
}
```

### Vendor Response
```json
{
  "id": "6739265ee089e4aae58f4d8",
  "vendorOrganizationId": "ORG123",
  "businessName": "ABC Catering",
  "contactName": "John Doe",
  "email": "john@abc.com",
  "mobile": "+919876543210",
  "addresses": [...],
  "licenseDocuments": [...],
  "isOnline": true,
  "lastSeenAt": "2025-12-10T15:45:00Z",
  "website": "https://www.abc.com",
  "yearsInBusiness": 10,
  "aboutBusiness": "Premium catering service..."
}
```

---

## 🧪 Testing with cURL

### Create Service Details
```bash
curl -X POST http://localhost:8080/api/service-details/6739265ee089e4aae58f4d8 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "cuisineSpecialties": ["Indian", "Chinese"],
    "serviceTypes": ["Wedding Catering"],
    "maximumCapacity": 500,
    "startingPricePerPerson": 500.00
  }'
```

### Update Vendor
```bash
curl -X PUT http://localhost:8080/api/vendor/6739265ee089e4aae58f4d8 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "website": "https://newwebsite.com",
    "yearsInBusiness": 15,
    "aboutBusiness": "Updated description"
  }'
```

### Search by Cuisine
```bash
curl http://localhost:8080/api/service-details/search/cuisine?cuisine=Indian
```

---

## 🚀 Postman Collection

Import this collection into Postman for easy testing:

```json
{
  "info": {
    "name": "Event Bidding - Service Details",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create Service Details",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/api/service-details/{{vendorId}}",
        "header": [
          {"key": "Content-Type", "value": "application/json"},
          {"key": "Authorization", "value": "Bearer {{jwt_token}}"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"cuisineSpecialties\": [\"Indian\"],\n  \"serviceTypes\": [\"Wedding Catering\"]\n}"
        }
      }
    }
  ]
}
```

---

## 💡 Tips

1. **Always authenticate** - Include JWT token in Authorization header
2. **Validate input** - Check required fields before sending requests
3. **Handle errors** - Check response codes and error messages
4. **Test search** - Use search endpoints to find vendors
5. **Update incrementally** - You can update individual fields without sending all data
6. **Check logs** - Server logs contain detailed error information

---

## 📞 Need Help?

- Check server logs: `logs/spring.log`
- Review documentation: `EMAIL_AND_VENDOR_IMPLEMENTATION.md`
- Test with cURL or Postman before frontend integration
- Verify JWT token is valid and not expired

