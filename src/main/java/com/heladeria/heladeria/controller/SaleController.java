package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Sale;
import com.heladeria.heladeria.model.Role;
import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.repository.BranchRepository;
import com.heladeria.heladeria.repository.SaleRepository;
import com.heladeria.heladeria.repository.UserRepository;
import com.heladeria.heladeria.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/helados")
@CrossOrigin(value = "*")
@RequiredArgsConstructor
public class SaleController {

    @Autowired
    private SaleService saleService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BranchRepository branchRepository;

    @GetMapping("/sale")
    public List<Sale> getSales(){
        //hola
        return saleService.listarSale();
    }



    @PostMapping("/sale")
    public ResponseEntity<Sale> crearVenta(
        @RequestBody Sale sale,
        @RequestParam(value = "branchId", required = false) Long requestedBranchId,
        Authentication authentication
        ){
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Long branchId;
        if (user.getRole() == Role.ADMIN_GLOBAL) {
            if (requestedBranchId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona la sucursal para la venta");
            }
            branchId = branchRepository.findById(requestedBranchId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sucursal no encontrada"))
                    .getId();
        } else {
            if (user.getBranch() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu usuario no tiene sucursal asignada");
            }
            branchId = user.getBranch().getId();
        }

        Sale ventaCreada = saleService.crearVenta(sale, user.getIdUser(), branchId);
        return ResponseEntity.ok(ventaCreada);
    }
}
