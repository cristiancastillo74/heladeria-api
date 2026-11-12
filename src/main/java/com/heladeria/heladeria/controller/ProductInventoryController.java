package com.heladeria.heladeria.controller;



import com.heladeria.heladeria.model.ProductInventory;
import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.repository.ProductInventoryRepository;
import com.heladeria.heladeria.security.InventoryAccessService;
import com.heladeria.heladeria.service.ProductInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("helados")
@CrossOrigin(value = "*")
public class ProductInventoryController {

    @Autowired
    private ProductInventoryService pis;
    @Autowired
    private ProductInventoryRepository inventoryRepository;
    @Autowired
    private InventoryAccessService inventoryAccess;

    @GetMapping("/productInventory")
    public List<ProductInventory> getPI(Authentication authentication){
        User actor = inventoryAccess.actor(authentication);
        Long branchId = inventoryAccess.visibleBranchId(actor);
        return branchId == null ? inventoryRepository.findAll() : inventoryRepository.findByBranch_Id(branchId);
    }

    @GetMapping("/productInventory/{id}")
    public ResponseEntity<ProductInventory> getProInventById(@PathVariable Long id, Authentication authentication){
        ProductInventory pid = pis.findProductInventoryById(id);
        if (pid == null) return ResponseEntity.notFound().build();
        User actor = inventoryAccess.actor(authentication);
        Long branchId = inventoryAccess.visibleBranchId(actor);
        if (branchId != null && (pid.getBranch() == null || !branchId.equals(pid.getBranch().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar inventario de otra sucursal");
        }
        return ResponseEntity.ok(pid);
    }

    @PostMapping("/productInventory")
    public ProductInventory save(@RequestBody ProductInventory productInventory, Authentication authentication){
        User actor = inventoryAccess.actor(authentication);
        inventoryAccess.requireInventoryManager(actor);
        if (productInventory.getId() != null) {
            ProductInventory existing = inventoryRepository.findById(productInventory.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventario no encontrado"));
            inventoryAccess.ensureCanManageBranch(actor, existing.getBranch());
        }
        productInventory.setBranch(inventoryAccess.resolveWriteBranch(
                actor, productInventory.getBranch() == null ? null : productInventory.getBranch().getId()));
        return pis.saveProductInventory(productInventory);
    }

    @DeleteMapping("/productInventory/{id}")
    public ResponseEntity<Void> deleteProInvent(@PathVariable Long id, Authentication authentication){
        User actor = inventoryAccess.actor(authentication);
        inventoryAccess.requireInventoryManager(actor);
        ProductInventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventario no encontrado"));
        inventoryAccess.ensureCanManageBranch(actor, inventory.getBranch());
        boolean eliminado = pis.deleteProductInventory(id);
        if (eliminado){
            return ResponseEntity.noContent().build();
        }else {
            return ResponseEntity.notFound().build();
        }
    }
}
