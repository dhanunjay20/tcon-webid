# Stripe Payment Integration - Production Ready

## Overview

This document provides comprehensive information about the Stripe payment integration module developed for the TCON WebID bidding platform.

## Table of Contents

1. [Features](#features)
2. [Architecture](#architecture)
3. [Setup Instructions](#setup-instructions)
4. [API Endpoints](#api-endpoints)
5. [Usage Examples](#usage-examples)
6. [Webhook Configuration](#webhook-configuration)
7. [Security Considerations](#security-considerations)
8. [Testing](#testing)
9. [Troubleshooting](#troubleshooting)

## Features

### Core Payment Features
- ✅ **Create Payment Intent** - Initialize secure payment for orders
- ✅ **Confirm Payment** - Process and verify successful payments
- ✅ **Cancel Payment** - Cancel pending payments
- ✅ **Refund Payment** - Full or partial refunds
- ✅ **Payment History** - Track all payment transactions
- ✅ **Webhook Support** - Real-time payment event notifications
- ✅ **Customer Management** - Automatic Stripe customer creation
- ✅ **Payment Method Storage** - Save cards for future use
- ✅ **Multi-currency Support** - Accept payments in different currencies

### Security Features
- ✅ Webhook signature verification
- ✅ Secure payment intent creation
- ✅ PCI-compliant payment processing
- ✅ Encrypted customer data storage
- ✅ Transaction logging and audit trails

## Architecture

### Components Created

```
src/main/java/com/tcon/webid/
├── config/
│   └── StripeConfig.java                 # Stripe API configuration
├── controller/
│   └── PaymentController.java            # REST API endpoints
├── dto/
│   ├── PaymentIntentRequestDto.java      # Create payment request
│   ├── PaymentIntentResponseDto.java     # Payment intent response
│   ├── PaymentResponseDto.java           # Payment details response
│   ├── RefundRequestDto.java             # Refund request
│   └── WebhookEventDto.java              # Webhook event data
├── entity/
│   └── Payment.java                      # Payment database model
├── exception/
│   └── PaymentException.java             # Payment-specific exceptions
├── repository/
│   └── PaymentRepository.java            # MongoDB repository
└── service/
    ├── PaymentService.java               # Service interface
    └── PaymentServiceImpl.java           # Service implementation
```

### Database Schema

**Payment Collection:**
```json
{
  "id": "ObjectId",
  "orderId": "string (indexed)",
  "customerId": "string (indexed)",
  "vendorOrganizationId": "string",
  "stripePaymentIntentId": "string (unique, indexed)",
  "stripeCustomerId": "string",
  "stripeChargeId": "string",
  "amount": "double (in cents)",
  "currency": "string (USD, EUR, etc.)",
  "status": "string (pending, succeeded, failed, canceled, refunded)",
  "paymentMethod": "string (card, bank_account)",
  "paymentMethodType": "string (visa, mastercard, etc.)",
  "last4": "string",
  "brand": "string",
  "description": "string",
  "receiptUrl": "string",
  "failureReason": "string",
  "refundId": "string",
  "refundAmount": "double",
  "refundReason": "string",
  "createdAt": "ISO Date",
  "updatedAt": "ISO Date",
  "paidAt": "ISO Date",
  "ipAddress": "string",
  "userAgent": "string"
}
```

## Setup Instructions

### 1. Configure Stripe API Keys

Add your Stripe API keys directly to `src/main/resources/application.properties` (no .env or environment variables required):

```properties
# Stripe Configuration
# Replace the example values below with your actual keys from the Stripe Dashboard
stripe.api.key=sk_test_your_test_secret_key_here
stripe.webhook.secret=whsec_your_webhook_secret_here
stripe.publishable.key=pk_test_your_test_publishable_key_here
```

### 2. Get Stripe API Keys

1. Sign up at [https://dashboard.stripe.com/register](https://dashboard.stripe.com/register)
2. Navigate to **Developers > API Keys**
3. Copy your **Secret key** and **Publishable key**
4. For webhooks, go to **Developers > Webhooks** and create an endpoint

### 3. Update User Entity

The `User` entity has been updated to include `stripeCustomerId` field for managing Stripe customers.

## API Endpoints

### 1. Create Payment Intent

**Endpoint:** `POST /api/payments/create-intent`

**Description:** Creates a Stripe payment intent for an order.

**Request Body:**
```json
{
  "orderId": "order123",
  "amount": 100.50,
  "currency": "usd",
  "description": "Payment for catering order #123",
  "savePaymentMethod": false,
  "customerEmail": "customer@example.com",
  "customerName": "John Doe"
}
```

**Response:**
```json
{
  "paymentIntentId": "pi_3L1234567890",
  "clientSecret": "pi_3L1234567890_secret_xyz",
  "status": "requires_payment_method",
  "amount": 100.50,
  "currency": "usd",
  "orderId": "order123",
  "customerId": "user123",
  "message": "Payment intent created successfully"
}
```

**Status Codes:**
- `200 OK` - Payment intent created successfully
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Order or customer not found

---

### 2. Confirm Payment

**Endpoint:** `POST /api/payments/confirm/{paymentIntentId}`

**Description:** Confirms a payment after successful client-side payment.

**Response:**
```json
{
  "id": "payment123",
  "orderId": "order123",
  "customerId": "user123",
  "vendorOrganizationId": "vendor123",
  "stripePaymentIntentId": "pi_3L1234567890",
  "amount": 100.50,
  "currency": "USD",
  "status": "succeeded",
  "paymentMethod": "card",
  "paymentMethodType": "visa",
  "last4": "4242",
  "brand": "visa",
  "receiptUrl": "https://pay.stripe.com/receipts/...",
  "createdAt": "2025-12-17T12:00:00Z",
  "updatedAt": "2025-12-17T12:05:00Z",
  "paidAt": "2025-12-17T12:05:00Z"
}
```

---

### 3. Get Payment by ID

**Endpoint:** `GET /api/payments/{paymentId}`

**Description:** Retrieves payment details by payment ID.

---

### 4. Get Payment by Payment Intent ID

**Endpoint:** `GET /api/payments/intent/{paymentIntentId}`

**Description:** Retrieves payment details by Stripe payment intent ID.

---

### 5. Get Payments by Order ID

**Endpoint:** `GET /api/payments/order/{orderId}`

**Description:** Retrieves all payments associated with an order.

**Response:**
```json
[
  {
    "id": "payment123",
    "orderId": "order123",
    "amount": 100.50,
    "status": "succeeded",
    ...
  }
]
```

---

### 6. Get Payments by Customer ID

**Endpoint:** `GET /api/payments/customer/{customerId}`

**Description:** Retrieves all payments made by a customer.

---

### 7. Get Payments by Vendor ID

**Endpoint:** `GET /api/payments/vendor/{vendorOrganizationId}`

**Description:** Retrieves all payments received by a vendor.

---

### 8. Cancel Payment

**Endpoint:** `POST /api/payments/{paymentId}/cancel`

**Description:** Cancels a pending payment intent.

**Note:** Only payments with status `requires_payment_method`, `requires_confirmation`, or `requires_action` can be cancelled.

---

### 9. Refund Payment

**Endpoint:** `POST /api/payments/refund`

**Description:** Creates a full or partial refund for a payment.

**Request Body:**
```json
{
  "paymentId": "payment123",
  "amount": 50.25,
  "reason": "requested_by_customer",
  "description": "Customer requested refund"
}
```

**Refund Reasons:**
- `duplicate` - Duplicate payment
- `fraudulent` - Fraudulent transaction
- `requested_by_customer` - Customer requested refund

**Response:**
```json
{
  "id": "payment123",
  "status": "partially_refunded",
  "refundId": "re_1234567890",
  "refundAmount": 50.25,
  "refundReason": "Customer requested refund",
  ...
}
```

---

### 10. Webhook Endpoint

**Endpoint:** `POST /api/payments/webhook`

**Description:** Receives Stripe webhook events for real-time payment updates.

**Supported Events:**
- `payment_intent.succeeded` - Payment successful
- `payment_intent.payment_failed` - Payment failed
- `payment_intent.canceled` - Payment cancelled
- `charge.refunded` - Payment refunded
- `charge.dispute.created` - Dispute created

**Headers Required:**
- `Stripe-Signature` - Webhook signature for verification

---

### 11. Health Check

**Endpoint:** `GET /api/payments/health`

**Description:** Health check endpoint for payment service.

**Response:**
```json
{
  "status": "healthy",
  "service": "payment"
}
```

## Usage Examples

### Frontend Integration (React/Next.js)

```javascript
import { loadStripe } from '@stripe/stripe-js';
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';

const stripePromise = loadStripe('pk_test_your_publishable_key');

// 1. Create Payment Intent
async function createPaymentIntent(orderId, amount) {
  const response = await fetch('/api/payments/create-intent', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`
    },
    body: JSON.stringify({
      orderId: orderId,
      amount: amount,
      currency: 'usd',
      description: `Payment for order ${orderId}`
    })
  });
  
  return await response.json();
}

// 2. Payment Form Component
function CheckoutForm({ clientSecret }) {
  const stripe = useStripe();
  const elements = useElements();
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!stripe || !elements) return;

    const { error, paymentIntent } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: 'https://yourapp.com/payment-success',
      },
    });

    if (error) {
      setMessage(error.message);
    } else if (paymentIntent && paymentIntent.status === 'succeeded') {
      // Confirm payment on backend
      await fetch(`/api/payments/confirm/${paymentIntent.id}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      setMessage('Payment successful!');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <PaymentElement />
      <button disabled={!stripe}>Pay Now</button>
      {message && <div>{message}</div>}
    </form>
  );
}

// 3. Main Payment Component
function PaymentPage({ orderId, amount }) {
  const [clientSecret, setClientSecret] = useState('');

  useEffect(() => {
    createPaymentIntent(orderId, amount)
      .then(data => setClientSecret(data.clientSecret));
  }, [orderId, amount]);

  return (
    <Elements stripe={stripePromise} options={{ clientSecret }}>
      <CheckoutForm clientSecret={clientSecret} />
    </Elements>
  );
}
```

### Backend Integration (Spring Boot)

```java
// Example: Creating a payment during order checkout
@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private PaymentService paymentService;
    
    @Transactional
    public OrderResponseDto createOrderWithPayment(OrderRequestDto orderDto) {
        // Create order
        Order order = createOrder(orderDto);
        
        // Create payment intent
        PaymentIntentRequestDto paymentRequest = new PaymentIntentRequestDto();
        paymentRequest.setOrderId(order.getId());
        paymentRequest.setAmount(order.getTotalPrice());
        paymentRequest.setCurrency("usd");
        paymentRequest.setDescription("Payment for order " + order.getId());
        
        PaymentIntentResponseDto paymentIntent = 
            paymentService.createPaymentIntent(paymentRequest, orderDto.getCustomerId());
        
        // Return order with payment details
        return mapToOrderResponse(order, paymentIntent);
    }
}
```

## Webhook Configuration

### 1. Configure Webhook in Stripe Dashboard

1. Go to **Developers > Webhooks**
2. Click **Add endpoint**
3. Enter your endpoint URL: `https://yourdomain.com/api/payments/webhook`
4. Select events to listen to:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `payment_intent.canceled`
   - `charge.refunded`
   - `charge.dispute.created`
5. Copy the webhook signing secret

### 2. Update Application Properties

```properties
stripe.webhook.secret=whsec_your_webhook_signing_secret
```

### 3. Test Webhooks Locally

Use Stripe CLI for local testing:

```bash
# Install Stripe CLI
# https://stripe.com/docs/stripe-cli

# Login to Stripe
stripe login

# Forward webhooks to local server
stripe listen --forward-to localhost:8080/api/payments/webhook

# Trigger test events
stripe trigger payment_intent.succeeded
```

## Security Considerations

### 1. API Key Management
- ✅ Never commit API keys to version control
- ✅ Use environment variables for production
- ✅ Rotate keys regularly
- ✅ Use separate keys for test and production

### 2. Webhook Security
- ✅ Always verify webhook signatures
- ✅ Use HTTPS endpoints only
- ✅ Implement rate limiting
- ✅ Log all webhook events

### 3. Payment Data
- ✅ Never store full card numbers
- ✅ Store only last 4 digits and brand
- ✅ Use Stripe for PCI compliance
- ✅ Encrypt sensitive data at rest

### 4. Authentication
- ✅ Require authentication for all payment endpoints
- ✅ Validate customer owns the order
- ✅ Implement role-based access control
- ✅ Log all payment transactions

### 5. Production Checklist
- [ ] Replace test API keys with production keys
- [ ] Enable webhook signature verification
- [ ] Configure SSL/TLS certificates
- [ ] Set up monitoring and alerts
- [ ] Implement rate limiting
- [ ] Configure error logging
- [ ] Test refund workflows
- [ ] Document disaster recovery procedures

## Testing

### Unit Tests Example

```java
@SpringBootTest
class PaymentServiceImplTest {
    
    @Autowired
    private PaymentService paymentService;
    
    @MockBean
    private StripeConfig stripeConfig;
    
    @Test
    void testCreatePaymentIntent() {
        PaymentIntentRequestDto request = new PaymentIntentRequestDto();
        request.setOrderId("order123");
        request.setAmount(100.0);
        request.setCurrency("usd");
        
        PaymentIntentResponseDto response = 
            paymentService.createPaymentIntent(request, "user123");
        
        assertNotNull(response.getPaymentIntentId());
        assertNotNull(response.getClientSecret());
        assertEquals("usd", response.getCurrency());
    }
}
```

### Integration Testing with Stripe Test Mode

Use Stripe test cards:
- Success: `4242 4242 4242 4242`
- Decline: `4000 0000 0000 0002`
- Requires Authentication: `4000 0025 0000 3155`

## Troubleshooting

### Common Issues

**1. "Invalid API Key" Error**
```
Solution: Verify stripe.api.key in application.properties
Check environment variables are set correctly
```

**2. "Webhook Signature Verification Failed"**
```
Solution: Ensure stripe.webhook.secret matches Stripe dashboard
Verify Stripe-Signature header is passed correctly
```

**3. "Payment Intent Already Succeeded"**
```
Solution: Check payment status before attempting to confirm
Use idempotency keys for retry logic
```

**4. "Amount Too Small/Large"**
```
Solution: Stripe has minimum amounts per currency
USD: $0.50 minimum, EUR: €0.50 minimum
```

**5. "Customer Not Found"**
```
Solution: Ensure user exists in database
Check getOrCreateStripeCustomer() is called
```

### Debug Mode

Enable detailed logging:

```properties
logging.level.com.tcon.webid.service.PaymentServiceImpl=DEBUG
logging.level.com.stripe=DEBUG
```

### Support Resources

- Stripe Documentation: https://stripe.com/docs
- Stripe Support: https://support.stripe.com
- API Reference: https://stripe.com/docs/api
- Testing Guide: https://stripe.com/docs/testing

## License

This payment integration module is part of the TCON WebID platform.

## Support

For issues or questions regarding the payment integration:
1. Check this documentation
2. Review Stripe documentation
3. Contact the development team
4. Check application logs for detailed error messages

---

**Last Updated:** December 17, 2025
**Version:** 1.0.0
**Author:** TCON Development Team

