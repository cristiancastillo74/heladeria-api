package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.CylinderConsumption;
import com.heladeria.heladeria.repository.CylinderConsumptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CylinderConsumptionServiceImp implements CylinderConsumptionService{

    @Autowired
    private CylinderConsumptionRepository cylinderConsumptionRepository;

    @Override
    public List<CylinderConsumption> getCylinderConsumption() {
        return cylinderConsumptionRepository.findAll();
    }

    @Override
    public CylinderConsumption saveCylinderConsumption(CylinderConsumption cylinderConsumption) {

        return cylinderConsumptionRepository.save(cylinderConsumption);
    }

    @Override
    public CylinderConsumption searchCylinderConsumptionById(Long id) {
        return cylinderConsumptionRepository.findById(id).orElse(null);
    }

    @Override
    public boolean deleteCylinderConsumption(Long id) {
        Optional<CylinderConsumption> cylinderConsumption = cylinderConsumptionRepository.findById(id);
        if(cylinderConsumption.isPresent()){
            cylinderConsumptionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
