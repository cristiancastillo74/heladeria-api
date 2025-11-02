package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.dto.ProductInventoryReportDTO;
import com.heladeria.heladeria.model.Branch;
import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, Long> {
    Optional<ProductInventory> findByProductAndBranch(Product product, Branch branch);

}
