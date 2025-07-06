package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("пользователя не существует"));
    }

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        validateUserDto(dto);

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        User user = UserMapper.toUser(dto);
        User savedUser = userRepository.save(user);
        log.info("Создан пользователь с ID: {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(Long userId, UserDto dto) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("пользователя не существует"));

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }

        if (dto.getEmail() != null && !existing.getEmail().equals(dto.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new ValidationException("Пользователь с таким email уже существует");
            }
            existing.setEmail(dto.getEmail());
        }

        User updatedUser = userRepository.save(existing);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("пользователя не существует");
        }
        userRepository.deleteById(userId);
        log.info("Удалён пользователь с ID: {}", userId);
    }

    private void validateUserDto(UserDto dto) {
        if (dto.getName() == null || dto.getEmail() == null) {
            throw new ValidationException("Имя и email не могут быть пустыми");
        }

        if (!isValidEmail(dto.getEmail())) {
            throw new ValidationException("Неверный формат email");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-.]+@[\\w-]+\\.[\\w-.]+$");
    }
}