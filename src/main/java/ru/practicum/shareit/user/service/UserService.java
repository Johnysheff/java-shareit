package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll();

    UserDto getById(Long userId);

    UserDto create(UserDto dto);

    UserDto update(Long userId, UserDto dto);

    void delete(Long userId);
}