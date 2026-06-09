package br.com.apisecurity.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponseDto(
        String message,
        int status,
        String error,
        LocalDateTime timestamp,
        Map<String, String> details
) {

    public ApiErrorResponseDto(String message, int status, String error) {
        this(message, status, error, LocalDateTime.now(), null);
    }
}