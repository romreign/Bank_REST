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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardLockRequestServiceTest {

    @Mock
    private CardLockRequestRepository cardLockRequestRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private CardLockRequestService cardLockRequestService;

    @Test
    void createLockRequest_ShouldReturnLockResponse_WhenCardBelongsToUser() {
        User user = createTestUser();
        Card card = createTestCard(user);
        CardLockRequestDTO requestDTO = new CardLockRequestDTO(card.getId(), "Lost card");

        when(securityContextService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByIdAndUserId(card.getId(), user.getId())).thenReturn(Optional.of(card));
        when(cardLockRequestRepository.save(any(CardLockRequest.class))).thenAnswer(invocation -> {
            CardLockRequest request = invocation.getArgument(0);
            request.setId(1L);
            return request;
        });

        CardLockResponseDTO result = cardLockRequestService.createLockRequest(requestDTO);

        assertNotNull(result);
        assertEquals(card.getId(), result.getCardId());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertEquals("Lost card", result.getReason());
        assertNotNull(result.getCreatedAt());
        assertNull(result.getProcessedAt());
        assertNull(result.getProcessedBy());

        verify(cardRepository).findByIdAndUserId(card.getId(), user.getId());
        verify(cardLockRequestRepository).save(any(CardLockRequest.class));
    }

    @Test
    void createLockRequest_ShouldThrowException_WhenCardDoesNotBelongToUser() {
        User user = createTestUser();
        CardLockRequestDTO requestDTO = new CardLockRequestDTO(999L, "Lost card");

        when(securityContextService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByIdAndUserId(999L, user.getId())).thenReturn(Optional.empty());

        assertThrows(CardNotBelongsToUserException.class, () -> {
            cardLockRequestService.createLockRequest(requestDTO);
        });

        verify(cardRepository).findByIdAndUserId(999L, user.getId());
        verify(cardLockRequestRepository, never()).save(any(CardLockRequest.class));
    }

    @Test
    void processLockRequest_ShouldBlockCard_WhenBlockIsTrue() {
        User adminUser = createTestAdminUser();
        User cardUser = createTestUser();
        Card card = createTestCard(cardUser);
        CardLockRequestDTO requestDTO = new CardLockRequestDTO(card.getId(), "Suspicious activity");

        when(securityContextService.getCurrentUser()).thenReturn(adminUser);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardLockRequestRepository.save(any(CardLockRequest.class))).thenAnswer(invocation -> {
            CardLockRequest request = invocation.getArgument(0);
            request.setId(1L);
            return request;
        });
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardResponseDTO result = cardLockRequestService.processLockRequest(card.getId(), requestDTO, true);

        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        assertEquals(card.getId(), result.getId());

        verify(cardRepository).findById(card.getId());
        verify(cardLockRequestRepository).save(any(CardLockRequest.class));
        verify(cardRepository).save(card);
    }

    @Test
    void processLockRequest_ShouldUnblockCard_WhenBlockIsFalse() {
        User adminUser = createTestAdminUser();
        User cardUser = createTestUser();
        Card card = createTestCard(cardUser);
        card.setStatus(CardStatus.BLOCKED);
        CardLockRequestDTO requestDTO = new CardLockRequestDTO(card.getId(), "False alarm");

        when(securityContextService.getCurrentUser()).thenReturn(adminUser);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardLockRequestRepository.save(any(CardLockRequest.class))).thenAnswer(invocation -> {
            CardLockRequest request = invocation.getArgument(0);
            request.setId(1L);
            return request;
        });
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardResponseDTO result = cardLockRequestService.processLockRequest(card.getId(), requestDTO, false);

        assertNotNull(result);
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertEquals(card.getId(), result.getId());

        verify(cardRepository).findById(card.getId());
        verify(cardLockRequestRepository).save(any(CardLockRequest.class));
        verify(cardRepository).save(card);
    }

    @Test
    void processLockRequest_ShouldThrowException_WhenCardNotFound() {
        User adminUser = createTestAdminUser();
        CardLockRequestDTO requestDTO = new CardLockRequestDTO(999L, "Suspicious activity");

        when(securityContextService.getCurrentUser()).thenReturn(adminUser);
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> {
            cardLockRequestService.processLockRequest(999L, requestDTO, true);
        });

        verify(cardRepository).findById(999L);
        verify(cardLockRequestRepository, never()).save(any(CardLockRequest.class));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void activateCard_ShouldActivateCard_WhenCardExists() {
        Card card = createTestCard(createTestUser());
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardResponseDTO result = cardLockRequestService.activateCard(card.getId());

        assertNotNull(result);
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertEquals(card.getId(), result.getId());

        verify(cardRepository).findById(card.getId());
        verify(cardRepository).save(card);
    }

    @Test
    void activateCard_ShouldThrowException_WhenCardNotFound() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> {
            cardLockRequestService.activateCard(999L);
        });

        verify(cardRepository).findById(999L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void convertToDTO_ShouldConvertEntityToDTO() {
        User user = createTestUser();
        User adminUser = createTestAdminUser();
        Card card = createTestCard(user);
        CardLockRequest request = new CardLockRequest();
        request.setId(1L);
        request.setCard(card);
        request.setUser(user);
        request.setProcessedBy(adminUser);
        request.setStatus(RequestStatus.APPROVED);
        request.setReason("Suspicious activity");
        request.setCreatedAt(LocalDateTime.now().minusHours(1));
        request.setProcessedAt(LocalDateTime.now());

        CardLockResponseDTO result = cardLockRequestService.convertToDTO(request);

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals(card.getId(), result.getCardId());
        assertEquals(RequestStatus.APPROVED, result.getStatus());
        assertEquals("Suspicious activity", result.getReason());
        assertEquals(request.getCreatedAt(), result.getCreatedAt());
        assertEquals(request.getProcessedAt(), result.getProcessedAt());
        assertEquals(adminUser.getLogin(), result.getProcessedBy());
        assertTrue(result.getMaskedCardNumber().contains("*"));
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setLogin("testuser");
        user.setName("John");
        user.setSurname("Doe");
        user.setPatronymic("Smith");
        return user;
    }

    private User createTestAdminUser() {
        User admin = new User();
        admin.setId(2L);
        admin.setLogin("admin");
        admin.setName("Admin");
        admin.setSurname("User");
        return admin;
    }

    private Card createTestCard(User user) {
        Card card = new Card();
        card.setId(1L);
        card.setUser(user);
        card.setCardNumber("1234567890123456");
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setBalance(BigDecimal.valueOf(1000.00));
        return card;
    }
}