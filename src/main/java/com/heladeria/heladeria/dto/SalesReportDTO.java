package com.heladeria.heladeria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;


@NoArgsConstructor
@Data
public class SalesReportDTO {

    private Date date;
    private String product;
    private Long quantity;
    private BigDecimal total;
    private String branch;
    private String user;


    public SalesReportDTO(Date date, String product, Long quantity, BigDecimal total, String branch, String user) {
        this.date = date;
        this.product = product;
        this.quantity = quantity;
        this.total = total;
        this.branch = branch;
        this.user = user;
    }

}
