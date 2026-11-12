package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.dto.PurchaseOrderCandidateDTO;
import com.heladeria.heladeria.dto.PurchaseOrderItemRequestDTO;
import com.heladeria.heladeria.dto.PurchaseOrderItemResponseDTO;
import com.heladeria.heladeria.dto.PurchaseOrderRequestDTO;
import com.heladeria.heladeria.dto.PurchaseOrderResponseDTO;
import com.heladeria.heladeria.model.Branch;
import com.heladeria.heladeria.model.Cylinder;
import com.heladeria.heladeria.model.CylinderInventory;
import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.model.ProductInventory;
import com.heladeria.heladeria.model.PurchaseOrder;
import com.heladeria.heladeria.model.PurchaseOrderItem;
import com.heladeria.heladeria.model.PurchaseOrderType;
import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.repository.BranchRepository;
import com.heladeria.heladeria.repository.CylinderInventoryRepository;
import com.heladeria.heladeria.repository.CylinderRepository;
import com.heladeria.heladeria.repository.ProductInventoryRepository;
import com.heladeria.heladeria.repository.ProductRepository;
import com.heladeria.heladeria.repository.PurchaseOrderRepository;
import com.heladeria.heladeria.security.InventoryAccessService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/purchase-orders")
@Transactional
public class PurchaseOrderController {
    private final PurchaseOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final CylinderRepository cylinderRepository;
    private final CylinderInventoryRepository cylinderInventoryRepository;
    private final BranchRepository branchRepository;
    private final InventoryAccessService inventoryAccess;

