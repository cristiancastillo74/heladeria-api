package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
