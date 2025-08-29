package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemRequestDtoTest {

    @Test
    void shouldContainCorrectItemRequestDetails() {
        LocalDateTime created = LocalDateTime.of(2025, 7, 1, 14, 30);

        ItemRequestDto.ItemDto item = new ItemRequestDto.ItemDto(
                1L,
                "Газонокосилка",
                "Электрическая газонокосилка",
                true,
                1L
        );

        ItemRequestDto request = new ItemRequestDto();
        request.setId(1L);
        request.setDescription("Нужна газонокосилка для дачи");
        request.setCreated(created);
        request.setItems(List.of(item));

        assertEquals(1L, request.getId());
        assertEquals("Газонокосилка", request.getItems().get(0).getName());
        assertEquals(2025, request.getCreated().getYear());
    }
}