package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InMemoryUserService implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1L;

    @Override
    public List<UserDto> getAll() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getById(Long userId) {
        return Optional.ofNullable(users.get(userId))
                .map(UserMapper::toUserDto)
                .orElse(null);
    }

    @Override
    public UserDto create(UserDto dto) {
        if (dto.getName() == null || dto.getEmail() == null) {
            throw new IllegalArgumentException("Имя и email не могут быть пустыми");
        }

        if (!isValidEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Неверный формат email");
        }

        for (User u : users.values()) {
            if (u.getEmail().equals(dto.getEmail())) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
        }

        User user = UserMapper.toUser(dto);
        user.setId(nextId++);
        users.put(user.getId(), user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long userId, UserDto dto) {
        User existing = users.get(userId);
        if (existing == null) return null;

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }

        if (dto.getEmail() != null && !existing.getEmail().equals(dto.getEmail())) {
            for (User u : users.values()) {
                if (u.getEmail().equals(dto.getEmail())) {
                    throw new IllegalArgumentException("Пользователь с таким email уже существует");
                }
            }
            existing.setEmail(dto.getEmail());
        }

        return UserMapper.toUserDto(existing);
    }

    @Override
    public void delete(Long userId) {
        users.remove(userId);
    }

    //Проверяем, что email имеет правильный формат
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-.]+@[\\w-]+\\.[\\w-.]+$");
    }
}