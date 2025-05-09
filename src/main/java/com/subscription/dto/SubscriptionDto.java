package com.subscription.dto;

import com.subscription.model.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {
    private Long id;
    
    @NotNull(message = "Subscription type ID is required")
    private Long subscriptionTypeId;
    
    private String subscriptionTypeName;
    
    private ZonedDateTime startDate;
    
    private ZonedDateTime endDate;
    
    private SubscriptionStatus status;
} 