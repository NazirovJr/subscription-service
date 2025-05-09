package com.subscription.controller;

import com.subscription.dto.ApiResponse;
import com.subscription.dto.SubscriptionDto;
import com.subscription.dto.SubscriptionTypeDto;
import com.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/users/{userId}/subscriptions")
    public ResponseEntity<ApiResponse<SubscriptionDto>> addSubscription(
            @PathVariable Long userId,
            @Valid @RequestBody SubscriptionDto subscriptionDto) {
        log.info("Received request to add subscription for user ID: {}", userId);
        SubscriptionDto addedSubscription = subscriptionService.addSubscription(userId, subscriptionDto);
        return new ResponseEntity<>(
                ApiResponse.success("Subscription added successfully", addedSubscription),
                HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}/subscriptions")
    public ResponseEntity<ApiResponse<List<SubscriptionDto>>> getUserSubscriptions(@PathVariable Long userId) {
        log.info("Received request to get subscriptions for user ID: {}", userId);
        List<SubscriptionDto> subscriptions = subscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @DeleteMapping("/users/{userId}/subscriptions/{subscriptionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscription(
            @PathVariable Long userId,
            @PathVariable Long subscriptionId) {
        log.info("Received request to delete subscription ID: {} for user ID: {}", subscriptionId, userId);
        subscriptionService.deleteSubscription(userId, subscriptionId);
        return ResponseEntity.ok(ApiResponse.success("Subscription deleted successfully", null));
    }

    @GetMapping("/subscriptions/top")
    public ResponseEntity<ApiResponse<List<SubscriptionTypeDto>>> getTopSubscriptions() {
        log.info("Received request to get top subscriptions");
        List<SubscriptionTypeDto> topSubscriptions = subscriptionService.getTopSubscriptions();
        return ResponseEntity.ok(ApiResponse.success(topSubscriptions));
    }
} 