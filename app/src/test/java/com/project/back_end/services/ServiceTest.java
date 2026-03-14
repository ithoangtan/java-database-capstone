package com.project.back_end.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceTest {

    @Mock
    private TokenService tokenService;

    private Service service;

    @BeforeEach
    void setUp() {
        service = new Service(tokenService);
    }

    @Test
    void validateToken_returnsEmptyMap_whenTokenIsValid() {
        when(tokenService.isTokenValidForRole("valid-token", "doctor")).thenReturn(true);

        Map<String, Object> result = service.validateToken("valid-token", "doctor");

        assertThat(result).isEmpty();
    }

    @Test
    void validateToken_returnsErrorMap_whenTokenIsInvalid() {
        when(tokenService.isTokenValidForRole("invalid-token", "patient")).thenReturn(false);

        Map<String, Object> result = service.validateToken("invalid-token", "patient");

        assertThat(result).containsKey("error");
        assertThat(result.get("error")).isEqualTo("Invalid or expired token");
    }
}
