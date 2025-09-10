package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.CylinderInventory;
import com.heladeria.heladeria.service.CylinderInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("helados")
@CrossOrigin(value = "http://localhost:55555")
public class CylinderInventoryController {

    @Autowired
    private CylinderInventoryService cylinderInventoryService;

    @GetMapping("/cyInventory")
    public List<CylinderInventory> listarCylinderInventory(){
        return cylinderInventoryService.obtenerCylinderInventory();
    }

    @GetMapping("/cyInventory/{id}")
    public ResponseEntity<CylinderInventory> obtenerPorIdCylinderInventory(@PathVariable Long id){
        CylinderInventory cylinderInventory = cylinderInventoryService.buscarPorIdCylinderInventory(id);
        if (cylinderInventory != null){
            return ResponseEntity.ok(cylinderInventory);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/cyInventory")
    public CylinderInventory crear(@RequestBody CylinderInventory cylinderInventory){
        return cylinderInventoryService.guardarCylinderInventory(cylinderInventory);
    }

    @DeleteMapping("/cyInventory/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean eliminado = cylinderInventoryService.eliminarCylinderInventory(id);
        if (eliminado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/cyInventory/branch/{branchId}")
    public List<CylinderInventory> obtenerCylinderInventoryByBranchId(@PathVariable Long branchId){
        return cylinderInventoryService.getByBranch(branchId);
    }

    @PutMapping("/cyInventory/{id}")
    public ResponseEntity<CylinderInventory> update(@PathVariable Long id, @RequestBody CylinderInventory cylinderInventory){
        CylinderInventory cylinder = cylinderInventoryService.buscarPorIdCylinderInventory(id);
        if (cylinder != null){
            cylinderInventory.setId(id);
            return ResponseEntity.ok(cylinderInventoryService.guardarCylinderInventory(cylinderInventory));
        }
        return ResponseEntity.notFound().build();
    }


}
