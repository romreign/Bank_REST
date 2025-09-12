package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardRequestDTO {

    @NotNull(message = "User id cannot be null")
    private Long userId;

    @NotNull(message = "Card id cannot be null")
    private Long cardId;

}
