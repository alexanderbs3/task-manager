package br.leetjourney.taskmanager.repository;

import br.leetjourney.taskmanager.domain.enums.TaskStatus;
import br.leetjourney.taskmanager.domain.model.Task;
import br.leetjourney.taskmanager.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {


    Optional<Task> findByIdAndUser(Long id, User user);

    Page<Task> findByUser(User user, Pageable pageable);

    Page<Task> findByUserAndStatus(User user, TaskStatus status, Pageable pageable);

    Page<Task> findByUserAndDueDateLessThanEqual(User user, LocalDate dueBefore, Pageable pageable);

    @Query("""
            SELECT t FROM Task t
            WHERE t.user = :user
              AND (:status IS NULL OR t.status = :status)
              AND (:dueBefore IS NULL OR t.dueDate <= :dueBefore)
            """)
    Page<Task> findByUserWithFilters(
            @Param("user") User user,
            @Param("status") TaskStatus status,
            @Param("dueBefore") LocalDate dueBefore,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(t) FROM Task t
            WHERE t.user = :user AND t.status = :status
            """)
    long countByUserAndStatus(@Param("user") User user, @Param("status") TaskStatus status);
}

