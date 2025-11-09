package com.heladeria.heladeria.service;

import com.heladeria.heladeria.dto.BalanceReportDTO;

import java.time.LocalDateTime;

public interface BalanceReportService {
    public BalanceReportDTO getBalance(LocalDateTime startDate, LocalDateTime endDate);
}
