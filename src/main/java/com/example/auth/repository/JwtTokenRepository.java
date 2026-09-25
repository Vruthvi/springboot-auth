package com.example.auth.repository;

import com.example.auth.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    Optional<JwtToken> findByToken(String token);
    void deleteByToken(String token);
}
