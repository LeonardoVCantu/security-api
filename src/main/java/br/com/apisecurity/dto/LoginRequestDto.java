package br.com.apisecurity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        @Size(max = 100)
        String email,

        @Size(max = 72)
        @NotBlank(message = "A senha é obrigatória")
        String password
) {}