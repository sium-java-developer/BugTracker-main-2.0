package org.bugtracker.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// Import the external Priority and Status enums
import org.bugtracker.entities.Priority;
import org.bugtracker.entities.Status;

/**
 * Represents a bug or issue in the tracking system.
 */
@Entity
@Table(name = "bug") // Changed to snake_case for database table naming convention
public class Bug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255) // Changed to snake_case, added length constraint
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT") // Changed to snake_case, TEXT for larger content
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false) // updatable = false ensures it's set only on creation
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false) // Changed to snake_case
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING) // Stores enum as String in DB
    @Column(name = "priority", nullable = false, length = 50) // Changed to snake_case, added length
    private Priority priority;

    @Enumerated(EnumType.STRING) // Stores enum as String in DB
    @Column(name = "status", nullable = false, length = 50) // Changed to snake_case, added length
    private Status status;

    // Inverse side of Many-to-Many relationship with User entity.
    // The 'mappedBy' attribute indicates that the owning side of the relationship is in the 'User' entity,
    // specifically the field named 'assignedBugs'.
    // FetchType.LAZY is generally recommended for collections.
    @ManyToMany(mappedBy = "assignedBugs", fetch = FetchType.LAZY)
    private Set<User> assignedUsers = new HashSet<>();

    /**
     * Default constructor required by JPA.
     * Timestamps are handled by @PrePersist.
     */
    public Bug() {
        // Initialization of createdAt and updatedAt moved to @PrePersist
    }

    /**
     * Constructs a new Bug.
     * Timestamps are handled by @PrePersist.
     *
     * @param title       The title of the bug.
     * @param description The detailed description of the bug.
     * @param priority    The priority of the bug.
     * @param status      The current status of the bug.
     */
    public Bug(String title, String description, Priority priority, Status status) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        // Initialization of createdAt and updatedAt moved to @PrePersist
    }

    /**
     * Callback method executed before the entity is first persisted.
     * Sets `createdAt` and `updatedAt` timestamps.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Callback method executed before the entity is updated.
     * Updates the `updatedAt` timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<User> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(Set<User> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    /**
     * Helper method to assign a user to this bug.
     * Manages both sides of the bidirectional relationship to keep data consistent.
     *
     * @param user The user to assign.
     */
    public void assignUser(User user) {
        // Add the user to this bug's assignedUsers set.
        // The 'add' method returns true if the element was not already present.
        if (this.assignedUsers.add(user)) {
            // If the user was successfully added to this bug's set,
            // then add this bug to the user's assignedBugs set to maintain bidirectionality.
            user.getAssignedBugs().add(this);
        }
    }

    /**
     * Helper method to unassign a user from this bug.
     * Manages both sides of the bidirectional relationship to keep data consistent.
     *
     * @param user The user to unassign.
     */
    public void unassignUser(User user) {
        // Remove the user from this bug's assignedUsers set.
        // The 'remove' method returns true if the element was present.
        if (this.assignedUsers.remove(user)) {
            // If the user was successfully removed from this bug's set,
            // then remove this bug from the user's assignedBugs set to maintain bidirectionality.
            user.getAssignedBugs().remove(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bug bug = (Bug) o;
        // For persisted entities, ID is the best way to check equality.
        // Using `id != null` ensures that two unpersisted entities (id == null) are not considered equal based on null id.
        return id != null && Objects.equals(id, bug.id);
    }

    @Override
    public int hashCode() {
        // Consistent with equals: if ID is used for equals, use it for hashCode.
        // If id is null (unpersisted entity), use a fallback like getClass().hashCode()
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Bug{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}