package com.frauddetection.fraud.service;

import com.frauddetection.fraud.dto.PaymentEventDTO;
import com.frauddetection.fraud.dto.RiskAssessmentDTO;
import com.frauddetection.fraud.model.RiskLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RiskScoringEngine {

    @Value("${fraud.rules.high-amount-threshold}")
    private double highAmountThreshold;

    @Value("${fraud.rules.suspicious-amount-threshold}")
    private double suspiciousAmountThreshold;

    private static final List<String> SUSPICIOUS_IP_PREFIXES =
            List.of("45.33", "192.241", "198.199", "104.236");

    public RiskAssessmentDTO assess(PaymentEventDTO event) {
        log.info("Assessing risk for payment: {}", event.getPaymentId());

        List<String> riskReasons = new ArrayList<>();
        double riskScore = 0.0;

        // Rule 1 — High amount check
        if (event.getAmount() != null &&
                event.getAmount().compareTo(
                        BigDecimal.valueOf(suspiciousAmountThreshold)) >= 0) {
            riskScore += 40.0;
            riskReasons.add("Transaction amount exceeds suspicious threshold of $"
                    + suspiciousAmountThreshold);
        } else if (event.getAmount() != null &&
                event.getAmount().compareTo(
                        BigDecimal.valueOf(highAmountThreshold)) >= 0) {
            riskScore += 20.0;
            riskReasons.add("Transaction amount exceeds high threshold of $"
                    + highAmountThreshold);
        }

        // Rule 2 — Suspicious IP check
        if (event.getIpAddress() != null) {
            boolean suspiciousIp = SUSPICIOUS_IP_PREFIXES.stream()
                    .anyMatch(prefix -> event.getIpAddress().startsWith(prefix));
            if (suspiciousIp) {
                riskScore += 30.0;
                riskReasons.add("Transaction originated from suspicious IP: "
                        + event.getIpAddress());
            }
        }

        // Rule 3 — Unknown device check
        if (event.getDeviceId() != null &&
                event.getDeviceId().toLowerCase().contains("unknown")) {
            riskScore += 20.0;
            riskReasons.add("Transaction from unknown device: " + event.getDeviceId());
        }

        // Rule 4 — Same sender/receiver check
        if (event.getSenderId() != null &&
                event.getSenderId().equals(event.getReceiverId())) {
            riskScore += 50.0;
            riskReasons.add("Sender and receiver are the same account");
        }

        // Cap at 100
        riskScore = Math.min(riskScore, 100.0);

        RiskLevel riskLevel = determineRiskLevel(riskScore);
        boolean requiresAiAnalysis = riskScore >= 40.0;

        log.info("Risk assessment complete for payment: {} | score: {} | level: {}",
                event.getPaymentId(), riskScore, riskLevel);

        return RiskAssessmentDTO.builder()
                .paymentId(event.getPaymentId())
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .riskReasons(riskReasons)
                .requiresAiAnalysis(requiresAiAnalysis)
                .build();
    }

    private RiskLevel determineRiskLevel(double score) {
        if (score >= 70.0) return RiskLevel.CRITICAL;
        if (score >= 40.0) return RiskLevel.HIGH;
        if (score >= 20.0) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
}