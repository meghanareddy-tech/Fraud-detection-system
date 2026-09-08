package com.frauddetection.fraud.dto;

import com.frauddetection.fraud.model.RiskLevel;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessmentDTO {

    private String paymentId;
    private Double riskScore;
    private RiskLevel riskLevel;
    private List<String> riskReasons;
    private boolean requiresAiAnalysis;
}