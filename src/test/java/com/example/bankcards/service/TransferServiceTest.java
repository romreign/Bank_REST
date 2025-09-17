package com.example.bankcards.service;

import com.example.bankcards.dto.transfer.TransferRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotBelongsToUserException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transfer_ShouldSuccessfullyTransferMoney() {
        User currentUser = new User();
        currentUser.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(currentUser);
        fromCard.setBalance(new BigDecimal("1000.00"));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(new BigDecimal("500.00"));

        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));

        when(securityContextService.getCurrentUser()).thenReturn(currentUser);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any())).thenAnswer(invocation -> {
            var transfer = invocation.getArgument(0);
            return transfer;
        });

        var response = transferService.transfer(request);

        assertNotNull(response);
        assertEquals(1L, response.getFromCardId());
        assertEquals(2L, response.getToCardId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("SUCCESS", response.getStatus());

        assertEquals(new BigDecimal("900.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("600.00"), toCard.getBalance());

        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transfer_ShouldThrowExceptionWhenFromCardNotFound() {
        User currentUser = new User();
        currentUser.setId(1L);

        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));

        when(securityContextService.getCurrentUser()).thenReturn(currentUser);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(CardNotBelongsToUserException.class, () -> {
            transferService.transfer(request);
        });
    }

    @Test
    void transfer_ShouldThrowExceptionWhenToCardNotFound() {
        User currentUser = new User();
        currentUser.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(currentUser);
        fromCard.setBalance(new BigDecimal("1000.00"));

        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));

        when(securityContextService.getCurrentUser()).thenReturn(currentUser);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> {
            transferService.transfer(request);
        });
    }

    @Test
    void transfer_ShouldThrowExceptionWhenInsufficientBalance() {
        User currentUser = new User();
        currentUser.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(currentUser);
        fromCard.setBalance(new BigDecimal("50.00"));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(new BigDecimal("500.00"));

        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));

        when(securityContextService.getCurrentUser()).thenReturn(currentUser);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(InsufficientBalanceException.class, () -> {
            transferService.transfer(request);
        });
    }
}