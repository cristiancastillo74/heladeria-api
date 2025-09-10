package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Status;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:55555")
@RestController
@RequestMapping("/status")
public class StatusController {

    @GetMapping("/all")
    public Status[] getStatus(){
        return Status.values();
    }
}
