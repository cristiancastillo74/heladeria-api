package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {
}
