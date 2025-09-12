package com.example.bankcards.service;

import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class EncryptionService {

    public String encryptCardNumber(String cardNumber) {
        return Base64.getEncoder().encodeToString(cardNumber.getBytes());
    }

    public String decryptCardNumber(String encryptedNumber) {
        return new String(Base64.getDecoder().decode(encryptedNumber));
    }
}
