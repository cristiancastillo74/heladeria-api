package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.*;
import com.heladeria.heladeria.repository.BranchRepository;
import com.heladeria.heladeria.repository.ProductRepository;
import com.heladeria.heladeria.repository.SaleRepository;
import com.heladeria.heladeria.repository.UserRepository;
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
    private ProductService productService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private ProductRepository productRepository;

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
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            if(product.getIsIceCream() == false) {
                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Stock insufficient for product: " + product.getName());
                }
            }
        }

        for(SaleItem item : sale.getItems()){
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            item.setPrice(product.getPrice()); // toma automáticamente el precio del producto
            item.setSale(sale);
            BigDecimal subTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subTotal);

            //actualizar stock
            if(!product.getIsIceCream()){
                productService.disminuirStock(item.getProduct().getId(), item.getQuantity());
                }

            // 3️⃣ Registrar consumo de cilindros solo si es helado
            if(product.getIsIceCream() && product.getBallsPerUnit() > 0){
                int totalBolas = product.getBallsPerUnit() * item.getQuantity();
                cylinderConsumptionService.registrarConsumo(userId, branchId, totalBolas);

            }
        }

        for(SaleItem item : sale.getItems()){
            item.setSale(sale);  // vincula cada item con la venta
        }

        sale.setTotalAmount(total);
        sale.setDayClosed(false);
        sale.setCreatedAt(LocalDateTime.now());
        sale.setPaymentAmount(sale.getPaymentAmount() != null ? sale.getPaymentAmount() : BigDecimal.ZERO);
        sale.setChangeAmount(sale.getChangeAmount() != null ? sale.getChangeAmount() : BigDecimal.ZERO);


        Sale savedSale =  saleRepository.save(sale);

        // llenar productos completos para la respuesta JSON
        for (SaleItem item : savedSale.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            item.setProduct(product);
        }

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
