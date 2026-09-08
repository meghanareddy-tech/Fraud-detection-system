package com.frauddetection.payment.dto;

import com.frauddetection.payment.model.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {

    private String id;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String ipAddress;
    private String deviceId;
    private String createdAt;
    private String message;
}