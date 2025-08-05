package com.davivienda.factoraje.domain.dto.Emails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HTMLVariablesDTO {
    
    private String nombreEmpresa;
    private String numeroLineaCredito;
    private String nombreProveedor;
    private String numeroCuentaProveedor;
    private String montoDesembolsar;
}
