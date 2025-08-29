package ru.practicum.shareit.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDto.BookingShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    void testSerialize() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        itemDto.setLastBooking(new BookingShortDto(1L, 1L));
        itemDto.setNextBooking(new BookingShortDto(2L, 2L));

        CommentDto comment = new CommentDto();
        comment.setId(1L);
        comment.setText("Comment");
        comment.setAuthorName("Author");
        comment.setCreated(LocalDateTime.now());
        itemDto.setComments(List.of(comment));

        String json = objectMapper.writeValueAsString(itemDto);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"Item\""));
        assertTrue(json.contains("\"comments\":["));
    }

    @Test
    @SneakyThrows
    void testDeserialize() {
        String json = "{\"id\":1,\"name\":\"Item\",\"description\":\"Description\"," +
                      "\"available\":true,\"requestId\":null,\"lastBooking\":{\"id\":1,\"bookerId\":1}," +
                      "\"nextBooking\":{\"id\":2,\"bookerId\":2},\"comments\":[{\"id\":1,\"text\":\"Comment\"," +
                      "\"authorName\":\"Author\",\"created\":\"2025-01-01T12:00:00\"}]}";

        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);

        assertNotNull(itemDto);
        assertEquals(1L, itemDto.getId());
        assertEquals("Item", itemDto.getName());
        assertEquals(1, itemDto.getComments().size());
        assertEquals(1L, itemDto.getLastBooking().getId());
        assertEquals(2L, itemDto.getNextBooking().getId());
    }

    @Test
    void testSerialize_withNullFields() throws JsonProcessingException {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Tool");

        dto.setComments(List.of());

        String json = objectMapper.writeValueAsString(dto);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"Tool\""));
        assertTrue(json.contains("\"comments\":[]"));
    }
}