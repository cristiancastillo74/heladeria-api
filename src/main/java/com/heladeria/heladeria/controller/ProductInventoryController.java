package com.heladeria.heladeria.controller;



import com.heladeria.heladeria.model.ProductInventory;
import com.heladeria.heladeria.service.ProductInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
