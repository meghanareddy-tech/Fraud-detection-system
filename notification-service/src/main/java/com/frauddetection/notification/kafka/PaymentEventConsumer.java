package com.frauddetection.notification.kafka;

import com.frauddetection.notification.dto.PaymentEventDTO;
import com.frauddetection.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.payment-events}",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentEvent(
            @Payload PaymentEventDTO event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received payment event | topic: {} | partition: {} | " +
                        "offset: {} | paymentId: {}",
                topic, partition, offset, event.getPaymentId());

        try {
            notificationService.processPaymentEvent(event);
            log.info("Notification processed for payment: {}",
                    event.getPaymentId());
        } catch (Exception e) {
            log.error("Error processing notification for payment: {} | error: {}",
                    event.getPaymentId(), e.getMessage());
        }
    }
}