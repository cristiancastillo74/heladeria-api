package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.Branch;
import com.heladeria.heladeria.model.CylinderInventory;
import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CylinderInventoryRepository extends JpaRepository<CylinderInventory, Long> {
    List<CylinderInventory> findByBranchId(Long branchId);
    Optional<CylinderInventory> findFirstByProductAndBranchAndStatusOrderByCreatedAtAsc(
            Product product,
            Branch branch,
            Status status
    );
}
