package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

public class Booking {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
    private Long bookerId;
    private Status status;

    public enum Status {
        WAITING, APPROVED, REJECTED, CANCELED
    }
}