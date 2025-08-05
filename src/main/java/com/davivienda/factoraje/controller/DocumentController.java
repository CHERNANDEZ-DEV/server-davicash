package com.davivienda.factoraje.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davivienda.factoraje.domain.model.DocumentModel;
import com.davivienda.factoraje.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
        log.info("DocumentController initialized");
    }

    @GetMapping("/all")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> getDocumentService() {
        log.info("GET /api/documents/all - fetching all documents");
        try {
            List<DocumentModel> docs = documentService.getAllDocuments();
            if (docs == null || docs.isEmpty()) {
                log.info("No documents found");
                return ResponseEntity.noContent().build();
            }
            log.info("Found {} documents", docs.size());
            return ResponseEntity.ok(docs);
        } catch (Exception ex) {
            log.error("Error fetching documents", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al obtener documentos");
        }
    }
}
