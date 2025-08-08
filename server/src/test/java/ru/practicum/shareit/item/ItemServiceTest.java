package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Иван Иванов");
        user.setEmail("ivan@mail.ru");

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная дрель");
        item.setAvailable(true);
        item.setOwner(user);

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Хорошая вещь");
        comment.setItem(item);
        comment.setAuthor(user);
    }

    @Test
    void createItem_whenValidData_thenReturnItemDto() throws Exception {
        when(userService.getById(anyLong())).thenReturn(user);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(1L, getItemCreateDto());
        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
    }

    @Test
    void updateItem_whenValidData_thenReturnUpdatedItemDto() {
        ItemCreateDto updates = new ItemCreateDto();
        updates.setName("Обновлённое имя");
        updates.setDescription("Обновленное описание");
        updates.setAvailable(false);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.updateItem(1L, 1L, updates);
        assertNotNull(result);
        assertEquals(updates.getName(), result.getName());
        assertEquals(updates.getDescription(), result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void getItemById_whenExists_thenReturnItemDto() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemIdAndStatus(anyLong(), any(BookingStatus.class))).thenReturn(List.of());

        ItemDto result = itemService.getItemById(1L);
        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
    }

    @Test
    void addComment_whenValidData_thenReturnCommentDto() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная вещь");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));

        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(userService.getById(eq(1L))).thenReturn(user);

        when(bookingRepository.searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(
                eq(1L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(booking));

        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(1L, 1L, commentDto);

        assertNotNull(result);
        assertEquals("Хорошая вещь", result.getText());
    }

    @Test
    void addComment_whenUserDidNotBookItem_thenThrowValidationException() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Комментарий без бронирования");

        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(userService.getById(eq(1L))).thenReturn(user);

        when(bookingRepository.searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(
                eq(1L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Collections.emptyList());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(1L, 1L, commentDto));

        assertEquals("Ошибка при создании отзыва: передан запрос на создание отзыва при отсуствии бронирования вещи", exception.getMessage());
    }

    @Test
    void searchItems_whenTextNotEmpty_thenReturnItems() {
        when(itemRepository.searchAvailableItems("дрель")).thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("дрель");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void searchItems_whenTextEmpty_thenReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("");
        assertTrue(result.isEmpty());
    }

    private ItemCreateDto getItemCreateDto() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Отвёртка");
        dto.setDescription("Крестовая");
        dto.setAvailable(true);
        return dto;
    }

    @Test
    void addComment_whenNoApprovedBooking_thenThrowValidationException() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Комментарий без бронирования");

        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(userService.getById(eq(1L))).thenReturn(user);

        when(bookingRepository.searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(
                eq(1L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Collections.emptyList());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(1L, 1L, commentDto));

        assertEquals("Ошибка при создании отзыва: передан запрос на создание отзыва при отсуствии бронирования вещи", exception.getMessage());
    }
}