package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JsonTest
class BookingDtoJsonTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSerialize() throws JsonProcessingException {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 2, 12, 0);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItemId(1L);
        bookingDto.setBookerId(1L);
        bookingDto.setStatus("WAITING");

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        bookingDto.setItem(itemDto);

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("User");
        userDto.setEmail("user@email.com");
        bookingDto.setBooker(userDto);

        String json = objectMapper.writeValueAsString(bookingDto);

        assertNotNull(json);

        BookingDto parsedDto = objectMapper.readValue(json, BookingDto.class);

        assertEquals(bookingDto.getId(), parsedDto.getId());
        assertEquals(bookingDto.getStart(), parsedDto.getStart());
        assertEquals(bookingDto.getEnd(), parsedDto.getEnd());
        assertEquals(bookingDto.getStatus(), parsedDto.getStatus());
        assertEquals(bookingDto.getItemId(), parsedDto.getItemId());
        assertEquals(bookingDto.getBookerId(), parsedDto.getBookerId());

        assertNotNull(parsedDto.getItem());
        assertEquals(bookingDto.getItem().getId(), parsedDto.getItem().getId());

        assertNotNull(parsedDto.getBooker());
        assertEquals(bookingDto.getBooker().getId(), parsedDto.getBooker().getId());
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        String json = "{\"id\":1,\"start\":\"2025-01-01T12:00:00\",\"end\":\"2025-01-02T12:00:00\"," +
                      "\"itemId\":1,\"bookerId\":1,\"status\":\"WAITING\"," +
                      "\"item\":{\"id\":1,\"name\":\"Item\",\"description\":\"Description\",\"available\":true}," +
                      "\"booker\":{\"id\":1,\"name\":\"User\",\"email\":\"user@email.com\"}}";

        BookingDto bookingDto = objectMapper.readValue(json, BookingDto.class);

        assertNotNull(bookingDto);
        assertEquals(1L, bookingDto.getId());
        assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0), bookingDto.getStart());
        assertEquals(LocalDateTime.of(2025, 1, 2, 12, 0), bookingDto.getEnd());
        assertEquals("WAITING", bookingDto.getStatus());
        assertEquals(1L, bookingDto.getItemId());
        assertEquals(1L, bookingDto.getBookerId());
        assertNotNull(bookingDto.getItem());
        assertEquals(1L, bookingDto.getItem().getId());
        assertNotNull(bookingDto.getBooker());
        assertEquals(1L, bookingDto.getBooker().getId());
    }
}