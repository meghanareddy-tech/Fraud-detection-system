package com.frauddetection.notification.service;

import com.frauddetection.notification.model.NotificationRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbService {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table-name}")
    private String tableName;

    public String saveNotification(NotificationRecord record) {
        String id = UUID.randomUUID().toString();
        log.info("Saving notification to DynamoDB for paymentId: {}", record.getPaymentId());

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(id).build());
        item.put("paymentId", AttributeValue.builder()
                .s(record.getPaymentId()).build());
        item.put("senderId", AttributeValue.builder()
                .s(record.getSenderId()).build());
        item.put("receiverId", AttributeValue.builder()
                .s(record.getReceiverId()).build());
        item.put("amount", AttributeValue.builder()
                .n(record.getAmount().toString()).build());
        item.put("currency", AttributeValue.builder()
                .s(record.getCurrency()).build());
        item.put("riskLevel", AttributeValue.builder()
                .s(record.getRiskLevel() != null ? record.getRiskLevel() : "UNKNOWN").build());
        item.put("status", AttributeValue.builder()
                .s(record.getStatus() != null ? record.getStatus() : "PENDING").build());
        item.put("message", AttributeValue.builder()
                .s(record.getMessage() != null ? record.getMessage() : "").build());
        item.put("ipAddress", AttributeValue.builder()
                .s(record.getIpAddress()).build());
        item.put("deviceId", AttributeValue.builder()
                .s(record.getDeviceId()).build());
        item.put("createdAt", AttributeValue.builder()
                .s(LocalDateTime.now().toString()).build());

        try {
            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build());
            log.info("Notification saved to DynamoDB with id: {}", id);
            return id;
        } catch (Exception e) {
            log.error("Failed to save notification to DynamoDB: {}", e.getMessage());
            throw new RuntimeException("Failed to save notification: " + e.getMessage());
        }
    }

    public List<Map<String, AttributeValue>> getNotificationsByPaymentId(
            String paymentId) {
        log.info("Fetching notifications for paymentId: {}", paymentId);
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(tableName)
                    .filterExpression("paymentId = :paymentId")
                    .expressionAttributeValues(Map.of(
                            ":paymentId", AttributeValue.builder()
                                    .s(paymentId).build()))
                    .build();

            ScanResponse response = dynamoDbClient.scan(scanRequest);
            return response.items();
        } catch (Exception e) {
            log.error("Failed to fetch notifications: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch notifications: "
                    + e.getMessage());
        }
    }

    public List<Map<String, AttributeValue>> getAllNotifications() {
        log.info("Fetching all notifications from DynamoDB");
        try {
            ScanResponse response = dynamoDbClient.scan(
                    ScanRequest.builder().tableName(tableName).build());
            return response.items();
        } catch (Exception e) {
            log.error("Failed to fetch all notifications: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch notifications: "
                    + e.getMessage());
        }
    }

    public void createTableIfNotExists() {
        try {
            dynamoDbClient.describeTable(
                    DescribeTableRequest.builder().tableName(tableName).build());
            log.info("DynamoDB table '{}' already exists", tableName);
        } catch (ResourceNotFoundException e) {
            log.info("Creating DynamoDB table: {}", tableName);
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("id")
                            .keyType(KeyType.HASH)
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("id")
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());
            log.info("DynamoDB table '{}' created successfully", tableName);
        }
    }
}