package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User requester;
    private ItemRequest itemRequest;
    private Item item;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setName("Иван Иванов");
        requester.setEmail("ivan@mail.ru");

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная дрель");
        item.setAvailable(true);

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Нужна дрель");
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());
    }

    @Test
    void getRequestById_whenExists_thenReturnRequest() {
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        ItemRequestDto result = requestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals("Нужна дрель", result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals("Дрель", result.getItems().get(0).getName());
    }

    @Test
    void getRequestById_whenNotFound_thenThrowNotFoundException() {
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> requestService.getRequestById(1L, 999L)
        );

        assertEquals("Request not found", exception.getMessage());
    }

    @Test
    void getRequestsByUser_whenExists_thenReturnList() {
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        List<ItemRequestDto> result = requestService.getRequestsByUser(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
    }

    @Test
    void getAllRequests_whenFound_thenReturnList() {
        when(itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(anyLong(), any()))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        List<ItemRequestDto> result = requestService.getAllRequests(1L, 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
    }

    @Test
    void createRequest_whenValidData_thenReturnRequestDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need item");

        when(userService.getById(anyLong())).thenReturn(requester);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        when(itemRepository.findByRequestId(anyLong())).thenReturn(Collections.emptyList());

        ItemRequestDto result = requestService.createRequest(1L, dto);

        assertNotNull(result);
        assertEquals("Нужна дрель", result.getDescription());
    }
}