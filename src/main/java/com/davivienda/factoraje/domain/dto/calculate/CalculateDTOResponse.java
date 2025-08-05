package com.davivienda.factoraje.domain.dto.calculate;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateDTOResponse {

    private double amountToFinance;
    private double interests;
    private double commissions;
    private double amountToBeDisbursed;
    private List<DetailDTOResponse> detail = new ArrayList<>();
}
