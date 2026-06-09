package br.com.apisecurity.controller;

import br.com.apisecurity.dto.LoginRequestDto;
import br.com.apisecurity.dto.RegisterRequestDto;
import br.com.apisecurity.dto.TokenResponseDto;
import br.com.apisecurity.service.impl.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário cadastrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        TokenResponseDto tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }
}