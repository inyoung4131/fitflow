package com.side.fitflow.user.repository;

import com.side.fitflow.comm.converter.usergrade.UserGrade;
import com.side.fitflow.user.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, UserGrade> {

    Optional<Authority> findByAuthorityName(UserGrade userGrade);
}
