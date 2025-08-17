package com.davivienda.factoraje.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/parameters")
public class ParameterController {

    private final CacheManager cacheManager;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public ParameterController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        log.info("ParameterController initialized");
    }

    @GetMapping("/update")
    public ResponseEntity<String> clearParametersCache() {
        // Nos aseguramos de limpiar la caché con el nombre "parameters"
        cacheManager.getCache("parameters").clear();
        log.info("!!! CACHÉ 'parameters' ACTUALIZADA CORRECTAMENTE !!!");
        return ResponseEntity.ok("La caché de parámetros ha sido actualizada correctamente.");
    }
}