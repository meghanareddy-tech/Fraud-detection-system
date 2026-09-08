package com.frauddetection.payment.repository;

import com.frauddetection.payment.model.Payment;
import com.frauddetection.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findBySenderId(String senderId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.senderId = :senderId " +
            "AND p.createdAt >= :since")
    long countRecentPaymentsBySender(@Param("senderId") String senderId,
                                     @Param("since") LocalDateTime since);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.senderId = :senderId " +
            "AND p.createdAt >= :since")
    BigDecimal sumRecentAmountBySender(@Param("senderId") String senderId,
                                       @Param("since") LocalDateTime since);
}