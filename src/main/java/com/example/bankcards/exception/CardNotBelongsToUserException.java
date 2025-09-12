package com.example.bankcards.exception;

public class CardNotBelongsToUserException extends RuntimeException {

    public CardNotBelongsToUserException(String message) {
        super(message);
    }
}
