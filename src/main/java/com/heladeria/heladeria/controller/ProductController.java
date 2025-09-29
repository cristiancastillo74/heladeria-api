package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("helados")
@CrossOrigin(value = "*")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/product")
    public List<Product> getProducts(){
        return productService.getProduct();
    }

    @PostMapping("/product")
    public Product saveProduct(@RequestBody Product product){
        return productService.saveProduct(product);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductByID(@PathVariable Long id) {
        Product product = productService.findProductById(id);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        boolean eliminado = productService.deleteProduct(id);
        if (eliminado) {
            return ResponseEntity.noContent().build(); // 204 si se borró
        } else {
            return ResponseEntity.notFound().build(); // 404 si no existe
        }
    }

}
