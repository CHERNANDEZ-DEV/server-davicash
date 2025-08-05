package com.davivienda.factoraje.controller;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davivienda.factoraje.domain.dto.Roles.AssignPermissionsDTO;
import com.davivienda.factoraje.domain.dto.Roles.RoleRequestDTO;
import com.davivienda.factoraje.domain.dto.Roles.RoleResponseDTO;
import com.davivienda.factoraje.domain.model.RoleModel;
import com.davivienda.factoraje.service.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
        log.info("RoleController initialized");
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRole(@RequestBody RoleRequestDTO roleRequest) {
        log.info("POST /api/roles/create - create role: {}", roleRequest);
        if (roleRequest == null) {
            log.warn("RoleRequestDTO is null");
            return ResponseEntity
                    .badRequest()
                    .body("El cuerpo de la petición no puede ser vacío");
        }
        try {
            RoleResponseDTO created = roleService.create(roleRequest);
            log.info("Role created with id={}", created.getRole_id());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(created);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid role data: {}", ex.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error creating role", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor al crear rol");
        }
    }

    @PostMapping("/assign-permission")
    public ResponseEntity<?> assignPermissionToRole(@RequestBody AssignPermissionsDTO assignRequest) {
        log.info("POST /api/roles/assign-permission - assign permissions: {}", assignRequest);
        if (assignRequest == null || assignRequest.getRole_id() == null) {
            log.warn("AssignPermissionsDTO or role_id is null");
            return ResponseEntity
                    .badRequest()
                    .body("El body o el role_id no pueden ser nulos");
        }
        try {
            RoleModel role = roleService.getById(assignRequest.getRole_id());
            RoleResponseDTO updated = roleService.assignPermissions(role, assignRequest.getPermissions());
            log.info("Permissions assigned to role_id={}", assignRequest.getRole_id());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid data in assign-permission: {}", ex.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ex.getMessage());
        } catch (RuntimeException ex) {
            log.warn("Resource not found: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error assigning permissions to role", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor al asignar permisos al rol");
        }
    }

    @GetMapping("/get-roles")
    public ResponseEntity<?> getAllRoles() {
        log.info("GET /api/roles/get-roles - fetch all roles");
        try {
            Set<RoleResponseDTO> roles = roleService.getAll();
            log.info("Found {} roles", roles.size());
            return ResponseEntity.ok(roles);
        } catch (Exception ex) {
            log.error("Error fetching all roles", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor al obtener roles");
        }
    }
}
