package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
        String message = ex.getMessage();

        if (message.contains("уже существует")) {
            log.warn("Ошибка: {}", message);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", message));
        }

        if (message.equals("вещь не найдена") || message.equals("пользователя не существует")) {
            log.warn("Ошибка: {}", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Запрашиваемый объект не найден"));
        }

        if (message.equals("только владелец может обновить вещь")) {
            log.warn("Ошибка: {}", message);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Обновлять вещь может только её владелец"));
        }

        log.error("Некорректный запрос: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<Map<String, String>> handleValidationExceptions() {
        String errorMessage = "Ошибка валидации данных";
        log.warn(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedError(Exception ex) {
        String errorMessage = "Внутренняя ошибка сервера";
        log.error("{}: {}", errorMessage, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", errorMessage));
    }
}