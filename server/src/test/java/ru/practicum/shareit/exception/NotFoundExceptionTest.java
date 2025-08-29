package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    void testNotFoundException_withMessage() {
        String message = "Объект не найден";
        NotFoundException exception = new NotFoundException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testNotFoundException_withoutMessage() {
        NotFoundException exception = new NotFoundException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testNotFoundException_thrownInServiceLogic() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException("Пользователь не существует");
        });

        assertEquals("Пользователь не существует", exception.getMessage());
    }
}