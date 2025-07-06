package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_shouldCreateItem() {
        User owner = new User(1L, "Owner", "owner@mail.ru");
        when(userService.getById(anyLong())).thenReturn(owner);

        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Дрель");
        dto.setDescription("Простая дрель");
        dto.setAvailable(true);

        Item item = new Item(1L, "Дрель", "Простая дрель", true, owner, null);
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto result = itemService.createItem(1L, dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void updateItem_shouldUpdateItem() {
        User owner = new User(1L, "Owner", "owner@mail.ru");
        Item item = new Item(1L, "Дрель", "Простая дрель", true, owner, null);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Дрель+");

        Item updatedItem = new Item(1L, "Дрель+", "Простая дрель", true, owner, null);
        when(itemRepository.save(any())).thenReturn(updatedItem);

        ItemDto result = itemService.updateItem(1L, 1L, dto);

        assertEquals("Дрель+", result.getName());
    }

    @Test
    void getItemById_shouldReturnItemWithComments() {
        Item item = new Item(1L, "Дрель", "Простая дрель", true,
                new User(1L, "Owner", "owner@mail.ru"), null);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Comment comment = new Comment(1L, "Хорошая дрель", item,
                new User(2L, "Author", "author@mail.ru"), LocalDateTime.now());
        when(commentRepository.findByItemId(anyLong())).thenReturn(List.of(comment));

        ItemDto result = itemService.getItemById(1L);

        assertNotNull(result);
        assertEquals(1, result.getComments().size());
    }

    @Test
    void searchItems_shouldReturnAvailableItems() {
        Item item = new Item(1L, "Дрель", "Простая дрель", true,
                new User(1L, "Owner", "owner@mail.ru"), null);
        when(itemRepository.searchAvailableItems(anyString())).thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("дрель");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void addComment_shouldAddComment() {
        User author = new User(1L, "Author", "author@mail.ru");
        when(userService.getById(anyLong())).thenReturn(author);

        Item item = new Item(1L, "Дрель", "Простая дрель", true,
                new User(2L, "Owner", "owner@mail.ru"), null);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(
                anyLong(), anyLong(), any(), any())).thenReturn(List.of(booking));

        Comment comment = new Comment(1L, "Отличная дрель!", item, author, LocalDateTime.now());
        when(commentRepository.save(any())).thenReturn(comment);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель!");

        CommentDto result = itemService.addComment(1L, 1L, commentDto);

        assertEquals("Отличная дрель!", result.getText());
    }
}