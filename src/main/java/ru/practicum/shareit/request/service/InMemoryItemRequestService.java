package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InMemoryItemRequestService implements ItemRequestService {

    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private long nextId = 1L;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto dto) {
        ItemRequest req = new ItemRequest();
        req.setId(nextId++);
        req.setDescription(dto.getDescription());
        req.setRequestorId(userId);
        req.setCreated(LocalDateTime.now());
        requests.put(req.getId(), req);
        return new ItemRequestDto(
                req.getId(),
                req.getDescription(),
                req.getCreated(),
                req.getRequestorId()
        );
    }

    @Override
    public List<ItemRequestDto> getAllRequestsByUserId(Long userId) {
        return Collections.emptyList();
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId) {
        return null;
    }

    @Override
    public List<ItemRequestDto> getRequestsFromUser(Long userId) {
        return Collections.emptyList();
    }
}