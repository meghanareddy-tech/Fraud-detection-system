package com.frauddetection.notification.service;

import com.frauddetection.notification.dto.NotificationResponseDTO;
import com.frauddetection.notification.dto.PaymentEventDTO;
import com.frauddetection.notification.model.NotificationRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DynamoDbService dynamoDbService;
    private final S3Service s3Service;

    public void processPaymentEvent(PaymentEventDTO event) {
        log.info("Processing notification for payment: {}", event.getPaymentId());

        NotificationRecord record = NotificationRecord.builder()
                .paymentId(event.getPaymentId())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .ipAddress(event.getIpAddress())
                .deviceId(event.getDeviceId())
                .riskLevel("PENDING_ANALYSIS")
                .status("RECEIVED")
                .message("Payment received and queued for fraud analysis")
                .createdAt(event.getCreatedAt())
                .build();

        // Save to DynamoDB
        String notificationId = dynamoDbService.saveNotification(record);
        log.info("Notification saved to DynamoDB: {}", notificationId);

        // Save audit log to S3
        s3Service.saveAuditLog(record);
        log.info("Audit log saved to S3 for payment: {}", event.getPaymentId());
    }

    public List<NotificationResponseDTO> getNotificationsByPaymentId(
            String paymentId) {
        return dynamoDbService.getNotificationsByPaymentId(paymentId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getAllNotifications() {
        return dynamoDbService.getAllNotifications()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private NotificationResponseDTO mapToDTO(
            Map<String, AttributeValue> item) {
        return NotificationResponseDTO.builder()
                .id(getStr(item, "id"))
                .paymentId(getStr(item, "paymentId"))
                .senderId(getStr(item, "senderId"))
                .receiverId(getStr(item, "receiverId"))
                .amount(item.containsKey("amount") ?
                        new BigDecimal(item.get("amount").n()) : null)
                .currency(getStr(item, "currency"))
                .riskLevel(getStr(item, "riskLevel"))
                .status(getStr(item, "status"))
                .message(getStr(item, "message"))
                .createdAt(getStr(item, "createdAt"))
                .build();
    }

    private String getStr(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? item.get(key).s() : null;
    }
}