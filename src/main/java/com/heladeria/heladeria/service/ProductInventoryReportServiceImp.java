package com.heladeria.heladeria.service;

import com.heladeria.heladeria.dto.ProductInventoryReportDTO;
import com.heladeria.heladeria.model.ProductInventory;
import com.heladeria.heladeria.repository.ProductInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductInventoryReportServiceImp implements ProductInventoryReportService{

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    /**
     * Retorna el inventario actual de todos los productos que NO son helados.
     */
    public List<ProductInventoryReportDTO> getCurrentInventoryReport() {
        List<ProductInventory> allInventories = productInventoryRepository.findAll();

        return allInventories.stream()
                .filter(pi -> !pi.getProduct().getIsIceCream()) // 🔹 solo productos normales
                .map(pi -> new ProductInventoryReportDTO(
                        pi.getProduct().getName(),
                        pi.getProduct().getCategory() != null ? pi.getProduct().getCategory().name() : "Sin categoría",
                        pi.getStock(),
                        pi.getBranch() != null ? pi.getBranch().getName() : "Sin sucursal"
                ))
                .collect(Collectors.toList());
    }

}
