package com.example.bankcards.dto.lock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardLockRequestDTO {

    @NotNull(message = "Card id not be null")
    private Long cardId;

    @NotBlank(message = "Reason not be blank")
    private String reason;
}
