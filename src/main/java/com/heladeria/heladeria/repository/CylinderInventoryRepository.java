package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.dto.CylinderInventoryReportDTO;
import com.heladeria.heladeria.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CylinderInventoryRepository extends JpaRepository<CylinderInventory, Long> {
    Optional<CylinderInventory> findByCylinder(Cylinder cylinder);
    Optional<CylinderInventory> findByCylinderAndBranch(Cylinder cylinder, Branch branch);
    List<CylinderInventory> findAllByCylinderAndBranchOrderByCreatedAtAsc(Cylinder cylinder, Branch branch);
    List<CylinderInventory> findByBranch_Id(Long branchId);

    List<CylinderInventory> findByStatusNot(Status status);


    Optional<CylinderInventory> findFirstByCylinderAndBranchAndStatusOrderByCreatedAtAsc(
            Cylinder cylinder,
            Branch branch,
            Status status
    );

    @Query("""
        SELECT new com.heladeria.heladeria.dto.CylinderInventoryReportDTO(
            c.flavor,
            b.name,
            ci.status,
            ci.fraction
        )
        FROM CylinderInventory ci
        JOIN ci.cylinder c
        JOIN ci.branch b
        where ci.status <> com.heladeria.heladeria.model.Status.CYLINDER_VACIO
        and (:branchId is null or b.id = :branchId)
        ORDER BY b.name, c.flavor
    """)
    List<CylinderInventoryReportDTO> getCylinderInventoryReport(@Param("branchId") Long branchId);
}
