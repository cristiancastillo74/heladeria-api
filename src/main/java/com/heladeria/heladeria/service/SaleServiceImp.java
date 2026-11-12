package com.heladeria.heladeria.service;

import com.heladeria.heladeria.dto.BallSelection;
import com.heladeria.heladeria.dto.ProductQuantity;
import com.heladeria.heladeria.model.*;
import com.heladeria.heladeria.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SaleServiceImp implements SaleService{

    @Autowired
    private  SaleRepository saleRepository;
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

    private static final Map<String, List<ProductQuantity>> HELADO_ADICIONALES = Map.of(
            "VT", List.of(new ProductQuantity("DVT", 1)),
            "PI", List.of(
                    new ProductQuantity("DPI", 1),
                    new ProductQuantity("BRQ", 1),
                    new ProductQuantity("MIEL", 1)
            ),
            "CUARTO", List.of(
                    new ProductQuantity("R14", 1),
                    new ProductQuantity("BRQ", 2),
                    new ProductQuantity("MIEL", 1)
            ),
            "MEDIO", List.of(
                    new ProductQuantity("R12", 1),
                    new ProductQuantity("BRQ", 3),
                    new ProductQuantity("MIEL", 1)
            )
    );

    @Override
    public List<Sale> listarSale() {
        return saleRepository.findAll();
    }

    @Override
    public List<Sale> listarVentasPorSucursal(Long branchId) {
        return saleRepository.findByBranch_IdOrderByCreatedAtDesc(branchId);
    }

    @Override
    public List<Sale> listarVentasPorUsuario(Long userId) {
        return saleRepository.findByUser_IdUserOrderByCreatedAtDesc(userId);
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
    public Sale corregirVenta(Long saleId, Sale replacement) {
        Sale original = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        Long userId = original.getUser().getIdUser();
        Long branchId = original.getBranch().getId();
        restaurarInventario(original);
        saleRepository.delete(original);
        saleRepository.flush();
        return crearVenta(replacement, userId, branchId);
    }

    @Override
    @Transactional
    public void eliminarVentaRestaurandoInventario(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        restaurarInventario(sale);
        saleRepository.delete(sale);
    }

    private void restaurarInventario(Sale sale) {
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            if (product == null) continue;
            if (Boolean.TRUE.equals(product.getIsIceCream())) {
                for (SaleFlavorSelection selection : item.getFlavorSelections()) {
                    Long consumedInventoryId = selection.getCylinderInventoryId();
                    if (consumedInventoryId != null) {
                        cylinderInventoryRepository.findById(consumedInventoryId)
                                .ifPresent(inventory -> restoreCylinderInventory(inventory, selection.getBalls()));
                    } else {
                        cylinderInventoryRepository.findAllByCylinderAndBranchOrderByCreatedAtAsc(
                                selection.getCylinder(), sale.getBranch()).stream()
                            .filter(inventory -> inventory.getFraction() == null || inventory.getFraction() < 1.0)
                            .findFirst()
                            .ifPresent(inventory -> restoreCylinderInventory(inventory, selection.getBalls()));
                    }
                }
            } else {
                productInventoryRepository.findByProductAndBranch(product, sale.getBranch())
                        .ifPresent(inventory -> {
                            inventory.setStock(inventory.getStock() + item.getQuantity());
                            productInventoryRepository.save(inventory);
                        });
            }
        }
    }

    private void restoreCylinderInventory(CylinderInventory inventory, Integer balls) {
        double estimatedBalls = inventory.getCylinder().getEstimatedBalls();
        if (estimatedBalls <= 0) return;
        double restored = inventory.getFraction() + (balls / estimatedBalls);
        inventory.setFraction(Math.min(1.0, restored));
        inventory.setStatus(restored >= 1.0 ? Status.CYLINDER_LLENO
                : restored > 0 ? Status.CYLINDER_EN_USO : Status.CYLINDER_VACIO);
        cylinderInventoryRepository.save(inventory);
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
                        .orElseThrow(() -> new RuntimeException("No hay inventario para producto: " + product.getName() + " en esta sucursal"));

                if (pi.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Stock insuficiente para producto: " + product.getName());
                }

                productInventoryService.disminuirStock(pi.getId(), item.getQuantity());
            }
            // Caso 2: Producto tipo helado
            else {

                // Validar que vengan selecciones de bolas
                if (item.getBallSelections() == null || item.getBallSelections().isEmpty()) {
                    throw new RuntimeException("Debe seleccionar un sorbete para el producto: " + product.getName());
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

                    if (cylinderInventory.getBranch() == null
                            || !cylinderInventory.getBranch().getId().equals(branch.getId())) {
                        throw new RuntimeException("El cilindro seleccionado no pertenece a la sucursal de la venta");
                    }

                    SaleFlavorSelection flavorSelection = new SaleFlavorSelection();
                    flavorSelection.setSaleItem(item);
                    flavorSelection.setCylinder(cylinderInventory.getCylinder());
                    flavorSelection.setCylinderInventoryId(cylinderInventory.getId());
                    flavorSelection.setBalls(selection.getBalls());
                    item.getFlavorSelections().add(flavorSelection);

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

                // Descontar adicionales si el producto tiene en el mapa
                List<ProductQuantity> adicionales = HELADO_ADICIONALES.get(product.getCode());

                if(adicionales != null){
                    for (ProductQuantity adicional : adicionales){
                        /*System.out.println("Buscando producto adicional con code: " + adicional.getProductCode());
                        System.out.println("Code recibido: '" + adicional.getProductCode() + "'");
*/
                        Product adicionalProduct = productRepository.findByCode(adicional.getProductCode())
                                .orElseThrow(() -> new RuntimeException("Producto adicional no encontrado: " ));
                        ProductInventory pi = productInventoryRepository.findByProductAndBranch(adicionalProduct, branch)
                                .orElseThrow(() -> new RuntimeException("no hay inventari de los productos: "));
                        int cantidadTotal = adicional.getQuantity() * item.getQuantity();

                        if(pi.getStock() < cantidadTotal){
                            throw new RuntimeException("inventario insuficiente para el producto: " );
                        }
                        System.out.println("adicionalProduct" + adicionalProduct.getId());

                        productInventoryService.disminuirStock(pi.getId(),cantidadTotal);
                    }
                }
            }

            // Lógica común (precio y subtotal)
            item.setPrice(price);
            item.setSale(sale);

            // ⚠️ Verificar si hay que ignorar el precio
            if (Boolean.TRUE.equals(item.getIgnorePrice())) {
                // No sumar al total
                item.setPrice(BigDecimal.ZERO); // ← opcional, para que quede claro en la base
            } else {
                BigDecimal subTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(subTotal);
            }
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
