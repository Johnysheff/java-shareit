package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDtoTest {

    @Test
    void shouldCreateUserWithCorrectFields() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("Сергей Иванов");
        user.setEmail("sergey@mail.ru");

        assertEquals("Сергей Иванов", user.getName());
        assertEquals("sergey@mail.ru", user.getEmail());
    }
}