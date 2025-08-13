package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImp implements ProductService{

    @Autowired
    ProductRepository productRepository;
    @Override
    public List<Product> getProduct() {
        return productRepository.findAll();
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product findProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public boolean deleteProduct(Long id) {
        Optional<Product> eliminado = productRepository.findById(id);
        if (eliminado.isPresent()){
            productRepository.delete(eliminado.get());
            return true;
        }
        return false;
    }
}
