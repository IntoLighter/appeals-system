package com.intolighter.appealssystem.repositories;

import com.intolighter.appealssystem.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);
}
