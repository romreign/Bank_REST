package com.example.bankcards.service;

import com.example.bankcards.dto.auth.AuthenticationRequestDTO;
import com.example.bankcards.dto.auth.AuthenticationResponseDTO;
import com.example.bankcards.dto.auth.RegisterRequestDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserRoleNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new UserRoleNotFoundException("Role not found"));

        User user = User.builder()
                .surname(request.getSurname())
                .name(request.getName())
                .patronymic(request.getPatronymic())
                .login(request.getLogin())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .birthday(request.getBirthday())
                .build();

        userRepository.save(user);
        var jwToken = jwtService.generateToken(user);
        return AuthenticationResponseDTO.builder()
                .token(jwToken)
                .build();
    }

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        authenticationManager.authenticate
                (new UsernamePasswordAuthenticationToken(
                        request.getLogin(),
                        request.getPassword()));
        User user = userRepository.findByLogin(request.getLogin()).orElseThrow();
        String jwToken = jwtService.generateToken(user);
        return AuthenticationResponseDTO.builder()
                .token(jwToken)
                .build();
    }
}
