package com.davivienda.factoraje.controller;

import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/parameters")
public class ParameterController {

    private final CacheManager cacheManager;

    public ParameterController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @PostMapping("/update")
    public ResponseEntity<String> clearParametersCache() {
        // Nos aseguramos de limpiar la caché con el nombre "parameters"
        cacheManager.getCache("parameters").clear();
        System.out.println("!!! CACHÉ 'parameters' LIMPIADA MANUALMENTE !!!");
        return ResponseEntity.ok("La caché de parámetros ha sido limpiada.");
    }
}