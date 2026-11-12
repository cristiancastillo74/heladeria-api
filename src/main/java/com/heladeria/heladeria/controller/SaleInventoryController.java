package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.CylinderInventory;
import com.heladeria.heladeria.model.ProductInventory;
import com.heladeria.heladeria.model.Role;
import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.repository.BranchRepository;
import com.heladeria.heladeria.repository.CylinderInventoryRepository;
import com.heladeria.heladeria.repository.ProductInventoryRepository;
import com.heladeria.heladeria.security.InventoryAccessService;
import com.heladeria.heladeria.service.CylinderInventoryService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/helados/sale/inventory")
public class SaleInventoryController {
    private final InventoryAccessService inventoryAccess;
    private final BranchRepository branchRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final CylinderInventoryRepository cylinderInventoryRepository;
    private final CylinderInventoryService cylinderInventoryService;

    public SaleInventoryController(
            InventoryAccessService inventoryAccess,
            BranchRepository branchRepository,
            ProductInventoryRepository productInventoryRepository,
            CylinderInventoryRepository cylinderInventoryRepository,
            CylinderInventoryService cylinderInventoryService) {
        this.inventoryAccess = inventoryAccess;
        this.branchRepository = branchRepository;
        this.productInventoryRepository = productInventoryRepository;
        this.cylinderInventoryRepository = cylinderInventoryRepository;
        this.cylinderInventoryService = cylinderInventoryService;
    }

    @GetMapping("/products")
    public List<ProductInventory> products(
            @RequestParam(value = "branchId", required = false) Long requestedBranchId,
            Authentication authentication) {
        User actor = inventoryAccess.actor(authentication);
        Long branchId = resolveBranch(actor, requestedBranchId);
        return productInventoryRepository.findByBranch_Id(branchId);
    }

    @GetMapping("/cylinders/available")
    public List<CylinderInventory> cylinders(
            @RequestParam(value = "branchId", required = false) Long requestedBranchId,
            Authentication authentication) {
        User actor = inventoryAccess.actor(authentication);
        Long branchId = resolveBranch(actor, requestedBranchId);
        return cylinderInventoryService.obtenerCylinderInventoryDisponiblesCondicional(branchId);
    }

    private Long resolveBranch(User actor, Long requestedBranchId) {
        if (actor.getRole() != Role.ADMIN_GLOBAL) {
            if (actor.getBranch() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu usuario no tiene sucursal asignada");
            }
            return actor.getBranch().getId();
        }
        if (requestedBranchId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una sucursal para consultar inventario de venta");
        }
        return branchRepository.findById(requestedBranchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sucursal no encontrada"))
                .getId();
    }
}