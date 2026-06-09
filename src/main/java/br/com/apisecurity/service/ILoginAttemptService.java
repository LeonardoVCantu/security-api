package br.com.apisecurity.service;

import br.com.apisecurity.domain.User;

public interface ILoginAttemptService {

    void registerFailedAttempt(User user);

    void resetFailedAttempts(User user);

    boolean unlockWhenTimeExpired(User user);

}
