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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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

    private User user;
    private Item item;
    private Booking booking;
    private Comment comment;
    private ItemCreateDto itemCreateDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Electric drill");
        item.setAvailable(true);
        item.setOwner(user);

        booking = new Booking();
        booking.setId(2L);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.APPROVED);

        comment = new Comment();
        comment.setId(3L);
        comment.setText("Great tool!");
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Screwdriver");
        itemCreateDto.setDescription("Flathead screwdriver");
        itemCreateDto.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setText("Good item");
    }

    @Test
    void createItem_whenValidData_thenReturnItemDto() {
        when(userService.getById(anyLong())).thenReturn(user);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(1L, itemCreateDto);

        assertNotNull(result);
        assertEquals("Drill", result.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_whenUserNotFound_thenThrowNotFoundException() {
        when(userService.getById(anyLong())).thenThrow(new NotFoundException("пользователя не существует"));

        assertThrows(NotFoundException.class, () -> itemService.createItem(999L, itemCreateDto));
    }

    @Test
    void createItem_whenRequestExists_thenReturnItemWithRequestId() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Rented Item");
        dto.setDescription("For rent");
        dto.setAvailable(true);
        dto.setRequestId(100L);

        ItemRequest request = new ItemRequest();
        request.setId(100L);

        Item savedItem = new Item();
        savedItem.setId(200L);
        savedItem.setRequest(request);

        when(userService.getById(anyLong())).thenReturn(user);
        when(itemRequestRepository.findById(eq(100L))).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.createItem(1L, dto);

        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertNotNull(result.getRequestId());
        assertEquals(100L, result.getRequestId());
    }

    @Test
    void createItem_whenRequestIsNull_thenReturnItemWithoutRequestId() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Test Item");
        dto.setDescription("Description");

        when(userService.getById(anyLong())).thenReturn(user);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item savedItem = invocation.getArgument(0);
            savedItem.setId(100L);
            return savedItem;
        });

        ItemDto result = itemService.createItem(1L, dto);

        assertNotNull(result);
        assertNull(result.getRequestId());
    }

    @Test
    void updateItem_whenValidUpdates_thenReturnUpdatedItem() {
        ItemCreateDto updates = new ItemCreateDto();
        updates.setName("Updated Name");
        updates.setDescription("Updated Description");
        updates.setAvailable(false);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.updateItem(1L, 1L, updates);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateItem_whenUserIsNotOwner_thenThrowNotFoundException() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Updated Drill");

        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(2L, 1L, dto));
        assertEquals("только владелец может обновить вещь", exception.getMessage());
    }

    @Test
    void updateItem_whenItemNotFound_thenThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(1L, 1L, itemCreateDto));

        assertEquals("вещь не найдена", exception.getMessage());
    }

    @Test
    void getItemById_whenItemExists_thenReturnItemWithBookingsAndComments() {
        LocalDateTime now = LocalDateTime.now();

        Booking activeBooking = new Booking();
        activeBooking.setId(1L);
        activeBooking.setItem(item);
        activeBooking.setBooker(user);
        activeBooking.setStatus(BookingStatus.APPROVED);
        activeBooking.setStart(now.minusDays(1));
        activeBooking.setEnd(now.plusDays(1));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemIdAndStatus(anyLong(), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(activeBooking));
        when(commentRepository.findByItemId(anyLong())).thenReturn(List.of(comment));

        ItemDto result = itemService.getItemById(1L);

        assertNotNull(result);
        assertNotNull(result.getNextBooking());
        assertEquals(1, result.getComments().size());
    }

    @Test
    void getItemById_whenItemDoesNotExist_thenThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(1L));

        assertEquals("Вещь не найдена", exception.getMessage());
    }

    @Test
    void getAllItemsByOwnerId_whenOwnerHasItems_thenReturnItemList() {
        List<Item> items = List.of(item);
        Map<Long, List<Booking>> bookingsMap = Collections.singletonMap(item.getId(), List.of(booking));
        Map<Long, List<Comment>> commentsMap = Collections.singletonMap(item.getId(), List.of(comment));

        when(itemRepository.findByOwnerId(anyLong())).thenReturn(items);
        when(bookingRepository.findByItemIdIn(anyList())).thenReturn(List.of(booking));
        when(commentRepository.findByItemIdIn(anyList())).thenReturn(List.of(comment));

        List<ItemDto> result = itemService.getAllItemsByOwnerId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }

    @Test
    void getAllItemsByOwnerId_whenNoItems_thenReturnEmptyList() {
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.getAllItemsByOwnerId(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_whenTextNotEmpty_thenReturnMatchingItems() {
        when(itemRepository.searchAvailableItems(anyString())).thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("drill");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }

    @Test
    void searchItems_whenTextIsEmpty_returnEmptyList() {
        List<ItemDto> result = itemService.searchItems("");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_whenCaseInsensitiveMatch_thenReturnItems() {
        when(itemRepository.searchAvailableItems(anyString())).thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("DRILL");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }

    @Test
    void addComment_whenValidData_thenReturnCommentDto() {
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));
        when(userService.getById(eq(1L))).thenReturn(user);
        when(bookingRepository.searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(
                eq(1L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(1L, 1L, commentDto);

        assertNotNull(result);
        assertEquals("Great tool!", result.getText());
    }

    @Test
    void addComment_whenTextIsEmpty_thenThrowValidationException() {

        CommentDto commentDto = new CommentDto();
        commentDto.setText(""); // Пустой текст

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(1L, 1L, commentDto));
        assertEquals("Текст комментария не может быть пустым", exception.getMessage());
    }

    @Test
    void addComment_whenUserNeverBookedItem_thenThrowValidationException() {
        commentDto.setText("Хорошая вещь");

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
    void addComment_whenNoBookings_thenThrowValidationException() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Хорошая вещь");

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