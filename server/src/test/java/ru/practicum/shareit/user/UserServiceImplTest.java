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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

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
        user.setName("User");
        user.setEmail("user@mail.ru");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("User");
        userDto.setEmail("user@mail.ru");
    }

    @Test
    void getById_whenUserExists_thenReturnUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertNotNull(result);
        assertEquals("User", result.getName());
    }

    @Test
    void getById_whenUserNotExists_thenThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void getAll_whenUsersExist_thenReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User", result.get(0).getName());
    }

    @Test
    void create_whenValidUser_thenReturnUserDto() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertEquals("User", result.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_whenEmailAlreadyExists_thenThrowValidationException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(userDto));
        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
    }

    @Test
    void update_whenUpdateName_thenReturnUpdatedUser() {
        UserDto dto = new UserDto();
        dto.setName("Updated Name");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.update(1L, dto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void update_whenUpdateEmailToExistingEmail_thenThrowValidationException() {
        UserDto dto = new UserDto();
        dto.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");

        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(eq("existing@example.com"))).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.update(1L, dto));
        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
    }

    @Test
    void delete_whenUserExists_thenDeleteUser() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        userService.delete(1L);

        verify(userRepository, times(1)).deleteById(eq(1L));
    }

    @Test
    void delete_whenUserDoesNotExist_thenThrowNotFoundException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.delete(999L));
        assertEquals("пользователя не существует", exception.getMessage());
    }

    @Test
    void create_whenNameIsNull_thenThrowValidationException() {
        UserDto dto = new UserDto();
        dto.setName(null);
        dto.setEmail("test@example.com");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(dto));
        assertEquals("Имя и email не могут быть пустыми", exception.getMessage());
    }

    @Test
    void create_whenEmailInvalidFormat_thenThrowValidationException() {
        UserDto dto = new UserDto();
        dto.setName("Ivan Petrov");
        dto.setEmail("invalid-email");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(dto));
        assertEquals("Неверный формат email", exception.getMessage());
    }
}