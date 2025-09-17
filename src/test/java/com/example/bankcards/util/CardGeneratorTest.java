package com.example.bankcards.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import static org.junit.jupiter.api.Assertions.*;

class CardGeneratorTest {

    @Test
    void calculateLuhnCheckDigit_ShouldCalculateCorrectCheckDigit() {
        assertEquals(2, CardGenerator.calculateLuhnCheckDigit("123456789012345"));
        assertEquals(4, CardGenerator.calculateLuhnCheckDigit("545454545454545"));
        assertEquals(5, CardGenerator.calculateLuhnCheckDigit("37828224631000"));
    }

    @Test
    void isValidLuhn_ShouldReturnTrueForValidCardNumbers() {
        assertTrue(CardGenerator.isValidLuhn("1234567890123452"));
        assertTrue(CardGenerator.isValidLuhn("5454545454545454"));
        assertTrue(CardGenerator.isValidLuhn("378282246310005"));
        assertTrue(CardGenerator.isValidLuhn("6011111111111117"));
    }

    @Test
    void isValidLuhn_ShouldReturnFalseForInvalidCardNumbers() {
        assertFalse(CardGenerator.isValidLuhn("1234567890123456"));
        assertFalse(CardGenerator.isValidLuhn("123"));
        assertFalse(CardGenerator.isValidLuhn("abcdefghijklmnop"));
    }

    @RepeatedTest(20)
    void generateCardNumber_ShouldGenerateValidLuhnCardNumber() {
        String cardNumber = CardGenerator.generateCardNumber();

        assertNotNull(cardNumber);
        assertEquals(16, cardNumber.length(), "Card number should be 16 digits");
        assertTrue(CardGenerator.isValidLuhn(cardNumber),
                "Generated card number should pass Luhn check: " + cardNumber);

        assertTrue(cardNumber.matches("\\d+"), "Card number should contain only digits: " + cardNumber);
    }

    @Test
    void generateCardNumber_ShouldGenerateDifferentNumbers() {
        String cardNumber1 = CardGenerator.generateCardNumber();
        String cardNumber2 = CardGenerator.generateCardNumber();

        assertNotEquals(cardNumber1, cardNumber2,
                "Generated card numbers should be different");
    }
}