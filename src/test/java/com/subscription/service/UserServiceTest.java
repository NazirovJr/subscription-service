package com.subscription.service;

import com.subscription.dto.UserDto;
import com.subscription.model.User;
import com.subscription.repository.UserRepository;
import com.subscription.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());

        verify(userRepository).existsByUsername(userDto.getUsername());
        verify(userRepository).existsByEmail(userDto.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(userDto)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername(userDto.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(userDto)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByUsername(userDto.getUsername());
        verify(userRepository).existsByEmail(userDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserById(1L)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(1L);
    }

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getId());
        assertEquals(user.getUsername(), result.get(0).getUsername());
        assertEquals(user.getEmail(), result.get(0).getEmail());
        assertEquals(user.getFirstName(), result.get(0).getFirstName());
        assertEquals(user.getLastName(), result.get(0).getLastName());

        verify(userRepository).findAll();
    }

    @Test
    void updateUser_Success() {
        UserDto updateDto = UserDto.builder()
                .username("updated")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .username("updated")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(1L, updateDto);

        assertNotNull(result);
        assertEquals(updatedUser.getId(), result.getId());
        assertEquals(updatedUser.getUsername(), result.getUsername());
        assertEquals(updatedUser.getEmail(), result.getEmail());
        assertEquals(updatedUser.getFirstName(), result.getFirstName());
        assertEquals(updatedUser.getLastName(), result.getLastName());

        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername(updateDto.getUsername());
        verify(userRepository).existsByEmail(updateDto.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUser(1L, userDto)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_UsernameExists_ThrowsException() {
        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        UserDto updateDto = UserDto.builder()
                .username("newuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(1L, updateDto)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername(updateDto.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_EmailExists_ThrowsException() {
        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        UserDto updateDto = UserDto.builder()
                .username("testuser")
                .email("new@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(1L, updateDto)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail(updateDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.deleteUser(1L)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }
} 