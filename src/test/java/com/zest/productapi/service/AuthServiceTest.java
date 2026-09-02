package com.zest.productapi.service;

import com.zest.productapi.dto.request.LoginRequest;
import com.zest.productapi.dto.request.RegisterRequest;
import com.zest.productapi.entity.Role;
import com.zest.productapi.entity.User;
import com.zest.productapi.exception.BadRequestException;
import com.zest.productapi.repository.RefreshTokenRepository;
import com.zest.productapi.repository.UserRepository;
import com.zest.productapi.security.JwtService;
import com.zest.productapi.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    void register_shouldCreateUser_whenUsernameAvailable() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshTokenValue(any())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);

        var response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    void register_shouldThrow_whenUsernameExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsValid() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        UserPrincipal principal = new UserPrincipal(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshTokenValue(any())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);

        var response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }
}
