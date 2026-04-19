package br.leetjourney.taskmanager.controller;

import br.leetjourney.taskmanager.domain.enums.TaskStatus;
import br.leetjourney.taskmanager.dto.request.TaskRequest;
import br.leetjourney.taskmanager.dto.request.TaskStatusRequest;
import br.leetjourney.taskmanager.dto.response.TaskResponse;
import br.leetjourney.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/tasks")

public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        return taskService.create(request);
    }

    @GetMapping
    public Page<TaskResponse> list(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) LocalDate dueBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort
    ) {
        int safeSize = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(sort).descending());
        return taskService.list(status, dueBefore, pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @PutMapping("/{id}")
    public TaskResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return taskService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusRequest request) {
        return taskService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

}
