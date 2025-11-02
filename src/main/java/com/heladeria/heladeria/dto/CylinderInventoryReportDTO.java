package com.heladeria.heladeria.dto;

import com.heladeria.heladeria.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
public class CylinderInventoryReportDTO {
    private String flavor;
    private String branch;
    private Status status;
    private Double fraction;

    public CylinderInventoryReportDTO(String flavor, String branch, Status status, Double fraction) {
        this.flavor = flavor;
        this.branch = branch;
        this.status = status;
        this.fraction = fraction;
    }

}
