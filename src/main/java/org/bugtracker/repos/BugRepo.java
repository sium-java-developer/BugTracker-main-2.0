package org.bugtracker.repos;

import org.bugtracker.entities.Bug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // For example custom queries

/**
 * Spring Data JPA repository for {@link Bug} entities.
 */
@Repository
public interface BugRepo extends JpaRepository<Bug, Long> {

    // --- Example Custom Query Methods (uncomment and adapt if needed) ---

    /**
     * Finds all bugs with a specific status.
     * @param status The status to filter by.
     * @return A list of bugs matching the status.
     */
    // List<Bug> findByStatus(Bug.Status status);

    /**
     * Finds all bugs assigned to a specific user.
     * This assumes the 'assignedUsers' collection in the Bug entity is correctly mapped.
     * @param userId The ID of the user.
     * @return A list of bugs assigned to the user.
     */
    // List<Bug> findByAssignedUsers_Id(Long userId);

    /**
     * Finds all bugs with a specific priority.
     * @param priority The priority to filter by.
     * @return A list of bugs matching the priority.
     */
    // List<Bug> findByPriority(Bug.Priority priority);
}