package com.example.BLOCO.common.web;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.BLOCO.auth.exception.EmailAlreadyRegisteredException;
import com.example.BLOCO.auth.exception.InvalidCredentialsException;
import com.example.BLOCO.auth.exception.InvalidSessionException;
import com.example.BLOCO.auth.dto.ApiErrorResponse;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	public ResponseEntity<ApiErrorResponse> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ApiErrorResponse("EMAIL_ALREADY_REGISTERED", exception.getMessage(), Map.of()));
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(new ApiErrorResponse("INVALID_CREDENTIALS", exception.getMessage(), Map.of()));
	}

	@ExceptionHandler(InvalidSessionException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidSession(InvalidSessionException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(new ApiErrorResponse("INVALID_SESSION", exception.getMessage(), Map.of()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> fields = exception.getBindingResult().getFieldErrors().stream()
			.collect(Collectors.toMap(
				error -> error.getField(),
				error -> error.getDefaultMessage() == null ? "Valor inválido." : error.getDefaultMessage(),
				(first, ignored) -> first));
		return ResponseEntity.badRequest()
			.body(new ApiErrorResponse("VALIDATION_ERROR", "Verifique os campos enviados.", fields));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception) {
		return ResponseEntity.badRequest()
			.body(new ApiErrorResponse("INVALID_REQUEST_BODY", "O corpo da requisição é inválido.", Map.of()));
	}
}
