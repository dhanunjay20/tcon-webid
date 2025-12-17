# Payment API - Quick Test Guide

## Postman Collection / cURL Examples

### 1. Create Payment Intent

```bash
curl -X POST http://localhost:8080/api/payments/create-intent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "orderId": "675d8f9e1234567890abcdef",
    "amount": 150.00,
    "currency": "usd",
    "description": "Payment for catering order",
    "savePaymentMethod": false,
    "customerEmail": "customer@example.com",
    "customerName": "John Doe"
  }'
```

### 2. Confirm Payment

```bash
curl -X POST http://localhost:8080/api/payments/confirm/pi_3L1234567890 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Get Payment by ID

```bash
curl -X GET http://localhost:8080/api/payments/payment123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Get Payments by Order

```bash
curl -X GET http://localhost:8080/api/payments/order/order123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Get Customer Payments

```bash
curl -X GET http://localhost:8080/api/payments/customer/user123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 6. Get Vendor Payments

```bash
curl -X GET http://localhost:8080/api/payments/vendor/vendor123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 7. Cancel Payment

```bash
curl -X POST http://localhost:8080/api/payments/payment123/cancel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 8. Refund Payment (Full)

```bash
curl -X POST http://localhost:8080/api/payments/refund \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "paymentId": "payment123",
    "reason": "requested_by_customer",
    "description": "Customer requested full refund"
  }'
```

### 9. Refund Payment (Partial)

```bash
curl -X POST http://localhost:8080/api/payments/refund \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "paymentId": "payment123",
    "amount": 50.00,
    "reason": "requested_by_customer",
    "description": "Partial refund for damaged items"
  }'
```

### 10. Health Check

```bash
curl -X GET http://localhost:8080/api/payments/health
```

## Stripe Test Cards

Use these test cards in Stripe test mode:

| Card Number          | Brand      | Result                      |
|---------------------|------------|-----------------------------|
| 4242 4242 4242 4242 | Visa       | Success                     |
| 5555 5555 5555 4444 | Mastercard | Success                     |
| 4000 0000 0000 0002 | Visa       | Card Declined               |
| 4000 0025 0000 3155 | Visa       | Requires Authentication     |
| 4000 0000 0000 9995 | Visa       | Insufficient Funds          |

**Expiry Date:** Any future date (e.g., 12/34)  
**CVC:** Any 3 digits (e.g., 123)  
**ZIP:** Any 5 digits (e.g., 12345)

## Payment Status Flow

```
CREATE PAYMENT INTENT
    ↓
requires_payment_method
    ↓
CUSTOMER ENTERS CARD
    ↓
processing
    ↓
succeeded / failed
    ↓
CONFIRM PAYMENT (Backend)
    ↓
Order Status: confirmed
```

## Webhook Testing (Local Development)

### Using Stripe CLI

1. **Install Stripe CLI:**
   ```bash
   # Windows (using Scoop)
   scoop install stripe
   
   # Mac (using Homebrew)
   brew install stripe/stripe-cli/stripe
   
   # Or download from: https://github.com/stripe/stripe-cli/releases
   ```

2. **Login to Stripe:**
   ```bash
   stripe login
   ```

3. **Forward webhooks to local server:**
   ```bash
   stripe listen --forward-to localhost:8080/api/payments/webhook
   ```

4. **Trigger test events:**
   ```bash
   # Payment succeeded
   stripe trigger payment_intent.succeeded
   
   # Payment failed
   stripe trigger payment_intent.payment_failed
   
   # Charge refunded
   stripe trigger charge.refunded
   ```

## Environment Variables Setup

This project stores keys directly in `src/main/resources/application.properties`. Add your Stripe keys there (do NOT commit them to git).

### Development (application.properties)

```properties
stripe.api.key=sk_test_...
stripe.webhook.secret=whsec_...
stripe.publishable.key=pk_test_...
```

### Production (Docker/Kubernetes)

For production you may set the same values in the deployment platform's secure configuration. The application reads values from `application.properties` at startup.

## Quick Troubleshooting

| Error | Solution |
|-------|----------|
| 401 Unauthorized | Check JWT token is valid and not expired |
| 404 Not Found | Verify order/payment ID exists in database |
| 400 Bad Request | Check request body format matches DTO |
| 500 Internal Error | Check Stripe API key is valid |
| Webhook signature failed | Verify webhook secret matches Stripe dashboard |

## Testing Workflow

1. **Create an Order** (use existing order endpoints)
2. **Create Payment Intent** for that order
3. **Use Frontend** or Stripe's test mode to complete payment
4. **Confirm Payment** on backend
5. **Verify** payment status is "succeeded"
6. **Check** order status is "confirmed"
7. **Test Refund** (optional)
8. **Verify** refund status and order update

## Production Deployment Checklist

- [ ] Replace test API keys with live keys
- [ ] Update webhook URL to production domain
- [ ] Enable webhook signature verification
- [ ] Configure HTTPS/SSL
- [ ] Test payment flow end-to-end
- [ ] Test refund flow
- [ ] Set up monitoring/alerts
- [ ] Test webhook events
- [ ] Review security configurations
- [ ] Enable rate limiting
- [ ] Configure backup payment provider (optional)

## Monitoring

Check these endpoints for health:

```bash
# Application health
curl http://localhost:8080/api/payments/health

# Actuator health (if enabled)
curl http://localhost:8080/actuator/health
```

## Support

- Stripe Dashboard: https://dashboard.stripe.com
- Stripe API Docs: https://stripe.com/docs/api
- Test Mode: https://dashboard.stripe.com/test/payments
- Logs: Check application logs for detailed error messages

---

**Quick Start:** Get your Stripe test keys → Update application.properties → Start server → Test with Postman/cURL
