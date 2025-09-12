package com.example.bankcards.dto.card;

import com.example.bankcards.entity.Card;
import com.example.bankcards.enums.CardStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.example.bankcards.util.CardMasker.maskCardNumber;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardResponseDTO {

    @NotNull(message = "Id cannot be null")
    private Long id;

    @NotNull(message = "User id cannot be null")
    private Long userId;

    @NotBlank(message = "Card number cannot be blank")
    private String maskedCardNumber;

    @NotBlank(message = "Owner name cannot be blank")
    private String ownerName;

    @NotNull(message = "Expiry date cannot be null")
    private LocalDate expiryDate;

    @NotNull(message = "Status cannot be null")
    private CardStatus status;

    @NotNull(message = "Balance cannot be null")
    private BigDecimal balance;

    public static CardResponseDTO fromEntity(Card card) {
        String patronymic = card.getUser().getPatronymic() == null ? "-" : card.getUser().getPatronymic();
        return CardResponseDTO.builder()
                .id(card.getId())
                .maskedCardNumber(maskCardNumber(card.getCardNumber()))
                .status(card.getStatus())
                .expiryDate(card.getExpiryDate())
                .balance(card.getBalance())
                .userId(card.getUser().getId())
                .ownerName(card.getUser().getName() + " " + patronymic + " " + card.getUser().getSurname().charAt(0))
                .build();
    }
}

