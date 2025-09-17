package com.example.bankcards.service;

import com.example.bankcards.dto.transfer.TransferRequestDTO;
import com.example.bankcards.dto.transfer.TransferResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotBelongsToUserException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.security.SecurityContextService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;

    private final CardRepository cardRepository;

    private final SecurityContextService securityContextService;

    @Transactional
    public TransferResponseDTO transfer(TransferRequestDTO request) {
        User currentUser = securityContextService.getCurrentUser();

        Card fromCard = cardRepository.findByIdAndUserId(request.getFromCardId(), currentUser.getId())
                .orElseThrow(() -> new CardNotBelongsToUserException("Source card not found or does not belong to you"));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new CardNotFoundException("Target card not found"));

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0)
            throw new InsufficientBalanceException("Insufficient balance");

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transfer transfer = new Transfer(fromCard, toCard, request.getAmount(), LocalDateTime.now());
        Transfer savedTransfer = transferRepository.save(transfer);

        return TransferResponseDTO.builder()
                .id(savedTransfer.getId())
                .fromCardId(savedTransfer.getFromCard().getId())
                .toCardId(savedTransfer.getToCard().getId())
                .amount(savedTransfer.getAmount())
                .timestamp(savedTransfer.getTimestamp())
                .status("SUCCESS")
                .build();
    }
}
