package com.davivienda.factoraje.domain.dto.calculate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PercentageDTORequest {

    private double interestRate;
    private double base;
    private int days;
}
