package com.heladeria.heladeria.config;

import com.heladeria.heladeria.model.Category;
import com.heladeria.heladeria.model.CylinderType;
import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.model.ProductCategory;
import com.heladeria.heladeria.model.ProductStatus;
import com.heladeria.heladeria.repository.CylinderTypeRepository;
import com.heladeria.heladeria.repository.ProductCategoryRepository;
import com.heladeria.heladeria.repository.ProductRepository;
import com.heladeria.heladeria.repository.ProductStatusRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogBootstrap {
    @Bean
    CommandLineRunner initializeCatalogs(
            ProductCategoryRepository categoryRepository,
            ProductStatusRepository statusRepository,
            CylinderTypeRepository cylinderTypeRepository,
            ProductRepository productRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                for (Category category : Category.values()) {
                    ProductCategory value = new ProductCategory();
                    value.setName(category.name());
                    categoryRepository.save(value);
                }
            }
            if (statusRepository.count() == 0) {
                for (String name : List.of("ACTIVO", "INACTIVO")) {
                    ProductStatus value = new ProductStatus();
                    value.setName(name);
                    statusRepository.save(value);
                }
            }
            if (cylinderTypeRepository.count() == 0) {
                seedCylinderType(cylinderTypeRepository, "CILINDRO_COMPLETO", 1.0);
                seedCylinderType(cylinderTypeRepository, "CILINDRO_MEDIO", 0.5);
                seedCylinderType(cylinderTypeRepository, "CILINDRO_VACIO", 0.0);
            }

            List<Product> products = productRepository.findAll().stream()
                    .filter(product -> product.getStatus() == null || product.getStatus().isBlank())
                    .toList();
            products.forEach(product -> product.setStatus("ACTIVO"));
            productRepository.saveAll(products);
        };
    }

    private void seedCylinderType(CylinderTypeRepository repository, String name, double fraction) {
        repository.findByNameIgnoreCase(name).orElseGet(() -> {
            CylinderType value = new CylinderType();
            value.setName(name);
            value.setFraction(fraction);
            return repository.save(value);
        });
    }
}