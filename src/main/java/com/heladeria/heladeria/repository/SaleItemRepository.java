package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
}
