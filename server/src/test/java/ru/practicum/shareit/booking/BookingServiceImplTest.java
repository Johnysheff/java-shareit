package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        booker = new User();
        booker.setId(1L);
        booker.setName("Booker");

        owner = new User();
        owner.setId(2L);
        owner.setName("Owner");

        item = new Item();
        item.setId(3L);
        item.setName("Item");
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(4L);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
    }

    @Test
    void getBookingById_whenBookingExistsAndUserIsBookerOrOwner_thenReturnBookingDto() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(1L, 4L);

        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals(1L, result.getBookerId());
        assertEquals(3L, result.getItemId());
        assertEquals(BookingStatus.WAITING.name(), result.getStatus());
    }

    @Test
    void getBookingById_whenUserIsNotBookerOrOwner_thenThrowNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(999L, 4L));

        assertEquals("Просматривать бронирование может только автор или владелец вещи", exception.getMessage());
    }

    @Test
    void approveBooking_whenValid_thenApprove() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        User owner = new User();
        owner.setId(2L);
        owner.setName("Test Owner");

        User booker = new User();
        booker.setId(1L);
        booker.setName("Test Booker");

        Item item = new Item();
        item.setId(3L);
        item.setName("Test Item");
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(4L);
        booking.setStart(now.minusDays(1));
        booking.setEnd(now.plusDays(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);


        when(bookingRepository.findById(eq(4L))).thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approveBooking(2L, 4L, true);

        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals(BookingStatus.APPROVED.name(), result.getStatus());
    }

    @Test
    void approveBooking_whenUserIsNotOwner_thenThrowForbiddenException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> bookingService.approveBooking(999L, 4L, true));

        assertEquals("Подтверждать бронирование может только владелец вещи", exception.getMessage());
    }

    @Test
    void getAllBookingsByUser_whenStateAll_thenReturnAllBookings() {
        when(userService.getById(anyLong())).thenReturn(booker);
        when(bookingRepository.findByBookerId(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByUser(1L, BookingState.ALL, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(4L, result.get(0).getId());
    }

    @Test
    void getAllBookingsByUser_whenStateCurrent_thenReturnCurrentBookings() {

        User booker = new User();
        booker.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setItem(new Item());
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);

        PageRequest page = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));

        when(userService.getById(anyLong())).thenReturn(booker);
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(page)
        )).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByUser(1L, BookingState.CURRENT, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getStart().isBefore(LocalDateTime.now()));
        assertTrue(result.get(0).getEnd().isAfter(LocalDateTime.now()));
    }

    @Test
    void getAllBookingsByUser_whenStatePast_thenReturnPastBookings() {
        booking.setEnd(LocalDateTime.now().minusDays(1));

        when(userService.getById(anyLong())).thenReturn(booker);
        when(bookingRepository.findByBookerIdAndEndIsBefore(eq(1L), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByUser(1L, BookingState.PAST, 0, 10);

        assertNotNull(result);
        assertTrue(result.get(0).getEnd().isBefore(LocalDateTime.now()));
    }

    @Test
    void getAllBookingsByUser_whenStateFuture_thenReturnFutureBookings() {
        booking.setStart(LocalDateTime.now().plusDays(1));

        when(userService.getById(anyLong())).thenReturn(booker);
        when(bookingRepository.findByBookerIdAndStartAfter(eq(1L), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByUser(1L, BookingState.FUTURE, 0, 10);

        assertNotNull(result);
        assertTrue(result.get(0).getStart().isAfter(LocalDateTime.now()));
    }

    @Test
    void getAllBookingsByUser_whenStateWaiting_thenReturnWaitingBookings() {
        booking.setStatus(BookingStatus.WAITING);

        when(userService.getById(anyLong())).thenReturn(booker);
        when(bookingRepository.findByBookerIdAndStatus(eq(1L), eq(BookingStatus.WAITING), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByUser(1L, BookingState.WAITING, 0, 10);

        assertNotNull(result);
        assertEquals(BookingStatus.WAITING.name(), result.get(0).getStatus());
    }

    @Test
    void getAllBookingsByUser_whenStateRejected_thenReturnRejectedBookings() {
        booking.setStatus(BookingStatus.REJECTED);

        when(userService.getById(anyLong())).thenReturn(booker);
        when(bookingRepository.findByBookerIdAndStatus(eq(1L), eq(BookingStatus.REJECTED), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByUser(1L, BookingState.REJECTED, 0, 10);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED.name(), result.get(0).getStatus());
    }

    @Test
    void getAllBookingsByOwner_whenStateAll_thenReturnAllBookings() {
        when(userService.getById(anyLong())).thenReturn(owner);
        when(bookingRepository.findByItemOwnerId(eq(2L), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByOwner(2L, BookingState.ALL, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(4L, result.get(0).getId());
    }

    @Test
    void getAllBookingsByOwner_whenStateCurrent_thenReturnCurrentBookings() {
        LocalDateTime now = LocalDateTime.now();
        User owner = new User();
        owner.setId(2L);
        owner.setName("Test Owner");

        User booker = new User();
        booker.setId(1L);
        booker.setName("Test Booker");

        Item item = new Item();
        item.setId(3L);
        item.setName("Test Item");
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(4L);
        booking.setStart(now.minusDays(1));
        booking.setEnd(now.plusDays(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);

        PageRequest page = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));

        when(userService.getById(anyLong())).thenReturn(owner);
        when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                eq(2L), any(LocalDateTime.class), any(LocalDateTime.class), eq(page)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByOwner(2L, BookingState.CURRENT, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(4L, result.get(0).getId());
        assertEquals(1L, result.get(0).getBookerId());
        assertEquals(3L, result.get(0).getItemId());
        assertEquals("APPROVED", result.get(0).getStatus());
    }

    @Test
    void getAllBookingsByOwner_whenStatePast_thenReturnPastBookings() {
        booking.setEnd(LocalDateTime.now().minusDays(1));

        when(userService.getById(anyLong())).thenReturn(owner);
        when(bookingRepository.findByItemOwnerIdAndEndIsBefore(eq(2L), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByOwner(2L, BookingState.PAST, 0, 10);

        assertNotNull(result);
        assertTrue(result.get(0).getEnd().isBefore(LocalDateTime.now()));
    }

    @Test
    void getAllBookingsByOwner_whenStateFuture_thenReturnFutureBookings() {
        booking.setStart(LocalDateTime.now().plusDays(1));

        when(userService.getById(anyLong())).thenReturn(owner);
        when(bookingRepository.findByItemOwnerIdAndStartAfter(eq(2L), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByOwner(2L, BookingState.FUTURE, 0, 10);

        assertNotNull(result);
        assertTrue(result.get(0).getStart().isAfter(LocalDateTime.now()));
    }

    @Test
    void getAllBookingsByOwner_whenStateWaiting_thenReturnWaitingBookings() {
        booking.setStatus(BookingStatus.WAITING);

        when(userService.getById(anyLong())).thenReturn(owner);
        when(bookingRepository.findByItemOwnerIdAndStatus(eq(2L), eq(BookingStatus.WAITING), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByOwner(2L, BookingState.WAITING, 0, 10);

        assertNotNull(result);
        assertEquals(BookingStatus.WAITING.name(), result.get(0).getStatus());
    }

    @Test
    void getAllBookingsByOwner_whenStateRejected_thenReturnRejectedBookings() {
        booking.setStatus(BookingStatus.REJECTED);

        when(userService.getById(anyLong())).thenReturn(owner);
        when(bookingRepository.findByItemOwnerIdAndStatus(eq(2L), eq(BookingStatus.REJECTED), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByOwner(2L, BookingState.REJECTED, 0, 10);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED.name(), result.get(0).getStatus());
    }

    @Test
    void createBooking_whenInvalidDates_thenThrowValidationException() {

        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1)); // end < start
        bookingDto.setItemId(1L);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userService.getById(anyLong())).thenReturn(booker);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(1L, bookingDto));

        assertEquals("Дата начала должна быть раньше даты окончания", exception.getMessage());
    }
}