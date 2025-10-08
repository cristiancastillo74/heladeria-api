package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.CylinderInventory;

import java.util.List;

public interface CylinderInventoryService {
    public List<CylinderInventory> obtenerCylinderInventory();
    public CylinderInventory guardarCylinderInventory(CylinderInventory cylinderInventory);
    public CylinderInventory buscarPorIdCylinderInventory(Long id);
    public boolean eliminarCylinderInventory(Long id);
    public List<CylinderInventory> getByBranch(Long branchId);
    public List<CylinderInventory> obtenerCylinderInventoryDisponiblesCondicional();
}
