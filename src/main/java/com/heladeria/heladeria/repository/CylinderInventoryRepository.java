package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CylinderInventoryRepository extends JpaRepository<CylinderInventory, Long> {
    Optional<CylinderInventory> findByCylinder(Cylinder cylinder);
    List<CylinderInventory> findByBranch_Id(Long branchId);

    List<CylinderInventory> findByStatusNot(Status status);


    Optional<CylinderInventory> findFirstByCylinderAndBranchAndStatusOrderByCreatedAtAsc(
            Cylinder cylinder,
            Branch branch,
            Status status
    );
}
