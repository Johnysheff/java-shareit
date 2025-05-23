package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    //Добавляем новую вещь
    @PostMapping
    public ResponseEntity<ItemDto> createItem(
            @RequestHeader(name = "X-Sharer-User-Id", required = false) Long userId,
            @Valid @RequestBody ItemCreateDto dto) {

        if (userId == null) {
            throw new IllegalArgumentException("Заголовок X-Sharer-User-Id обязателен");
        }

        ItemDto created = itemService.createItem(userId, dto);
        return ResponseEntity.status(201).body(created);
    }

    //Обновляем вещь, только владелец может это делать
    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemCreateDto dto) {

        ItemDto updated = itemService.updateItem(userId, itemId, dto);
        return ResponseEntity.ok(updated);
    }

    //Получаем вещь по id
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@PathVariable Long itemId) {
        ItemDto item = itemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    //Получаем все вещи владельца
    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return ResponseEntity.ok(itemService.getAllItemsByOwnerId(ownerId));
    }

    //Поиск доступных вещей по тексту
    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam String text) {
        return ResponseEntity.ok(itemService.searchItems(text));
    }
}