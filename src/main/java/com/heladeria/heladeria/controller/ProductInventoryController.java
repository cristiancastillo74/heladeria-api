package com.heladeria.heladeria.controller;



import com.heladeria.heladeria.model.ProductInventory;
import com.heladeria.heladeria.service.ProductInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("helados")
@CrossOrigin(value = "*")
public class ProductInventoryController {

    @Autowired
    private ProductInventoryService pis;

    @GetMapping("/productInventory")
    public List<ProductInventory> getPI(){
        return pis.getProductProductInventory();
    }

    @GetMapping("/productInventory/{id}")
    public ResponseEntity<ProductInventory> getProInventById(@PathVariable Long id){
        ProductInventory pid = pis.findProductInventoryById(id);
        return ResponseEntity.ok(pid);
    }

    @PostMapping("/productInventory")
    public ProductInventory save(@RequestBody ProductInventory productInventory){
        return pis.saveProductInventory(productInventory);
    }

    @DeleteMapping("/productInventory/{id}")
    public ResponseEntity<Void> deleteProInvent(@PathVariable Long id){
        boolean eliminado = pis.deleteProductInventory(id);
        if (eliminado){
            return ResponseEntity.noContent().build();
        }else {
            return ResponseEntity.notFound().build();
        }
    }
}
