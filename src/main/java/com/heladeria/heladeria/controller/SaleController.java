package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.model.Sale;
import com.heladeria.heladeria.model.SaleItem;
import com.heladeria.heladeria.model.Role;
import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.dto.SaleFlavorDetailDTO;
import com.heladeria.heladeria.dto.SaleHistoryDTO;
import com.heladeria.heladeria.dto.SaleItemDetailDTO;
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
    public List<SaleHistoryDTO> getSales(
            @RequestParam(value = "branchId", required = false) Long branchId,
            Authentication authentication){
        User actor = getActor(authentication);
        List<Sale> sales;
        if (actor.getRole() == Role.ADMIN_GLOBAL) {
            if (branchId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una sucursal");
            }
            if (!branchRepository.existsById(branchId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal no encontrada");
            }
            sales = saleService.listarVentasPorSucursal(branchId);
        } else {
            sales = saleService.listarVentasPorUsuario(actor.getIdUser());
        }
        return sales.stream().map(this::toHistory).toList();
    }

    @GetMapping("/sale/{id}")
    public SaleHistoryDTO getSale(@PathVariable Long id, Authentication authentication) {
        User actor = getActor(authentication);
        Sale sale = saleService.buscarSalePorId(id);
        if (sale == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada");
        if (actor.getRole() != Role.ADMIN_GLOBAL && !sale.getUser().getIdUser().equals(actor.getIdUser())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes consultar tus propias ventas");
        }
        return toHistory(sale);
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

    @PutMapping("/sale/{id}")
    public SaleHistoryDTO correctSale(@PathVariable Long id, @RequestBody Sale replacement, Authentication authentication) {
        requireGlobalAdmin(authentication);
        return toHistory(saleService.corregirVenta(id, replacement));
    }

    @DeleteMapping("/sale/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id, Authentication authentication) {
        requireGlobalAdmin(authentication);
        saleService.eliminarVentaRestaurandoInventario(id);
        return ResponseEntity.noContent().build();
    }

    private User getActor(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private void requireGlobalAdmin(Authentication authentication) {
        if (getActor(authentication).getRole() != Role.ADMIN_GLOBAL) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN_GLOBAL puede editar o eliminar ventas");
        }
    }

    private SaleHistoryDTO toHistory(Sale sale) {
        List<SaleItemDetailDTO> items = sale.getItems().stream().map(this::toItemDetail).toList();
        return new SaleHistoryDTO(sale.getId(), sale.getCreatedAt(), sale.getUser().getUsername(),
                sale.getBranch().getName(), sale.getTotalAmount(), sale.getPaymentAmount(),
                sale.getChangeAmount(), items);
    }

    private SaleItemDetailDTO toItemDetail(SaleItem item) {
        List<SaleFlavorDetailDTO> flavors = item.getFlavorSelections().stream()
                .map(selection -> new SaleFlavorDetailDTO(selection.getCylinder().getId(),
                        selection.getCylinder().getFlavor(), selection.getBalls()))
                .toList();
        return new SaleItemDetailDTO(item.getProduct().getId(), item.getProduct().getName(),
            item.getQuantity(), item.getPrice(), flavors,
            Boolean.TRUE.equals(item.getProduct().getIsIceCream()), Boolean.TRUE.equals(item.getIgnorePrice()));
    }
}
