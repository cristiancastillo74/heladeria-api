package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.model.ProductInventory;
import com.heladeria.heladeria.repository.ProductInventoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductInventoryServiceImp implements ProductInventoryService{

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Override
    public List<ProductInventory> getProductProductInventory() {
        return productInventoryRepository.findAll();
    }

    @Override
    public ProductInventory saveProductInventory(ProductInventory productInventory) {
        return productInventoryRepository.save(productInventory);
    }

    @Override
    public ProductInventory findProductInventoryById(Long id) {
        return productInventoryRepository.findById(id).orElse(null);
    }

    @Override
    public boolean deleteProductInventory(Long id) {
        Optional<ProductInventory> eliminado = productInventoryRepository.findById(id);
        if (eliminado.isPresent()){
            productInventoryRepository.delete(eliminado.get());
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void disminuirStock(Long id, int quantity) {
        ProductInventory productInventory = productInventoryRepository
                .findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        if(productInventory.getStock() < quantity){
            throw new RuntimeException("Stock insufficient");
        }
        productInventory.setStock(productInventory.getStock() - quantity);
        productInventoryRepository.save(productInventory);
    }
}
