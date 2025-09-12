package com.example.bankcards.dto.transfer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequestDTO {

    @NotNull(message = "From card id cannot be null")
    private Long fromCardId;

    @NotNull(message = "To card id cannot be null")
    private Long toCardId;

    @NotNull(message = "Amount cannot be blank")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
}
