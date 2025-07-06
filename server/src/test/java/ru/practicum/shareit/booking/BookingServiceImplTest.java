package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
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

    @Test
    void createBooking_shouldCreateBooking() {
        User booker = new User(2L, "Booker", "booker@mail.ru");
        Item item = new Item(1L, "Дрель", "Простая дрель", true,
                new User(1L, "Owner", "owner@mail.ru"), null);

        when(userService.getById(anyLong())).thenReturn(booker);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto result = bookingService.createBooking(2L, bookingDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookingRepository).save(any());
    }

    @Test
    void createBooking_whenItemNotAvailable_shouldThrowException() {
        User booker = new User(2L, "Booker", "booker@mail.ru");
        Item item = new Item(1L, "Дрель", "Простая дрель", false,
                new User(1L, "Owner", "owner@mail.ru"), null);

        when(userService.getById(anyLong())).thenReturn(booker);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(2L, bookingDto));
    }

    @Test
    void approveBooking_shouldApproveBooking() {
        User owner = new User(1L, "Owner", "owner@mail.ru");
        Item item = new Item(1L, "Дрель", "Простая дрель", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                item, new User(2L, "Booker", "booker@mail.ru"), BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto result = bookingService.approveBooking(1L, 1L, true);

        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void getBookingById_shouldReturnBooking() {
        User owner = new User(1L, "Owner", "owner@mail.ru");
        User booker = new User(2L, "Booker", "booker@mail.ru");
        Item item = new Item(1L, "Дрель", "Простая дрель", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                item, booker, BookingStatus.APPROVED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(1L, 1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getAllBookingsByUser_shouldReturnBookings() {
        User user = new User(1L, "User", "user@mail.ru");
        User owner = new User(2L, "Owner", "owner@mail.ru");
        Item item = new Item(1L, "Дрель", "Простая дрель", true, owner, null);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.APPROVED);

        when(userService.getById(anyLong())).thenReturn(user);
        when(bookingRepository.findByBookerId(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingDto> result = bookingService.getAllBookingsByUser(1L, BookingState.ALL, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}