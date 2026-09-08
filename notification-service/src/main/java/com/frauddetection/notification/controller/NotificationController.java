package com.frauddetection.notification.controller;

import com.frauddetection.notification.dto.NotificationResponseDTO;
import com.frauddetection.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification API",
        description = "Endpoints for payment notifications and audit logs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/payment/{paymentId}")
    @Operation(summary = "Get notifications by payment ID")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByPaymentId(
            @PathVariable String paymentId) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByPaymentId(paymentId));
    }
}