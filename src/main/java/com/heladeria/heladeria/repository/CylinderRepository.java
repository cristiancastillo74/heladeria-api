package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.Cylinder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface CylinderRepository extends JpaRepository<Cylinder,Long> {

    Optional<Cylinder> findFirstByOrderByIdAsc();
}
