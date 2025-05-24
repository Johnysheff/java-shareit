package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemRepository;

import java.util.List;

@RestController
@RequestMapping("/items")
@Validated
public class ItemController {

    private final ItemRepository itemService;

    @Autowired
    public ItemController(ItemRepository itemService) {
        this.itemService = itemService;
    }

    //Добавляем новую вещь
    @PostMapping
    public ItemDto createItem(
            @RequestHeader(name = "X-Sharer-User-Id") @Positive(message = "ID пользователя должен быть положительным") Long userId,
            @Valid @RequestBody ItemCreateDto dto) {

        if (userId == null) {
            throw new IllegalArgumentException("Заголовок X-Sharer-User-Id обязателен");
        }

        return itemService.createItem(userId, dto);
    }

    //Обновляем вещь, только владелец может это делать
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "ID пользователя должен быть положительным") Long userId,
            @PathVariable @Positive(message = "ID вещи должен быть положительным") Long itemId,
            @RequestBody ItemCreateDto dto) {

        return itemService.updateItem(userId, itemId, dto);
    }

    //Получаем вещь по id
    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable @Positive(message = "ID вещи должен быть положительным") Long itemId) {
        return itemService.getItemById(itemId);
    }

    //Получаем все вещи владельца
    @GetMapping
    public List<ItemDto> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive(message = "ID пользователя должен быть положительным") Long ownerId) {
        return itemService.getAllItemsByOwnerId(ownerId);
    }

    //Поиск доступных вещей по тексту
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchItems(text);
    }
}