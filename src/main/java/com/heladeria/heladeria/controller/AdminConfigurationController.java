package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.dto.BranchRequestDTO;
import com.heladeria.heladeria.dto.CatalogOptionDTO;
import com.heladeria.heladeria.dto.CatalogValueRequestDTO;
import com.heladeria.heladeria.dto.CylinderTypeRequestDTO;
import com.heladeria.heladeria.dto.InventoryThresholdDTO;
import com.heladeria.heladeria.dto.ThresholdUpdateDTO;
import com.heladeria.heladeria.model.Cylinder;
import com.heladeria.heladeria.model.Product;
import com.heladeria.heladeria.repository.CylinderRepository;
import com.heladeria.heladeria.model.Branch;
import com.heladeria.heladeria.model.CylinderType;
import com.heladeria.heladeria.model.ProductCategory;
import com.heladeria.heladeria.model.ProductStatus;
import com.heladeria.heladeria.repository.BranchRepository;
import com.heladeria.heladeria.repository.CylinderTypeRepository;
import com.heladeria.heladeria.repository.ProductCategoryRepository;
import com.heladeria.heladeria.repository.ProductRepository;
import com.heladeria.heladeria.repository.ProductStatusRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/admin")
@Transactional
public class AdminConfigurationController {
    private final BranchRepository branchRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductStatusRepository statusRepository;
    private final CylinderTypeRepository cylinderTypeRepository;
    private final ProductRepository productRepository;
    private final CylinderRepository cylinderRepository;

    public AdminConfigurationController(
            BranchRepository branchRepository,
            ProductCategoryRepository categoryRepository,
            ProductStatusRepository statusRepository,
            CylinderTypeRepository cylinderTypeRepository,
            ProductRepository productRepository,
            CylinderRepository cylinderRepository) {
        this.branchRepository = branchRepository;
        this.categoryRepository = categoryRepository;
        this.statusRepository = statusRepository;
        this.cylinderTypeRepository = cylinderTypeRepository;
        this.productRepository = productRepository;
        this.cylinderRepository = cylinderRepository;
    }

    @GetMapping("/branches")
    public List<Branch> branches() {
        return branchRepository.findAll();
    }

    @PostMapping("/branches")
    public Branch createBranch(@Valid @RequestBody BranchRequestDTO request) {
        ensureBranchNameAvailable(request.name(), null);
        Branch branch = new Branch();
        applyBranch(branch, request);
        return branchRepository.save(branch);
    }

    @PutMapping("/branches/{id}")
    public Branch updateBranch(@PathVariable Long id, @Valid @RequestBody BranchRequestDTO request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal no encontrada"));
        ensureBranchNameAvailable(request.name(), id);
        applyBranch(branch, request);
        return branchRepository.save(branch);
    }

    @DeleteMapping("/branches/{id}")
    public void deleteBranch(@PathVariable Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal no encontrada"));
        try {
            branchRepository.delete(branch);
            branchRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar una sucursal que tiene usuarios o movimientos asociados");
        }
    }

    @GetMapping("/categories")
    public List<CatalogOptionDTO> categories() {
        return categoryRepository.findAll().stream().map(value -> new CatalogOptionDTO(value.getId(), value.getName(), null)).toList();
    }

    @PostMapping("/categories")
    public CatalogOptionDTO createCategory(@Valid @RequestBody CatalogValueRequestDTO request) {
        ensureUniqueName(categoryRepository.findByNameIgnoreCase(request.name()).isPresent());
        ProductCategory value = new ProductCategory();
        value.setName(cleanName(request.name()));
        return categoryOption(categoryRepository.save(value));
    }

