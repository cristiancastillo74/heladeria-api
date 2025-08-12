package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Cylinder;

import java.util.List;

public interface CylinderService {
    public List<Cylinder> listarCylinder();
    public Cylinder obtenerCylinderPorId(Long idCylinder);
    public Cylinder guardarCylinder(Cylinder cylinder);
    public boolean eliminarCylinder(Long idCylinder);

}
