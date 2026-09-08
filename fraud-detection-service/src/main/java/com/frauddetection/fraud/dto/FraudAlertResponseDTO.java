package com.frauddetection.fraud.dto;

import com.frauddetection.fraud.model.AlertStatus;
import com.frauddetection.fraud.model.RiskLevel;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlertResponseDTO {

    private String id;
    private String paymentId;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String currency;
    private Double riskScore;
    private RiskLevel riskLevel;
    private AlertStatus status;
    private String riskReasons;
    private String aiExplanation;
    private String createdAt;
}