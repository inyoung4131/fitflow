package com.side.fitflow.user.repository;

import com.side.fitflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserId(String userId);
    User getByUserId(String userId);
    Optional<User> findByUserId(String userId);
}
