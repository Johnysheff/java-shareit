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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requester;
    private ItemRequest request;
    private Item item;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setName("Requester");
        requester.setEmail("requester@email.com");

        request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need item");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(requester);
        item.setRequest(request);
    }

    @Test
    void createRequest_whenValidData_thenReturnRequestDto() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need item");

        when(userService.getById(1L)).thenReturn(requester);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(itemRepository.findByRequestId(1L)).thenReturn(Collections.emptyList());

        ItemRequestDto result = itemRequestService.createRequest(1L, requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need item", result.getDescription());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void getRequestsByUser_whenUserExists_thenReturnRequests() {
        when(userService.getById(anyLong())).thenReturn(requester);
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getRequestsByUser(1L);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
    }

    @Test
    void getRequestById_whenRequestExists_thenReturnRequest() {
        when(userService.getById(anyLong())).thenReturn(requester);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void getRequestById_whenRequestNotExists_thenThrowNotFoundException() {
        when(userService.getById(anyLong())).thenReturn(requester);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(1L, 999L));
    }
}