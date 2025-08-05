package com.davivienda.factoraje.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.davivienda.factoraje.domain.model.PermissionModel;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionModel, UUID> {
    // si necesitas métodos adicionales, agrégalos aquí
}
