package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.repository.ProductCategoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/enum")
public class CategoryController {

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @GetMapping("/category")
    public List<String> getCategory(){
        return categoryRepository.findAll().stream().map(category -> category.getName()).sorted().toList();
    }
}
