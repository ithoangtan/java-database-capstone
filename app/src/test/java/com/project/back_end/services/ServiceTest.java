package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorService doctorService;

    @Mock
    private PatientService patientService;

    private Service service;

    @BeforeEach
    void setUp() {
        service = new Service(tokenService, adminRepository, doctorRepository, patientRepository, doctorService, patientService);
    }

    @Test
    void validateToken_returnsOkWithEmptyBody_whenTokenIsValid() {
        when(tokenService.validateToken("valid-token", "doctor")).thenReturn(true);

        ResponseEntity<Map<String, String>> result = service.validateToken("valid-token", "doctor");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEmpty();
    }

    @Test
    void validateToken_returns401_whenTokenIsInvalid() {
        when(tokenService.validateToken("invalid-token", "patient")).thenReturn(false);

        ResponseEntity<Map<String, String>> result = service.validateToken("invalid-token", "patient");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(result.getBody()).containsKey("message");
        assertThat(result.getBody().get("message")).isEqualTo("Invalid or expired token");
    }

    @Test
    void validateTokenAsMap_returnsEmptyMap_whenTokenIsValid() {
        when(tokenService.validateToken("valid-token", "doctor")).thenReturn(true);

        Map<String, Object> result = service.validateTokenAsMap("valid-token", "doctor");

        assertThat(result).isEmpty();
    }

    @Test
    void validateTokenAsMap_returnsErrorMap_whenTokenIsInvalid() {
        when(tokenService.validateToken("invalid-token", "patient")).thenReturn(false);

        Map<String, Object> result = service.validateTokenAsMap("invalid-token", "patient");

        assertThat(result).containsKey("error");
        assertThat(result.get("error")).isEqualTo("Invalid or expired token");
    }
}
