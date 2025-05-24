package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestDto dto);

    List<ItemRequestDto> getAllRequestsByUserId(Long userId);

    ItemRequestDto getRequestById(Long requestId);

    List<ItemRequestDto> getRequestsFromUser(Long userId);
}