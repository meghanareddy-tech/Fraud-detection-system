package com.frauddetection.fraud.kafka;

import com.frauddetection.fraud.dto.PaymentEventDTO;
import com.frauddetection.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(
            topics = "${kafka.topics.payment-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentEvent(
            @Payload PaymentEventDTO event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received payment event | topic: {} | partition: {} | offset: {} | paymentId: {}",
                topic, partition, offset, event.getPaymentId());

        try {
            fraudDetectionService.processPaymentEvent(event);
            log.info("Payment event processed successfully: {}", event.getPaymentId());
        } catch (Exception e) {
            log.error("Error processing payment event: {} | error: {}",
                    event.getPaymentId(), e.getMessage());
        }
    }
}