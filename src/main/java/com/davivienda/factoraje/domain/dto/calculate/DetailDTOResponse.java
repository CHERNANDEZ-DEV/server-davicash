package com.davivienda.factoraje.domain.dto.calculate;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailDTOResponse {

    private String cutOffDate;
    private String documentNumber;
    private BigDecimal amountToFinance;
    private BigDecimal interests;
    private BigDecimal commissions;
    private BigDecimal amountToBeDisbursed;
}
