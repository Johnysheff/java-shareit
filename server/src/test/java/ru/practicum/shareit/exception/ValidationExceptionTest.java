package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {

    @Test
    void testValidationException_withMessage() {
        String message = "Некорректный email";
        ValidationException exception = new ValidationException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testValidationException_withEmptyMessage() {
        String message = "";
        ValidationException exception = new ValidationException(message);

        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    void testValidationException_withNullMessage() {
        ValidationException exception = new ValidationException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }
}