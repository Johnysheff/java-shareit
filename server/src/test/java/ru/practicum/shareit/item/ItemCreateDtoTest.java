package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemCreateDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemCreateDtoTest {

    @Test
    void shouldCreateItemWithCorrectProperties() {

        ItemCreateDto item = new ItemCreateDto();
        item.setName("Мотоблок");
        item.setDescription("Мотоблок Нева МБ-2");
        item.setAvailable(true);
        item.setRequestId(1L);

        assertEquals("Мотоблок", item.getName());
        assertTrue(item.getAvailable());
    }
}