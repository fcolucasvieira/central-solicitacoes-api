package com.fcolucasvieira.central_solicitacoes_api.auth.service;

import com.fcolucasvieira.central_solicitacoes_api.auth.dto.AuthResponse;
import com.fcolucasvieira.central_solicitacoes_api.auth.dto.LoginRequest;
import com.fcolucasvieira.central_solicitacoes_api.auth.dto.RegisterRequest;
import com.fcolucasvieira.central_solicitacoes_api.exception.ResourceAlreadyExistsException;
import com.fcolucasvieira.central_solicitacoes_api.jwt.JwtService;
import com.fcolucasvieira.central_solicitacoes_api.user.entity.User;
import com.fcolucasvieira.central_solicitacoes_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        boolean emailExists = userRepository.existsByEmail(request.email());

        if (emailExists) {
            throw new ResourceAlreadyExistsException("Email já existente.");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(request.name(), request.email(), passwordHash);
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.from(token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));

        String token = jwtService.generateToken(user);

        return AuthResponse.from(token);
    }
}
