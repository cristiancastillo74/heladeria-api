package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Cylinder;
import com.heladeria.heladeria.repository.CylinderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CylinderServiceImp implements CylinderService{

    @Autowired
    private CylinderRepository cylinderRepository;

    @Override
    public List<Cylinder> listarCylinder() {
        return cylinderRepository.findAll();
    }

    @Override
    public Cylinder obtenerCylinderPorId(Long idCylinder) {
        return cylinderRepository.findById(idCylinder).orElse(null);
    }

    @Override
    public Cylinder guardarCylinder(Cylinder cylinder) {
        return cylinderRepository.save(cylinder);
    }

    @Override
    public boolean eliminarCylinder(Long idCylinder) {
        Optional<Cylinder> cylinder = cylinderRepository.findById(idCylinder);
        if(cylinder.isPresent()){
            cylinderRepository.delete(cylinder.get());
            return true;
        }
        return false;
    }
}
