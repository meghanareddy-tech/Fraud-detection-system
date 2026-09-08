package com.frauddetection.payment.controller;

import com.frauddetection.payment.dto.PaymentRequestDTO;
import com.frauddetection.payment.dto.PaymentResponseDTO;
import com.frauddetection.payment.model.PaymentStatus;
import com.frauddetection.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment API", description = "Endpoints for managing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Initiate a new payment",
            description = "Creates a payment and publishes it to Kafka for fraud analysis")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<PaymentResponseDTO> initiatePayment(
            @Valid @RequestBody PaymentRequestDTO request) {
        log.info("Received payment request from sender: {}", request.getSenderId());
        PaymentResponseDTO response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponseDTO> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/sender/{senderId}")
    @Operation(summary = "Get all payments by sender ID")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsBySender(
            @PathVariable String senderId) {
        return ResponseEntity.ok(paymentService.getPaymentsBySender(senderId));
    }

    @PatchMapping("/{paymentId}/status")
    @Operation(summary = "Update payment status",
            description = "Used by fraud detection service to update payment status")
    public ResponseEntity<PaymentResponseDTO> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
    }
}