package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.dto.CatalogOptionDTO;
import com.heladeria.heladeria.repository.CylinderTypeRepository;
import com.heladeria.heladeria.repository.ProductStatusRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final ProductStatusRepository productStatusRepository;
    private final CylinderTypeRepository cylinderTypeRepository;

    public CatalogController(
            ProductStatusRepository productStatusRepository,
            CylinderTypeRepository cylinderTypeRepository) {
        this.productStatusRepository = productStatusRepository;
        this.cylinderTypeRepository = cylinderTypeRepository;
    }

    @GetMapping("/product-statuses")
    public List<String> productStatuses() {
        return productStatusRepository.findAll().stream().map(status -> status.getName()).sorted().toList();
    }

    @GetMapping("/cylinder-types")
    public List<CatalogOptionDTO> cylinderTypes() {
        return cylinderTypeRepository.findAll().stream()
                .map(type -> new CatalogOptionDTO(type.getId(), type.getName(), type.getFraction()))
                .toList();
    }
}