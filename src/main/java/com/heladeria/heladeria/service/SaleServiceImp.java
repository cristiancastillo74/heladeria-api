package com.heladeria.heladeria.service;

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

        // Validar stock de productos normales antes de procesar
        for (SaleItem item : sale.getItems()) {
            Optional<ProductInventory> productInventoryOpt = productInventoryRepository.findById(item.getProduct().getId());
            if (productInventoryOpt.isPresent()) {
                ProductInventory productInventory = productInventoryOpt.get();
                if (!productInventory.getProduct().getIsIceCream()) {
                    if (productInventory.getStock() < item.getQuantity()) {
                        throw new RuntimeException("Stock insuficiente para producto: " + productInventory.getProduct().getName());
                    }
                }
            }
        }

        // Procesar items
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            BigDecimal price;

            Optional<ProductInventory> productInventoryOpt = productInventoryRepository.findById(product.getId());
            if (productInventoryOpt.isPresent()) {
                // Caso 1: Producto normal
                ProductInventory pi = productInventoryOpt.get();
                price = pi.getProduct().getPrice();

                // Actualizar stock
                if (!pi.getProduct().getIsIceCream()) {
                    productInventoryService.disminuirStock(product.getId(), item.getQuantity());
                }

            } else {
                // Caso 2: Cilindro (helado)
                CylinderInventory ci = cylinderInventoryRepository.findByProduct(product)
                        .orElseThrow(() -> new RuntimeException("Inventario de cilindro no encontrado para producto " + product.getName()));
                price = ci.getProduct().getPrice();

                if (ci.getProduct().getIsIceCream() && ci.getProduct().getBallsPerUnit() > 0) {
                    Optional<CylinderInventory> optionalCylinder = cylinderInventoryRepository.findFirstByProductAndBranchAndStatusOrderByCreatedAtAsc(
                            ci.getProduct(),
                            branch,
                            Status.CYLINDER_EN_USO
                    );

                    CylinderInventory cylinderInventory = optionalCylinder.orElseGet(() ->
                            cylinderInventoryRepository.findFirstByProductAndBranchAndStatusOrderByCreatedAtAsc(
                                    ci.getProduct(),
                                    branch,
                                    Status.CYLINDER_LLENO
                            ).orElseThrow(() -> new RuntimeException("No Cylinder available"))
                    );

                    // Si estaba lleno, pasarlo a EN_USO
                    if (cylinderInventory.getStatus() == Status.CYLINDER_LLENO) {
                        cylinderInventory.setStatus(Status.CYLINDER_EN_USO);
                    }

                    // Calcular consumo de bolas
                    int bolasConsumidas = item.getQuantity() * ci.getProduct().getBallsPerUnit();
                    double fraccionConsumida = (double) bolasConsumidas / (double) cylinderInventory.getCylinder().getEstimatedBalls();
                    double nuevaFraccion = cylinderInventory.getFraction() - fraccionConsumida;

                    cylinderInventory.setFraction(Math.max(0, nuevaFraccion));
                    if (nuevaFraccion <= 0) {
                        cylinderInventory.setStatus(Status.CYLINDER_VACIO);
                    }

                    cylinderInventoryRepository.save(cylinderInventory);
                }
            }

            // Lógica común
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

        Sale savedSale = saleRepository.save(sale);

        return savedSale;
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
