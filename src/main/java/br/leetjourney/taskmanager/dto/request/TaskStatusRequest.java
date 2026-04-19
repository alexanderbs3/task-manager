package br.leetjourney.taskmanager.dto.request;

import br.leetjourney.taskmanager.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusRequest(
        @NotNull(message = "Status é obrigatório")
        TaskStatus status
) {
}
