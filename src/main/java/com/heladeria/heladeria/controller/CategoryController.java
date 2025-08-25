package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Category;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:55555")
@RestController
@RequestMapping("/enum")
public class CategoryController {

    @GetMapping("/category")
    public Category[] getCategory(){
        return Category.values();
    }
}
