package ru.practicum.shareit.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class ItemRequestDtoJsonTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSerialize() throws JsonProcessingException {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("Need item");
        requestDto.setCreated(LocalDateTime.of(2025, 1, 1, 12, 0));

        ItemRequestDto.ItemDto itemDto = new ItemRequestDto.ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(1L);
        requestDto.setItems(List.of(itemDto));

        String json = objectMapper.writeValueAsString(requestDto);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"description\":\"Need item\""));
        assertTrue(json.contains("\"items\":["));
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        String json = "{\"id\":1,\"description\":\"Need item\",\"created\":\"2025-01-01T12:00:00\"," +
                      "\"items\":[{\"id\":1,\"name\":\"Item\",\"description\":\"Description\"," +
                      "\"available\":true,\"requestId\":1}]}";

        ItemRequestDto requestDto = objectMapper.readValue(json, ItemRequestDto.class);

        assertNotNull(requestDto);
        assertEquals(1L, requestDto.getId());
        assertEquals("Need item", requestDto.getDescription());
        assertEquals(1, requestDto.getItems().size());
        assertEquals(1L, requestDto.getItems().get(0).getId());
    }
}