package com.davivienda.factoraje.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.davivienda.factoraje.domain.dto.Documents.UpdateDocumentsRequestDTO;
import com.davivienda.factoraje.domain.model.AgreementModel;
import com.davivienda.factoraje.domain.model.DocumentModel;
import com.davivienda.factoraje.exception.ResourceNotFoundException;
import com.davivienda.factoraje.repository.AgreementRepository;

@Service
public class AgreementService {

    private static final Logger log = LoggerFactory.getLogger(AgreementService.class);
    private final AgreementRepository agreementRepository;

    public AgreementService(AgreementRepository agreementRepository) {
        this.agreementRepository = agreementRepository;
        log.info("AgreementService initialized");
    }

    public List<AgreementModel> findAll() {
        log.debug("Fetching all agreements");
        return agreementRepository.findAll();
    }

    public AgreementModel findById(UUID id) {
        if (id == null) {
            log.warn("findById called with null id");
            throw new IllegalArgumentException("El parámetro 'id' no puede ser nulo");
        }
        return agreementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acuerdo no encontrado con id=" + id));
    }

    public AgreementModel findByIdentifier(String code) {
        if (code == null || code.trim().isEmpty()) {
            log.warn("findByIdentifier called with empty code");
            throw new IllegalArgumentException("El identificador no puede ser vacío");
        }
        AgreementModel agreement = agreementRepository.findByIdentifier(code);
        if (agreement == null) {
            log.warn("No agreement found with identifier {}", code);
            throw new ResourceNotFoundException("Acuerdo no encontrado con identificador=" + code);
        }
        return agreement;
    }

    public AgreementModel save(AgreementModel agreement) {
        if (agreement == null) {
            throw new IllegalArgumentException("El acuerdo no puede ser nulo");
        }
        log.debug("Saving new agreement with identifier {}", agreement.getIdentifier());
        return agreementRepository.save(agreement);
    }

    public List<AgreementModel> findByPayer(String payerId) {
        if (payerId == null || payerId.trim().isEmpty()) {
            log.warn("findByPayer called with empty payerId");
            throw new IllegalArgumentException("El parámetro 'payerId' no puede ser vacío");
        }
        log.debug("Filtering agreements by payerId {}", payerId);
        return agreementRepository.findAll().stream()
                .filter(a -> a.getIdentifier() != null &&
                        a.getIdentifier().length() >= 36 &&
                        a.getIdentifier().substring(0, 36).equals(payerId))
                .collect(Collectors.toList());
    }

    public List<AgreementModel> findByPayerWithStatus(String payerId, String status) {
        if (payerId == null || status == null) {
            throw new IllegalArgumentException("payerId cannot be null or status cannot be null");
        }

        List<AgreementModel> agreements = agreementRepository.findAll().stream()
                .filter(agreement -> {
                    String identifier = agreement.getIdentifier();
                    return identifier != null &&
                            identifier.length() >= 36 &&
                            identifier.substring(0, 36).equals(payerId);
                })
                .collect(Collectors.toList());

        for (AgreementModel agreement : agreements) {
            List<DocumentModel> documents = new ArrayList<>();
            for (DocumentModel document : agreement.getDocuments()) {
                if (document.getStatus() != null && document.getStatus().equals(status)) {
                    documents.add(document);
                }
            }
            agreement.setDocuments(documents);
        }

        return agreements;
    }

    public List<AgreementModel> findBySupplierWithStatus(String supplierId, String status) {
        if (supplierId == null || status == null) {
            throw new IllegalArgumentException("supplierId cannot be null or status cannot be null");
        }

        List<AgreementModel> agreements = agreementRepository.findAll().stream()
                .filter(agreement -> {
                    String identifier = agreement.getIdentifier();
                    return identifier != null &&
                            identifier.length() >= 36 &&
                            identifier.substring(36).equals(supplierId);
                })
                .collect(Collectors.toList());

        for (AgreementModel agreement : agreements) {
            List<DocumentModel> documents = new ArrayList<>();
            for (DocumentModel document : agreement.getDocuments()) {
                if (document.getStatus() != null && document.getStatus().equals(status)) {
                    documents.add(document);
                }
            }
            agreement.setDocuments(documents);
        }

        return agreements;
    }

    public AgreementModel updateDocuments(String agreementId, String status,
            UpdateDocumentsRequestDTO documentIds) {

        if (agreementId == null || agreementId.trim().isEmpty()) {
            log.warn("updateDocuments called with empty agreementId");
            throw new IllegalArgumentException("El parámetro 'agreementId' no puede ser vacío");
        }
        if (status == null || status.trim().isEmpty()) {
            log.warn("updateDocuments called with empty status");
            throw new IllegalArgumentException("El parámetro 'status' no puede ser vacío");
        }
        if (documentIds == null || documentIds.getDocumentIds() == null
                || documentIds.getDocumentIds().isEmpty()) {
            log.warn("updateDocuments called with no document IDs");
            throw new IllegalArgumentException("Debe especificar al menos un documento a actualizar");
        }

        UUID id;
        try {
            id = UUID.fromString(agreementId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for agreementId {}", agreementId);
            throw new IllegalArgumentException("El parámetro 'agreementId' debe ser un UUID válido");
        }

        AgreementModel agreement = agreementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Acuerdo no encontrado con id=" + agreementId));

        log.debug("Updating {} documents to status {} for agreement {}",
                documentIds.getDocumentIds().size(), status, agreementId);

        agreement.getDocuments().forEach(doc -> {
            if (documentIds.getDocumentIds().contains(doc.getDocument_id())) {
                doc.setStatus(status);
            }
        });
        return agreementRepository.save(agreement);
    }

    public List<AgreementModel> findBySupplier(String supplierId) {
        if (supplierId == null) {
            throw new IllegalArgumentException("payerId cannot be null");
        }

        return agreementRepository.findAll().stream()
                .filter(agreement -> {
                    String identifier = agreement.getIdentifier();
                    return identifier != null &&
                            identifier.length() >= 36 &&
                            identifier.substring(36).equals(supplierId);
                })
                .collect(Collectors.toList());
    }

    public AgreementModel findByIdAndStatus(UUID id, String status) {
        if (id == null) {
            log.warn("findByIdAndStatus called with null id");
            throw new IllegalArgumentException("El parámetro 'id' no puede ser nulo");
        }
        if (status == null || status.trim().isEmpty()) {
            log.warn("findByIdAndStatus called with empty status");
            throw new IllegalArgumentException("El parámetro 'status' no puede ser vacío");
        }

        AgreementModel agreement = agreementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Acuerdo no encontrado con id=" + id));

        log.debug("Filtering documents for agreement {} by status {}", id, status);

        List<DocumentModel> filtered = agreement.getDocuments().stream()
                .filter(d -> status.equals(d.getStatus()))
                .collect(Collectors.toList());

        agreement.setDocuments(filtered);
        return agreement;
    }
}