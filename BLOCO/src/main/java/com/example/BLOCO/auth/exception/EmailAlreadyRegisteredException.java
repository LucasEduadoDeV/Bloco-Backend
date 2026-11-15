package com.example.BLOCO.auth.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {

	public EmailAlreadyRegisteredException() {
		super("Este e-mail já está cadastrado.");
	}

	public EmailAlreadyRegisteredException(Throwable cause) {
		super("Este e-mail já está cadastrado.", cause);
	}
}
