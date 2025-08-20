package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.model.ProductInventory;

import java.util.List;

public interface ProductInventoryService {
    public List<ProductInventory> getProductProductInventory();
    public ProductInventory saveProductInventory(ProductInventory productInventory);
    public ProductInventory findProductInventoryById(Long id);
    public boolean deleteProductInventory(Long id);
    public void disminuirStock(Long id, int cantidad);

}
