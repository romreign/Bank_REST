package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardRequestDTO;
import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardGenerator;
import com.example.bankcards.util.CardMasker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @InjectMocks
    private CardService cardService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionService encryptionService;

    @Test
    public void getCards_ShouldReturnListOfAllCards() {
        Role userRole = new Role();
        userRole.setName("USER");

        User user1 = User.builder()
                        .role(userRole)
                        .login("ivanov")
                        .password("password123")
                        .surname("Иванов")
                        .name("Иван")
                        .patronymic("Иванович")
                        .birthday(LocalDate.of(1990, 5, 15))
                        .build();
        user1.setId(1L);

        User user2 = User.builder()
                        .role(userRole)
                        .login("petrov")
                        .password("password456")
                        .surname("Петров")
                        .name("Петр")
                        .patronymic(null)
                        .birthday(LocalDate.of(1985, 8, 20))
                        .build();
        user2.setId(2L);

        String cardNumber1 = "1234567812345678";
        String cardNumber2 = "8765432187654321";

        Card card1 = new Card(
                        user1,
                        cardNumber1,
                        CardStatus.ACTIVE,
                        LocalDate.of(2025, 12, 31),
                        new BigDecimal("1000.00"));
        card1.setId(1L);

        Card card2 = new Card(
                        user2,
                        cardNumber2,
                        CardStatus.BLOCKED,
                        LocalDate.of(2024, 6, 30),
                        new BigDecimal("500.50"));
        card2.setId(2L);

        List<Card> mockCards = List.of(card1, card2);

        when(cardRepository.findAll()).thenReturn(mockCards);

        List<CardResponseDTO> result = cardService.getCards();

        assertNotNull(result);
        assertEquals(2, result.size());

        CardResponseDTO firstCard = result.getFirst();
        assertEquals(1L, firstCard.getId());
        assertEquals(1L, firstCard.getUserId());
        assertEquals(CardStatus.ACTIVE, firstCard.getStatus());
        assertEquals(LocalDate.of(2025, 12, 31), firstCard.getExpiryDate());
        assertEquals(new BigDecimal("1000.00"), firstCard.getBalance());
        assertEquals(CardMasker.maskCardNumber(cardNumber1), firstCard.getMaskedCardNumber());
        assertEquals("Иван Иванович И", firstCard.getOwnerName());

        CardResponseDTO secondCard = result.get(1);
        assertEquals(2L, secondCard.getId());
        assertEquals(2L, secondCard.getUserId());
        assertEquals(CardStatus.BLOCKED, secondCard.getStatus());
        assertEquals(LocalDate.of(2024, 6, 30), secondCard.getExpiryDate());
        assertEquals(new BigDecimal("500.50"), secondCard.getBalance());
        assertEquals(CardMasker.maskCardNumber(cardNumber2), secondCard.getMaskedCardNumber());
        assertEquals("Петр - П", secondCard.getOwnerName());

        verify(cardRepository, times(1)).findAll();
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void getUserCards_ShouldReturnPagedCards_ForGivenUserId() {
        Long userId = 1L;
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        Role userRole = new Role();
        userRole.setName("USER");

        User user = User.builder()
                        .role(userRole)
                        .login("ivanov")
                        .password("password123")
                        .surname("Иванов")
                        .name("Иван")
                        .patronymic("Иванович")
                        .birthday(LocalDate.of(1990, 5, 15))
                        .build();
        user.setId(userId);

        String cardNumber1 = "1234567812345678";
        String cardNumber2 = "8765432187654321";

        Card card1 = new Card(
                        user,
                        cardNumber1,
                        CardStatus.ACTIVE,
                        LocalDate.of(2025, 12, 31),
                        new BigDecimal("1000.00"));
        card1.setId(1L);

        Card card2 = new Card(
                        user,
                        cardNumber2,
                        CardStatus.BLOCKED,
                        LocalDate.of(2024, 6, 30),
                        new BigDecimal("500.50"));
        card2.setId(2L);

        List<Card> userCards = List.of(card1, card2);
        Page<Card> pagedCards = new PageImpl<>(userCards, pageable, userCards.size());

        when(cardRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(pagedCards);

        Page<CardResponseDTO> result = cardService.getUserCards(userId, page, size);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(pageable, result.getPageable());

        CardResponseDTO firstCard = result.getContent().getFirst();
        assertEquals(1L, firstCard.getId());
        assertEquals(userId, firstCard.getUserId());
        assertEquals(CardStatus.ACTIVE, firstCard.getStatus());
        assertEquals(LocalDate.of(2025, 12, 31), firstCard.getExpiryDate());
        assertEquals(new BigDecimal("1000.00"), firstCard.getBalance());
        assertEquals(CardMasker.maskCardNumber(cardNumber1), firstCard.getMaskedCardNumber());
        assertEquals("Иван Иванович И", firstCard.getOwnerName());

        CardResponseDTO secondCard = result.getContent().get(1);
        assertEquals(2L, secondCard.getId());
        assertEquals(userId, secondCard.getUserId());
        assertEquals(CardStatus.BLOCKED, secondCard.getStatus());
        assertEquals(LocalDate.of(2024, 6, 30), secondCard.getExpiryDate());
        assertEquals(new BigDecimal("500.50"), secondCard.getBalance());
        assertEquals(CardMasker.maskCardNumber(cardNumber2), secondCard.getMaskedCardNumber());
        assertEquals("Иван Иванович И", secondCard.getOwnerName());

        verify(cardRepository, times(1)).findByUserId(eq(userId), any(Pageable.class));
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void getUserCards_ShouldReturnListOfCards_ForGivenUserId() {
        Long userId = 1L;

        Role userRole = new Role();
        userRole.setName("USER");

        User user = User.builder()
                        .role(userRole)
                        .login("ivanov")
                        .password("password123")
                        .surname("Иванов")
                        .name("Иван")
                        .patronymic("Иванович")
                        .birthday(LocalDate.of(1990, 5, 15))
                        .build();
        user.setId(userId);

        String cardNumber1 = "1234567812345678";
        String cardNumber2 = "8765432187654321";
        String cardNumber3 = "1111222233334444";

        Card card1 = new Card(
                        user,
                        cardNumber1,
                        CardStatus.ACTIVE,
                        LocalDate.of(2025, 12, 31),
                        new BigDecimal("1000.00"));
        card1.setId(1L);

        Card card2 = new Card(
                        user,
                        cardNumber2,
                        CardStatus.BLOCKED,
                        LocalDate.of(2024, 6, 30),
                        new BigDecimal("500.50"));
        card2.setId(2L);

        Card card3 = new Card(
                        user,
                        cardNumber3,
                        CardStatus.EXPIRED,
                        LocalDate.of(2023, 1, 15),
                        new BigDecimal("250.75"));
        card3.setId(3L);

        List<Card> userCards = List.of(card1, card2, card3);

        when(cardRepository.findAllByUserId(eq(userId))).thenReturn(userCards);

        List<CardResponseDTO> result = cardService.getUserCards(userId);

        assertNotNull(result);
        assertEquals(3, result.size());

        CardResponseDTO firstCard = result.getFirst();
        assertEquals(1L, firstCard.getId());
        assertEquals(userId, firstCard.getUserId());
        assertEquals(CardStatus.ACTIVE, firstCard.getStatus());
        assertEquals(LocalDate.of(2025, 12, 31), firstCard.getExpiryDate());
        assertEquals(new BigDecimal("1000.00"), firstCard.getBalance());
        assertEquals(CardMasker.maskCardNumber(cardNumber1), firstCard.getMaskedCardNumber());
        assertEquals("Иван Иванович И", firstCard.getOwnerName());

        CardResponseDTO secondCard = result.get(1);
        assertEquals(2L, secondCard.getId());
        assertEquals(userId, secondCard.getUserId());
        assertEquals(CardStatus.BLOCKED, secondCard.getStatus());
        assertEquals(LocalDate.of(2024, 6, 30), secondCard.getExpiryDate());
        assertEquals(new BigDecimal("500.50"), secondCard.getBalance());
        assertEquals(CardMasker.maskCardNumber(cardNumber2), secondCard.getMaskedCardNumber());
        assertEquals("Иван Иванович И", secondCard.getOwnerName());

        CardResponseDTO thirdCard = result.get(2);
        assertEquals(3L, thirdCard.getId());
        assertEquals(userId, thirdCard.getUserId());
        assertEquals(CardStatus.EXPIRED, thirdCard.getStatus());
        assertEquals(LocalDate.of(2023, 1, 15), thirdCard.getExpiryDate());
        assertEquals(new BigDecimal("250.75"), thirdCard.getBalance());
        assertEquals(CardMasker.maskCardNumber(cardNumber3), thirdCard.getMaskedCardNumber());
        assertEquals("Иван Иванович И", thirdCard.getOwnerName());

        verify(cardRepository, times(1)).findAllByUserId(eq(userId));
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void getUserCards_ShouldReturnEmptyList_WhenUserHasNoCards() {
        Long userId = 2L;
        List<Card> emptyCards = List.of();

        when(cardRepository.findAllByUserId(eq(userId))).thenReturn(emptyCards);

        List<CardResponseDTO> result = cardService.getUserCards(userId);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(cardRepository, times(1)).findAllByUserId(eq(userId));
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void createCard_ShouldCreateNewCardWithEncryptedNumberAndReturnCardResponse() {
        Long userId = 1L;
        CardRequestDTO cardRequest = CardRequestDTO.builder().userId(userId).build();

        Role userRole = new Role();
        userRole.setName("USER");

        User user = User.builder()
                .role(userRole)
                .login("ivanov")
                .password("password123")
                .surname("Иванов")
                .name("Иван")
                .patronymic("Иванович")
                .birthday(LocalDate.of(1990, 5, 15))
                .build();
        user.setId(userId);

        String generatedCardNumber = "1234567812345678";
        LocalDate expiryDate = LocalDate.now().plusYears(3);
        BigDecimal initialBalance = new BigDecimal("0.00");

        Card savedCard = new Card(user, generatedCardNumber, CardStatus.ACTIVE, expiryDate, initialBalance);
        savedCard.setId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardResponseDTO result;
        try (var mockedGenerator = mockStatic(CardGenerator.class)) {
            mockedGenerator.when(CardGenerator::generateCardNumber).thenReturn(generatedCardNumber);

            result = cardService.createCard(userId, cardRequest);
        }

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(expiryDate, result.getExpiryDate());
        assertEquals(initialBalance, result.getBalance());
        assertEquals(CardMasker.maskCardNumber(generatedCardNumber), result.getMaskedCardNumber());
        assertEquals("Иван Иванович И", result.getOwnerName());

        verify(userRepository, times(1)).findById(userId);
        verify(cardRepository, times(1)).save(any(Card.class));
        verifyNoMoreInteractions(userRepository, cardRepository);
    }

    @Test
    public void createCard_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        Long userId = 999L;
        CardRequestDTO cardRequest = CardRequestDTO.builder().userId(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> cardService.createCard(userId, cardRequest)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(encryptionService);
        verifyNoInteractions(cardRepository);
    }

    @Test
    public void addBalance_ShouldIncreaseCardBalanceAndReturnUpdatedCard() {
        Long cardId = 1L;
        Long userId = 1L;
        BigDecimal amountToAdd = new BigDecimal("500.00");
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal expectedBalance = new BigDecimal("1500.00");

        Role userRole = new Role();
        userRole.setName("USER");

        User user = User.builder()
                .role(userRole)
                .login("ivanov")
                .password("password123")
                .surname("Иванов")
                .name("Иван")
                .patronymic("Иванович")
                .birthday(LocalDate.of(1990, 5, 15))
                .build();
        user.setId(userId);

        String cardNumber = "1234567812345678";

        Card existingCard = new Card(user, cardNumber, CardStatus.ACTIVE,
                LocalDate.of(2025, 12, 31), initialBalance);
        existingCard.setId(cardId);

        Card updatedCard = new Card(user, cardNumber, CardStatus.ACTIVE,
                LocalDate.of(2025, 12, 31), expectedBalance);
        updatedCard.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);

        CardResponseDTO result = cardService.addBalance(cardId, amountToAdd);

        assertNotNull(result);
        assertEquals(cardId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(LocalDate.of(2025, 12, 31), result.getExpiryDate());
        assertEquals(expectedBalance, result.getBalance());
        assertEquals(CardMasker.maskCardNumber(cardNumber), result.getMaskedCardNumber());
        assertEquals("Иван Иванович И", result.getOwnerName());

        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).save(any(Card.class));
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void addBalance_ShouldUseArgumentCaptorToVerifyBalanceUpdate() {
        Long cardId = 1L;
        Long userId = 1L;
        BigDecimal amountToAdd = new BigDecimal("300.50");
        BigDecimal initialBalance = new BigDecimal("200.00");
        BigDecimal expectedBalance = new BigDecimal("500.50");

        Role userRole = new Role();
        userRole.setName("USER");

        User user = User.builder()
                        .role(userRole)
                        .login("ivanov")
                        .password("password123")
                        .surname("Иванов")
                        .name("Иван")
                        .patronymic("Иванович")
                        .birthday(LocalDate.of(1990, 5, 15))
                        .build();
        user.setId(userId);

        String cardNumber = "1234567812345678";

        Card existingCard = new Card(user, cardNumber, CardStatus.ACTIVE,
                LocalDate.of(2025, 12, 31), initialBalance);
        existingCard.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation ->  invocation.<Card>getArgument(0));

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);

        cardService.addBalance(cardId, amountToAdd);

        verify(cardRepository).save(cardCaptor.capture());
        Card capturedCard = cardCaptor.getValue();

        assertEquals(expectedBalance, capturedCard.getBalance());
        assertEquals(user, capturedCard.getUser());
        assertEquals(cardNumber, capturedCard.getCardNumber());
        assertEquals(CardStatus.ACTIVE, capturedCard.getStatus());

        verify(cardRepository, times(1)).findById(cardId);
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void addBalance_ShouldHandleZeroAmount() {
        Long cardId = 1L;
        Long userId = 1L;
        BigDecimal amountToAdd = BigDecimal.ZERO;
        BigDecimal initialBalance = new BigDecimal("1000.00");

        Role userRole = new Role();
        userRole.setName("USER");

        User user = User.builder()
                        .role(userRole)
                        .login("ivanov")
                        .password("password123")
                        .surname("Иванов")
                        .name("Иван")
                        .patronymic("Иванович")
                        .birthday(LocalDate.of(1990, 5, 15))
                        .build();
        user.setId(userId);

        String cardNumber = "1234567812345678";

        Card existingCard = new Card(user, cardNumber, CardStatus.ACTIVE,
                LocalDate.of(2025, 12, 31), initialBalance);
        existingCard.setId(cardId);

        assertThrows(IllegalArgumentException.class, () -> cardService.addBalance(userId, amountToAdd));
    }

    @Test
    public void addBalance_ShouldThrowCardNotFoundException_WhenCardNotFound() {
        Long cardId = 999L;
        BigDecimal amountToAdd = new BigDecimal("500.00");

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> cardService.addBalance(cardId, amountToAdd)
        );

        assertEquals("Card not found", exception.getMessage());

        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, never()).save(any());
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void getBalance_ShouldReturnCurrentCardBalance() {
        Long cardId = 1L;
        Long userId = 1L;
        BigDecimal expectedBalance = new BigDecimal("1234.56");

        Role userRole = new Role();
        userRole.setName("USER");

        User user = User.builder()
                        .role(userRole)
                        .login("ivanov")
                        .password("password123")
                        .surname("Иванов")
                        .name("Иван")
                        .patronymic("Иванович")
                        .birthday(LocalDate.of(1990, 5, 15))
                        .build();
        user.setId(userId);

        String cardNumber = "1234567812345678";

        Card card = new Card(user, cardNumber, CardStatus.ACTIVE,
                LocalDate.of(2025, 12, 31), expectedBalance);
        card.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getBalance(cardId);

        assertNotNull(result);
        assertEquals(expectedBalance, result);
        assertEquals(0, expectedBalance.compareTo(result));

        verify(cardRepository, times(1)).findById(cardId);
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void getBalance_ShouldReturnZeroBalance_ForNewCard() {
        Long cardId = 2L;
        Long userId = 1L;
        BigDecimal zeroBalance = BigDecimal.ZERO;

        Role userRole = new Role();
        userRole.setName("USER");

        User user = User.builder()
                        .role(userRole)
                        .login("ivanov")
                        .password("password123")
                        .surname("Иванов")
                        .name("Иван")
                        .patronymic("Иванович")
                        .birthday(LocalDate.of(1990, 5, 15))
                        .build();
        user.setId(userId);

        String cardNumber = "8765432187654321";

        Card card = new Card(user, cardNumber, CardStatus.ACTIVE,
                LocalDate.of(2025, 12, 31), zeroBalance);
        card.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getBalance(cardId);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result));

        verify(cardRepository, times(1)).findById(cardId);
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void getBalance_ShouldReturnNegativeBalance_ForOverdraft() {
        Long cardId = 3L;
        Long userId = 1L;
        BigDecimal negativeBalance = new BigDecimal("-500.00");

        Role userRole = new Role();
        userRole.setName("USER");

        User user =
                User.builder()
                        .role(userRole)
                        .login("ivanov")
                        .password("password123")
                        .surname("Иванов")
                        .name("Иван")
                        .patronymic("Иванович")
                        .birthday(LocalDate.of(1990, 5, 15))
                        .build();
        user.setId(userId);

        String cardNumber = "1111222233334444";

        Card card = new Card(user, cardNumber, CardStatus.ACTIVE,
                        LocalDate.of(2025, 12, 31), negativeBalance);
        card.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getBalance(cardId);

        assertNotNull(result);
        assertEquals(negativeBalance, result);
        assertEquals(-1, result.compareTo(BigDecimal.ZERO));

        verify(cardRepository, times(1)).findById(cardId);
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void getBalance_ShouldThrowCardNotFoundException_WhenCardNotFound() {
        Long cardId = 999L;

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getBalance(cardId));

        verify(cardRepository, times(1)).findById(cardId);
        verifyNoMoreInteractions(cardRepository);
    }

    @Test
    public void deleteCard_ShouldDeleteCard_WhenCardExists() {
        Long cardId = 1L;
        Long userId = 1L;

        Role userRole = new Role();
        userRole.setName("USER");

        User user =
                User.builder()
                        .role(userRole)
                        .login("ivanov")
                        .password("password123")
                        .surname("Иванов")
                        .name("Иван")
                        .patronymic("Иванович")
                        .birthday(LocalDate.of(1990, 5, 15))
                        .build();
        user.setId(userId);

        String cardNumber = "1234567812345678";
        BigDecimal balance = new BigDecimal("1000.00");

        Card card = new Card(user, cardNumber, CardStatus.ACTIVE,
                        LocalDate.of(2025, 12, 31), balance);
        card.setId(cardId);

        when(cardRepository.existsById(cardId)).thenReturn(true);

        assertDoesNotThrow(() -> cardService.deleteCard(cardId));

        verify(cardRepository, times(1)).existsById(cardId);

    }

    @Test
    public void deleteCard_ShouldThrowCardNotFoundException_WhenCardNotFound() {
        Long cardId = 1L;

        when(cardRepository.existsById(cardId)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(cardId));

        verify(cardRepository, times(1)).existsById(cardId);
        verify(cardRepository, never()).deleteById(cardId);
    }

}
