package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardRequestDTO;
import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final UserRepository userRepository;

    private final CardRepository cardRepository;

    private final EncryptionService encryptionService;

    public List<CardResponseDTO> getCards() {
        List<Card> cards = cardRepository.findAll();
        return cards.stream()
                .map(CardResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<CardResponseDTO> getUserCards(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards = cardRepository.findByUserId(userId, pageable);
        return cards.map(CardResponseDTO::fromEntity);
    }

    public List<CardResponseDTO> getUserCards(long userId) {
        List<Card> cards = cardRepository.findAllByUserId(userId);
        return cards.stream()
                .map(CardResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CardResponseDTO createCard(long userId, CardRequestDTO cardRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String cardNumber = CardGenerator.generateCardNumber();
        String encryptedCardNumber = encryptionService.encryptCardNumber(cardNumber);

        Card newCard = new Card();
        newCard.setUser(user);
        newCard.setCardNumber(encryptedCardNumber);
        newCard.setStatus(CardStatus.NOT_ACTIVE);
        newCard.setExpiryDate(LocalDate.now().plusYears(10));
        newCard.setBalance(BigDecimal.ZERO);

        Card savedCard = cardRepository.save(newCard);
        return CardResponseDTO.fromEntity(savedCard);
    }

    @Transactional
    public CardResponseDTO addBalance(Long cardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        card.setBalance(card.getBalance().add(amount));
        Card updatedCard = cardRepository.save(card);
        return CardResponseDTO.fromEntity(updatedCard);
    }

    public BigDecimal getBalance(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        return card.getBalance();
    }

    @Transactional
    public void deleteCard(long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException("Card not found with id: " + cardId);
        }
        cardRepository.deleteById(cardId);
    }
}
