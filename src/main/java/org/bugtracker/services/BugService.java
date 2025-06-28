package org.bugtracker.services;

import org.bugtracker.entities.Bug;
import org.bugtracker.problems.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing Bug entities.
 */
public interface BugService {

    /**
     * Saves a given bug to the database. Use for both creating new bugs and updating existing ones.
     * @param bug The bug entity to save.
     * @return The saved bug entity, including its generated ID.
     */
     Bug save(Bug bug);
    
    /**
     * Creates a new bug.
     *
     * @param bug The bug entity to create.
     * @return The created bug entity, with a generated ID.
     */
    Bug createBug(Bug bug);

    /**
     * Retrieves a bug by its ID.
     *
     * @param id The ID of the bug to retrieve.
     * @return The found bug entity.
     * @throws NotFoundException if no bug is found with the given ID.
     */
    Bug getBugById(Long id) throws NotFoundException;

    /**
     * Retrieves a paginated list of all bugs.
     *
     * @param pageable The pagination information.
     * @return A Page of bug entities.
     */
    Page<Bug> getAllBugs(Pageable pageable);

    /**
     * Updates an existing bug's information.
     *
     * @param id         The ID of the bug to update.
     * @param bugDetails The bug entity with updated information.
     * @return The updated bug entity.
     * @throws NotFoundException if the bug to update is not found.
     */
    Bug updateBug(Long id, Bug bugDetails) throws NotFoundException;

    /**
     * Deletes a bug by its ID.
     *
     * @param id The ID of the bug to delete.
     * @throws NotFoundException if the bug to delete is not found.
     */
    void deleteBug(Long id) throws NotFoundException;

    long count();
}