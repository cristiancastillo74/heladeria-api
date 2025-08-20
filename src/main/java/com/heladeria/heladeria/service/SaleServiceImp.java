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

        // Primero validar stock de todos los items antes de modificar nada
        for (SaleItem item : sale.getItems()) {
            Optional<ProductInventory> productInventoryOpt  = productInventoryRepository.findById(item.getProduct().getId());
            if (productInventoryOpt.isPresent()) {
                ProductInventory productInventory = productInventoryOpt.get();
                if (productInventory.getProduct().getIsIceCream() == false) {
                    if (productInventory.getStock() < item.getQuantity()) {
                        throw new RuntimeException("Stock insufficient for product: " + productInventory.getProduct().getName());
                    }
                }
            }
        }

        for(SaleItem item : sale.getItems()){
            Optional<ProductInventory> productInventoryOpt  = productInventoryRepository.findById(item.getProduct().getId());
            if (productInventoryOpt.isPresent()) {
                ProductInventory productInventory = productInventoryOpt.get();
                }

                item.setPrice(productInventory.getProduct().getPrice()); // toma automáticamente el precio del producto
                item.setSale(sale);
                BigDecimal subTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(subTotal);

                //actualizar stock
                if(!productInventory.getProduct().getIsIceCream()){
                    productInventoryService.disminuirStock(item.getProduct().getId(), item.getQuantity());
                    }


            // 3️⃣ Registrar consumo de cilindros solo si es helado
            if(productInventory.getProduct().getIsIceCream() && productInventory.getProduct().getBallsPerUnit() > 0) {


                Optional<CylinderInventory> optionalCylinder = cylinderInventoryRepository.findFirstByProductAndBranchAndStatusOrderByCreatedAtAsc(
                        productInventory.getProduct(),
                        branch,
                        Status.CYLINDER_EN_USO
                );


                if (optionalCylinder.isEmpty()) {
                    throw new RuntimeException("No hay cilindros activos para este producto en la sucursal " + branch.getName());
                }

                CylinderInventory cylinderInventory;
                if (optionalCylinder.isPresent()) {
                    cylinderInventory = optionalCylinder.get();
                } else {
                    // 2️⃣ No hay en uso -> buscar uno lleno
                    cylinderInventory = cylinderInventoryRepository
                            .findFirstByProductAndBranchAndStatusOrderByCreatedAtAsc(
                                    productInventory.getProduct(),
                                    branch,
                                    Status.CYLINDER_LLENO
                            )
                            .orElseThrow(() -> new RuntimeException("No Cylinder available"));

                    // Cambiar su status a EN_USO
                    cylinderInventory.setStatus(Status.CYLINDER_EN_USO);
                }

                // Calcular fracción a restar
                int bolasConsumidas = item.getQuantity() * productInventory.getProduct().getBallsPerUnit();
                double fraccionConsumida = (double) bolasConsumidas / (double) cylinderInventory.getCylinder().getEstimatedBalls();
                double nuevaFraccion = cylinderInventory.getFraction() - fraccionConsumida;

                cylinderInventory.setFraction(Math.max(0, nuevaFraccion));

                if (nuevaFraccion <= 0) {
                    cylinderInventory.setStatus(Status.CYLINDER_VACIO);
                }

                cylinderInventoryRepository.save(cylinderInventory);


            }


        }

        for(SaleItem item : sale.getItems()){
            item.setSale(sale);  // vincula cada item con la venta
        }

        BigDecimal payment = sale.getPaymentAmount();
        if (payment == null || payment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Debe indicar con cuánto pagó el cliente");
        }

        if (payment.compareTo(total) < 0) {
            throw new RuntimeException("El pago es insuficiente");
        }

        //calculamos el vuelto en base a lo q pago el cliente
        BigDecimal change = payment.subtract(total);

        sale.setTotalAmount(total);
        sale.setDayClosed(false);
        sale.setCreatedAt(LocalDateTime.now());
        sale.setPaymentAmount(payment);
        sale.setChangeAmount(change);

        Sale savedSale =  saleRepository.save(sale);

        // llenar productos completos para la respuesta JSON
        /*for (SaleItem item : savedSale.getItems()) {
            Product product = productInventoryRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            item.setProduct(product);
        }*/

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
