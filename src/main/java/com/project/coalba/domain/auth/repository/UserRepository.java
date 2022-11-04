package com.project.coalba.domain.auth.repository;

import com.project.coalba.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
