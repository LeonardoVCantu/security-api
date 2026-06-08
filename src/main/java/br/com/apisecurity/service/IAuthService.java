package br.com.apisecurity.service;

import br.com.apisecurity.dto.RegisterRequestDto;

public interface IAuthService {

    void register(RegisterRequestDto request);

}
