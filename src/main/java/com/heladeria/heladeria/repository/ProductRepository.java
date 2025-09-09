package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.Branch;
import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Optional<Product> findByCode(String code);

}
