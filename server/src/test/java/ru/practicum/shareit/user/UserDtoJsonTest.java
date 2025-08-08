package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class UserDtoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SneakyThrows
    void testSerialize() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("User");
        userDto.setEmail("user@email.ru");

        String json = objectMapper.writeValueAsString(userDto);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"User\""));
        assertTrue(json.contains("\"email\":\"user@email.ru\""));
    }

    @Test
    @SneakyThrows
    void testDeserialize() {
        String json = "{\"id\":1,\"name\":\"User\",\"email\":\"user@email.ru\"}";

        UserDto userDto = objectMapper.readValue(json, UserDto.class);

        assertNotNull(userDto);
        assertEquals(1L, userDto.getId());
        assertEquals("User", userDto.getName());
        assertEquals("user@email.ru", userDto.getEmail());
    }
}