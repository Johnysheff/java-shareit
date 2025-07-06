package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAll_shouldReturnAllUsers() {
        User user = new User(1L, "User", "user@mail.ru");
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getById_shouldReturnUser() {
        User user = new User(1L, "User", "user@mail.ru");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void create_shouldCreateUser() {
        UserDto dto = new UserDto();
        dto.setName("Иван Иванов");
        dto.setEmail("ivan@mail.ru");

        User user = new User(1L, "Иван Иванов", "ivan@mail.ru");
        when(userRepository.save(any())).thenReturn(user);

        UserDto result = userService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void update_shouldUpdateUser() {
        User existing = new User(1L, "Иван Иванов", "ivan@mail.ru");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existing));

        UserDto dto = new UserDto();
        dto.setName("Иван Петров");

        User updated = new User(1L, "Иван Петров", "ivan@mail.ru");
        when(userRepository.save(any())).thenReturn(updated);

        UserDto result = userService.update(1L, dto);

        assertEquals("Иван Петров", result.getName());
    }

    @Test
    void delete_shouldDeleteUser() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void create_whenEmailExists_shouldThrowException() {
        UserDto dto = new UserDto();
        dto.setName("Иван Иванов");
        dto.setEmail("ivan@mail.ru");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ValidationException.class, () -> userService.create(dto));
    }
}