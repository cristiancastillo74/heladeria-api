package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.ProductStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStatusRepository extends JpaRepository<ProductStatus, Long> {
    Optional<ProductStatus> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}