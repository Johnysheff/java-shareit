package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemDtoTest {

    @Test
    void shouldContainBookingAndCommentInfo() {

        ItemDto.BookingShortDto lastBooking =
                new ItemDto.BookingShortDto(1L, 1L);

        CommentDto comment = new CommentDto();
        comment.setText("Отличный инструмент!");
        comment.setAuthorName("Алексей");
        comment.setCreated(LocalDateTime.of(2025, 6, 30, 15, 30));

        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Бетономешалка");
        item.setLastBooking(lastBooking);
        item.setComments(List.of(comment));

        assertEquals("Бетономешалка", item.getName());
        assertEquals(1, item.getComments().size());
    }
}