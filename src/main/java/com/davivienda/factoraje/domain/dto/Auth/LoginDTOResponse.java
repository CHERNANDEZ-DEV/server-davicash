package com.davivienda.factoraje.domain.dto.Auth;

import java.util.List;
import java.util.UUID;

import org.apache.poi.hssf.record.BoolErrRecord;

import com.davivienda.factoraje.domain.model.EntityModel;
import com.davivienda.factoraje.domain.model.RoleModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTOResponse {
    private String token;
}
