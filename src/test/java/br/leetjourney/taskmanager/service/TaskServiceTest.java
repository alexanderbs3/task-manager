package br.leetjourney.taskmanager.service;

import br.leetjourney.taskmanager.domain.enums.TaskStatus;
import br.leetjourney.taskmanager.domain.model.Task;
import br.leetjourney.taskmanager.domain.model.User;
import br.leetjourney.taskmanager.dto.request.TaskRequest;
import br.leetjourney.taskmanager.dto.request.TaskStatusRequest;
import br.leetjourney.taskmanager.dto.response.TaskResponse;
import br.leetjourney.taskmanager.exception.BusinessException;
import br.leetjourney.taskmanager.exception.ForbiddenException;
import br.leetjourney.taskmanager.exception.ResourceNotFoundException;
import br.leetjourney.taskmanager.mapper.TaskMapper;
import br.leetjourney.taskmanager.repository.TaskRepository;
import br.leetjourney.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock UserRepository userRepository;
    @Mock TaskMapper taskMapper;
    @Mock SecurityContext securityContext;
    @Mock Authentication authentication;

    @InjectMocks
    TaskService taskService;

    User owner;
    User otherUser;
    Task task;
    TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L).name("João").email("joao@email.com").password("encoded").build();

        otherUser = User.builder()
                .id(2L).name("Maria").email("maria@email.com").password("encoded").build();

        task = Task.builder()
                .id(1L).title("Estudar JPA").status(TaskStatus.PENDING)
                .user(owner).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        taskResponse = new TaskResponse(
                1L, "Estudar JPA", null, TaskStatus.PENDING,
                null, LocalDateTime.now(), LocalDateTime.now());

        when(authentication.getName()).thenReturn("joao@email.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(owner));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("deve criar tarefa com status PENDING e associar ao usuário autenticado")
        void shouldCreateTaskWithPendingStatus() {
            TaskRequest request = new TaskRequest("Estudar JPA", null, null);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);

            TaskResponse result = taskService.create(request);

            assertThat(result.status()).isEqualTo(TaskStatus.PENDING);
            verify(taskRepository).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("deve avançar status de PENDING para IN_PROGRESS")
        void shouldAdvanceFromPendingToInProgress() {
            TaskStatusRequest request = new TaskStatusRequest(TaskStatus.IN_PROGRESS);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);

            taskService.updateStatus(1L, request);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("deve lançar BusinessException ao tentar retroceder status")
        void shouldThrowWhenStatusRegresses() {
            task.setStatus(TaskStatus.DONE);
            TaskStatusRequest request = new TaskStatusRequest(TaskStatus.IN_PROGRESS);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> taskService.updateStatus(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Transição inválida");
        }

        @Test
        @DisplayName("deve lançar BusinessException ao tentar editar tarefa DONE")
        void shouldThrowWhenEditingDoneTask() {
            task.setStatus(TaskStatus.DONE);
            TaskRequest request = new TaskRequest("Novo título", null, null);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> taskService.update(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("concluída");
        }
    }

    @Nested
    @DisplayName("isolamento entre usuários")
    class UserIsolation {

        @Test
        @DisplayName("deve lançar ForbiddenException ao acessar tarefa de outro usuário")
        void shouldThrowWhenAccessingOtherUserTask() {
            task.setUser(otherUser);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> taskService.getById(1L))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException para tarefa inexistente")
        void shouldThrowWhenTaskNotFound() {
            when(taskRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve deletar tarefa do usuário autenticado")
        void shouldDeleteOwnTask() {
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            taskService.delete(1L);

            verify(taskRepository).delete(task);
        }

        @Test
        @DisplayName("deve lançar ForbiddenException ao tentar deletar tarefa de outro usuário")
        void shouldThrowWhenDeletingOtherUserTask() {
            task.setUser(otherUser);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> taskService.delete(1L))
                    .isInstanceOf(ForbiddenException.class);
        }
    }
}