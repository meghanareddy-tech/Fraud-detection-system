package com.frauddetection.payment.service;

import com.frauddetection.payment.dto.PaymentEventDTO;
import com.frauddetection.payment.dto.PaymentRequestDTO;
import com.frauddetection.payment.dto.PaymentResponseDTO;
import com.frauddetection.payment.kafka.PaymentEventProducer;
import com.frauddetection.payment.model.Payment;
import com.frauddetection.payment.model.PaymentStatus;
import com.frauddetection.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public PaymentResponseDTO initiatePayment(PaymentRequestDTO request) {
        log.info("Initiating payment from sender: {} to receiver: {}",
                request.getSenderId(), request.getReceiverId());

        // Save payment to DB
        Payment payment = Payment.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .ipAddress(request.getIpAddress())
                .deviceId(request.getDeviceId())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment saved with id: {}", saved.getId());

        // Publish event to Kafka for fraud detection
        PaymentEventDTO event = PaymentEventDTO.builder()
                .paymentId(saved.getId())
                .senderId(saved.getSenderId())
                .receiverId(saved.getReceiverId())
                .amount(saved.getAmount())
                .currency(saved.getCurrency())
                .ipAddress(saved.getIpAddress())
                .deviceId(saved.getDeviceId())
                .createdAt(saved.getCreatedAt())
                .build();

        paymentEventProducer.publishPaymentEvent(event);

        return mapToResponseDTO(saved, "Payment initiated and sent for fraud analysis");
    }

    @Cacheable(value = "payments", key = "#paymentId")
    public PaymentResponseDTO getPaymentById(String paymentId) {
        log.info("Fetching payment by id: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        return mapToResponseDTO(payment, "Payment retrieved successfully");
    }

    public List<PaymentResponseDTO> getPaymentsBySender(String senderId) {
        log.info("Fetching payments for sender: {}", senderId);
        return paymentRepository.findBySenderId(senderId)
                .stream()
                .map(p -> mapToResponseDTO(p, null))
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "payments", key = "#paymentId")
    public PaymentResponseDTO updatePaymentStatus(String paymentId, PaymentStatus status) {
        log.info("Updating payment {} status to {}", paymentId, status);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        payment.setStatus(status);
        Payment updated = paymentRepository.save(payment);
        return mapToResponseDTO(updated, "Payment status updated");
    }

    private PaymentResponseDTO mapToResponseDTO(Payment payment, String message) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .senderId(payment.getSenderId())
                .receiverId(payment.getReceiverId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .ipAddress(payment.getIpAddress())
                .deviceId(payment.getDeviceId())
                .createdAt(payment.getCreatedAt() != null ?
                        payment.getCreatedAt().toString() : null)
                .message(message)
                .build();
    }
}