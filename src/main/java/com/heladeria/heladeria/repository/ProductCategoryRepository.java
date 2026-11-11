package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.ProductCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Optional<ProductCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}