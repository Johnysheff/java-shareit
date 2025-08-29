package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Иван Иванов");
        user.setEmail("ivan@mail.ru");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Иван Иванов");
        userDto.setEmail("ivan@mail.ru");
    }

    @Test
    void getAll_whenUsersExist_thenReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Иван Иванов", result.get(0).getName());
    }

    @Test
    void getAll_whenNoUsersExist_thenReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_whenUserExists_thenReturnUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertNotNull(result);
        assertEquals("Иван Иванов", result.getName());
    }

    @Test
    void getById_whenUserNotExists_thenThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void create_whenValidUser_thenReturnUserDto() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertEquals("Иван Иванов", result.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_whenEmailExists_thenThrowValidationException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ValidationException.class, () -> userService.create(userDto));
    }

    @Test
    void update_whenUpdateName_thenReturnUpdatedUser() {
        UserDto updates = new UserDto();
        updates.setName("Петр Петров");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.update(1L, updates);

        assertNotNull(result);
        assertEquals("Петр Петров", result.getName());
    }

    @Test
    void update_whenUpdateEmail_thenReturnUpdatedUser() {
        UserDto updates = new UserDto();
        updates.setEmail("newemail@mail.ru");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.update(1L, updates);

        assertNotNull(result);
        assertEquals("newemail@mail.ru", result.getEmail());
    }

    @Test
    void update_whenUpdateNameAndEmail_thenReturnBothUpdated() {
        UserDto updates = new UserDto();
        updates.setName("Петр Петров");
        updates.setEmail("newemail@mail.ru");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.update(1L, updates);

        assertNotNull(result);
        assertEquals("Петр Петров", result.getName());
        assertEquals("newemail@mail.ru", result.getEmail());
    }

    @Test
    void delete_whenUserNotExists_thenThrowNotFoundException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.delete(1L));
        assertEquals("пользователя не существует", exception.getMessage());
    }

    @Test
    void delete_whenUserExists_thenDeleteUser() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        assertDoesNotThrow(() -> userService.delete(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }
}