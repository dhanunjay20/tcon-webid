package com.tcon.webid.repository;

import com.tcon.webid.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Payment> findByOrderId(String orderId);

    List<Payment> findByCustomerId(String customerId);

    List<Payment> findByVendorOrganizationId(String vendorOrganizationId);

    List<Payment> findByStatus(String status);

    List<Payment> findByCustomerIdAndStatus(String customerId, String status);

    Optional<Payment> findByOrderIdAndStatus(String orderId, String status);
}

