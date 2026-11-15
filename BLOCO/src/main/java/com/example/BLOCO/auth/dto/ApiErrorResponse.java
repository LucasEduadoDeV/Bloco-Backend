package com.example.BLOCO.auth.dto;

import java.util.Map;

public record ApiErrorResponse(String code, String message, Map<String, String> fields) {
}
