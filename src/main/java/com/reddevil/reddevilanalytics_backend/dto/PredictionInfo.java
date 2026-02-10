package com.reddevil.reddevilanalytics_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionInfo {
    private BigDecimal homeWinProb;
    private BigDecimal drawProb;
    private BigDecimal awayWinProb;
    private BigDecimal confidence;
}