    @PutMapping("/categories/{id}")
    public CatalogOptionDTO updateCategory(@PathVariable Long id, @Valid @RequestBody CatalogValueRequestDTO request) {
        ProductCategory value = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        String oldName = value.getName();
        String newName = cleanName(request.name());
        categoryRepository.findByNameIgnoreCase(newName).filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> ensureUniqueName(true));
        value.setName(newName);
        ProductCategory saved = categoryRepository.save(value);
        if (!oldName.equals(newName)) productRepository.renameCategory(oldName, newName);
        return categoryOption(saved);
    }

    @DeleteMapping("/categories/{id}")
    public void deleteCategory(@PathVariable Long id) {
        ProductCategory value = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        if (productRepository.existsByCategory(value.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría está asignada a productos");
        }
        categoryRepository.delete(value);
    }

    @GetMapping("/product-statuses")
    public List<CatalogOptionDTO> productStatuses() {
        return statusRepository.findAll().stream().map(value -> new CatalogOptionDTO(value.getId(), value.getName(), null)).toList();
    }

    @PostMapping("/product-statuses")
    public CatalogOptionDTO createProductStatus(@Valid @RequestBody CatalogValueRequestDTO request) {
        ensureUniqueName(statusRepository.findByNameIgnoreCase(request.name()).isPresent());
        ProductStatus value = new ProductStatus();
        value.setName(cleanName(request.name()));
        return statusOption(statusRepository.save(value));
    }

    @PutMapping("/product-statuses/{id}")
    public CatalogOptionDTO updateProductStatus(@PathVariable Long id, @Valid @RequestBody CatalogValueRequestDTO request) {
        ProductStatus value = statusRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado no encontrado"));
        String oldName = value.getName();
        String newName = cleanName(request.name());
        statusRepository.findByNameIgnoreCase(newName).filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> ensureUniqueName(true));
        value.setName(newName);
        ProductStatus saved = statusRepository.save(value);
        if (!oldName.equals(newName)) productRepository.renameStatus(oldName, newName);
        return statusOption(saved);
    }

    @DeleteMapping("/product-statuses/{id}")
    public void deleteProductStatus(@PathVariable Long id) {
        ProductStatus value = statusRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado no encontrado"));
        if (productRepository.existsByStatus(value.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El estado está asignado a productos");
        }
        statusRepository.delete(value);
    }

    @GetMapping("/cylinder-types")
    public List<CatalogOptionDTO> cylinderTypes() {
        return cylinderTypeRepository.findAll().stream().map(this::cylinderOption).toList();
    }

    @PostMapping("/cylinder-types")
    public CatalogOptionDTO createCylinderType(@Valid @RequestBody CylinderTypeRequestDTO request) {
        ensureUniqueName(cylinderTypeRepository.findByNameIgnoreCase(request.name()).isPresent());
        validateFraction(request.fraction());
        CylinderType value = new CylinderType();
        value.setName(cleanName(request.name()));
        value.setFraction(request.fraction());
        return cylinderOption(cylinderTypeRepository.save(value));
    }

    @PutMapping("/cylinder-types/{id}")
    public CatalogOptionDTO updateCylinderType(@PathVariable Long id, @Valid @RequestBody CylinderTypeRequestDTO request) {
        CylinderType value = cylinderTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de cilindro no encontrado"));
        cylinderTypeRepository.findByNameIgnoreCase(cleanName(request.name())).filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> ensureUniqueName(true));
        validateFraction(request.fraction());
        value.setName(cleanName(request.name()));
        value.setFraction(request.fraction());
        return cylinderOption(cylinderTypeRepository.save(value));
    }

    @DeleteMapping("/cylinder-types/{id}")
    public void deleteCylinderType(@PathVariable Long id) {
        CylinderType value = cylinderTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de cilindro no encontrado"));
        cylinderTypeRepository.delete(value);
    }

    @GetMapping("/inventory-thresholds/products")
    public List<InventoryThresholdDTO> productThresholds() {
        return productRepository.findAllByOrderByNameAsc().stream()
                .map(product -> new InventoryThresholdDTO(product.getId(), product.getName(),
                        product.getLowStockThreshold() == null ? 0 : product.getLowStockThreshold(), "unidades"))
                .toList();
    }

    @PutMapping("/inventory-thresholds/products/{id}")
    public InventoryThresholdDTO updateProductThreshold(
            @PathVariable Long id, @Valid @RequestBody ThresholdUpdateDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        double threshold = request.threshold().doubleValue();
        if (threshold < 0 || threshold != Math.floor(threshold)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mínimo de stock debe ser un entero no negativo");
        }
        product.setLowStockThreshold((int) threshold);
        productRepository.save(product);
        return new InventoryThresholdDTO(product.getId(), product.getName(), product.getLowStockThreshold(), "unidades");
    }

    @GetMapping("/inventory-thresholds/cylinders")
    public List<InventoryThresholdDTO> cylinderThresholds() {
        return cylinderRepository.findAllByOrderByFlavorAsc().stream()
                .map(cylinder -> new InventoryThresholdDTO(cylinder.getId(), cylinder.getFlavor(),
                (cylinder.getLowFractionThreshold() == null ? 0.2 : cylinder.getLowFractionThreshold()) * 100, "%"))
                .toList();
    }

    @PutMapping("/inventory-thresholds/cylinders/{id}")
    public InventoryThresholdDTO updateCylinderThreshold(
            @PathVariable Long id, @Valid @RequestBody ThresholdUpdateDTO request) {
        Cylinder cylinder = cylinderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sabor de cilindro no encontrado"));
        double threshold = request.threshold().doubleValue();
        if (threshold < 0 || threshold > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El porcentaje debe estar entre 0 y 100");
        }
        cylinder.setLowFractionThreshold(threshold / 100.0);
        cylinderRepository.save(cylinder);
        return new InventoryThresholdDTO(cylinder.getId(), cylinder.getFlavor(), request.threshold(), "%");
    }

    private void ensureBranchNameAvailable(String name, Long ignoredId) {
        branchRepository.findByNameIgnoreCase(cleanName(name)).filter(existing -> !existing.getId().equals(ignoredId))
                .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una sucursal con ese nombre"); });
    }

    private void applyBranch(Branch branch, BranchRequestDTO request) {
        branch.setName(cleanName(request.name()));
        branch.setLocation(request.location());
        branch.setPhone(request.phone());
    }

    private void ensureUniqueName(boolean exists) {
        if (exists) throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un valor con ese nombre");
    }

    private String cleanName(String name) {
        return name == null ? "" : name.trim();
    }

    private void validateFraction(Double fraction) {
        if (fraction == null || !Double.isFinite(fraction) || fraction < 0 || fraction > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fracción debe estar entre 0 y 1");
        }
    }

    private CatalogOptionDTO categoryOption(ProductCategory value) {
        return new CatalogOptionDTO(value.getId(), value.getName(), null);
    }

    private CatalogOptionDTO statusOption(ProductStatus value) {
        return new CatalogOptionDTO(value.getId(), value.getName(), null);
    }

    private CatalogOptionDTO cylinderOption(CylinderType value) {
        return new CatalogOptionDTO(value.getId(), value.getName(), value.getFraction());
    }
}