package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardRequestDTO;
import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.dto.lock.CardLockRequestDTO;
import com.example.bankcards.dto.user.UserDTO;
import com.example.bankcards.dto.user.UserUpdateDTO;
import com.example.bankcards.service.CardLockRequestService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin API", description = "Administrative operations for managing users and cards")
public class AdminController {

    private final UserService userService;

    private final CardService cardService;

    private final CardLockRequestService cardLockRequestService;

    @Operation(summary = "Get all users", description = "Retrieve a list of all registered users")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users")
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @Operation(summary = "Update user", description = "Update user information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable long userId, @Valid @RequestBody UserUpdateDTO user) {
        return ResponseEntity.ok(userService.updateUser(userId, user));
    }

    @Operation(summary = "Delete user", description = "Delete a user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all cards", description = "Retrieve a list of all bank cards")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of cards")
    @GetMapping("/cards")
    public ResponseEntity<List<CardResponseDTO>> getCards() {
        return ResponseEntity.ok(cardService.getCards());
    }

    @Operation(summary = "Get user cards", description = "Retrieve all cards belonging to a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user cards"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<List<CardResponseDTO>> getUserCards(@PathVariable long userId) {
        return ResponseEntity.ok(cardService.getUserCards(userId));
    }

    @Operation(summary = "Create card for user", description = "Create a new bank card for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card created successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid card data")
    })
    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<CardResponseDTO> createCard(@PathVariable long userId,
                                                      @Valid @RequestBody CardRequestDTO card) {
        return ResponseEntity.ok(cardService.createCard(userId, card));
    }

    @Operation(summary = "Block card", description = "Block a specific bank card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card blocked successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "400", description = "Invalid lock request")
    })
    @PatchMapping("/cards/{cardId}/block")
    public ResponseEntity<CardResponseDTO> blockCard(@PathVariable long cardId,
                                                     @Valid @RequestBody CardLockRequestDTO cardRequest) {
        return ResponseEntity.ok(cardLockRequestService.processLockRequest(cardId, cardRequest, true));
    }

    @Operation(summary = "Activate card", description = "Activate a previously blocked card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card activated successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PatchMapping("/cards/{cardId}/activate")
    public ResponseEntity<CardResponseDTO> activateCard(@PathVariable long cardId) {
        return ResponseEntity.ok(cardLockRequestService.activateCard(cardId));
    }

    @Operation(summary = "Delete card", description = "Delete a bank card by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

}
