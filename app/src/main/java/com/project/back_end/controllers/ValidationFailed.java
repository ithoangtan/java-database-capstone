package com.project.back_end.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidationFailed {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        String firstMessage = null;
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String msg = error.getDefaultMessage();
            fieldErrors.put(error.getField(), msg != null ? msg : "Invalid");
            if (firstMessage == null) firstMessage = msg;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("errors", fieldErrors);
        body.put("message", firstMessage != null ? firstMessage : "Validation failed.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", message));
    }
}