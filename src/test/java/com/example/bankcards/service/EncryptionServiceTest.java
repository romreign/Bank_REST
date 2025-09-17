package com.example.bankcards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EncryptionServiceTest {

    private final EncryptionService encryptionService = new EncryptionService();

    @Test
    public void encryptCardNumber_ShouldReturnBase64EncodedString() {
        String cardNumber = "1234567812345678";
        String expectedEncrypted = Base64.getEncoder().encodeToString(cardNumber.getBytes());

        String result = encryptionService.encryptCardNumber(cardNumber);

        assertNotNull(result);
        assertEquals(expectedEncrypted, result);

        byte[] decodedBytes = Base64.getDecoder().decode(result);
        String decodedString = new String(decodedBytes);
        assertEquals(cardNumber, decodedString);
    }

    @Test
    public void encryptCardNumber_ShouldHandleDifferentCardNumbers() {
        String cardNumber1 = "1234567812345678";
        String encrypted1 = encryptionService.encryptCardNumber(cardNumber1);
        assertEquals(cardNumber1, new String(Base64.getDecoder().decode(encrypted1)));

        String cardNumber2 = "8765432187654321";
        String encrypted2 = encryptionService.encryptCardNumber(cardNumber2);
        assertEquals(cardNumber2, new String(Base64.getDecoder().decode(encrypted2)));

        String cardNumber3 = "4111111111111111";
        String encrypted3 = encryptionService.encryptCardNumber(cardNumber3);
        assertEquals(cardNumber3, new String(Base64.getDecoder().decode(encrypted3)));
    }

    @Test
    public void decryptCardNumber_ShouldReturnOriginalCardNumber_WhenGivenEncryptedString() {
        String originalCardNumber = "1234567812345678";
        String encryptedNumber = Base64.getEncoder().encodeToString(originalCardNumber.getBytes());

        String result = encryptionService.decryptCardNumber(encryptedNumber);

        assertNotNull(result);
        assertEquals(originalCardNumber, result);
    }

    @Test
    public void decryptCardNumber_ShouldHandleDifferentEncryptedValues() {
        String cardNumber1 = "1234567812345678";
        String encrypted1 = Base64.getEncoder().encodeToString(cardNumber1.getBytes());
        assertEquals(cardNumber1, encryptionService.decryptCardNumber(encrypted1));

        String cardNumber2 = "5555555555554444";
        String encrypted2 = Base64.getEncoder().encodeToString(cardNumber2.getBytes());
        assertEquals(cardNumber2, encryptionService.decryptCardNumber(encrypted2));

        String cardNumber3 = "378282246310005";
        String encrypted3 = Base64.getEncoder().encodeToString(cardNumber3.getBytes());
        assertEquals(cardNumber3, encryptionService.decryptCardNumber(encrypted3));
    }

    @Test
    public void encryptDecrypt_ShouldBeReversible_ReturningOriginalValue() {
        String[] testCardNumbers = {
                "1234567812345678",
                "8765432187654321",
                "4111111111111111",
                "5555555555554444",
                "378282246310005",
                "6011111111111117"
        };

        for (String originalCardNumber : testCardNumbers) {
            String encrypted = encryptionService.encryptCardNumber(originalCardNumber);
            String decrypted = encryptionService.decryptCardNumber(encrypted);

            assertEquals(originalCardNumber, decrypted,
                    "Failed for card number: " + originalCardNumber);
        }
    }

    @Test
    public void decryptCardNumber_ShouldThrowException_WhenGivenInvalidBase64() {
        String invalidBase64 = "Not-a-valid-base64-string!";

        assertThrows(IllegalArgumentException.class,
                () -> encryptionService.decryptCardNumber(invalidBase64));
    }

    @Test
    public void encryptCardNumber_ShouldHandleEmptyString() {
        String emptyString = "";

        String encrypted = encryptionService.encryptCardNumber(emptyString);
        String decrypted = encryptionService.decryptCardNumber(encrypted);

        assertEquals(emptyString, decrypted);
    }

    @Test
    public void encryptCardNumber_ShouldHandleNullString() {
        assertThrows(NullPointerException.class, () -> encryptionService.encryptCardNumber(null));
    }

    @Test
    public void decryptCardNumber_ShouldHandleNullString() {
        String nullString = null;

        assertThrows(NullPointerException.class,
                () -> encryptionService.decryptCardNumber(nullString));
    }
}