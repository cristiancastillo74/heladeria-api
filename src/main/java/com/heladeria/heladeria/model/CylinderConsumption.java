package com.heladeria.heladeria.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CylinderConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // Usuario que registró el consumo

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;  // Sucursal donde se consumió

    @ManyToOne
    @JoinColumn(name = "cylinder_id")
    private Cylinder cylinder;


    private Integer ballsConsumed;  // Cantidad de bolas consumidas en este registro

    @CreatedDate
    private LocalDateTime createdAt;



}
