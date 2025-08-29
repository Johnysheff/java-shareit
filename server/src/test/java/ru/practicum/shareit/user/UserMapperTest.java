package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserMapperTest {

    @Test
    void toUser() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Иван Иванов");
        dto.setEmail("ivan@mail.ru");

        User user = UserMapper.toUser(dto);
        assertNotNull(user);
        assertEquals("Иван Иванов", user.getName());
        assertEquals("ivan@mail.ru", user.getEmail());
    }

    @Test
    void toUserDto() {
        User user = new User();
        user.setId(1L);
        user.setName("Петр Петров");
        user.setEmail("petr@mail.ru");

        UserDto dto = UserMapper.toUserDto(user);
        assertNotNull(dto);
        assertEquals("Петр Петров", dto.getName());
        assertEquals("petr@mail.ru", dto.getEmail());
    }
}