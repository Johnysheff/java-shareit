package ru.practicum.shareit.request;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requester;

    @Column(nullable = false)
    private LocalDateTime created;

    public Long getRequestorId() {
        return requester != null ? requester.getId() : null;
    }

    public void setRequestorId(Long requestorId) {
        if (requestorId != null) {
            User user = new User();
            user.setId(requestorId);
            this.requester = user;
        }
    }
}