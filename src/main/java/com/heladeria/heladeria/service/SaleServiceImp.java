package com.heladeria.heladeria.service;

import com.heladeria.heladeria.dto.BallSelection;
import com.heladeria.heladeria.model.*;
import com.heladeria.heladeria.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SaleServiceImp implements SaleService{

    @Autowired
    private  SaleRepository saleRepository;
    @Autowired
    private CylinderConsumptionService cylinderConsumptionService;
    @Autowired
    private ProductInventoryService productInventoryService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductInventoryRepository productInventoryRepository;
    @Autowired
    private CylinderInventoryRepository cylinderInventoryRepository;
    @Autowired
    private CylinderRepository cylinderRepository;

    @Override
    public List<Sale> listarSale() {
        return saleRepository.findAll();
    }

    @Override
    public Sale buscarSalePorId(Long idSale) {
        return saleRepository.findById(idSale).orElse(null);
    }

    @Override
    @Transactional
    public Sale guardarSale(Sale sale) {
        return saleRepository.save(sale);
    }

    @Override
    @Transactional
    public boolean eliminarSale(Long id) {
        Optional<Sale> sale = saleRepository.findById(id);
        if (sale.isPresent()){
            saleRepository.delete(sale.get());
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Sale crearVenta(Sale sale, Long userId, Long branchId) {
        // Validar que haya items
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new RuntimeException("No hay items en la venta");
        }

        // Obtener usuario y sucursal
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        sale.setUser(user);
        sale.setBranch(branch);

        BigDecimal total = BigDecimal.ZERO;

        // Procesar items
        for (SaleItem item : sale.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            item.setProduct(product);

            BigDecimal price = product.getPrice();

            // Caso 1: Producto normal (no es helado)
            if (!product.getIsIceCream()) {
                ProductInventory pi = productInventoryRepository.findByProductAndBranch(product, branch)
                        .orElseThrow(() -> new RuntimeException("No hay inventario para producto: " + product.getName()));

                if (pi.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Stock insuficiente para producto: " + product.getName());
                }

                productInventoryService.disminuirStock(product.getId(), item.getQuantity());
            }
            // Caso 2: Producto tipo helado
            else {
                // Validar que vengan selecciones de bolas
                if (item.getBallSelections() == null || item.getBallSelections().isEmpty()) {
                    throw new RuntimeException("Debe seleccionar cilindros/sabores para el producto: " + product.getName());
                }

                // Calcular cuántas bolas debería llevar en total
                int bolasEsperadas = product.getBallsPerUnit() * item.getQuantity();
                int bolasSeleccionadas = item.getBallSelections().stream()
                        .mapToInt(BallSelection::getBalls)
                        .sum();

                if (bolasEsperadas != bolasSeleccionadas) {
                    throw new RuntimeException("El producto " + product.getName() +
                            " requiere " + bolasEsperadas + " bolas, pero seleccionó " + bolasSeleccionadas);
                }

                // Descontar bolas de cada cilindro
                for (BallSelection selection : item.getBallSelections()) {
                    CylinderInventory cylinderInventory = cylinderInventoryRepository.findById(selection.getCylinderId())
                            .orElseThrow(() -> new RuntimeException("Cilindro no encontrado: " + selection.getCylinderId()));

                    if (!cylinderInventory.getBranch().getId().equals(branch.getId())) {
                        throw new RuntimeException("El cilindro no pertenece a la sucursal actual");
                    }

                    int bolasConsumidas = selection.getBalls();
                    double fraccionConsumida = (double) bolasConsumidas /
                            (double) cylinderInventory.getCylinder().getEstimatedBalls();

                    double nuevaFraccion = cylinderInventory.getFraction() - fraccionConsumida;
                    cylinderInventory.setFraction(Math.max(0, nuevaFraccion));

                    if (nuevaFraccion <= 0) {
                        cylinderInventory.setStatus(Status.CYLINDER_VACIO);
                    } else if (cylinderInventory.getStatus() == Status.CYLINDER_LLENO) {
                        cylinderInventory.setStatus(Status.CYLINDER_EN_USO);
                    }

                    cylinderInventoryRepository.save(cylinderInventory);
                }
            }

            // Lógica común (precio y subtotal)
            item.setPrice(price);
            item.setSale(sale);

            BigDecimal subTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subTotal);
        }

        for (SaleItem item : sale.getItems()) {
            item.setSale(sale);  // vincula cada item con la venta
        }

        BigDecimal payment = sale.getPaymentAmount();
        if (payment == null || payment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Debe indicar con cuánto pagó el cliente");
        }

        if (payment.compareTo(total) < 0) {
            throw new RuntimeException("El pago es insuficiente");
        }

        // calcular vuelto
        BigDecimal change = payment.subtract(total);

        sale.setTotalAmount(total);
        sale.setDayClosed(false);
        sale.setCreatedAt(LocalDateTime.now());
        sale.setPaymentAmount(payment);
        sale.setChangeAmount(change);

        return saleRepository.save(sale);
    }

    @Override
    @Transactional
    public void cerrarDia(Long branchId) {
        // Cierra todas las ventas de hoy para una sucursal
        List<Sale> ventasHoy = saleRepository.findByBranchIdAndCreatedAtBetween(
                branchId,
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(23, 59, 59)
        );

        for (Sale sale : ventasHoy) {
            sale.setDayClosed(true);
        }
        saleRepository.saveAll(ventasHoy);
    }

}
