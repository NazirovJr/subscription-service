package com.subscription.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.dto.SubscriptionDto;
import com.subscription.dto.UserDto;
import com.subscription.model.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubscriptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        UserDto userDto = UserDto.builder()
                .username("subtest")
                .email("subtest@example.com")
                .firstName("Sub")
                .lastName("Test")
                .build();

        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        userId = objectMapper.readTree(responseBody)
                .path("data")
                .path("id")
                .asLong();
    }

    @Test
    void addAndGetSubscription() throws Exception {
        SubscriptionDto subscriptionDto = SubscriptionDto.builder()
                .subscriptionTypeId(1L)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        MvcResult result = mockMvc.perform(post("/api/users/{userId}/subscriptions", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subscriptionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.subscriptionTypeId", is(1)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Long subscriptionId = objectMapper.readTree(responseBody)
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(get("/api/users/{userId}/subscriptions", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(subscriptionId.intValue())))
                .andExpect(jsonPath("$.data[0].subscriptionTypeId", is(1)))
                .andExpect(jsonPath("$.data[0].status", is("ACTIVE")));
    }

    @Test
    void addAndDeleteSubscription() throws Exception {
        SubscriptionDto subscriptionDto = SubscriptionDto.builder()
                .subscriptionTypeId(2L)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        MvcResult result = mockMvc.perform(post("/api/users/{userId}/subscriptions", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subscriptionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Long subscriptionId = objectMapper.readTree(responseBody)
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(delete("/api/users/{userId}/subscriptions/{subscriptionId}", userId, subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(get("/api/users/{userId}/subscriptions", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void addMultipleSubscriptionsAndGetTopSubscriptions() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/subscriptions", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SubscriptionDto.builder()
                        .subscriptionTypeId(1L) // Netflix
                        .status(SubscriptionStatus.ACTIVE)
                        .build())))
                .andExpect(status().isCreated());

        UserDto user2Dto = UserDto.builder()
                .username("subtest2")
                .email("subtest2@example.com")
                .firstName("Sub2")
                .lastName("Test2")
                .build();

        MvcResult user2Result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long user2Id = objectMapper.readTree(user2Result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(post("/api/users/{userId}/subscriptions", user2Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SubscriptionDto.builder()
                        .subscriptionTypeId(1L) // Netflix
                        .status(SubscriptionStatus.ACTIVE)
                        .build())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users/{userId}/subscriptions", user2Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SubscriptionDto.builder()
                        .subscriptionTypeId(3L) // Spotify
                        .status(SubscriptionStatus.ACTIVE)
                        .build())))
                .andExpect(status().isCreated());

        UserDto user3Dto = UserDto.builder()
                .username("subtest3")
                .email("subtest3@example.com")
                .firstName("Sub3")
                .lastName("Test3")
                .build();

        MvcResult user3Result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user3Dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long user3Id = objectMapper.readTree(user3Result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(post("/api/users/{userId}/subscriptions", user3Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SubscriptionDto.builder()
                        .subscriptionTypeId(2L) // YouTube Premium
                        .status(SubscriptionStatus.ACTIVE)
                        .build())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/subscriptions/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].name", is("Netflix")))
                .andExpect(jsonPath("$.data[0].count", is(2)))
                .andExpect(jsonPath("$.data[1].count", in(Arrays.asList(1, 1))));
    }

    @Test
    void addSubscriptionForNonExistentUser() throws Exception {
        SubscriptionDto subscriptionDto = SubscriptionDto.builder()
                .subscriptionTypeId(1L)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/users/{userId}/subscriptions", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subscriptionDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    void addSubscriptionWithNonExistentType() throws Exception {
        SubscriptionDto subscriptionDto = SubscriptionDto.builder()
                .subscriptionTypeId(999L)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/users/{userId}/subscriptions", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subscriptionDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Subscription type not found")));
    }
} 