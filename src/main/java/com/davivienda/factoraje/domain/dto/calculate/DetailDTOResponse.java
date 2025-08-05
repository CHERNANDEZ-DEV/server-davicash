package com.davivienda.factoraje.domain.dto.calculate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailDTOResponse {

    private String cutOffDate;
    private String documentNumber;
    private double amountToFinance;
    private double interests;
    private double commissions;
    private double amountToBeDisbursed;
}
