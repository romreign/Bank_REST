package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.AuthenticationRequestDTO;
import com.example.bankcards.dto.auth.AuthenticationResponseDTO;
import com.example.bankcards.dto.auth.RegisterRequestDTO;
import com.example.bankcards.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {
    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    void register_ShouldReturnAuthenticationResponse() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        AuthenticationResponseDTO responseDTO = AuthenticationResponseDTO.builder()
                .token("jwt-token")
                .build();
        when(authenticationService.register(any(RegisterRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<AuthenticationResponseDTO> response = authenticationController.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(authenticationService).register(any(RegisterRequestDTO.class));
    }

    @Test
    void authenticate_ShouldReturnAuthenticationResponse() {
        AuthenticationRequestDTO request = new AuthenticationRequestDTO();
        AuthenticationResponseDTO responseDTO = AuthenticationResponseDTO.builder()
                .token("jwt-token")
                .build();
        when(authenticationService.authenticate(any(AuthenticationRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<AuthenticationResponseDTO> response = authenticationController.authenticate(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(authenticationService).authenticate(any(AuthenticationRequestDTO.class));
    }
}
