package com.example.bankcards.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardMaskerTest {

    @Test
    void maskCardNumber_ShouldMaskCardNumberCorrectly() {
        String cardNumber = "1234567890123456";

        String masked = CardMasker.maskCardNumber(cardNumber);

        assertEquals("**** **** **** 3456", masked);
    }
}