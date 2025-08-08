package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingDtoTest {

    @Test
    void shouldCreateBookingWithCorrectFields() {
        LocalDateTime start = LocalDateTime.of(2025, 7, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 7, 20, 18, 0);

        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная дрель Makita");

        UserDto booker = new UserDto();
        booker.setId(1L);
        booker.setName("Иван Петров");

        BookingDto booking = new BookingDto();
        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItemId(1L);
        booking.setBookerId(1L);
        booking.setStatus("WAITING");
        booking.setItem(item);
        booking.setBooker(booker);

        assertEquals(1L, booking.getId());
        assertEquals(start, booking.getStart());
        assertEquals("Дрель", booking.getItem().getName());
    }

    @Test
    void shouldReturnCorrectStringRepresentation() {
        LocalDateTime start = LocalDateTime.of(2025, 7, 5, 9, 0);
        BookingDto booking = new BookingDto(1L, start, null, 1L, 1L, "REJECTED", null, null);

        assertTrue(booking.toString().contains("start=2025-07-05T09:00"));
    }
}