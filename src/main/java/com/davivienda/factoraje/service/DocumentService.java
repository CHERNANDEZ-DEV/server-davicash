package com.davivienda.factoraje.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.davivienda.factoraje.domain.model.DocumentModel;
import com.davivienda.factoraje.repository.DocumentRepository;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
        log.info("DocumentService initialized");
    }

    public DocumentModel createDocument(DocumentModel document) {
        if (document == null) {
            log.warn("createDocument called with null document");
            throw new IllegalArgumentException("El documento no puede ser nulo");
        }
        log.debug("Saving document with number {}", document.getDocumentNumber());
        return documentRepository.save(document);
    }

    public List<DocumentModel> getAllDocuments() {
        log.debug("Fetching all documents");
        List<DocumentModel> docs = documentRepository.findAll();
        if (docs == null || docs.isEmpty()) {
            log.info("No documents found in database");
        } else {
            log.info("Found {} documents", docs.size());
        }
        return docs;
    }
}
