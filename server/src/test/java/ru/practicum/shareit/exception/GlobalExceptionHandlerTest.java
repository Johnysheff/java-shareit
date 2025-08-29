package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound() {
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(new NotFoundException("Объект не найден"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Объект не найден", response.getBody().get("error"));
    }

    @Test
    void handleForbidden() {
        ResponseEntity<Map<String, String>> response = handler.handleForbidden(new ForbiddenException("Доступ запрещён"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Доступ запрещён", response.getBody().get("error"));
    }

    @Test
    void handleBadRequest() {
        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(new IllegalArgumentException("Ошибка валидации"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Ошибка валидации", response.getBody().get("error"));
    }

    @Test
    void handleConflict() {
        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(new IllegalArgumentException("уже существует"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("уже существует", response.getBody().get("error"));
    }

    @Test
    void handleForbiddenException() {
        ResponseEntity<Map<String, String>> response = handler.handleForbidden(new ForbiddenException("Доступ запрещён"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Доступ запрещён", response.getBody().get("error"));
    }
}