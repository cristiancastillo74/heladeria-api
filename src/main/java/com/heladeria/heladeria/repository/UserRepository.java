package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    java.util.List<User> findByBranch_Id(Long branchId);
    java.util.List<User> findByBranch_IdAndRole(Long branchId, Role role);
}
