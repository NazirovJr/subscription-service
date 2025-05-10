package com.subscription.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndGetUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .username("integrationtest")
                .email("integration@test.com")
                .firstName("Integration")
                .lastName("Test")
                .build();

        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.username", is("integrationtest")))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Long userId = objectMapper.readTree(responseBody)
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(userId.intValue())))
                .andExpect(jsonPath("$.data.username", is("integrationtest")))
                .andExpect(jsonPath("$.data.email", is("integration@test.com")))
                .andExpect(jsonPath("$.data.firstName", is("Integration")))
                .andExpect(jsonPath("$.data.lastName", is("Test")));
    }

    @Test
    void createUpdateAndDeleteUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .username("updatetest")
                .email("update@test.com")
                .firstName("Update")
                .lastName("Test")
                .build();

        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Long userId = objectMapper.readTree(responseBody)
                .path("data")
                .path("id")
                .asLong();

        UserDto updateDto = UserDto.builder()
                .username("updatetest")
                .email("update@test.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.firstName", is("Updated")))
                .andExpect(jsonPath("$.data.lastName", is("User")));

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserWithDuplicateUsername() throws Exception {
        UserDto userDto = UserDto.builder()
                .username("duplicatetest")
                .email("duplicate1@test.com")
                .firstName("Duplicate")
                .lastName("Test")
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        UserDto duplicateDto = UserDto.builder()
                .username("duplicatetest")
                .email("duplicate2@test.com")
                .firstName("Duplicate")
                .lastName("Test")
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Username already exists")));
    }

    @Test
    void getAllUsers() throws Exception {
        UserDto user1 = UserDto.builder()
                .username("testuser1")
                .email("test1@example.com")
                .firstName("Test1")
                .lastName("User1")
                .build();

        UserDto user2 = UserDto.builder()
                .username("testuser2")
                .email("test2@example.com")
                .firstName("Test2")
                .lastName("User2")
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data[*].username", hasItems("testuser1", "testuser2")));
    }
} 