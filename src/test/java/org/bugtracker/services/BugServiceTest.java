package org.bugtracker.services;

import org.bugtracker.entities.Bug;
import org.bugtracker.entities.Priority;
import org.bugtracker.entities.Status;
import org.bugtracker.problems.NotFoundException;
import org.bugtracker.repos.BugRepo;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BugServiceTest {

    @Mock
    private BugRepo bugRepo;

    @InjectMocks
    private BugServiceImpl bugService;

    private Bug testBug;

    @BeforeEach
    void setUp() {
        testBug = new Bug("Test Bug", "A test bug", Priority.HIGH, Status.OPEN);
        testBug.setId(1L);
    }

    @Test
    @DisplayName("Create Bug - Should save and return the bug")
    void testCreateBug() {
        when(bugRepo.save(any(Bug.class))).thenReturn(testBug);

        Bug createdBug = bugService.createBug(testBug);

        assertNotNull(createdBug);
        assertEquals("Test Bug", createdBug.getTitle());
        verify(bugRepo, times(1)).save(testBug);
    }

    @Test
    @DisplayName("Get All Bugs - Should return a paginated list of bugs")
    void testGetAllBugs() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Bug> bugs = List.of(testBug);
        Page<Bug> bugPage = new PageImpl<>(bugs, pageable, bugs.size());
        when(bugRepo.findAll(pageable)).thenReturn(bugPage);

        Page<Bug> foundBugsPage = bugService.getAllBugs(pageable);

        assertEquals(1, foundBugsPage.getTotalElements());
        assertThat(foundBugsPage.getContent()).contains(testBug);
        verify(bugRepo, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Get Bug By ID - Should return bug when found")
    void testGetBugById_Found() {
        // Mock the repository to return an Optional.of(testBug)
        when(bugRepo.findById(1L)).thenReturn(Optional.of(testBug));

        // Call the service method, which handles the Optional and throws NotFoundException if empty
        Bug foundBug = bugService.getBugById(1L);

        assertNotNull(foundBug);
        assertEquals(1L, foundBug.getId());
        verify(bugRepo, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get Bug By ID - Should throw NotFoundException when not found")
    void testGetBugById_NotFound() {
        // Mock the repository to return an empty Optional
        when(bugRepo.findById(1L)).thenReturn(Optional.empty());

        // Assert that the service method throws NotFoundException
        assertThrows(NotFoundException.class, () -> bugService.getBugById(1L));
        verify(bugRepo, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Update Bug - Should succeed for existing bug")
    void testUpdateBug_Success() {
        Bug existingBug = new Bug("Old Title", "Old Desc", Priority.LOW, Status.OPEN);
        existingBug.setId(1L);

        Bug updatedDetails = new Bug("New Title", "New Desc", Priority.HIGH, Status.CLOSED);

        when(bugRepo.findById(1L)).thenReturn(Optional.of(existingBug));
        when(bugRepo.save(any(Bug.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bug result = bugService.updateBug(1L, updatedDetails);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Desc", result.getDescription());
        assertEquals(Status.CLOSED, result.getStatus());
        assertEquals(Priority.HIGH, result.getPriority());
        verify(bugRepo, times(1)).findById(1L);
        verify(bugRepo, times(1)).save(existingBug);
    }

    @Test
    @DisplayName("Update Bug - Should throw NotFoundException for non-existent bug")
    void testUpdateBug_NotFound() {
        Bug updatedDetails = new Bug("New Title", "New Desc", Priority.HIGH, Status.CLOSED);
        when(bugRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bugService.updateBug(1L, updatedDetails));

        verify(bugRepo, times(1)).findById(1L);
        verify(bugRepo, never()).save(any(Bug.class));
    }

    @Test
    @DisplayName("Delete Bug - Should succeed for existing bug")
    void testDeleteBug_Success() {
        when(bugRepo.existsById(1L)).thenReturn(true);
        doNothing().when(bugRepo).deleteById(1L);

        assertDoesNotThrow(() -> bugService.deleteBug(1L));

        verify(bugRepo, times(1)).existsById(1L);
        verify(bugRepo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete Bug - Should throw NotFoundException for non-existent bug")
    void testDeleteBug_NotFound() {
        when(bugRepo.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bugService.deleteBug(1L));

        verify(bugRepo, times(1)).existsById(1L);
        verify(bugRepo, never()).deleteById(1L);
    }
}