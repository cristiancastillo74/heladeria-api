package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch,Long> {
}
