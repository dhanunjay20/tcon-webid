# 🚀 Stripe Payment Integration - Deployment Checklist

## Phase 1: Setup & Configuration (15 minutes)

### Step 1: Get Stripe Account & API Keys
- [ ] Sign up at https://dashboard.stripe.com/register
- [ ] Complete business verification
- [ ] Navigate to **Developers > API Keys**
- [ ] Copy **Publishable key** (starts with `pk_test_`)
- [ ] Copy **Secret key** (starts with `sk_test_`)
- [ ] Keep keys secure, never commit to Git

### Step 2: Configure Application
- [ ] Open `src/main/resources/application.properties`
- [ ] Update Stripe configuration (put keys directly here):
```properties
stripe.api.key=sk_test_YOUR_KEY_HERE
stripe.webhook.secret=whsec_YOUR_WEBHOOK_SECRET
stripe.publishable.key=pk_test_YOUR_KEY_HERE
```

### Step 3: Set Runtime Configuration (Production)

For production, store the same values securely inside your deployment platform (Kubernetes secrets, Docker secrets, or PaaS configuration). The application reads values from `application.properties` at startup.

---

## Phase 2: Build & Test (20 minutes)

### Step 4: Build the Project
```bash
# Clean and build
./mvnw clean package -DskipTests

# Verify build successful
# Look for: BUILD SUCCESS
```

### Step 5: Start the Application
```bash
# Start Spring Boot app
./mvnw spring-boot:run

# Or run the JAR
java -jar target/webid-0.0.1-SNAPSHOT.jar
```

### Step 6: Test Health Check
```bash
curl http://localhost:8080/api/payments/health

# Expected response:
# {"status":"healthy","service":"payment"}
```

### Step 7: Test Create Payment Intent
```bash
# Replace YOUR_JWT_TOKEN with actual token from login
curl -X POST http://localhost:8080/api/payments/create-intent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "orderId": "EXISTING_ORDER_ID",
    "amount": 100.00,
    "currency": "usd",
    "description": "Test payment"
  }'

# Expected: Returns clientSecret and paymentIntentId
```

---

## Phase 3: Frontend Integration (30 minutes)

### Step 8: Install Stripe.js in Frontend
```bash
# For React/Next.js
npm install @stripe/stripe-js @stripe/react-stripe-js

# For vanilla JavaScript
# Add to HTML: <script src="https://js.stripe.com/v3/"></script>
```

### Step 9: Create Payment Form Component
- [ ] Import Stripe Elements
- [ ] Create payment form with CardElement
- [ ] Call `/api/payments/create-intent` endpoint
- [ ] Use `clientSecret` to confirm payment
- [ ] Call `/api/payments/confirm/{paymentIntentId}` after success

### Step 10: Test with Stripe Test Cards
- [ ] Use card: `4242 4242 4242 4242`
- [ ] Expiry: Any future date (e.g., 12/34)
- [ ] CVC: Any 3 digits (e.g., 123)
- [ ] ZIP: Any 5 digits (e.g., 12345)
- [ ] Complete a test payment
- [ ] Verify payment shows in Stripe Dashboard > Payments

---

## Phase 4: Webhook Configuration (15 minutes)

### Step 11: Setup Webhooks for Local Testing
```bash
# Install Stripe CLI
# Windows: scoop install stripe
# Mac: brew install stripe/stripe-cli/stripe

# Login to Stripe
stripe login

# Forward webhooks to local server
stripe listen --forward-to localhost:8080/api/payments/webhook

# Copy the webhook signing secret (starts with whsec_)
# Add to application.properties:
# stripe.webhook.secret=whsec_...
```

### Step 12: Test Webhook Events
```bash
# Trigger test events
stripe trigger payment_intent.succeeded
stripe trigger payment_intent.payment_failed
stripe trigger charge.refunded

# Check application logs for webhook processing
```

### Step 13: Configure Production Webhooks
- [ ] Go to Stripe Dashboard > **Developers > Webhooks**
- [ ] Click **Add endpoint**
- [ ] URL: `https://yourdomain.com/api/payments/webhook`
- [ ] Select events:
  - `payment_intent.succeeded`
  - `payment_intent.payment_failed`
  - `payment_intent.canceled`
  - `charge.refunded`
  - `charge.dispute.created`
- [ ] Copy webhook signing secret
- [ ] Update production environment variables

---

## Phase 5: End-to-End Testing (20 minutes)

### Step 14: Complete Payment Flow Test
- [ ] Create a new order in your application
- [ ] Navigate to payment page
- [ ] Enter test card details
- [ ] Complete payment
- [ ] Verify payment status = "succeeded"
- [ ] Verify order status = "confirmed"
- [ ] Check payment appears in database
- [ ] Check payment appears in Stripe Dashboard

