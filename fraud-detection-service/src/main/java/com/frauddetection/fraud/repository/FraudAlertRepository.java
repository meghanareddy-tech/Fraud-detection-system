package com.frauddetection.fraud.repository;

import com.frauddetection.fraud.model.AlertStatus;
import com.frauddetection.fraud.model.FraudAlert;
import com.frauddetection.fraud.model.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, String> {

    Optional<FraudAlert> findByPaymentId(String paymentId);

    List<FraudAlert> findByStatus(AlertStatus status);

    List<FraudAlert> findByRiskLevel(RiskLevel riskLevel);

    List<FraudAlert> findBySenderId(String senderId);

    @Query("SELECT COUNT(f) FROM FraudAlert f WHERE f.senderId = :senderId " +
            "AND f.createdAt >= :since AND f.riskLevel IN ('HIGH', 'CRITICAL')")
    long countHighRiskAlertsBySender(@Param("senderId") String senderId,
                                     @Param("since") LocalDateTime since);
}