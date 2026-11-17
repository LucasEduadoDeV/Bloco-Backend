package com.example.BLOCO.auth.exception;

public class InvalidSessionException extends RuntimeException {

	public InvalidSessionException() {
		super("A sessão não é mais válida.");
	}
}
