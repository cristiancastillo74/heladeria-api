package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.repository.ProductRepository;
import com.heladeria.heladeria.repository.ProductCategoryRepository;
import com.heladeria.heladeria.repository.ProductStatusRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImp implements ProductService{

    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductCategoryRepository categoryRepository;
    @Autowired
    ProductStatusRepository statusRepository;
    @Override
    public List<Product> getProduct() {
        return productRepository.findAll();
    }

    @Override
    public Product saveProduct(Product product) {
        if (product.getPurchasePrice() == null || product.getPurchasePrice().signum() < 0) {
            throw new IllegalArgumentException("El precio de compra es obligatorio y no puede ser negativo");
        }
        if (product.getCategory() == null || !categoryRepository.existsByNameIgnoreCase(product.getCategory())) {
            throw new IllegalArgumentException("Selecciona una categoría existente");
        }
        if (product.getStatus() == null || product.getStatus().isBlank()
                || !statusRepository.existsByNameIgnoreCase(product.getStatus())) {
            throw new IllegalArgumentException("Selecciona un estado de producto existente");
        }
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
