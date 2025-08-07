package com.davivienda.factoraje.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.davivienda.factoraje.components.AppParameterLoader;
import com.davivienda.factoraje.domain.dto.Documents.UpdateDocumentsRequestDTO;
import com.davivienda.factoraje.domain.dto.Emails.DestinatarioRequestDTO;
import com.davivienda.factoraje.domain.dto.Emails.EmailRequestDTO;
import com.davivienda.factoraje.domain.dto.Emails.HTMLVariablesDTO;
import com.davivienda.factoraje.domain.model.AgreementModel;
import com.davivienda.factoraje.domain.model.DocumentModel;
import com.davivienda.factoraje.domain.model.EntityModel;
import com.davivienda.factoraje.domain.model.ParameterModel;
import com.davivienda.factoraje.event.EmailEvent;
import com.davivienda.factoraje.exception.ResourceNotFoundException;
import com.davivienda.factoraje.repository.AgreementRepository;

@Service
public class AgreementService {

    private final AppParameterLoader parameterLoader;

    private static final Logger log = LoggerFactory.getLogger(AgreementService.class);
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EntityService entityService;
    private final AgreementRepository agreementRepository;
    private final DocumentService documentService;

    private static final String PARAM_KEY_EMAIL_MANAGER_BANK = "mailjet.email.manager.bank";
    private static final String PARAM_KEY_NAME_MANAGER_BANK = "mailjet.name.manager.bank";
    private String paramValueEmailManagerBank;
    private String paramValueNameManagerBank;

    public AgreementService(AgreementRepository agreementRepository, ApplicationEventPublisher applicationEventPublisher, EntityService entityService, DocumentService documentService, AppParameterLoader parameterLoader) {
        this.agreementRepository = agreementRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.entityService = entityService;
        this.documentService = documentService;
        this.parameterLoader = parameterLoader;
        log.info("AgreementService initialized");
    }

    public void loadParameters() {
        for (ParameterModel p : parameterLoader.getParameters()) {

            if(p.getKey().equals(PARAM_KEY_EMAIL_MANAGER_BANK)){
                paramValueEmailManagerBank = p.getValue();
            }

            if(p.getKey().equals(PARAM_KEY_NAME_MANAGER_BANK)){
                paramValueNameManagerBank = p.getValue();
            }
        }
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
            log.info("Agreement not found for identifier {}");
            return null;
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

    @Transactional
    public AgreementModel updateDocuments(
            String agreementId, 
            String status,
            UpdateDocumentsRequestDTO documentIds, 
            String payerId,
            Integer authMode) {

        BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (UUID docId : documentIds.getDocumentIds()) {
            DocumentModel doc = documentService.getDocumentById(docId).get();
            if (doc != null && doc.getAmount() != null) {
                totalAmount = totalAmount.add(doc.getAmount());
            }
        }

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

        switch (authMode) {
            case 1:

                if(status.equals("SELECTED")){
                    
                    // Correo al pagador
                    UUID payerIdToSend;
                    try {
                        payerIdToSend = UUID.fromString(payerId);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid UUID format for payerId {}", payerId);
                        throw new IllegalArgumentException("El parámetro 'agreementId' debe ser un UUID válido");
                    }

                    EntityModel payer = new EntityModel();
                    payer = entityService.getEntityById(payerIdToSend);
                    EmailRequestDTO request = new EmailRequestDTO();
                    HTMLVariablesDTO payerVariables = new HTMLVariablesDTO();

                    DestinatarioRequestDTO destinoPagador = new DestinatarioRequestDTO();
                    destinoPagador.setName(payer.getName());
                    destinoPagador.setEmail(payer.getEmail());

                    List<DestinatarioRequestDTO> destinos = new ArrayList<>();
                    destinos.add(destinoPagador);

                    payerVariables.setNombreEmpresa(payer.getName());
                    payerVariables.setNombreProveedor("");
                    payerVariables.setNumeroCuentaProveedor("");
                    payerVariables.setNumeroLineaCredito("");
                    payerVariables.setMontoDesembolsar(BigDecimal.ZERO);

                    request.setTipoHtml(3);
                    request.setDestinatarios(destinos);
                    request.setHtmlVariables(payerVariables);

                    EmailEvent evt = new EmailEvent(request);
                    applicationEventPublisher.publishEvent(evt);
                } 

                // Correo al banco
                UUID payerIdToSend;
                try {
                    payerIdToSend = UUID.fromString(payerId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID format for payerId {}", payerId);
                    throw new IllegalArgumentException("El parámetro 'agreementId' debe ser un UUID válido");
                }
                EntityModel payer = new EntityModel();
                payer = entityService.getEntityById(payerIdToSend);
                EmailRequestDTO request = new EmailRequestDTO();
                HTMLVariablesDTO bankVariables = new HTMLVariablesDTO();

                DestinatarioRequestDTO bank = new DestinatarioRequestDTO();
                bank.setName(paramValueNameManagerBank);
                bank.setEmail(paramValueEmailManagerBank);

                List<DestinatarioRequestDTO> destinos = new ArrayList<>();
                destinos.add(bank);

                bankVariables.setNombreEmpresa(payer.getName());
                bankVariables.setNombreProveedor("");
                bankVariables.setNumeroCuentaProveedor("");
                bankVariables.setNumeroLineaCredito(payer.getCreditLineNumber());
                bankVariables.setMontoDesembolsar(totalAmount);
                bankVariables.setNIT(payer.getNit());

                request.setTipoHtml(1);
                request.setDestinatarios(destinos);
                request.setHtmlVariables(bankVariables);

                EmailEvent evt = new EmailEvent(request);
                applicationEventPublisher.publishEvent(evt);
                
                break;
            
            case 2:

                // Correo al banco
                EmailRequestDTO requestAuthTwo = new EmailRequestDTO();
                HTMLVariablesDTO bankVariablesAuthTwo = new HTMLVariablesDTO();

                DestinatarioRequestDTO bankAuthTwo = new DestinatarioRequestDTO();
                bankAuthTwo.setName(paramValueNameManagerBank);
                bankAuthTwo.setEmail(paramValueEmailManagerBank);

                List<DestinatarioRequestDTO> destinosAuthTwo = new ArrayList<>();
                destinosAuthTwo.add(bankAuthTwo);

                bankVariablesAuthTwo.setNombreEmpresa("");
                bankVariablesAuthTwo.setNombreProveedor("");
                bankVariablesAuthTwo.setNumeroCuentaProveedor("");
                bankVariablesAuthTwo.setNumeroLineaCredito("");
                bankVariablesAuthTwo.setMontoDesembolsar(totalAmount);

                requestAuthTwo.setTipoHtml(6);
                requestAuthTwo.setDestinatarios(destinosAuthTwo);
                requestAuthTwo.setHtmlVariables(bankVariablesAuthTwo);

                EmailEvent evtAuthTwo = new EmailEvent(requestAuthTwo);
                applicationEventPublisher.publishEvent(evtAuthTwo);

                break;

            default:
                break;
        }

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