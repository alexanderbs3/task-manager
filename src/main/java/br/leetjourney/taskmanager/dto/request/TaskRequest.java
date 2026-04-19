package br.leetjourney.taskmanager.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(min = 3, max = 200, message = "Título deve ter entre 3 e 200 caracteres")
        String title,

        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String description,

        @FutureOrPresent(message = "O prazo não pode ser uma data no passado")
        LocalDate dueDate
) {
}
