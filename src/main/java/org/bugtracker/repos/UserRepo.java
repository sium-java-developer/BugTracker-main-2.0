package org.bugtracker.repos;

import org.bugtracker.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     * Assumes the User entity has a field named 'username'.
     * @param username The username to search for.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * Finds users by their first name.
     * This is a derived query method.
     * @param firstName The first name to search for.
     * @return A list of users matching the first name.
     */
    List<User> findByFirstName(String firstName);

    /**
     * Finds users by their last name.
     * This is a derived query method.
     * @param lastName The last name to search for.
     * @return A list of users matching the last name.
     */
    List<User> findByLastName(String lastName);

    /**
     * Finds a user by their email address.
     * Assumes email addresses are unique.
     * This is a derived query method.
     * @param email The email address to search for.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds users assigned to a specific bug.
     * This query assumes a ManyToMany relationship where the User entity has a collection
     * named 'assignedBugs' that maps to the Bug entity.
     * @param bugId The ID of the bug.
     * @return A list of users assigned to the specified bug.
     */
    @Query("SELECT u FROM User u JOIN u.assignedBugs b WHERE b.id = :bugId")
    List<User> findUsersByAssignedBugId(@Param("bugId") Long bugId);
}