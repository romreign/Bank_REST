package com.example.bankcards.service;

import com.example.bankcards.dto.auth.AuthenticationRequestDTO;
import com.example.bankcards.dto.auth.RegisterRequestDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_ShouldSuccessfullyRegisterUser() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .surname("Ivanov")
                .name("Ivan")
                .patronymic("Ivanovich")
                .login("ivanov")
                .password("password123")
                .role("USER")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Role userRole = new Role("USER");
        userRole.setId(2L);

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

        var response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void authenticate_ShouldReturnTokenForValidCredentials() {
        AuthenticationRequestDTO request = new AuthenticationRequestDTO("ivanov", "password123");
        User user = new User();
        user.setLogin("ivanov");
        user.setPassword("encodedPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userRepository.findByLogin("ivanov")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        var response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(authenticationManager, times(1)).
                authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_ShouldThrowExceptionForInvalidCredentials() {
        AuthenticationRequestDTO request = new AuthenticationRequestDTO("ivanov", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(request);
        });
    }
}