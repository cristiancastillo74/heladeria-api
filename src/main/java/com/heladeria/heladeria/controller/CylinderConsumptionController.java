package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.CylinderConsumption;
import com.heladeria.heladeria.service.CylinderConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("helados")
@CrossOrigin(value = "http://localhost:3001")
public class CylinderConsumptionController {

    @Autowired
    CylinderConsumptionService cylinderConsumptionService;

    @GetMapping("/cylinderConsumption")
    public List<CylinderConsumption> getCylinderConsumption(){
        var cylinderConsumption = cylinderConsumptionService.getCylinderConsumption();
        return  cylinderConsumption;
    }

    @GetMapping("/cylinderConsumption/{id}")
    public ResponseEntity<CylinderConsumption> getCylinderConsumptionById(@PathVariable Long id){
        CylinderConsumption cylinderConsumption = cylinderConsumptionService.searchCylinderConsumptionById(id);
        if(cylinderConsumption== null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cylinderConsumption);
    }

    @PostMapping("/cylinderConsumption")
    public CylinderConsumption saveCylinderConsumption(@RequestBody CylinderConsumption cylinderConsumption){
        return cylinderConsumptionService.saveCylinderConsumption(cylinderConsumption);
    }

    @DeleteMapping("/cylinderConsumption/{id}")
    public ResponseEntity<Void> deleteCylinderConsumption(@PathVariable Long id){
        boolean eliminado = cylinderConsumptionService.deleteCylinderConsumption(id);
        if(eliminado){
            return ResponseEntity.noContent().build();
        }
        return  ResponseEntity.notFound().build();
    }
}