    public PurchaseOrderController(
            PurchaseOrderRepository orderRepository,
            ProductRepository productRepository,
            ProductInventoryRepository productInventoryRepository,
            CylinderRepository cylinderRepository,
            CylinderInventoryRepository cylinderInventoryRepository,
            BranchRepository branchRepository,
            InventoryAccessService inventoryAccess) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productInventoryRepository = productInventoryRepository;
        this.cylinderRepository = cylinderRepository;
        this.cylinderInventoryRepository = cylinderInventoryRepository;
        this.branchRepository = branchRepository;
        this.inventoryAccess = inventoryAccess;
    }

    @GetMapping("/{type}/candidates")
    public List<PurchaseOrderCandidateDTO> candidates(
            @PathVariable PurchaseOrderType type,
            @RequestParam Long branchId) {
        Branch branch = findBranch(branchId);
        if (type == PurchaseOrderType.PRODUCT) {
            Map<Long, Integer> stockByProduct = new HashMap<>();
            for (ProductInventory inventory : productInventoryRepository.findByBranch_Id(branchId)) {
                if (inventory.getProduct() != null && inventory.getStock() != null) {
                    stockByProduct.merge(inventory.getProduct().getId(), inventory.getStock(), Integer::sum);
                }
            }
            return productRepository.findAll().stream()
                    .filter(product -> !Boolean.TRUE.equals(product.getIsIceCream()))
                    .map(product -> new PurchaseOrderCandidateDTO(
                            product.getId(), product.getName(),
                            stockByProduct.getOrDefault(product.getId(), 0).doubleValue(),
                            "unidades", product.getPurchasePrice()))
                    .sorted(Comparator.comparing(PurchaseOrderCandidateDTO::currentStock)
                            .thenComparing(PurchaseOrderCandidateDTO::name, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }

        Map<Long, Double> stockByCylinder = new HashMap<>();
        for (CylinderInventory inventory : cylinderInventoryRepository.findByBranch_Id(branchId)) {
            if (inventory.getCylinder() != null && inventory.getFraction() != null) {
                stockByCylinder.merge(inventory.getCylinder().getId(), inventory.getFraction(), Double::sum);
            }
        }
        return cylinderRepository.findAllByOrderByFlavorAsc().stream()
                .map(cylinder -> new PurchaseOrderCandidateDTO(
                        cylinder.getId(), cylinder.getFlavor(),
                        stockByCylinder.getOrDefault(cylinder.getId(), 0.0),
                        "cilindros equivalentes", cylinder.getPurchasePrice()))
                .sorted(Comparator.comparing(PurchaseOrderCandidateDTO::currentStock)
                        .thenComparing(PurchaseOrderCandidateDTO::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @GetMapping("/{type}")
    public List<PurchaseOrderResponseDTO> list(@PathVariable PurchaseOrderType type) {
        return orderRepository.findByTypeOrderByUpdatedAtDesc(type).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{type}/{id}")
    public PurchaseOrderResponseDTO get(@PathVariable PurchaseOrderType type, @PathVariable Long id) {
        return toResponse(findOrder(type, id));
    }

    @PostMapping("/{type}")
    public PurchaseOrderResponseDTO create(
            @PathVariable PurchaseOrderType type,
            @Valid @RequestBody PurchaseOrderRequestDTO request,
            org.springframework.security.core.Authentication authentication) {
        User actor = inventoryAccess.actor(authentication);
        Branch branch = findBranch(request.branchId());
        PurchaseOrder order = new PurchaseOrder();
        order.setType(type);
        order.setBranch(branch);
        order.setCreatedBy(actor);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(order.getCreatedAt());
        applyRequest(order, request);
        return toResponse(orderRepository.save(order));
    }

    @PutMapping("/{type}/{id}")
    public PurchaseOrderResponseDTO update(
            @PathVariable PurchaseOrderType type,
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderRequestDTO request) {
        PurchaseOrder order = findOrder(type, id);
        order.setBranch(findBranch(request.branchId()));
        order.setUpdatedAt(LocalDateTime.now());
        applyRequest(order, request);
        return toResponse(orderRepository.save(order));
    }

    private void applyRequest(PurchaseOrder order, PurchaseOrderRequestDTO request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agrega al menos un artículo al pedido");
        }
        order.setTitle(request.title() == null || request.title().isBlank()
                ? (order.getType() == PurchaseOrderType.PRODUCT ? "Pedido de productos" : "Pedido de helados")
                : request.title().trim());
        order.setIncludePrices(request.includePrices());
        order.getItems().clear();

        Map<Long, PurchaseOrderCandidateDTO> candidates = new HashMap<>();
        candidates(order.getType(), order.getBranch().getId()).forEach(candidate -> candidates.put(candidate.id(), candidate));
        for (PurchaseOrderItemRequestDTO lineRequest : request.items()) {
            PurchaseOrderCandidateDTO candidate = candidates.get(lineRequest.itemId());
            if (candidate == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El artículo no pertenece al tipo de pedido seleccionado");
            }
            BigDecimal price = lineRequest.unitPrice();
            if (request.includePrices() && (price == null || price.signum() < 0)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica un precio unitario válido para cada línea");
            }

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(order);
            item.setItemName(candidate.name());
            item.setCurrentStock(candidate.currentStock());
            item.setRequestedQuantity(lineRequest.quantity());
            item.setUnitPrice(request.includePrices() ? price : null);
            if (order.getType() == PurchaseOrderType.PRODUCT) {
                Product product = productRepository.findById(candidate.id()).orElseThrow();
                item.setProduct(product);
            } else {
                Cylinder cylinder = cylinderRepository.findById(candidate.id()).orElseThrow();
                item.setCylinder(cylinder);
            }
            order.getItems().add(item);
        }
    }

    private Branch findBranch(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sucursal no encontrada"));
    }

    private PurchaseOrder findOrder(PurchaseOrderType type, Long id) {
        return orderRepository.findByIdAndType(id, type)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte de pedido no encontrado"));
    }

    private PurchaseOrderResponseDTO toResponse(PurchaseOrder order) {
        List<PurchaseOrderItemResponseDTO> items = order.getItems().stream()
                .map(item -> new PurchaseOrderItemResponseDTO(
                        item.getProduct() != null ? item.getProduct().getId() : item.getCylinder().getId(),
                        item.getItemName(), item.getCurrentStock(),
                        order.getType() == PurchaseOrderType.PRODUCT ? "unidades" : "cilindros equivalentes",
                        item.getRequestedQuantity(), item.getUnitPrice()))
                .toList();
        return new PurchaseOrderResponseDTO(
                order.getId(), order.getType(), order.getTitle(), order.getBranch().getId(),
                order.getBranch().getName(), order.isIncludePrices(), order.getCreatedAt(),
                order.getUpdatedAt(), order.getCreatedBy().getUsername(), items);
    }
}