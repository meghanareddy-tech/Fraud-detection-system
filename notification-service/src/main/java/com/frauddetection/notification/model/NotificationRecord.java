package com.frauddetection.notification.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecord {

    private String id;
    private String paymentId;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String currency;
    private String riskLevel;
    private String status;
    private String message;
    private String ipAddress;
    private String deviceId;
    private LocalDateTime createdAt;
}