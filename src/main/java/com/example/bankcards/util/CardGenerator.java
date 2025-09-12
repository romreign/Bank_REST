package com.example.bankcards.util;

import java.security.SecureRandom;

public class CardGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateCardNumber() {
        // Генерируем 15 цифр (без контрольной цифры)
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            cardNumber.append(secureRandom.nextInt(10));
        }

        // Вычисляем контрольную цифру по алгоритму Луна
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    public static int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean doubleDigit = true; // Начинаем удваивать с предпоследней цифры

        // Идем справа налево по цифрам
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit - 9; // Эквивалентно digit = digit % 10 + digit / 10
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit; // Чередуем: удваиваем, не удваиваем, удваиваем...
        }

        // Контрольная цифра такая, чтобы сумма стала кратной 10
        return (10 - (sum % 10)) % 10;
    }

    public static boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean doubleDigit = false; // Для полного номера начинаем с последней цифры (не удваиваем)

        // Идем справа налево по всем цифрам (включая контрольную)
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