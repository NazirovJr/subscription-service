package com.subscription.service;

import com.subscription.dto.SubscriptionDto;
import com.subscription.dto.SubscriptionTypeDto;
import com.subscription.model.Subscription;
import com.subscription.model.SubscriptionStatus;
import com.subscription.model.SubscriptionType;
import com.subscription.model.User;
import com.subscription.repository.SubscriptionRepository;
import com.subscription.repository.SubscriptionTypeRepository;
import com.subscription.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionTypeRepository subscriptionTypeRepository;

    @Override
    @Transactional
    public SubscriptionDto addSubscription(Long userId, SubscriptionDto subscriptionDto) {
        log.debug("Adding subscription for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new EntityNotFoundException("User not found with ID: " + userId);
                });
        
        SubscriptionType subscriptionType = subscriptionTypeRepository.findById(subscriptionDto.getSubscriptionTypeId())
                .orElseThrow(() -> {
                    log.error("Subscription type not found with ID: {}", subscriptionDto.getSubscriptionTypeId());
                    return new EntityNotFoundException("Subscription type not found with ID: " + subscriptionDto.getSubscriptionTypeId());
                });
        
        if (subscriptionDto.getStartDate() == null) {
            subscriptionDto.setStartDate(ZonedDateTime.now());
        }
        
        if (subscriptionDto.getStatus() == null) {
            subscriptionDto.setStatus(SubscriptionStatus.ACTIVE);
        }
        
        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionType(subscriptionType)
                .startDate(subscriptionDto.getStartDate())
                .endDate(subscriptionDto.getEndDate())
                .status(subscriptionDto.getStatus())
                .build();
        
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription added with ID: {} for user ID: {}", savedSubscription.getId(), userId);
        
        return mapToDto(savedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDto> getUserSubscriptions(Long userId) {
        log.debug("Fetching subscriptions for user ID: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
        
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        
        return subscriptions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSubscription(Long userId, Long subscriptionId) {
        log.debug("Deleting subscription ID: {} for user ID: {}", subscriptionId, userId);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> {
                    log.error("Subscription not found with ID: {}", subscriptionId);
                    return new EntityNotFoundException("Subscription not found with ID: " + subscriptionId);
                });
        
        if (!subscription.getUser().getId().equals(userId)) {
            log.error("Subscription ID: {} does not belong to user ID: {}", subscriptionId, userId);
            throw new IllegalArgumentException("Subscription does not belong to user");
        }
        
        subscriptionRepository.deleteById(subscriptionId);
        log.info("Subscription deleted with ID: {}", subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionTypeDto> getTopSubscriptions() {
        log.debug("Fetching top subscriptions");
        
        List<Object[]> topSubscriptionTypes = subscriptionRepository.findTopSubscriptionTypes();
        List<SubscriptionTypeDto> result = new ArrayList<>();
        
        for (Object[] row : topSubscriptionTypes) {
            SubscriptionTypeDto dto = SubscriptionTypeDto.builder()
                    .id(((Number) row[0]).longValue())
                    .name((String) row[1])
                    .count(((Number) row[2]).longValue())
                    .build();
            result.add(dto);
        }
        
        return result;
    }
    
    private SubscriptionDto mapToDto(Subscription subscription) {
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .subscriptionTypeId(subscription.getSubscriptionType().getId())
                .subscriptionTypeName(subscription.getSubscriptionType().getName())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .build();
    }
} 