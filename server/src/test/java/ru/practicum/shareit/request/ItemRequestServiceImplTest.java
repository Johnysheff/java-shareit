package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void getAllRequests_shouldReturnRequests() {
        User user = new User();
        user.setId(1L);

        User requester = new User();
        requester.setId(2L);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Нужна дрель");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        List<ItemRequest> requestList = List.of(request);

        when(userService.getById(anyLong())).thenReturn(user);
        when(itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(requestList);

        when(itemRepository.findByRequestId(1L)).thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Нужна дрель", result.get(0).getDescription());
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        User requester = new User();
        requester.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Нужна дрель");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setOwner(requester);

        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals("Нужна дрель", result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals("Дрель", result.getItems().get(0).getName());
    }
}