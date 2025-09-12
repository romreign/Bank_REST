package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.dto.lock.CardLockRequestDTO;
import com.example.bankcards.dto.lock.CardLockResponseDTO;
import com.example.bankcards.dto.transfer.TransferRequestDTO;
import com.example.bankcards.dto.transfer.TransferResponseDTO;
import com.example.bankcards.service.CardLockRequestService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.SecurityContextService;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "User operations for managing cards and transactions")
public class UserController {

    private final CardService cardService;

    private final TransferService transferService;

    private final CardLockRequestService cardLockRequestService;

    private final SecurityContextService securityContextService;

    @Operation(summary = "Get user cards", description = "Retrieve paginated list of cards for a user. Admins can view any user's cards.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user cards"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/cards")
    @PreAuthorize("#userId == null or @securityContextService.isCurrentUser(#userId) or @securityContextService.isAdmin()")
    public ResponseEntity<Page<CardResponseDTO>> getCards(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (userId == null)
            userId = securityContextService.getCurrentUser().getId();

        return ResponseEntity.ok(cardService.getUserCards(userId, page, size));
    }

    @Operation(summary = "Create card lock request", description = "Request to block a card with a reason")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lock request created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid lock request data"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PostMapping("/cards/block")
    public ResponseEntity<CardLockResponseDTO> createLockRequest(@Valid @RequestBody CardLockRequestDTO request) {
        return ResponseEntity.ok(cardLockRequestService.createLockRequest(request));
    }

    @Operation(summary = "Transfer funds", description = "Transfer money between cards")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transfer data or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transfer(@Valid @RequestBody TransferRequestDTO request) {
        return ResponseEntity.ok(transferService.transfer(request));
    }

    @Operation(summary = "Get card balance", description = "Retrieve the balance of a specific card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @GetMapping("/cards/{cardId}/balance")
    @PreAuthorize("@cardService.isCardOwner(#cardId, @securityContextService.getCurrentUser().getId()) or @securityContextService.isAdmin()")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getBalance(cardId));
    }

    @Operation(summary = "Add balance to card", description = "Add funds to a specific card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance added successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "400", description = "Invalid amount")
    })
    @PatchMapping("/cards/{cardId}/balance")
    @PreAuthorize("@cardService.isCardOwner(#cardId, @securityContextService.getCurrentUser().getId()) or @securityContextService.isAdmin()")
    public ResponseEntity<CardResponseDTO> addBalance(
            @PathVariable Long cardId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(cardService.addBalance(cardId, amount));
    }
}



