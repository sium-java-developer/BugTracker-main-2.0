package org.bugtracker.services;

import jakarta.validation.Valid;
import org.bugtracker.entities.User;
import org.bugtracker.entities.UserProfileUpdateDto;
import org.bugtracker.problems.NotFoundException;
import org.bugtracker.problems.UserAlreadyExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Interface for the User Service. Defines the contract for user-related business operations.
 * This promotes loose coupling and improves testability and maintainability.
 */
public interface UserService {

    /**
     * Finds a user by their username. This is a key method for Spring Security's
     * UserDetailsService.
     * @param username The username to search for.
     * @return An Optional containing the User if found, or empty otherwise.
     */
    Optional<User> findByUsername(String username);


    /**
     * Saves a User entity to the database. This is a general-purpose save method.
     * It's used by the diagnostic data loader and other internal processes.
     * For creating new users with password encoding, use `createUser` or `registerUser`.
     * @param user The User entity to save.
     * @return The saved User entity.
     */
    User save(User user);

    /**
     * Creates a new user. This method handles encoding the password and
     * checking for existing usernames/emails.
     * @param user The user entity to create, containing a plain text password.
     * @return The created user entity with its new ID and encoded password.
     * @throws UserAlreadyExistsException if the username or email is already in use.
     */
    User createUser(User user) throws UserAlreadyExistsException;

    /**
     * Registers a new user, typically from a public registration form.
     * It ensures the username and email are not already taken and encodes the password.
     * @param user The user entity to register, containing a plain text password.
     * @return The registered user entity with its new ID and encoded password.
     * @throws UserAlreadyExistsException if the username or email is already in use.
     */
    User registerUser(User user) throws UserAlreadyExistsException;

    /**
     * Retrieves a user by their unique ID.
     * @param id The ID of the user.
     * @return An Optional containing the user entity if found, or empty otherwise.
     */
    Optional<User> getUserById(Long id);

    /**
     * Retrieves a paginated list of all users in the system.
     * @param pageable An object containing pagination information (page number, size, sort).
     * @return A Page of users.
     */
    Page<User> getAllUsers(Pageable pageable);

    /**
     * Updates an existing user's details.
     * @param userUpdates The User entity containing updates. The ID must be present.
     * @return The updated User entity.
     * @throws NotFoundException if the user to update is not found.
     * @throws UserAlreadyExistsException if the updated username or email is already in use by another user.
     */
    User updateUser(User userUpdates) throws NotFoundException, UserAlreadyExistsException;

    /**
     * Deletes a user from the database by their ID.
     * @param id The ID of the user to delete.
     * @throws NotFoundException if no user with the given ID is found.
     */
    void deleteUser(Long id) throws NotFoundException;

    User updateUserProfile(String user, @Valid UserProfileUpdateDto newPassword) throws UserAlreadyExistsException;

}