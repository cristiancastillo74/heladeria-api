package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.*;
import com.heladeria.heladeria.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CylinderConsumptionServiceImp implements CylinderConsumptionService{

    @Autowired
    private CylinderConsumptionRepository cylinderConsumptionRepository;
    @Autowired
    private CylinderRepository cylinderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BranchRepository branchRepository;

    //( promedio = 200 per cylinder)
    private static final int BOLAS_POR_CILINDRO = 200;

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

    @Override
    @Transactional
    public void registrarConsumo(Long userId, Long branchId, int totalBolas) {
        // Obtener usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Obtener sucursal
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("branch not found"));

        // Obtener cilindro activo (primero disponible)
        Cylinder cylinder = cylinderRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("cylinders not availables"));

        //convierte bolas a fraccion
        double consumptionCylinder = (double) totalBolas / BOLAS_POR_CILINDRO;
        CylinderConsumption consumo = new CylinderConsumption();
        consumo.setCylinder(cylinder);
        consumo.setUser(user);
        consumo.setBranch(branch);
        consumo.setBallsConsumed(totalBolas);
        //consumo.setQuantity(consumptionCylinder);

        cylinderConsumptionRepository.save(consumo);
    }
}
