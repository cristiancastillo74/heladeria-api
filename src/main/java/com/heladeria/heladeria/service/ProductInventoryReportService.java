package com.heladeria.heladeria.service;

import com.heladeria.heladeria.dto.ProductInventoryReportDTO;
import com.heladeria.heladeria.repository.ProductInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface ProductInventoryReportService {

    public List<ProductInventoryReportDTO> getCurrentInventoryReport();

}
