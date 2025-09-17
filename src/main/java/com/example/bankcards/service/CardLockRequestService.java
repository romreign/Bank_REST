package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.dto.lock.CardLockRequestDTO;
import com.example.bankcards.dto.lock.CardLockResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardLockRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.enums.RequestStatus;
import com.example.bankcards.exception.CardNotBelongsToUserException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardLockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.SecurityContextService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.example.bankcards.util.CardMasker.maskCardNumber;


@Service
@RequiredArgsConstructor
public class CardLockRequestService {

    private final CardLockRequestRepository cardLockRequestRepository;

    private final CardRepository cardRepository;

    private final SecurityContextService securityContextService;

    @Transactional
    public CardLockResponseDTO createLockRequest(CardLockRequestDTO request) {
        User currentUser = securityContextService.getCurrentUser();

        Card card = cardRepository.findByIdAndUserId(request.getCardId(), currentUser.getId())
                .orElseThrow(() -> new CardNotBelongsToUserException("Card not found or does not belong to the user"));

        CardLockRequest lockRequest = new CardLockRequest();
        lockRequest.setCard(card);
        lockRequest.setUser(currentUser);
        lockRequest.setStatus(RequestStatus.PENDING);
        lockRequest.setReason(request.getReason());
        lockRequest.setCreatedAt(LocalDateTime.now());

        CardLockRequest savedRequest = cardLockRequestRepository.save(lockRequest);

        return convertToDTO(savedRequest);
    }

    @Transactional
    public CardResponseDTO processLockRequest(Long cardId, CardLockRequestDTO request, boolean block) {
        User adminUser = securityContextService.getCurrentUser();

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        CardLockRequest lockRequest = new CardLockRequest();
        lockRequest.setCard(card);
        lockRequest.setUser(card.getUser());
        lockRequest.setProcessedBy(adminUser);
        lockRequest.setStatus(block ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        lockRequest.setReason(request.getReason());
        lockRequest.setCreatedAt(LocalDateTime.now());
        lockRequest.setProcessedAt(LocalDateTime.now());

        cardLockRequestRepository.save(lockRequest);

        card.setStatus(block ? CardStatus.BLOCKED : CardStatus.ACTIVE);
        Card updatedCard = cardRepository.save(card);

        return CardResponseDTO.fromEntity(updatedCard);
    }

    @Transactional
    public CardResponseDTO activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        card.setStatus(CardStatus.ACTIVE);
        Card updatedCard = cardRepository.save(card);

        return CardResponseDTO.fromEntity(updatedCard);
    }

    CardLockResponseDTO convertToDTO(CardLockRequest request) {
        return CardLockResponseDTO.builder()
                .id(request.getId())
                .cardId(request.getCard().getId())
                .maskedCardNumber(maskCardNumber(request.getCard().getCardNumber()))
                .status(request.getStatus())
                .reason(request.getReason())
                .createdAt(request.getCreatedAt())
                .processedAt(request.getProcessedAt())
                .processedBy(request.getProcessedBy() != null ?
                        request.getProcessedBy().getLogin() : null)
                .build();
    }

}
