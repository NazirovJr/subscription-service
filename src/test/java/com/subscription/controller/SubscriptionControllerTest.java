package com.subscription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.dto.SubscriptionDto;
import com.subscription.dto.SubscriptionTypeDto;
import com.subscription.model.SubscriptionStatus;
import com.subscription.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    private SubscriptionDto subscriptionDto;
    private List<SubscriptionTypeDto> topSubscriptions;

    @BeforeEach
    void setUp() {
        subscriptionDto = SubscriptionDto.builder()
                .id(1L)
                .subscriptionTypeId(1L)
                .subscriptionTypeName("Netflix")
                .startDate(ZonedDateTime.now())
                .status(SubscriptionStatus.ACTIVE)
                .build();

        topSubscriptions = List.of(
                SubscriptionTypeDto.builder()
                        .id(1L)
                        .name("Netflix")
                        .count(10L)
                        .build(),
                SubscriptionTypeDto.builder()
                        .id(2L)
                        .name("YouTube Premium")
                        .count(5L)
                        .build(),
                SubscriptionTypeDto.builder()
                        .id(3L)
                        .name("Spotify")
                        .count(3L)
                        .build()
        );
    }

    @Test
    void addSubscription_Success() throws Exception {
        when(subscriptionService.addSubscription(anyLong(), any(SubscriptionDto.class))).thenReturn(subscriptionDto);

        mockMvc.perform(post("/users/1/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subscriptionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Subscription added successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.subscriptionTypeId", is(1)))
                .andExpect(jsonPath("$.data.subscriptionTypeName", is("Netflix")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        verify(subscriptionService).addSubscription(eq(1L), any(SubscriptionDto.class));
    }

    @Test
    void addSubscription_UserNotFound() throws Exception {
        when(subscriptionService.addSubscription(anyLong(), any(SubscriptionDto.class)))
                .thenThrow(new EntityNotFoundException("User not found with ID: 1"));

        mockMvc.perform(post("/users/1/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subscriptionDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("User not found with ID: 1")));

        verify(subscriptionService).addSubscription(eq(1L), any(SubscriptionDto.class));
    }

    @Test
    void getUserSubscriptions_Success() throws Exception {
        when(subscriptionService.getUserSubscriptions(anyLong())).thenReturn(List.of(subscriptionDto));

        mockMvc.perform(get("/users/1/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].subscriptionTypeId", is(1)))
                .andExpect(jsonPath("$.data[0].subscriptionTypeName", is("Netflix")))
                .andExpect(jsonPath("$.data[0].status", is("ACTIVE")));

        verify(subscriptionService).getUserSubscriptions(1L);
    }

    @Test
    void getUserSubscriptions_UserNotFound() throws Exception {
        when(subscriptionService.getUserSubscriptions(anyLong()))
                .thenThrow(new EntityNotFoundException("User not found with ID: 1"));

        mockMvc.perform(get("/users/1/subscriptions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("User not found with ID: 1")));

        verify(subscriptionService).getUserSubscriptions(1L);
    }

    @Test
    void deleteSubscription_Success() throws Exception {
        doNothing().when(subscriptionService).deleteSubscription(anyLong(), anyLong());

        mockMvc.perform(delete("/users/1/subscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Subscription deleted successfully")));

        verify(subscriptionService).deleteSubscription(1L, 1L);
    }

    @Test
    void deleteSubscription_SubscriptionNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Subscription not found with ID: 1"))
                .when(subscriptionService).deleteSubscription(anyLong(), anyLong());

        mockMvc.perform(delete("/users/1/subscriptions/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Subscription not found with ID: 1")));

        verify(subscriptionService).deleteSubscription(1L, 1L);
    }

    @Test
    void getTopSubscriptions_Success() throws Exception {
        when(subscriptionService.getTopSubscriptions()).thenReturn(topSubscriptions);

        mockMvc.perform(get("/subscriptions/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].name", is("Netflix")))
                .andExpect(jsonPath("$.data[0].count", is(10)))
                .andExpect(jsonPath("$.data[1].id", is(2)))
                .andExpect(jsonPath("$.data[1].name", is("YouTube Premium")))
                .andExpect(jsonPath("$.data[1].count", is(5)))
                .andExpect(jsonPath("$.data[2].id", is(3)))
                .andExpect(jsonPath("$.data[2].name", is("Spotify")))
                .andExpect(jsonPath("$.data[2].count", is(3)));

        verify(subscriptionService).getTopSubscriptions();
    }
} 