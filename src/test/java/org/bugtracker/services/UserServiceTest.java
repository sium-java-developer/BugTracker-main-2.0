package org.bugtracker.services;

import org.bugtracker.entities.User;
import org.bugtracker.problems.NotFoundException;
import org.bugtracker.problems.UserAlreadyExistsException;
// CORRECTED: Using the standard 'repositories' package and 'UserRepository' interface
import org.bugtracker.repos.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the UserServiceImpl class.
 * Uses Mockito to mock dependencies like UserRepository and PasswordEncoder.
 */
@ExtendWith(MockitoExtension.class) // Enables Mockito annotations and automatic initialization
public class UserServiceTest {

    @Mock // Mocks the UserRepository dependency
    private UserRepo userRepository;

    @Mock // Mocks the PasswordEncoder dependency
    private PasswordEncoder passwordEncoder;

    @InjectMocks // Injects the mocked dependencies into UserServiceImpl
    private UserServiceImpl userService;

    private User testUser;

    /**
     * Sets up the test environment before each test method.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedOldPassword");
        testUser.setAdmin(false);
    }

    @Test
    @DisplayName("Find By Username - Should return user when found")
    void findByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.findByUsername("testuser");

        // Assert
        assertTrue(foundUser.isPresent(), "User should be found");
        assertEquals("testuser", foundUser.get().getUsername(), "Found user's username should match");
    }

    @Test
    @DisplayName("Find By Username - Should return empty when user not found")
    void findByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> foundUser = userService.findByUsername("nonexistent");

        // Assert
        assertFalse(foundUser.isPresent(), "User should not be found");
    }

    @Test
    @DisplayName("Register User - Should succeed for new user")
    void registerUser_Success() {
        // Arrange
        User newUser = new User("newuser", "password", "New", "User", "new@example.com", false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L); // Simulate ID generation
            return savedUser;
        });

        // Act
        User registeredUser = userService.registerUser(newUser);

        // Assert
        assertNotNull(registeredUser, "Registered user should not be null");
        assertEquals(2L, registeredUser.getId(), "Registered user should have a generated ID");
        assertEquals("encodedPassword", registeredUser.getPassword(), "Password should be encoded");
        assertFalse(registeredUser.getAdmin(), "New user should not be an admin by default");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register User - Should fail if username already exists")
    void registerUser_UsernameAlreadyExists() {
        // Arrange
        User existingUser = new User("testuser", "password", "Existing", "User", "existing@example.com", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(existingUser);
        });
        assertEquals("Username 'testuser' is already taken.", exception.getMessage());
    }

    @Test
    @DisplayName("Get All Users - Should return a paginated list of users")
    void getAllUsers_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<User> userList = List.of(testUser, new User());
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<User> resultPage = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertThat(resultPage.getContent()).contains(testUser);
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Update User - Should succeed with valid changes")
    void updateUser_Success() {
        // Arrange
        User userUpdates = new User();
        userUpdates.setId(1L);
        userUpdates.setFirstName("Updated");
        userUpdates.setLastName("Name");
        userUpdates.setEmail("updated@example.com");
        userUpdates.setPassword("newRawPassword"); // Raw password
        userUpdates.setAdmin(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newRawPassword")).thenReturn("encodedNewPassword"); // Mock encoding
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved user

        // Act
        User result = userService.updateUser(userUpdates);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("encodedNewPassword", result.getPassword()); // Verify encoded password
        assertTrue(result.getAdmin());
        verify(userRepository, times(1)).save(testUser); // Verify save was called with the updated user
    }

    @Test
    @DisplayName("Update User - Should fail if username is taken by another user")
    void updateUser_UsernameConflict_ThrowsException() {
        // Arrange
        User conflictingUser = new User();
        conflictingUser.setId(2L); // Different ID
        conflictingUser.setUsername("new.username");

        User userUpdates = new User();
        userUpdates.setId(1L);
        userUpdates.setUsername("new.username"); // Attempt to change username to one that exists

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("new.username")).thenReturn(Optional.of(conflictingUser));

        // Act & Assert
        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.updateUser(userUpdates);
        });
        assertEquals("Username 'new.username' is already taken.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Delete User - Should succeed for existing user")
    void deleteUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        assertDoesNotThrow(() -> userService.deleteUser(1L));

        // Assert
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete User - Should fail for non-existent user")
    void deleteUser_NotFound() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            userService.deleteUser(1L);
        });
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, never()).deleteById(1L);
    }
}