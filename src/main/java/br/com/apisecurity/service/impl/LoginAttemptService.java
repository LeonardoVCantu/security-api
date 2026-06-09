package br.com.apisecurity.service.impl;

import br.com.apisecurity.domain.User;
import br.com.apisecurity.repository.UserRepository;
import br.com.apisecurity.service.ILoginAttemptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService implements ILoginAttemptService {

    private final UserRepository userRepository;

    @Value("${api.security.brute-force.max-attempts}")
    private int maxAttempts;

    @Value("${api.security.brute-force.lock-time-minutes}")
    private int lockTimeMinutes;

    public LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void registerFailedAttempt(User user) {
        int newAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newAttempts);

        if (newAttempts >= maxAttempts) {
            user.setAccountNonLocked(false);
            user.setLockTime(LocalDateTime.now());
        }

        userRepository.save(user);
    }
    @Override
    public void resetFailedAttempts(User user) {
        if (user.getFailedAttempts() > 0) {
            user.setFailedAttempts(0);
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            userRepository.save(user);
        }
    }

    @Override
    public boolean unlockWhenTimeExpired(User user) {
        if (!user.isAccountNonLocked() && user.getLockTime() != null) {
            LocalDateTime expireTime = user.getLockTime().plusMinutes(lockTimeMinutes);

            if (LocalDateTime.now().isAfter(expireTime)) {
                user.setAccountNonLocked(true);
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

}
