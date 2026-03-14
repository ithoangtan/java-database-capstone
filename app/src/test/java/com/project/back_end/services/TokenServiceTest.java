package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private static final String JWT_SECRET = "test-secret-key-at-least-256-bits-for-HS256-algorithm-here-32chars";

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(adminRepository, doctorRepository, patientRepository);
        ReflectionTestUtils.setField(tokenService, "jwtSecret", JWT_SECRET);
    }

    @Test
    void generateToken_returnsNonEmptyToken() {
        String token = tokenService.generateToken("user@test.com");

        assertThat(token).isNotBlank();
    }

    @Test
    void extractSubject_returnsSubject_whenTokenIsValid() {
        String subject = "admin1";
        String token = tokenService.generateToken(subject);

        String result = tokenService.extractSubject(token);

        assertThat(result).isEqualTo(subject);
    }

    @Test
    void extractSubject_returnsNull_whenTokenIsInvalid() {
        String result = tokenService.extractSubject("invalid.jwt.token");

        assertThat(result).isNull();
    }

    @Test
    void extractSubject_returnsNull_whenTokenIsNull() {
        String result = tokenService.extractSubject(null);

        assertThat(result).isNull();
    }

    @Test
    void isTokenValidForRole_returnsTrue_forAdminWhenUserExists() {
        String token = tokenService.generateToken("admin1");
        when(adminRepository.findByUsername("admin1")).thenReturn(new Admin());

        boolean result = tokenService.isTokenValidForRole(token, "admin");

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValidForRole_returnsFalse_forAdminWhenUserNotFound() {
        String token = tokenService.generateToken("admin1");
        when(adminRepository.findByUsername("admin1")).thenReturn(null);

        boolean result = tokenService.isTokenValidForRole(token, "admin");

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValidForRole_returnsTrue_forDoctorWhenUserExists() {
        String token = tokenService.generateToken("doctor@test.com");
        when(doctorRepository.findByEmail("doctor@test.com")).thenReturn(new Doctor());

        boolean result = tokenService.isTokenValidForRole(token, "doctor");

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValidForRole_returnsTrue_forPatientWhenUserExists() {
        String token = tokenService.generateToken("patient@test.com");
        when(patientRepository.findByEmail("patient@test.com")).thenReturn(new Patient());

        boolean result = tokenService.isTokenValidForRole(token, "patient");

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValidForRole_returnsFalse_whenTokenIsInvalid() {
        boolean result = tokenService.isTokenValidForRole("bad-token", "admin");

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValidForRole_returnsFalse_forUnknownRole() {
        String token = tokenService.generateToken("user@test.com");

        boolean result = tokenService.isTokenValidForRole(token, "unknown");

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValidForRole_returnsFalse_whenSubjectIsBlank() {
        String token = tokenService.generateToken("  ");

        boolean result = tokenService.isTokenValidForRole(token, "admin");

        assertThat(result).isFalse();
    }
}
