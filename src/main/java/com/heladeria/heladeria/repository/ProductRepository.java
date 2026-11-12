package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.model.Branch;
import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Optional<Product> findByCode(String code);
    boolean existsByCategory(String category);
    boolean existsByStatus(String status);
    List<Product> findAllByOrderByNameAsc();

    @Modifying
    @Transactional
    @Query("update Product p set p.category = :newValue where p.category = :oldValue")
    int renameCategory(@Param("oldValue") String oldValue, @Param("newValue") String newValue);

    @Modifying
    @Transactional
    @Query("update Product p set p.status = :newValue where p.status = :oldValue")
    int renameStatus(@Param("oldValue") String oldValue, @Param("newValue") String newValue);

}
