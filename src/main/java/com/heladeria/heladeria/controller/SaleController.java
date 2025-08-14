package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Sale;
import com.heladeria.heladeria.repository.SaleRepository;
import com.heladeria.heladeria.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/helados")
@RequiredArgsConstructor
public class SaleController {

    @Autowired
    private SaleService saleService;

    @GetMapping("/sale")
    public List<Sale> getSales(){
        return saleService.listarSale();
    }

    @PostMapping("/sale")
    public ResponseEntity<Sale> crearVenta(
        @RequestBody Sale sale,
        @RequestParam Long userId,
        @RequestParam Long branchId
        ){
        Sale ventaCreada = saleService.crearVenta(sale,userId,branchId);
        return ResponseEntity.ok(ventaCreada);
    }
}
