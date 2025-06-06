package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemRepository {
    ItemDto createItem(Long userId, ItemCreateDto dto);

    ItemDto updateItem(Long userId, Long itemId, ItemCreateDto dto);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getAllItemsByOwnerId(Long ownerId);

    List<ItemDto> searchItems(String text);
}