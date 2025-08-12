package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.Cylinder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CylinderRepository extends JpaRepository<Cylinder,Long> {
}
