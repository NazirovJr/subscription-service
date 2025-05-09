package com.subscription.repository;

import com.subscription.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId);
    
    @Query("SELECT s.subscriptionType.id, s.subscriptionType.name, COUNT(s) as count " +
           "FROM Subscription s " +
           "GROUP BY s.subscriptionType.id, s.subscriptionType.name " +
           "ORDER BY count DESC " +
           "LIMIT 3")
    List<Object[]> findTopSubscriptionTypes();
} 