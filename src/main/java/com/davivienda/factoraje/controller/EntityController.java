package com.davivienda.factoraje.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.davivienda.factoraje.auth.RolesAllowed;
import com.davivienda.factoraje.domain.model.EntityModel;
import com.davivienda.factoraje.service.EntityService;

@RestController
@RequestMapping("/api/entities")
public class EntityController {

    private static final Logger log = LoggerFactory.getLogger(EntityController.class);
    private final EntityService entityService;

    public EntityController(EntityService entityService) {
        this.entityService = entityService;
        log.info("EntityController initialized");
    }

    @RolesAllowed({ "MANAGER" })
    @PostMapping("/create")
    public ResponseEntity<?> createEntity(@RequestBody EntityModel entity) {
        log.info("POST /api/entities/create - create entity: {}", entity);
        if (entity == null) {
            log.warn("Entity model is null");
            return ResponseEntity
                .badRequest()
                .body("El cuerpo de la petición no puede ser vacío");
        }
        try {
            EntityModel created = entityService.createEntity(entity);
            log.info("Entity created with id={}", created.getId());
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid entity data: {}", ex.getMessage());
            return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error creating entity", ex);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor al crear entidad");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getEntities() {
        log.info("GET /api/entities/all - get all entities");
        try {
            List<EntityModel> list = entityService.getEntities();
            log.info("Found {} entities", list.size());
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            log.error("Error fetching entities", ex);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor al obtener entidades");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEntityById(@PathVariable UUID id) {
        log.info("GET /api/entities/{} - get entity by id", id);
        if (id == null) {
            log.warn("ID is null");
            return ResponseEntity
                .badRequest()
                .body("El parámetro 'id' no puede ser nulo");
        }
        try {
            EntityModel entity = entityService.getEntityById(id);
            if (entity == null) {
                log.info("Entity not found for id={}", id);
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Entidad no encontrada");
            }
            log.info("Entity found for id={}", id);
            return ResponseEntity.ok(entity);
        } catch (Exception ex) {
            log.error("Error getting entity by id", ex);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor al obtener entidad");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEntity(@PathVariable UUID id, @RequestBody EntityModel entity) {
        log.info("PUT /api/entities/update/{} - update entity: {}", id, entity);
        if (id == null) {
            log.warn("ID is null");
            return ResponseEntity
                .badRequest()
                .body("El parámetro 'id' no puede ser nulo");
        }
        if (entity == null) {
            log.warn("Entity model is null");
            return ResponseEntity
                .badRequest()
                .body("El cuerpo de la petición no puede ser vacío");
        }
        try {
            EntityModel updated = entityService.updateEntity(id, entity);
            log.info("Entity updated for id={}", id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid data for update: {}", ex.getMessage());
            return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error updating entity", ex);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor al actualizar entidad");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEntity(@PathVariable UUID id) {
        log.info("DELETE /api/entities/delete/{} - delete entity", id);
        if (id == null) {
            log.warn("ID is null");
            return ResponseEntity
                .badRequest()
                .body("El parámetro 'id' no puede ser nulo");
        }
        try {
            entityService.deleteEntity(id);
            log.info("Entity deleted for id={}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid ID for delete: {}", ex.getMessage());
            return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error deleting entity", ex);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor al eliminar entidad");
        }
    }

    @GetMapping("/type")
    public ResponseEntity<?> getEntitiesByType(@RequestParam boolean isEntityType) {
        log.info("GET /api/entities/type?isEntityType={}", isEntityType);
        try {
            List<EntityModel> list = entityService.getEntitiesByType(isEntityType);
            log.info("Found {} entities for type {}", list.size(), isEntityType);
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            log.error("Error fetching entities by type", ex);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor al obtener entidades por tipo");
        }
    }
}
