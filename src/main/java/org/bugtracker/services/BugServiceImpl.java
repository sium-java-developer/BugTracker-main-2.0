package org.bugtracker.services;

import org.bugtracker.entities.Bug;
import org.bugtracker.problems.NotFoundException;
import org.bugtracker.repos.BugRepo; // Corrected from BugRepository to BugRepo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the BugService interface.
 */
@Service
public class BugServiceImpl implements BugService { // Removed 'abstract' keyword

    private final BugRepo bugRepo; // Corrected from BugRepository to BugRepo

    @Autowired
    public BugServiceImpl(BugRepo bugRepo) { // Corrected from BugRepository to BugRepo
        this.bugRepo = bugRepo;
    }

    // Renamed to match the interface method 'save' (lowercase 's')
    @Override
    @Transactional
    public Bug save(Bug bug){
        return bugRepo.save(bug);
    }

    @Override
    @Transactional
    public Bug createBug(Bug bug) {
        // Timestamps are handled by @PrePersist in the Bug entity
        return bugRepo.save(bug);
    }

    @Override
    @Transactional(readOnly = true)
    public Bug getBugById(Long id) throws NotFoundException {
        return bugRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Bug not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bug> getAllBugs(Pageable pageable) {
        return bugRepo.findAll(pageable);
    }

    @Override
    @Transactional
    public Bug updateBug(Long id, Bug bugDetails) throws NotFoundException {
        // Fetch the existing bug using the ID from the path to ensure correctness
        Bug existingBug = bugRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Cannot update. Bug not found with id: " + id));

        // Apply updates from the 'bugDetails' DTO to the managed 'existingBug' entity
        existingBug.setTitle(bugDetails.getTitle());
        existingBug.setDescription(bugDetails.getDescription());
        existingBug.setPriority(bugDetails.getPriority());
        existingBug.setStatus(bugDetails.getStatus());

        // The updatedAt timestamp is handled by @PreUpdate in the Bug entity
        return bugRepo.save(existingBug);
    }

    @Override
    @Transactional
    public void deleteBug(Long id) throws NotFoundException {
        if (!bugRepo.existsById(id)) {
            throw new NotFoundException("Cannot delete. Bug not found with id: " + id);
        }
        bugRepo.deleteById(id);
    }

    @Override
    public long count() {
        return bugRepo.count();
    }
}