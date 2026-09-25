package com.example.auth.service;

import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.SignupRequest;
import com.example.auth.entity.JwtToken;
import com.example.auth.entity.User;
import com.example.auth.repository.JwtTokenRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository,
                       JwtTokenRepository jwtTokenRepository,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setPhno(req.getPhno());
        user = userRepository.save(user);

        return createTokenForUser(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return createTokenForUser(user);
    }

    private AuthResponse createTokenForUser(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        JwtToken jt = new JwtToken();
        jt.setUser(user);
        jt.setToken(token);
        jt.setExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getExpirationMs() / 1000));
        jwtTokenRepository.save(jt);
        return new AuthResponse(token, user);
    }

    public User getUserFromToken(String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        JwtToken stored = jwtTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found (logged out?)"));
        if (stored.getExpiresAt() != null && stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }
        Long userId = Long.valueOf(jwtUtil.getUserIdFromToken(token));
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void logout(String token) {
        jwtTokenRepository.deleteByToken(token);
    }
}
