package br.com.apisecurity.config;


import br.com.apisecurity.dto.ApiErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiErrorResponseDto errorResponse = new ApiErrorResponseDto(
                "Falha na validação dos dados enviados.",
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDto> handleBadCredentials(BadCredentialsException ex) {
        ApiErrorResponseDto errorResponse = new ApiErrorResponseDto(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiErrorResponseDto> handleAccountLocked(LockedException ex) {
        ApiErrorResponseDto errorResponse = new ApiErrorResponseDto(
                ex.getMessage(),
                HttpStatus.LOCKED.value(),
                HttpStatus.LOCKED.getReasonPhrase()
        );
        return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponseDto> handleRuntimeException(RuntimeException ex) {
        ApiErrorResponseDto errorResponse = new ApiErrorResponseDto(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleGeneralException(Exception ex) {
        ApiErrorResponseDto errorResponse = new ApiErrorResponseDto(
                "Ocorreu um erro inesperado no servidor.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
