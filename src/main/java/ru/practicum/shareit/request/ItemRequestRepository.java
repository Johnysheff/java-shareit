package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    Optional<ItemRequest> findById(Long requestId);
}