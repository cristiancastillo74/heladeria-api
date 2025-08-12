package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface SaleRepository extends JpaRepository<Sale,Long> {

    List<Sale> findByBranchIdAndCreatedAtBetween(
            Long branchId,
            LocalDateTime start,
            LocalDateTime end
    );

}
