package com.example.bankcards.util;

public class CardMasker {

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber.length() != 16)
            throw new RuntimeException("Card number length < 16");
        return "**** **** **** " +  cardNumber.substring(cardNumber.length() - 4);
    }
}
