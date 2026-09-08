package com.frauddetection.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.notification.model.NotificationRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public void saveAuditLog(NotificationRecord record) {
        String key = generateS3Key(record.getPaymentId());
        log.info("Saving audit log to S3: {}/{}", bucketName, key);

        try {
            String content = objectMapper.writeValueAsString(record);

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromString(content));

            log.info("Audit log saved to S3: {}/{}", bucketName, key);
        } catch (Exception e) {
            log.error("Failed to save audit log to S3: {}", e.getMessage());
        }
    }

    public void createBucketIfNotExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName).build());
            log.info("S3 bucket '{}' already exists", bucketName);
        } catch (NoSuchBucketException e) {
            log.info("Creating S3 bucket: {}", bucketName);
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName).build());
            log.info("S3 bucket '{}' created", bucketName);
        } catch (Exception e) {
            log.warn("Could not verify S3 bucket: {}", e.getMessage());
        }
    }

    private String generateS3Key(String paymentId) {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("audit-logs/%s/%s.json", date, paymentId);
    }
}