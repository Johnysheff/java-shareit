package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemCreateDto dto) {
        User owner = userService.getById(userId);
        if (owner == null) {
            throw new NotFoundException("пользователя не существует");
        }

        Item item = ItemMapper.toItem(dto);
        item.setOwner(owner);

        if (dto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        log.info("Создана вещь с ID: {}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemCreateDto dto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("только владелец может обновить вещь");
        }

        if (dto.getName() != null) {
            item.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto itemDto = ItemMapper.toItemDto(item);
        addBookingDates(itemDto, item.getId());

        List<Comment> comments = commentRepository.findByItemId(itemId);
        itemDto.setComments(comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));

        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, List<Booking>> bookingsMap = bookingRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Long, List<Comment>> commentsMap = commentRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream().map(item -> {
            ItemDto dto = ItemMapper.toItemDto(item);

            List<Booking> itemBookings = bookingsMap.getOrDefault(item.getId(), Collections.emptyList());
            itemBookings.forEach(booking -> {
            });

            addBookingDates(dto, item.getId());

            dto.setComments(commentsMap.getOrDefault(item.getId(), Collections.emptyList())
                    .stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList()));

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchAvailableItems(text.toLowerCase()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        User author = userService.getById(userId);
        if (author == null) {
            throw new NotFoundException("пользователя не существует");
        }
        List<Booking> bookings = bookingRepository.searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(userId, itemId, LocalDateTime.now(),
                BookingStatus.APPROVED);

        bookingRepository.searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(userId, itemId, LocalDateTime.now(),
                        BookingStatus.APPROVED).stream().filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .findAny().orElseThrow(() -> new ValidationException("Ошибка при создании отзыва: передан запрос на создание отзыва при отсуствии бронирования вещи"));

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        log.info("Добавлен комментарий с ID: {} к вещи с ID: {}", savedComment.getId(), itemId);

        return CommentMapper.toCommentDto(savedComment);
    }

    private void addBookingDates(ItemDto dto, Long itemId) {
        dto.setLastBooking(null);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByItemIdAndStatus(itemId, BookingStatus.APPROVED);

        dto.setNextBooking(bookings.stream()
                .filter(b -> b.getEnd().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(b -> new ItemDto.BookingShortDto(b.getId(), b.getBooker().getId()))
                .orElse(null));
    }

    private void addComments(ItemDto itemDto, Long itemId) {
        List<Comment> comments = commentRepository.findByItemId(itemId);
        itemDto.setComments(comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));
    }
}