package com.example.BLOCO.auth.controller;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BLOCO.auth.service.AuthService;
import com.example.BLOCO.auth.service.JwtService;
import com.example.BLOCO.auth.dto.CurrentUserResponse;
import com.example.BLOCO.auth.dto.LoginRequest;
import com.example.BLOCO.auth.dto.RegisterRequest;
import com.example.BLOCO.auth.dto.UserResponse;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

	private final AuthService authService;
	private final JwtService jwtService;

	public AuthController(AuthService authService, JwtService jwtService) {
		this.authService = authService;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.cacheControl(CacheControl.noStore())
			.body(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
		UserResponse user = authService.login(request);
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, jwtService.createAccessTokenCookie(user).toString())
			.cacheControl(CacheControl.noStore())
			.body(user);
	}

	@GetMapping("/me")
	public CurrentUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
		return authService.getCurrentUser(UUID.fromString(jwt.getSubject()));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout() {
		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, jwtService.clearAccessTokenCookie().toString())
			.cacheControl(CacheControl.noStore())
			.build();
	}
}
