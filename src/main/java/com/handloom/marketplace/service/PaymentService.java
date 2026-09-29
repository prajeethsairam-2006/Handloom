package com.handloom.marketplace.service;

import com.handloom.marketplace.exception.ResourceNotFoundException;
import com.handloom.marketplace.model.Payment;
import com.handloom.marketplace.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order ID: " + orderId));
    }

    public void updatePaymentStatus(Long paymentId, String status) {
        paymentRepository.updateStatus(paymentId, status);
    }

    public void markPaidByOrderId(Long orderId) {
        paymentRepository.updateStatusByOrderId(orderId, "PAID");
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }
}
