package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.PurchaseOrder;
import com.heladeria.heladeria.model.PurchaseOrderType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByTypeOrderByUpdatedAtDesc(PurchaseOrderType type);
    Optional<PurchaseOrder> findByIdAndType(Long id, PurchaseOrderType type);
}