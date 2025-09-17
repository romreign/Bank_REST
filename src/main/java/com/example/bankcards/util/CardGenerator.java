package com.example.bankcards.util;

import java.security.SecureRandom;

public class CardGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 15; i++)
            cardNumber.append(secureRandom.nextInt(10));

        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    public static int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean doubleDigit = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return (10 - (sum % 10)) % 10;
    }

    public static boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean doubleDigit = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return (sum % 10 == 0);
    }
}