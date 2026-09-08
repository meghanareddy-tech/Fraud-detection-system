package com.frauddetection.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@Slf4j
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.dynamodb.endpoint}")
    private String dynamoDbEndpoint;

    @Value("${aws.s3.endpoint}")
    private String s3Endpoint;

    @Value("${aws.use-local}")
    private boolean useLocal;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        if (useLocal) {
            log.info("Using local DynamoDB endpoint: {}", dynamoDbEndpoint);
            return DynamoDbClient.builder()
                    .region(Region.of(region))
                    .endpointOverride(URI.create(dynamoDbEndpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("dummy", "dummy")))
                    .build();
        }
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .build();
    }

    @Bean
    public S3Client s3Client() {
        if (useLocal) {
            log.info("Using local S3 endpoint: {}", s3Endpoint);
            return S3Client.builder()
                    .region(Region.of(region))
                    .endpointOverride(URI.create(s3Endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("dummy", "dummy")))
                    .forcePathStyle(true)
                    .build();
        }
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}