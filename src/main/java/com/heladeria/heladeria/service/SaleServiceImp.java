package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Sale;
import com.heladeria.heladeria.model.SaleItem;
import com.heladeria.heladeria.repository.SaleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    public Sale crearVenta(Sale sale) {

        BigDecimal total = BigDecimal.ZERO;
        for(SaleItem item : sale.getItems()){
            BigDecimal subTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subTotal);

            //actualizar stock
            productService.disminuirStock(item.getProduct().getId(), item.getQuantity());

            // 3️⃣ Registrar consumo de cilindros
            cylinderConsumptionService.registrarConsumo(item.getProduct().getId(), item.getQuantity());
        }
        sale.setTotalAmount(total);
        sale.setDayClosed(false);

        return saleRepository.save(sale)

        sale.setDayClosed(false);
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
