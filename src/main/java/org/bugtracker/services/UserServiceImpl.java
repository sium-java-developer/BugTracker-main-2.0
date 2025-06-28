package org.bugtracker.services;

import org.bugtracker.entities.User;
import org.bugtracker.entities.UserProfileUpdateDto;
import org.bugtracker.problems.NotFoundException;
import org.bugtracker.problems.UserAlreadyExistsException;
import org.bugtracker.repos.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepo userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User save(User user) {
        // General-purpose save, used by data loader. Assumes password is pre-encoded if necessary.
        return userRepository.save(user);
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username '" + user.getUsername() + "' already exists.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email '" + user.getEmail() + "' already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        logger.info("Creating new user: {}", user.getUsername());
        return userRepository.save(user);
    }

    /**
     * Implements the registration logic for a new user.
     * This method ensures that any user registered through this flow is not an admin.
     */
    @Override
    @Transactional
    public User registerUser(User user) throws UserAlreadyExistsException {
        // For public registration, always ensure the user is not created as an admin.
        user.setAdmin(false);
        // The rest of the creation logic is the same as createUser.
        return this.createUser(user);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + user.getId()));

        // Check for username change (if allowed)
        if (!existingUser.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                throw new UserAlreadyExistsException("Username '" + user.getUsername() + "' already exists.");
            }
            existingUser.setUsername(user.getUsername());
        }

        // Check for email change
        if (!existingUser.getEmail().equals(user.getEmail())) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(user.getId())) {
                throw new UserAlreadyExistsException("Email '" + user.getEmail() + "' already exists.");
            }
            existingUser.setEmail(user.getEmail());
        }

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setAdmin(user.isAdmin());
        existingUser.setUpdatedAt(LocalDateTime.now());

        // Only update password if a new one is provided
        if (StringUtils.hasText(user.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        logger.info("Updating user with ID: {}", user.getId());
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        logger.info("Deleted user with ID: {}", id);
    }

    /**
     * Updates the profile of the currently logged-in user based on the provided DTO.
     * This method correctly implements the signature from the UserService interface.
     *
     * @param username The username of the user whose profile is being updated.
     * @param userProfileUpdateDto The DTO containing the updated profile information.
     * @return The updated User entity.
     * @throws UsernameNotFoundException if the user is not found.
     * @throws UserAlreadyExistsException if the new email is already taken by another user.
     */
    @Override
    @Transactional
    public User updateUserProfile(String username, UserProfileUpdateDto userProfileUpdateDto) {
        User existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Check for email change
        if (!existingUser.getEmail().equals(userProfileUpdateDto.getEmail())) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(userProfileUpdateDto.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(existingUser.getId())) {
                throw new UserAlreadyExistsException("Email '" + userProfileUpdateDto.getEmail() + "' is already in use.");
            }
            existingUser.setEmail(userProfileUpdateDto.getEmail());
        }

        existingUser.setFirstName(userProfileUpdateDto.getFirstName());
        existingUser.setLastName(userProfileUpdateDto.getLastName());
        existingUser.setUpdatedAt(LocalDateTime.now());

        // Only update password if a new one is provided in the DTO
        if (StringUtils.hasText(userProfileUpdateDto.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(userProfileUpdateDto.getPassword()));
            logger.info("Password updated for user: {}", username);
        }

        logger.info("Profile updated for user: {}", username);
        return userRepository.save(existingUser);
    }
}