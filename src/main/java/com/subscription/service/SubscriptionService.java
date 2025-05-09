package com.subscription.service;

import com.subscription.dto.SubscriptionDto;
import com.subscription.dto.SubscriptionTypeDto;

import java.util.List;

public interface SubscriptionService {
    SubscriptionDto addSubscription(Long userId, SubscriptionDto subscriptionDto);
    List<SubscriptionDto> getUserSubscriptions(Long userId);
    void deleteSubscription(Long userId, Long subscriptionId);
    List<SubscriptionTypeDto> getTopSubscriptions();
} 