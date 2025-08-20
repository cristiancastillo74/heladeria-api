package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("helados")
@CrossOrigin(value = "http://localhost:55555")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/product")
    public List<Product> getProducts(){
        return productService.getProduct();
    }
}
