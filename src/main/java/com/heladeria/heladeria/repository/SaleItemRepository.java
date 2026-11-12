package com.heladeria.heladeria.repository;

import com.heladeria.heladeria.dto.SalesReportDTO;
import com.heladeria.heladeria.model.SaleItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    @Query("""
    SELECT new com.heladeria.heladeria.dto.SalesReportDTO(
        DATE(s.createdAt),
        p.name,
        SUM(i.quantity),
        SUM(i.price * i.quantity),
        b.name,
        u.username
    )
    FROM SaleItem i
    JOIN i.sale s
    JOIN s.branch b
    JOIN s.user u
    JOIN i.product p
    WHERE s.createdAt BETWEEN :startDate AND :endDate
    AND (:branchId IS NULL OR b.id = :branchId)
    GROUP BY DATE(s.createdAt), p.name, b.name, u.username
    ORDER BY DATE(s.createdAt) DESC
""")
    Page<SalesReportDTO> getSalesReport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("branchId") Long branchId,
            Pageable pageable
    );


    @Query("""
        SELECT SUM(i.price * i.quantity)
        FROM SaleItem i
        JOIN i.sale s
        WHERE s.createdAt BETWEEN :startDate AND :endDate
        AND (:branchId IS NULL OR s.branch.id = :branchId)
    """)
    BigDecimal getTotalSalesAmount(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("branchId") Long branchId
    );

    @Query("""
        SELECT COALESCE(SUM(i.price * i.quantity), 0)
        FROM SaleItem i JOIN i.sale s
        WHERE s.createdAt >= :startDate AND s.createdAt < :endDate
        AND (:branchId IS NULL OR s.branch.id = :branchId)
    """)
    BigDecimal totalSalesBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("branchId") Long branchId
    );

    @Query("""
        SELECT new com.heladeria.heladeria.dto.ProductSalesDTO(
            i.product.name, SUM(i.quantity), SUM(i.price * i.quantity)
        )
        FROM SaleItem i JOIN i.sale s
        WHERE s.createdAt >= :startDate AND s.createdAt < :endDate
        AND (:branchId IS NULL OR s.branch.id = :branchId)
        GROUP BY i.product.name
        ORDER BY SUM(i.quantity) DESC
    """)
    List<com.heladeria.heladeria.dto.ProductSalesDTO> getProductSales(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("branchId") Long branchId
    );

    @Query("""
        SELECT new com.heladeria.heladeria.dto.FlavorSalesDTO(
            selection.cylinder.flavor, SUM(selection.balls)
        )
        FROM SaleFlavorSelection selection
        JOIN selection.saleItem item
        JOIN item.sale sale
        WHERE sale.createdAt >= :startDate AND sale.createdAt < :endDate
        AND (:branchId IS NULL OR sale.branch.id = :branchId)
        GROUP BY selection.cylinder.flavor
        ORDER BY SUM(selection.balls) DESC
    """)
    List<com.heladeria.heladeria.dto.FlavorSalesDTO> getFlavorSales(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("branchId") Long branchId,
            Pageable pageable
    );
}
