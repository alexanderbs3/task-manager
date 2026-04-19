package br.leetjourney.taskmanager.service;

import br.leetjourney.taskmanager.domain.enums.TaskStatus;
import br.leetjourney.taskmanager.domain.model.Task;
import br.leetjourney.taskmanager.domain.model.User;
import br.leetjourney.taskmanager.dto.request.TaskRequest;
import br.leetjourney.taskmanager.dto.request.TaskStatusRequest;
import br.leetjourney.taskmanager.dto.response.TaskResponse;
import br.leetjourney.taskmanager.dto.response.UserSummaryResponse;
import br.leetjourney.taskmanager.exception.BusinessException;
import br.leetjourney.taskmanager.exception.ForbiddenException;
import br.leetjourney.taskmanager.exception.ResourceNotFoundException;
import br.leetjourney.taskmanager.mapper.TaskMapper;
import br.leetjourney.taskmanager.repository.TaskRepository;
import br.leetjourney.taskmanager.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskMapper taskMapper, TaskRepository taskRepository, UserRepository userRepository) {
        this.taskMapper = taskMapper;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    public TaskResponse create(TaskRequest request) {
        User user = authenticatedUser();

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .user(user)
                .build();


        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(TaskStatus status, LocalDate dueBefore, Pageable pageable) {
        User user = authenticatedUser();
        return taskRepository
                .findByUserWithFilters(user, status, dueBefore, pageable)
                .map(taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        User user = authenticatedUser();
        return taskMapper.toResponse(findTaskForUser(id, user));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        User user = authenticatedUser();
        Task task = findTaskForUser(id, user);

        if (task.getStatus() == TaskStatus.DONE) {
            throw new BusinessException("Tarefa concluída não pode ser editada");
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatusRequest request) {
        User user = authenticatedUser();
        Task task = findTaskForUser(id, user);

        validateStatusTransition(task.getStatus(), request.status());
        task.setStatus(request.status());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        User user = authenticatedUser();
        Task task = findTaskForUser(id, user);
        taskRepository.delete(task);
    }


    @Transactional(readOnly = true)
    public UserSummaryResponse summary() {
        User user = authenticatedUser();

        long pending = taskRepository.countByUserAndStatus(user, TaskStatus.PENDING);
        long inProgress = taskRepository.countByUserAndStatus(user, TaskStatus.IN_PROGRESS);
        long done = taskRepository.countByUserAndStatus(user, TaskStatus.DONE);
        long total = pending + inProgress + done;

        return new UserSummaryResponse(total, Map.of(
                "PENDING", pending,
                "IN_PROGRESS", inProgress,
                "DONE", done
        ));
    }

// -------------------------------------------------------------------------
// Helpers privados
// -------------------------------------------------------------------------

    private User authenticatedUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private Task findTaskForUser(Long id, User user) {
        // Primeiro verifica se a tarefa existe
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));

        // Depois verifica se pertence ao usuário autenticado
        if (!task.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Acesso negado a esta tarefa");
        }

        return task;
    }

    private void validateStatusTransition(TaskStatus current, TaskStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == TaskStatus.DONE;
            case DONE -> false;
        };

        if (!valid) {
            throw new BusinessException(
                    "Transição inválida: %s → %s".formatted(current, next));
        }
    }
}
