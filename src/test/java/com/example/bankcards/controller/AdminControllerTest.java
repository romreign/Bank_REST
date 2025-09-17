package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardRequestDTO;
import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.dto.lock.CardLockRequestDTO;
import com.example.bankcards.dto.user.UserDTO;
import com.example.bankcards.dto.user.UserUpdateDTO;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.service.CardLockRequestService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private CardService cardService;

    @Mock
    private CardLockRequestService cardLockRequestService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void getUsers_ShouldReturnListOfUsers() {
        UserDTO userDTO = createUserDTO();
        when(userService.getUsers()).thenReturn(List.of(userDTO));

        ResponseEntity<List<UserDTO>> response = adminController.getUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(userDTO, response.getBody().getFirst());
        verify(userService).getUsers();
    }

    @Test
    void getUser_ShouldReturnUser() {
        UserDTO userDTO = createUserDTO();
        when(userService.getUser(1L)).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = adminController.getUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDTO, response.getBody());
        verify(userService).getUser(1L);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        UserUpdateDTO updateDTO = new UserUpdateDTO("Doe", "John");
        UserDTO userDTO = createUserDTO();
        when(userService.updateUser(eq(1L), any(UserUpdateDTO.class))).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = adminController.updateUser(1L, updateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDTO, response.getBody());
        verify(userService).updateUser(eq(1L), any(UserUpdateDTO.class));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = adminController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).deleteUser(1L);
    }

    @Test
    void getCards_ShouldReturnListOfCards() {
        CardResponseDTO cardDTO = createCardResponseDTO();
        when(cardService.getCards()).thenReturn(List.of(cardDTO));

        ResponseEntity<List<CardResponseDTO>> response = adminController.getCards();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(cardDTO, response.getBody().getFirst());
        verify(cardService).getCards();
    }

    @Test
    void getUserCards_ShouldReturnUserCards() {
        CardResponseDTO cardDTO = createCardResponseDTO();
        when(cardService.getUserCards(1L)).thenReturn(List.of(cardDTO));

        ResponseEntity<List<CardResponseDTO>> response = adminController.getUserCards(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(cardDTO, response.getBody().getFirst());
        verify(cardService).getUserCards(1L);
    }

    @Test
    void createCard_ShouldReturnCreatedCard() {
        CardRequestDTO requestDTO = new CardRequestDTO(1L, 1L);
        CardResponseDTO cardDTO = createCardResponseDTO();
        when(cardService.createCard(eq(1L), any(CardRequestDTO.class))).thenReturn(cardDTO);

        ResponseEntity<CardResponseDTO> response = adminController.createCard(1L, requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cardDTO, response.getBody());
        verify(cardService).createCard(eq(1L), any(CardRequestDTO.class));
    }

    @Test
    void blockCard_ShouldReturnBlockedCard() {
        CardLockRequestDTO requestDTO = new CardLockRequestDTO(1L, "Suspicious activity");
        CardResponseDTO cardDTO = createCardResponseDTO();
        cardDTO.setStatus(CardStatus.BLOCKED);
        when(cardLockRequestService.processLockRequest(eq(1L), any(CardLockRequestDTO.class),
                eq(true))).thenReturn(cardDTO);

        ResponseEntity<CardResponseDTO> response = adminController.blockCard(1L, requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CardStatus.BLOCKED, response.getBody().getStatus());
        verify(cardLockRequestService).processLockRequest(eq(1L), any(CardLockRequestDTO.class), eq(true));
    }

    @Test
    void activateCard_ShouldReturnActivatedCard() {
        CardResponseDTO cardDTO = createCardResponseDTO();
        when(cardLockRequestService.activateCard(1L)).thenReturn(cardDTO);

        ResponseEntity<CardResponseDTO> response = adminController.activateCard(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cardDTO, response.getBody());
        verify(cardLockRequestService).activateCard(1L);
    }

    @Test
    void deleteCard_ShouldReturnNoContent() {
        doNothing().when(cardService).deleteCard(1L);

        ResponseEntity<Void> response = adminController.deleteCard(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(cardService).deleteCard(1L);
    }

    private UserDTO createUserDTO() {
        return UserDTO.builder()
                .id(1L)
                .login("testuser")
                .name("John")
                .surname("Doe")
                .patronymic("Smith")
                .role("USER")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    private CardResponseDTO createCardResponseDTO() {
        return CardResponseDTO.builder()
                .id(1L)
                .userId(1L)
                .maskedCardNumber("****3456")
                .ownerName("John S D")
                .expiryDate(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000.00))
                .build();
    }
}