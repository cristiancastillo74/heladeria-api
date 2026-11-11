package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.CylinderType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CylinderTypeRepository extends JpaRepository<CylinderType, Long> {
    Optional<CylinderType> findByNameIgnoreCase(String name);
}