### Step 15: Test Refund Flow
```bash
# Get payment ID from previous test
curl -X POST http://localhost:8080/api/payments/refund \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "paymentId": "PAYMENT_ID_FROM_PREVIOUS_TEST",
    "amount": 50.00,
    "reason": "requested_by_customer",
    "description": "Test partial refund"
  }'

# Verify refund in Stripe Dashboard
# Verify payment status updated to "partially_refunded"
```

### Step 16: Test Cancel Payment
```bash
# Create a payment intent (don't complete it)
# Get the payment ID
curl -X POST http://localhost:8080/api/payments/PAYMENT_ID/cancel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Verify status = "canceled"
```

---

## Phase 6: Production Deployment

### Step 17: Switch to Production Keys
- [ ] Go to Stripe Dashboard > **Developers > API Keys**
- [ ] Toggle to **Production** mode
- [ ] Copy production keys (starts with `sk_live_` and `pk_live_`)
- [ ] Update production environment variables:
```bash
STRIPE_API_KEY=sk_live_your_production_key
STRIPE_PUBLISHABLE_KEY=pk_live_your_production_key
STRIPE_WEBHOOK_SECRET=whsec_your_production_webhook_secret
```

### Step 18: Security Checklist
- [ ] SSL/HTTPS enabled on production domain
- [ ] API keys stored in secure environment variables
- [ ] Never commit API keys to Git
- [ ] Webhook signature verification enabled
- [ ] Rate limiting configured
- [ ] Error logging enabled
- [ ] Monitoring/alerts configured

### Step 19: Production Testing
- [ ] Test with real card (small amount like $1)
- [ ] Verify payment successful
- [ ] Test refund
- [ ] Monitor logs for errors
- [ ] Check Stripe Dashboard for payments

---

## Phase 7: Monitoring & Maintenance

### Step 20: Setup Monitoring
- [ ] Configure application logging
- [ ] Setup error alerts (email/Slack)
- [ ] Monitor payment success rate
- [ ] Track failed payments
- [ ] Review Stripe Dashboard regularly

### Step 21: Documentation
- [ ] Share API documentation with frontend team
- [ ] Document troubleshooting steps
- [ ] Create runbook for common issues
- [ ] Train support team on refund process

---

## 📊 Verification Checklist

After completing all phases, verify:

✅ **Backend:**
- [ ] Application starts without errors
- [ ] All payment endpoints accessible
- [ ] Database payment records created
- [ ] Webhooks receiving events
- [ ] Logs showing payment processing

✅ **Frontend:**
- [ ] Payment form loads correctly
- [ ] Stripe Elements rendering
- [ ] Test card accepted
- [ ] Success page displays
- [ ] Error messages shown for failed payments

✅ **Integration:**
- [ ] Create payment intent works
- [ ] Payment confirmation works
- [ ] Order status updates
- [ ] Refunds process correctly
- [ ] Webhooks updating payment status

✅ **Security:**
- [ ] HTTPS enabled
- [ ] API keys secured
- [ ] Webhook signature verified
- [ ] No sensitive data in logs
- [ ] PCI compliance maintained

---

## 🆘 Common Issues & Solutions

### Issue: "Invalid API Key"
**Solution:** 
- Verify key copied correctly (no extra spaces)
- Check environment variable is set
- Restart application after updating keys

### Issue: "Payment Intent not found"
**Solution:**
- Verify order exists in database
- Check customerId matches authenticated user
- Ensure payment intent was created successfully

### Issue: "Webhook signature verification failed"
**Solution:**
- Verify webhook secret matches Stripe Dashboard
- Check Stripe-Signature header is present
- Ensure endpoint URL is correct

### Issue: "Amount too small"
**Solution:**
- Minimum amounts: USD $0.50, EUR €0.50
- Convert amount to cents correctly (multiply by 100)

---

## 📞 Support Contacts

**Stripe Support:**
- Dashboard: https://dashboard.stripe.com
- Docs: https://stripe.com/docs
- Support: https://support.stripe.com

**Application Support:**
- Check logs: `logs/spring.log`
- Health check: `GET /api/payments/health`
- Documentation: See `PAYMENT_INTEGRATION_README.md`

---

## 🎉 Success Criteria

Your payment integration is **READY FOR PRODUCTION** when:

✅ All checklist items completed  
✅ Test payments successful  
✅ Refunds working  
✅ Webhooks processing events  
✅ Production keys configured  
✅ SSL/HTTPS enabled  
✅ Monitoring active  
✅ Team trained  

---

**Estimated Total Time:** 1.5 - 2 hours  
**Difficulty:** Intermediate  
**Prerequisites:** Stripe account, Basic API knowledge

**Good Luck! 🚀**
