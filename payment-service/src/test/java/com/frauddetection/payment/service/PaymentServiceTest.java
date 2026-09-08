package com.frauddetection.payment.service;

import com.frauddetection.payment.dto.PaymentRequestDTO;
import com.frauddetection.payment.dto.PaymentResponseDTO;
import com.frauddetection.payment.kafka.PaymentEventProducer;
import com.frauddetection.payment.model.Payment;
import com.frauddetection.payment.model.PaymentStatus;
import com.frauddetection.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private Payment mockPayment;
    private PaymentRequestDTO mockRequest;

    @BeforeEach
    void setUp() {
        mockPayment = Payment.builder()
                .id("pay-001")
                .senderId("sender-001")
                .receiverId("receiver-001")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .ipAddress("192.168.1.1")
                .deviceId("device-001")
                .createdAt(LocalDateTime.now())
                .build();

        mockRequest = PaymentRequestDTO.builder()
                .senderId("sender-001")
                .receiverId("receiver-001")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .ipAddress("192.168.1.1")
                .deviceId("device-001")
                .build();
    }

    @Test
    @DisplayName("Should initiate payment successfully and publish Kafka event")
    void shouldInitiatePaymentSuccessfully() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        doNothing().when(paymentEventProducer).publishPaymentEvent(any());

        PaymentResponseDTO response = paymentService.initiatePayment(mockRequest);

        assertThat(response).isNotNull();
        assertThat(response.getSenderId()).isEqualTo("sender-001");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentEventProducer, times(1)).publishPaymentEvent(any());
    }

    @Test
    @DisplayName("Should return payment when valid ID is provided")
    void shouldReturnPaymentById() {
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(mockPayment));

        PaymentResponseDTO response = paymentService.getPaymentById("pay-001");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("pay-001");
        assertThat(response.getSenderId()).isEqualTo("sender-001");
        verify(paymentRepository, times(1)).findById("pay-001");
    }

    @Test
    @DisplayName("Should throw exception when payment ID not found")
    void shouldThrowExceptionWhenPaymentNotFound() {
        when(paymentRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById("invalid-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");

        verify(paymentRepository, times(1)).findById("invalid-id");
    }

    @Test
    @DisplayName("Should return all payments for a sender")
    void shouldReturnPaymentsBySender() {
        when(paymentRepository.findBySenderId("sender-001"))
                .thenReturn(List.of(mockPayment));

        List<PaymentResponseDTO> payments = paymentService.getPaymentsBySender("sender-001");

        assertThat(payments).isNotNull();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getSenderId()).isEqualTo("sender-001");
        verify(paymentRepository, times(1)).findBySenderId("sender-001");
    }

    @Test
    @DisplayName("Should update payment status successfully")
    void shouldUpdatePaymentStatus() {
        when(paymentRepository.findById("pay-001")).thenReturn(Optional.of(mockPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

        PaymentResponseDTO response = paymentService
                .updatePaymentStatus("pay-001", PaymentStatus.FLAGGED);

        assertThat(response).isNotNull();
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when updating status for non-existent payment")
    void shouldThrowExceptionWhenUpdatingStatusForNonExistentPayment() {
        when(paymentRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                paymentService.updatePaymentStatus("invalid-id", PaymentStatus.FLAGGED))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");
    }
}