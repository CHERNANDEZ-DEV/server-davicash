package com.davivienda.factoraje.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davivienda.factoraje.domain.dto.calculate.CalculateDTORequest;
import com.davivienda.factoraje.domain.dto.calculate.CalculateDTOResponse;
import com.davivienda.factoraje.service.OperationsService;

@RestController
@RequestMapping("api/operations")
public class OperationsController {

    private static final Logger logger = LoggerFactory.getLogger(OperationsController.class);
    private final OperationsService operationsService;

    public OperationsController(OperationsService operationsService) {
        this.operationsService = operationsService;
        logger.info("OperationsController initialized");
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> doOperations(@RequestBody List<CalculateDTORequest> request) {
        logger.info("POST /api/operations/calculate - Received {} calculation items",
                request != null ? request.size() : 0);

        if (request == null || request.isEmpty()) {
            logger.warn("Calculation request is null or empty");
            return ResponseEntity
                    .badRequest()
                    .body("El cuerpo de la petición debe contener al menos un elemento para calcular");
        }

        try {
            //operationsService.loadParameters();
            CalculateDTOResponse response = operationsService.calculate(request);
            logger.info("Calculation successful for {} items", request.size());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid argument in calculation: {}", ex.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ex.getMessage());

        } catch (Exception ex) {
            logger.error("Internal server error during calculation", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor al procesar la operación");
        }
    }

}
