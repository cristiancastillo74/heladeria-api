package com.heladeria.heladeria.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProductInventoryReportDTO {

    private String product;
    private String category;
    private Integer stock;
    private String branch;


}
