package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface ProductService {

    public List<Product> getProduct();
    public Product saveProduct(Product product);
    public Product findProductById(Long id);
    public boolean deleteProduct(Long id);
}
