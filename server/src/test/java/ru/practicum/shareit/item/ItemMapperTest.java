package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toItem_whenValidDto_thenMappedCorrectly() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная дрель");
        dto.setAvailable(true);

        Item item = ItemMapper.toItem(dto);

        assertNotNull(item);
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
        assertTrue(item.getAvailable());
        assertNull(item.getRequest());
    }

    @Test
    void toItemDto_whenItemHasNoRequest_thenReturnDtoWithoutRequestId() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Отвёртка");
        item.setDescription("Крестовая");
        item.setAvailable(true);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertNotNull(dto);
        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertTrue(dto.getAvailable());
        assertNull(dto.getRequestId());
    }

    @Test
    void toItemDto_whenItemHasRequest_thenReturnDtoWithRequestId() {
        ItemRequest request = new ItemRequest();
        request.setId(100L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Молоток");
        item.setDescription("Ударный");
        item.setAvailable(true);
        item.setRequest(request);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertNotNull(dto);
        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(request.getId(), dto.getRequestId());
    }
}