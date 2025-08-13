package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Cylinder;
import com.heladeria.heladeria.service.CylinderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("helados")
@CrossOrigin(value = "http://localhost:3001")
public class CylinderController {

    @Autowired
    private CylinderService cylinderService;

    @GetMapping("/cylinder")
    public List<Cylinder> obtenerCylinder(){
        var cylinder = cylinderService.listarCylinder();
        return cylinder;
    }

    @PostMapping("/cylinder")
    public Cylinder guardarCylinder(@RequestBody Cylinder cylinder){
        return cylinderService.guardarCylinder(cylinder);
    }

    @GetMapping("/cylinder/{id}")
    public ResponseEntity<Cylinder> obtenerCylinderById(@PathVariable Long id){
        Cylinder cylinder = cylinderService.obtenerCylinderPorId(id);
        if( cylinder == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cylinder);
    }

    @DeleteMapping("cylinder/{id}")
    public ResponseEntity<Void> deleteCylinderById(@PathVariable Long id){
        boolean eliminado = cylinderService.eliminarCylinder(id);
        if(!eliminado){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
