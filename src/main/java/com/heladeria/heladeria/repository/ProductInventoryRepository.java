package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, Long> {
}
