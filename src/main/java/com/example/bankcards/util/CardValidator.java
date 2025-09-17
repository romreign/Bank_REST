package com.example.bankcards.util;

import java.time.LocalDate;

public class CardValidator {

    public static boolean isNotExpired(LocalDate expiryDate) {
        return expiryDate.isAfter(LocalDate.now());
    }

    public static boolean isValidCardNumber(String cardNumber) {
        String card = cardNumber.replaceAll("\\s+", "");
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--, alternate = !alternate) {
            int n = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9)
                    n -= 9;
            }
            sum += n;
        }
        return (sum % 10 == 0);
    }
}
