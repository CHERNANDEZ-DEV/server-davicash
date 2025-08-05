package com.davivienda.factoraje.domain.dto.calculate;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalculateDTORequest {

    private String cutOffDate;
    private String documentNumber;
    private Integer diffDays;
    private double amount;

}
