package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.CylinderInventory;
import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.repository.CylinderInventoryRepository;
import com.heladeria.heladeria.security.InventoryAccessService;
import com.heladeria.heladeria.service.CylinderInventoryService;
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
public class CylinderInventoryController {

    @Autowired
    private CylinderInventoryService cylinderInventoryService;
    @Autowired
    private CylinderInventoryRepository inventoryRepository;
    @Autowired
    private InventoryAccessService inventoryAccess;

    @GetMapping("/cyInventory")
    public List<CylinderInventory> listarCylinderInventory(Authentication authentication){
        User actor = inventoryAccess.actor(authentication);
        Long branchId = inventoryAccess.visibleBranchId(actor);
        return branchId == null
                ? cylinderInventoryService.obtenerCylinderInventory()
                : cylinderInventoryService.getByBranch(branchId);
    }

    @GetMapping("cyInventory/buy")
    public List<CylinderInventory> listCyInvDisponiblesCondicional(Authentication authentication){
        User actor = inventoryAccess.actor(authentication);
        Long branchId = inventoryAccess.visibleBranchId(actor);
        return cylinderInventoryService.obtenerCylinderInventoryDisponiblesCondicional(branchId);
    }

    @GetMapping("/cyInventory/{id}")
    public ResponseEntity<CylinderInventory> obtenerPorIdCylinderInventory(@PathVariable Long id, Authentication authentication){
        CylinderInventory cylinderInventory = cylinderInventoryService.buscarPorIdCylinderInventory(id);
        if (cylinderInventory == null){
            return ResponseEntity.notFound().build();
        }
        User actor = inventoryAccess.actor(authentication);
        Long branchId = inventoryAccess.visibleBranchId(actor);
        if (branchId != null && (cylinderInventory.getBranch() == null
                || !branchId.equals(cylinderInventory.getBranch().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar inventario de otra sucursal");
        }
        return ResponseEntity.ok(cylinderInventory);
    }

    @PostMapping("/cyInventory")
    public CylinderInventory crear(@RequestBody CylinderInventory cylinderInventory, Authentication authentication){
        User actor = inventoryAccess.actor(authentication);
        inventoryAccess.requireInventoryManager(actor);
        if (cylinderInventory.getId() != null) {
            CylinderInventory existing = inventoryRepository.findById(cylinderInventory.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventario no encontrado"));
            inventoryAccess.ensureCanManageBranch(actor, existing.getBranch());
        }
        cylinderInventory.setBranch(inventoryAccess.resolveWriteBranch(
                actor, cylinderInventory.getBranch() == null ? null : cylinderInventory.getBranch().getId()));
        cylinderInventory.setUser(actor);
        return cylinderInventoryService.guardarCylinderInventory(cylinderInventory);
    }

    @DeleteMapping("/cyInventory/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        User actor = inventoryAccess.actor(authentication);
        inventoryAccess.requireInventoryManager(actor);
        CylinderInventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventario no encontrado"));
        inventoryAccess.ensureCanManageBranch(actor, inventory.getBranch());
        boolean eliminado = cylinderInventoryService.eliminarCylinderInventory(id);
        if (eliminado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/cyInventory/branch/{branchId}")
    public List<CylinderInventory> obtenerCylinderInventoryByBranchId(@PathVariable Long branchId, Authentication authentication){
        User actor = inventoryAccess.actor(authentication);
        Long visibleBranchId = inventoryAccess.visibleBranchId(actor);
        if (visibleBranchId != null && !visibleBranchId.equals(branchId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar inventario de otra sucursal");
        }
        return cylinderInventoryService.getByBranch(branchId);
    }

    @PutMapping("/cyInventory/{id}")
    public ResponseEntity<CylinderInventory> update(@PathVariable Long id, @RequestBody CylinderInventory cylinderInventory, Authentication authentication){
        User actor = inventoryAccess.actor(authentication);
        inventoryAccess.requireInventoryManager(actor);
        CylinderInventory cylinder = cylinderInventoryService.buscarPorIdCylinderInventory(id);
        if (cylinder != null){
            inventoryAccess.ensureCanManageBranch(actor, cylinder.getBranch());
            cylinderInventory.setBranch(inventoryAccess.resolveWriteBranch(
                    actor, cylinderInventory.getBranch() == null ? null : cylinderInventory.getBranch().getId()));
            cylinderInventory.setUser(actor);
            cylinderInventory.setId(id);
            return ResponseEntity.ok(cylinderInventoryService.guardarCylinderInventory(cylinderInventory));
        }
        return ResponseEntity.notFound().build();
    }


}
