package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.dto.lock.CardLockRequestDTO;
import com.example.bankcards.dto.lock.CardLockResponseDTO;
import com.example.bankcards.dto.transfer.TransferRequestDTO;
import com.example.bankcards.dto.transfer.TransferResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.enums.RequestStatus;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.service.CardLockRequestService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.security.SecurityContextService;
import com.example.bankcards.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @Mock
    private TransferService transferService;

    @Mock
    private CardLockRequestService cardLockRequestService;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CardResponseDTO testCard;
    private CardLockRequestDTO lockRequestDTO;
    private CardLockResponseDTO lockResponseDTO;
    private TransferRequestDTO transferRequestDTO;
    private TransferResponseDTO transferResponseDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean()).build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");

        testCard = new CardResponseDTO(
                1L,
                1L,
                "****5678",
                "Test User",
                java.time.LocalDate.now().plusYears(1),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(1000.00)
        );

        lockRequestDTO = new CardLockRequestDTO(1L, "Потеря карты");

        lockResponseDTO = new CardLockResponseDTO(
                1L,
                1L,
                "****5678",
                RequestStatus.PENDING,
                "Потеря карты",
                LocalDateTime.now(),
                null,
                null
        );

        transferRequestDTO = new TransferRequestDTO(1L, 2L, BigDecimal.valueOf(100.00));

        transferResponseDTO = new TransferResponseDTO(
                1L,
                1L,
                2L,
                BigDecimal.valueOf(100.00),
                LocalDateTime.now(),
                "SUCCESS"
        );
    }

    @Test
    void createLockRequest_WithValidData_ShouldCreateLockRequest() throws Exception {
        when(cardLockRequestService.createLockRequest(any(CardLockRequestDTO.class))).thenReturn(lockResponseDTO);

        mockMvc.perform(post("/api/user/cards/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lockRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(1L));
    }

    @Test
    void transfer_WithValidData_ShouldCompleteTransfer() throws Exception {
        when(transferService.transfer(any(TransferRequestDTO.class))).thenReturn(transferResponseDTO);

        mockMvc.perform(post("/api/user/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCardId").value(1L));
    }

    @Test
    void getBalance_WhenCardOwner_ShouldReturnBalance() throws Exception {
        when(securityContextService.getCurrentUser()).thenReturn(testUser);
        when(securityContextService.isCurrentUser(anyLong())).thenReturn(true);
        when(cardService.getBalance(anyLong())).thenReturn(BigDecimal.valueOf(1000.00));

        mockMvc.perform(get("/api/user/cards/1/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.0"));
    }

    @Test
    void getBalance_WhenAdmin_ShouldReturnBalance() throws Exception {
        when(securityContextService.isAdmin()).thenReturn(true);
        when(cardService.getBalance(anyLong())).thenReturn(BigDecimal.valueOf(1000.00));

        mockMvc.perform(get("/api/user/cards/1/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.0"));
    }

    @Test
    void addBalance_WhenCardOwner_ShouldAddFunds() throws Exception {
        when(securityContextService.getCurrentUser()).thenReturn(testUser);
        when(securityContextService.isCurrentUser(anyLong())).thenReturn(true);
        when(cardService.addBalance(anyLong(), any(BigDecimal.class))).thenReturn(testCard);

        mockMvc.perform(patch("/api/user/cards/1/balance")
                        .param("amount", "500.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void addBalance_WithNegativeAmount_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/api/user/cards/1/balance")
                        .param("amount", "-100.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }
}