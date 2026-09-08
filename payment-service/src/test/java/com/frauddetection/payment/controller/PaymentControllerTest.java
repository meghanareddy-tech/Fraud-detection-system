package com.frauddetection.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.payment.dto.PaymentRequestDTO;
import com.frauddetection.payment.dto.PaymentResponseDTO;
import com.frauddetection.payment.model.PaymentStatus;
import com.frauddetection.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentRequestDTO validRequest;
    private PaymentResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        validRequest = PaymentRequestDTO.builder()
                .senderId("sender-001")
                .receiverId("receiver-001")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .ipAddress("192.168.1.1")
                .deviceId("device-001")
                .build();

        mockResponse = PaymentResponseDTO.builder()
                .id("pay-001")
                .senderId("sender-001")
                .receiverId("receiver-001")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now().toString())
                .message("Payment initiated and sent for fraud analysis")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/payments - Should create payment and return 201")
    void shouldCreatePaymentAndReturn201() throws Exception {
        when(paymentService.initiatePayment(any(PaymentRequestDTO.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("pay-001"))
                .andExpect(jsonPath("$.senderId").value("sender-001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message")
                        .value("Payment initiated and sent for fraud analysis"));
    }

    @Test
    @DisplayName("POST /api/v1/payments - Should return 400 when request is invalid")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        PaymentRequestDTO invalidRequest = PaymentRequestDTO.builder()
                .senderId("")
                .receiverId("")
                .amount(new BigDecimal("-100"))
                .currency("US")
                .build();

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/payments/{id} - Should return payment when found")
    void shouldReturnPaymentWhenFound() throws Exception {
        when(paymentService.getPaymentById("pay-001")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/payments/pay-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pay-001"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/payments/{id} - Should return 404 when not found")
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        when(paymentService.getPaymentById("invalid-id"))
                .thenThrow(new RuntimeException("Payment not found: invalid-id"));

        mockMvc.perform(get("/api/v1/payments/invalid-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment not found: invalid-id"));
    }

    @Test
    @DisplayName("PATCH /api/v1/payments/{id}/status - Should update status successfully")
    void shouldUpdatePaymentStatus() throws Exception {
        mockResponse.setStatus(PaymentStatus.FLAGGED);
        when(paymentService.updatePaymentStatus("pay-001", PaymentStatus.FLAGGED))
                .thenReturn(mockResponse);

        mockMvc.perform(patch("/api/v1/payments/pay-001/status")
                        .param("status", "FLAGGED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FLAGGED"));
    }
}