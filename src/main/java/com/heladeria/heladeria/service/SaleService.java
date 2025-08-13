package com.heladeria.heladeria.service;

import com.heladeria.heladeria.model.Sale;

import java.util.List;

public interface SaleService {

    public List<Sale> listarSale();

    public Sale buscarSalePorId(Long idSale);

    public Sale guardarSale(Sale sale);

    public boolean eliminarSale(Long id);

    // Lógica de negocio extra
    Sale crearVenta(Sale sale); // procesa venta con ítems y totales
    void cerrarDia(Long branchId); // cierra todas las ventas del día para una sucursal

}
