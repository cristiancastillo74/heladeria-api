package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.CylinderInventory;
import com.heladeria.heladeria.model.Expense;
import com.heladeria.heladeria.model.Status;
import com.heladeria.heladeria.repository.CylinderInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CylinderInventoryServiceImp implements CylinderInventoryService{

    @Autowired
    private CylinderInventoryRepository cylinderInventoryRepository;

    @Override
    public List<CylinderInventory> obtenerCylinderInventory() {
        return cylinderInventoryRepository.findAll();
    }


    public List<CylinderInventory> obtenerCylinderInventoryNoEmpty(){
        return cylinderInventoryRepository.findByStatusNot(Status.CYLINDER_VACIO);
    }

    public List<CylinderInventory> obtenerCylinderInventoryDisponiblesCondicional() {
        // 1. Traer todos menos los vacíos
        List<CylinderInventory> todos = cylinderInventoryRepository.findByStatusNot(Status.CYLINDER_VACIO);

        // 2. Agrupar por sabor
        Map<String, List<CylinderInventory>> agrupadoPorSabor = todos.stream()
                .collect(Collectors.groupingBy(cy -> cy.getCylinder().getFlavor()));

        List<CylinderInventory> resultado = new ArrayList<>();

        for (Map.Entry<String, List<CylinderInventory>> entry : agrupadoPorSabor.entrySet()) {
            String sabor = entry.getKey();
            List<CylinderInventory> cilindrosDelSabor = entry.getValue();

            // 3. Revisar si hay alguno en estado EN_USO
            boolean hayEnUso = cilindrosDelSabor.stream()
                    .anyMatch(cy -> cy.getStatus() == Status.CYLINDER_EN_USO);

            // 4. Filtrar según la regla
            List<CylinderInventory> filtrados;

            if (hayEnUso) {
                // Si hay en uso, excluir los llenos
                filtrados = cilindrosDelSabor.stream()
                        .filter(cy -> cy.getStatus() != Status.CYLINDER_LLENO)
                        .collect(Collectors.toList());
            } else {
                // Si no hay en uso, incluir todos (menos vacíos que ya excluimos)
                filtrados = cilindrosDelSabor;
            }

            resultado.addAll(filtrados);
        }

        return resultado;
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
