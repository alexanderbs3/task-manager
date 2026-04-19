package br.leetjourney.taskmanager.dto.response;

public record AuthResponse(
         String token,
         String type,
         long expiresIn
) {
}
