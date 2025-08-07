package com.davivienda.factoraje.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.davivienda.factoraje.domain.dto.Documents.UpdateDocumentsRequestDTO;
import com.davivienda.factoraje.domain.model.AgreementModel;
import com.davivienda.factoraje.domain.model.DocumentModel;
import com.davivienda.factoraje.service.AgreementService;

@RestController
@RequestMapping("/api/agreement")
public class AgreementController {

    private final AgreementService agreementService;
    private static final Logger log = LoggerFactory.getLogger(AgreementController.class);

    public AgreementController(AgreementService agreementService) {
        this.agreementService = agreementService;
        log.info("AgreementController initialized");
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        log.info("GET /api/agreement/getAll - fetching all agreements");
        try {
            List<AgreementModel> list = agreementService.findAll();
            log.info("Found {} agreements", list.size());
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            log.error("Error fetching agreements", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al obtener acuerdos");
        }
    }

    @GetMapping("/getDocumentsApproved/{id}")
    public ResponseEntity<?> exportExcel(@PathVariable("id") String id) {
        log.info("GET /api/agreement/getDocumentsApproved/{} - export documents to Excel", id);
        if (id == null || id.trim().isEmpty()) {
            log.warn("Agreement id is null or empty");
            return ResponseEntity.badRequest()
                    .body("El parámetro 'id' no puede ser vacío");
        }
        try {
            AgreementModel agreement = agreementService.findByIdentifier(id);
            List<DocumentModel> docs = agreement.getDocuments().stream()
                    .filter(d -> "APPROVED".equals(d.getStatus()))
                    .collect(Collectors.toList());

            if (docs.isEmpty()) {
                log.info("No approved documents found for agreement {}", id);
                return ResponseEntity.noContent().build();
            }

            StreamingResponseBody stream = out -> {
                try (Workbook wb = new XSSFWorkbook()) {
                    Sheet sheet = wb.createSheet("Datos");
                    Row header = sheet.createRow(0);
                    header.createCell(0).setCellValue("Nombre del proveedor");
                    header.createCell(1).setCellValue("Número de documento");
                    header.createCell(2).setCellValue("Monto a financiar");
                    header.createCell(3).setCellValue("Fecha de emisión");

                    for (int i = 0; i < docs.size(); i++) {
                        DocumentModel doc = docs.get(i);
                        Row row = sheet.createRow(i + 1);
                        row.createCell(0).setCellValue(doc.getSupplierName());
                        row.createCell(1).setCellValue(doc.getDocumentNumber());
                        row.createCell(2).setCellValue(doc.getAmount().doubleValue());
                        row.createCell(3).setCellValue(doc.getIssueDate().toString());
                    }
                    for (int col = 0; col <= 3; col++) {
                        sheet.autoSizeColumn(col);
                    }
                    log.debug("Writing {} rows to Excel", docs.size());
                    wb.write(out);
                }
            };

            String filename = "reporte_" + id + ".xlsx";
            log.info("Excel export successful, preparing response with filename {}", filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(stream);

        } catch (RuntimeException ex) {
            log.warn("Resource not found or bad input: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error generating Excel for agreement {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al exportar documentos");
        }
    }

    @GetMapping("/byPayer/{payerId}")
    public ResponseEntity<?> getByPayer(@PathVariable String payerId) {
        log.info("GET /api/agreement/byPayer/{} - fetch agreements by payer", payerId);
        if (payerId == null || payerId.trim().isEmpty()) {
            log.warn("payerId is null or empty");
            return ResponseEntity.badRequest()
                    .body("El parámetro 'payerId' no puede ser vacío");
        }
        try {
            List<AgreementModel> agreements = agreementService.findByPayer(payerId);
            if (agreements.isEmpty()) {
                log.info("No agreements found for payer {}", payerId);
                return ResponseEntity.noContent().build();
            }
            log.info("Found {} agreements for payer {}", agreements.size(), payerId);
            return ResponseEntity.ok(agreements);
        } catch (Exception ex) {
            log.error("Error fetching agreements for payer {}", payerId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al procesar la solicitud");
        }
    }

    @GetMapping("/byPayer/{payerId}/{status}")
    public ResponseEntity<?> getByPayerWithStatus(@PathVariable String payerId,
            @PathVariable String status) {
        log.info("GET /api/agreement/byPayer/{}/{} - fetch agreements by payer and status", payerId, status);
        if (payerId == null || payerId.trim().isEmpty() || status == null || status.trim().isEmpty()) {
            log.warn("payerId or status is null/empty");
            return ResponseEntity.badRequest()
                    .body("Los parámetros 'payerId' y 'status' no pueden ser vacíos");
        }
        try {
            List<AgreementModel> agreements = agreementService.findByPayerWithStatus(payerId, status);
            if (agreements.isEmpty()) {
                log.info("No agreements with status {} for payer {}", status, payerId);
                return ResponseEntity.noContent().build();
            }
            log.info("Found {} agreements for payer {} with status {}", agreements.size(), payerId, status);
            return ResponseEntity.ok(agreements);
        } catch (Exception ex) {
            log.error("Error fetching agreements for payer {} with status {}", payerId, status, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al procesar la solicitud");
        }
    }

    @GetMapping("/bySupplier/{supplierId}/{status}")
    public ResponseEntity<?> getBySupplierWithStatus(@PathVariable String supplierId,
            @PathVariable String status) {
        log.info("GET /api/agreement/bySupplier/{}/{} - fetch agreements by supplier and status", supplierId, status);
        if (supplierId == null || supplierId.trim().isEmpty() || status == null || status.trim().isEmpty()) {
            log.warn("supplierId or status is null/empty");
            return ResponseEntity.badRequest()
                    .body("Los parámetros 'supplierId' y 'status' no pueden ser vacíos");
        }
        try {
            List<AgreementModel> agreements = agreementService.findBySupplierWithStatus(supplierId, status);
            if (agreements.isEmpty()) {
                log.info("No agreements with status {} for supplier {}", status, supplierId);
                return ResponseEntity.noContent().build();
            }
            log.info("Found {} agreements for supplier {} with status {}", agreements.size(), supplierId, status);
            return ResponseEntity.ok(agreements);
        } catch (Exception ex) {
            log.error("Error fetching agreements for supplier {} with status {}", supplierId, status, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al procesar la solicitud");
        }
    }

    @GetMapping("/bySupplier/{supplierId}")
    public ResponseEntity<?> getBySupplier(@PathVariable String supplierId) {
        log.info("GET /api/agreement/bySupplier/{} - fetch agreements by supplier", supplierId);
        if (supplierId == null || supplierId.trim().isEmpty()) {
            log.warn("supplierId is null or empty");
            return ResponseEntity.badRequest()
                    .body("El parámetro 'supplierId' no puede ser vacío");
        }
        try {
            List<AgreementModel> agreements = agreementService.findBySupplier(supplierId);
            if (agreements.isEmpty()) {
                log.info("No agreements found for supplier {}", supplierId);
                return ResponseEntity.noContent().build();
            }
            log.info("Found {} agreements for supplier {}", agreements.size(), supplierId);
            return ResponseEntity.ok(agreements);
        } catch (Exception ex) {
            log.error("Error fetching agreements for supplier {}", supplierId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al procesar la solicitud");
        }
    }

    @PostMapping("/updateDocuments/{agreementId}/{status}/{payerId}/{authMode}")
    public ResponseEntity<?> updateDocuments(
            @PathVariable String agreementId,
            @PathVariable String status,
            @PathVariable String payerId,
            @PathVariable Integer authMode,
            @RequestBody UpdateDocumentsRequestDTO documentIds) {
        log.info("POST /api/agreement/updateDocuments/{}/{} - update documents: {}", agreementId, status, documentIds);
        if (agreementId == null || agreementId.trim().isEmpty() || status == null || status.trim().isEmpty()) {
            log.warn("agreementId or status is null/empty");
            return ResponseEntity.badRequest()
                    .body("Los parámetros 'agreementId' y 'status' no pueden ser vacíos");
        }
        try {
            agreementService.loadParameters();
            AgreementModel updated = agreementService.updateDocuments(agreementId, status, documentIds, payerId, authMode);
            log.info("Documents updated for agreement {} with status {}", agreementId, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            log.warn("Resource not found or bad input: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error updating documents for agreement {} with status {}", agreementId, status, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al procesar la solicitud");
        }
    }

    @GetMapping("/byId/{id}/{status}")
    public ResponseEntity<?> getByIdAndStatus(@PathVariable UUID id,
            @PathVariable String status) {
        log.info("GET /api/agreement/byId/{}/{} - fetch agreement by id and status", id, status);
        if (id == null) {
            log.warn("id is null");
            return ResponseEntity.badRequest()
                    .body("El parámetro 'id' no puede ser nulo");
        }
        if (status == null || status.trim().isEmpty()) {
            log.warn("status is null or empty");
            return ResponseEntity.badRequest()
                    .body("El parámetro 'status' no puede ser vacío");
        }
        try {
            AgreementModel agreement = agreementService.findByIdAndStatus(id, status);
            if (agreement == null) {
                log.info("No agreement found for id {} with status {}", id, status);
                return ResponseEntity.notFound().build();
            }
            log.info("Agreement {} found with status {}", id, status);
            return ResponseEntity.ok(agreement);
        } catch (Exception ex) {
            log.error("Error fetching agreement by id {} and status {}", id, status, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al procesar la solicitud");
        }
    }

    @GetMapping("/byId/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        log.info("GET /api/agreement/byId/{} - fetch agreement by id", id);
        if (id == null) {
            log.warn("id is null");
            return ResponseEntity.badRequest()
                    .body("El parámetro 'id' no puede ser nulo");
        }
        try {
            AgreementModel agreement = agreementService.findById(id);
            if (agreement == null) {
                log.info("No agreement found for id {}", id);
                return ResponseEntity.notFound().build();
            }
            log.info("Agreement {} found", id);
            return ResponseEntity.ok(agreement);
        } catch (Exception ex) {
            log.error("Error fetching agreement by id {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al procesar la solicitud");
        }
    }
}
