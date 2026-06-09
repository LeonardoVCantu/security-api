package br.com.apisecurity.service.impl;

import br.com.apisecurity.domain.User;
import br.com.apisecurity.dto.LoginRequestDto;
import br.com.apisecurity.dto.RegisterRequestDto;
import br.com.apisecurity.dto.TokenResponseDto;
import br.com.apisecurity.repository.UserRepository;
import br.com.apisecurity.service.IAuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       LoginAttemptService loginAttemptService,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public void register(RegisterRequestDto request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Este e-mail já está cadastrado.");
        }

        User newUser = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .accountNonLocked(true)
                .lockTime(null)
                .failedAttempts(0)
                .build();

        userRepository.save(newUser);
    }


    public TokenResponseDto login(LoginRequestDto request) {
        final String DUMMY_HASH = "$2a$10$Nx7N.N7gJ9vj7R7Y8U8hHeG5v5F5v5F5v5F5v5F5v5F5v5F5v5F5v";

        Optional<User> userOptional = userRepository.findByEmail(request.email());

        User user = userOptional.orElseGet(() -> {
            User dummy = new User();
            dummy.setEmail(request.email());
            dummy.setPassword(DUMMY_HASH);
            dummy.setAccountNonLocked(true);
            return dummy;
        });

        loginAttemptService.unlockWhenTimeExpired(user);

        if (!user.isAccountNonLocked()) {
            throw new LockedException("Esta conta está temporariamente bloqueada por excesso de tentativas.");
        }

        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(
                    request.email(), request.password()
            );

            authenticationManager.authenticate(authenticationToken);

            loginAttemptService.resetFailedAttempts(user);

            String realJwtToken = jwtService.generateToken(user);

            return new TokenResponseDto(realJwtToken, "Bearer");
        } catch (AuthenticationException e) {
            loginAttemptService.registerFailedAttempt(user);
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }
    }
}