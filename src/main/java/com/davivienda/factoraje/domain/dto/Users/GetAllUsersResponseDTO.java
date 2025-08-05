package com.davivienda.factoraje.domain.dto.Users;

import java.util.UUID;

import com.davivienda.factoraje.domain.model.RoleModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllUsersResponseDTO {
    
    private UUID id;
    private String dui;
    private String name;
    private String roleName;
    private String entityName;

}
