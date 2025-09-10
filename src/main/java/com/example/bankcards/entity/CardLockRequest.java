package com.example.bankcards.entity;

import com.example.bankcards.enums.RequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "card_lock_requests")
@NoArgsConstructor
@Getter
@Setter
public class CardLockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @NotNull(message = "User cannot be null")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Card cannot be null")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "card_id")
    private Card card;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    @Column(name = "reason")
    private String reason;

    @NotNull(message = "Created at cannot be null")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public CardLockRequest(User processedBy, User user, Card card, RequestStatus status, String reason,
                           LocalDateTime createdAt, LocalDateTime processedAt) {
        this.processedBy = processedBy;
        this.user = user;
        this.card = card;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }
}
