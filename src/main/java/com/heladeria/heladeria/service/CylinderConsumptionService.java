package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.CylinderConsumption;

import java.util.List;

public interface CylinderConsumptionService {
    public List<CylinderConsumption> getCylinderConsumption();
    public CylinderConsumption saveCylinderConsumption(CylinderConsumption cylinderConsumption);
    public CylinderConsumption searchCylinderConsumptionById(Long id);
    public boolean deleteCylinderConsumption(Long id);
}
