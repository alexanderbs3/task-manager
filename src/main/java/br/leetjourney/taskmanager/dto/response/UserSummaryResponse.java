package br.leetjourney.taskmanager.dto.response;

import java.util.Map;

public record UserSummaryResponse(
        long total,
        Map<String, Long> byStatus
) {
}
