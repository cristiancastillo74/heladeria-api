package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Product;

import java.util.List;

public interface ProductService {

    public List<Product> getProduct();
    public Product saveProduct(Product product);
    public Product findProductById(Long id);
    public boolean deleteProduct(Long id);
}
