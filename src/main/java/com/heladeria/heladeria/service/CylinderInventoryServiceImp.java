package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.CylinderInventory;
import com.heladeria.heladeria.model.Expense;
import com.heladeria.heladeria.repository.CylinderInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CylinderInventoryServiceImp implements CylinderInventoryService{

    @Autowired
    private CylinderInventoryRepository cylinderInventoryRepository;

    @Override
    public List<CylinderInventory> obtenerCylinderInventory() {
        return cylinderInventoryRepository.findAll();
    }

    @Override
    public CylinderInventory guardarCylinderInventory(CylinderInventory cylinderInventory) {
        if (cylinderInventory.getId() == null) {
            // Es nuevo, asignar createdAt y updatedAt
            cylinderInventory.setCreatedAt(LocalDateTime.now());
            //cylinderInventory.setUpdatedAt(LocalDateTime.now());
        } else {
            // Es update, solo actualizar updatedAt
            // Para conservar createdAt original, buscar el registro actual
            CylinderInventory original = cylinderInventoryRepository.findById(cylinderInventory.getId())
                    .orElseThrow(() -> new RuntimeException("Expense no encontrado"));

            cylinderInventory.setCreatedAt(original.getCreatedAt()); // conservar createdAt
            cylinderInventory.setUpdatedAt(LocalDateTime.now());
        }
        return cylinderInventoryRepository.save(cylinderInventory);
    }

    @Override
    public CylinderInventory buscarPorIdCylinderInventory(Long id) {
        CylinderInventory cylinderInventory = cylinderInventoryRepository.findById(id).orElse(null);
        return cylinderInventory;
    }

    @Override
    public boolean eliminarCylinderInventory(Long id) {
        Optional<CylinderInventory> cylinderInventory = cylinderInventoryRepository.findById(id);
        if(cylinderInventory.isPresent()){
            cylinderInventoryRepository.delete(cylinderInventory.get());
            return true;
        }
        return false;
    }

    @Override
    public List<CylinderInventory> getByBranch(Long branchId) {
        return cylinderInventoryRepository.findByBranch_Id(branchId);
    }
}
