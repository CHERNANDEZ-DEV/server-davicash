package com.davivienda.factoraje.domain.dto.Auth;

import java.util.List;
import java.util.UUID;

import com.davivienda.factoraje.domain.model.EntityModel;
import com.davivienda.factoraje.domain.model.RoleModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTORequest {

    private String email;
    private String name;
    private String dui;
    private UUID entityId;
    private UUID roleId;
}
