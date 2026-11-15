package com.example.BLOCO.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
	@NotBlank(message = "O e-mail é obrigatório.")
	@Email(message = "Informe um e-mail válido.")
	@Size(max = 320, message = "O e-mail deve ter no máximo 320 caracteres.")
	String email,
	@NotBlank(message = "A senha é obrigatória.")
	String password
) {
}
