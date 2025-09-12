package com.example.bankcards.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponseDTO {

    private Long id;

    private Long fromCardId;

    private Long toCardId;

    private BigDecimal amount;

    private LocalDateTime timestamp;

    private String status;
}
