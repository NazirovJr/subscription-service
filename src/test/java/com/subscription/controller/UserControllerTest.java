package com.subscription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.dto.UserDto;
import com.subscription.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void createUser_Success() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User created successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.username", is("testuser")))
                .andExpect(jsonPath("$.data.email", is("test@example.com")))
                .andExpect(jsonPath("$.data.firstName", is("Test")))
                .andExpect(jsonPath("$.data.lastName", is("User")));

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    void createUser_ValidationFailure() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .username("")
                .email("invalid-email")
                .firstName("")
                .lastName("")
                .build();

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserDto.class));
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.username", is("testuser")))
                .andExpect(jsonPath("$.data.email", is("test@example.com")))
                .andExpect(jsonPath("$.data.firstName", is("Test")))
                .andExpect(jsonPath("$.data.lastName", is("User")));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(anyLong())).thenThrow(new EntityNotFoundException("User not found with ID: 1"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("User not found with ID: 1")));

        verify(userService).getUserById(1L);
    }

    @Test
    void getAllUsers_Success() throws Exception {
        UserDto user2 = UserDto.builder()
                .id(2L)
                .username("anotheruser")
                .email("another@example.com")
                .firstName("Another")
                .lastName("User")
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(userDto, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].username", is("testuser")))
                .andExpect(jsonPath("$.data[1].id", is(2)))
                .andExpect(jsonPath("$.data[1].username", is("anotheruser")));

        verify(userService).getAllUsers();
    }

    @Test
    void updateUser_Success() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .username("updated")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        when(userService.updateUser(anyLong(), any(UserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User updated successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.username", is("updated")))
                .andExpect(jsonPath("$.data.email", is("updated@example.com")))
                .andExpect(jsonPath("$.data.firstName", is("Updated")))
                .andExpect(jsonPath("$.data.lastName", is("User")));

        verify(userService).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User deleted successfully")));

        verify(userService).deleteUser(1L);
    }
} 