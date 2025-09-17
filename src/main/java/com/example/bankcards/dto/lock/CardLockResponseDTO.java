package com.example.bankcards.dto.lock;

import com.example.bankcards.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardLockResponseDTO {

    private Long id;

    private Long cardId;

    private String maskedCardNumber;

    private RequestStatus status;

    private String reason;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private String processedBy;
}

