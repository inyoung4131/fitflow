package com.side.fitflow.jwt.repository;

import com.dntwk.jwt.entity.RefreshToken;
import com.side.fitflow.jwt.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserKey(String userId);

}
