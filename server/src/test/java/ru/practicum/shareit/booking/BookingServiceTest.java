package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        booker = new User();
        booker.setId(1L);
        booker.setName("Иван Иванов");
        booker.setEmail("ivan@mail.ru");

        owner = new User();
        owner.setId(2L);
        owner.setName("Петр Петров");
        owner.setEmail("petr@mail.ru");

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная дрель");
        item.setAvailable(true);
        item.setOwner(owner);

        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(1L);
        bookingDto.setBookerId(1L);
    }

    @Test
    void createBooking_whenValidData_thenCreated() throws Exception {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userService.getById(anyLong())).thenReturn(booker);

        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setStart(bookingDto.getStart());
        savedBooking.setEnd(bookingDto.getEnd());
        savedBooking.setItem(item);
        savedBooking.setBooker(booker);
        savedBooking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingDto result = bookingService.createBooking(1L, bookingDto);

        assertNotNull(result);
        assertEquals(bookingDto.getItemId(), result.getItemId());
        assertEquals(BookingStatus.WAITING.name(), result.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_whenStartAfterEnd_thenThrowValidationException() {

        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userService.getById(anyLong())).thenReturn(booker);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(1L, bookingDto));
        assertEquals("Дата начала должна быть раньше даты окончания", exception.getMessage());
    }

    @Test
    void createBooking_whenSameStartAndEndDates_thenThrowValidationException() {

        LocalDateTime now = LocalDateTime.now();
        bookingDto.setStart(now);
        bookingDto.setEnd(now);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userService.getById(anyLong())).thenReturn(booker);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(10L, bookingDto));
        assertEquals("Даты не могут совпадать", exception.getMessage());
    }
}