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
import com.subscription.service.impl.SubscriptionServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionTypeRepository subscriptionTypeRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private User user;
    private SubscriptionType subscriptionType;
    private Subscription subscription;
    private SubscriptionDto subscriptionDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        subscriptionType = SubscriptionType.builder()
                .id(1L)
                .name("Netflix")
                .description("Streaming service")
                .build();

        subscription = Subscription.builder()
                .id(1L)
                .user(user)
                .subscriptionType(subscriptionType)
                .startDate(ZonedDateTime.now())
                .status(SubscriptionStatus.ACTIVE)
                .build();

        subscriptionDto = SubscriptionDto.builder()
                .subscriptionTypeId(1L)
                .startDate(ZonedDateTime.now())
                .status(SubscriptionStatus.ACTIVE)
                .build();
    }

    @Test
    void addSubscription_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(subscriptionTypeRepository.findById(anyLong())).thenReturn(Optional.of(subscriptionType));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        SubscriptionDto result = subscriptionService.addSubscription(1L, subscriptionDto);

        assertNotNull(result);
        assertEquals(subscription.getId(), result.getId());
        assertEquals(subscription.getSubscriptionType().getId(), result.getSubscriptionTypeId());
        assertEquals(subscription.getSubscriptionType().getName(), result.getSubscriptionTypeName());
        assertEquals(subscription.getStatus(), result.getStatus());

        verify(userRepository).findById(1L);
        verify(subscriptionTypeRepository).findById(1L);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void addSubscription_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> subscriptionService.addSubscription(1L, subscriptionDto)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(1L);
        verify(subscriptionTypeRepository, never()).findById(anyLong());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void addSubscription_SubscriptionTypeNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(subscriptionTypeRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> subscriptionService.addSubscription(1L, subscriptionDto)
        );

        assertTrue(exception.getMessage().contains("Subscription type not found"));
        verify(userRepository).findById(1L);
        verify(subscriptionTypeRepository).findById(1L);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void getUserSubscriptions_Success() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(subscriptionRepository.findByUserId(anyLong())).thenReturn(List.of(subscription));

        List<SubscriptionDto> result = subscriptionService.getUserSubscriptions(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(subscription.getId(), result.get(0).getId());
        assertEquals(subscription.getSubscriptionType().getId(), result.get(0).getSubscriptionTypeId());
        assertEquals(subscription.getSubscriptionType().getName(), result.get(0).getSubscriptionTypeName());
        assertEquals(subscription.getStatus(), result.get(0).getStatus());

        verify(userRepository).existsById(1L);
        verify(subscriptionRepository).findByUserId(1L);
    }

    @Test
    void getUserSubscriptions_UserNotFound_ThrowsException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> subscriptionService.getUserSubscriptions(1L)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).existsById(1L);
        verify(subscriptionRepository, never()).findByUserId(anyLong());
    }

    @Test
    void deleteSubscription_Success() {
        when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(subscription));
        doNothing().when(subscriptionRepository).deleteById(anyLong());

        subscriptionService.deleteSubscription(1L, 1L);

        verify(subscriptionRepository).findById(1L);
        verify(subscriptionRepository).deleteById(1L);
    }

    @Test
    void deleteSubscription_SubscriptionNotFound_ThrowsException() {
        when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> subscriptionService.deleteSubscription(1L, 1L)
        );

        assertTrue(exception.getMessage().contains("Subscription not found"));
        verify(subscriptionRepository).findById(1L);
        verify(subscriptionRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteSubscription_SubscriptionNotBelongToUser_ThrowsException() {
        User anotherUser = User.builder().id(2L).build();
        Subscription anotherSubscription = Subscription.builder()
                .id(1L)
                .user(anotherUser)
                .subscriptionType(subscriptionType)
                .build();

        when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(anotherSubscription));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.deleteSubscription(1L, 1L)
        );

        assertEquals("Subscription does not belong to user", exception.getMessage());
        verify(subscriptionRepository).findById(1L);
        verify(subscriptionRepository, never()).deleteById(anyLong());
    }

    @Test
    void getTopSubscriptions_Success() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{1L, "Netflix", 10L});
        mockResults.add(new Object[]{2L, "YouTube Premium", 5L});
        mockResults.add(new Object[]{3L, "Spotify", 3L});

        when(subscriptionRepository.findTopSubscriptionTypes()).thenReturn(mockResults);

        List<SubscriptionTypeDto> result = subscriptionService.getTopSubscriptions();

        assertNotNull(result);
        assertEquals(3, result.size());
        
        assertEquals(1L, result.get(0).getId());
        assertEquals("Netflix", result.get(0).getName());
        assertEquals(10L, result.get(0).getCount());
        
        assertEquals(2L, result.get(1).getId());
        assertEquals("YouTube Premium", result.get(1).getName());
        assertEquals(5L, result.get(1).getCount());
        
        assertEquals(3L, result.get(2).getId());
        assertEquals("Spotify", result.get(2).getName());
        assertEquals(3L, result.get(2).getCount());

        verify(subscriptionRepository).findTopSubscriptionTypes();
    }
} 