package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InMemoryItemService implements ItemService {

    private final Map<Long, Item> items = new HashMap<>();
    private final UserService userService;
    private long nextId = 1L;

    public InMemoryItemService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemDto createItem(Long userId, ItemCreateDto dto) {
        if (userId == null || userService.getById(userId) == null) {
            throw new IllegalArgumentException("пользователя не существует");
        }

        Item item = ItemMapper.toItem(dto, userId);
        item.setId(nextId++);
        items.put(item.getId(), item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemCreateDto dto) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("вещь не найдена");
        }

        if (!item.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("только владелец может обновить вещь");
        }

        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) item.setAvailable(dto.getAvailable());

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("вещь не найдена");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(i -> i.getOwnerId().equals(ownerId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lowerText = text.toLowerCase(Locale.ROOT);
        return items.values().stream()
                .filter(i -> i.getAvailable() &&
                             (i.getName().toLowerCase(Locale.ROOT).contains(lowerText) ||
                              i.getDescription().toLowerCase(Locale.ROOT).contains(lowerText)))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}