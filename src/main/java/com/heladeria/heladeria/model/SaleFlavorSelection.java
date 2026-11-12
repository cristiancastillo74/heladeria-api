package com.heladeria.heladeria.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sale_flavor_selections")
@Getter
@Setter
@NoArgsConstructor
public class SaleFlavorSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sale_item_id", nullable = false)
    private SaleItem saleItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cylinder_id", nullable = false)
    private Cylinder cylinder;

    private Long cylinderInventoryId;

    private Integer balls;
}