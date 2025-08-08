package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForbiddenExceptionTest {

    @Test
    void testForbiddenException_withMessage() {
        String message = "Доступ запрещён";
        ForbiddenException exception = new ForbiddenException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testForbiddenException_withoutMessage_throwsExceptionWithNullMessage() {
        ForbiddenException exception = new ForbiddenException("Ошибка доступа");

        assertNotNull(exception);
        assertEquals("Ошибка доступа", exception.getMessage());
    }

    @Test
    void testForbiddenException_thrownInServiceLogic() {
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            throw new ForbiddenException("У вас нет прав для выполнения этого действия");
        });

        assertEquals("У вас нет прав для выполнения этого действия", exception.getMessage());
    }
}