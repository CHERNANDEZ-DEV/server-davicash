package com.davivienda.factoraje.domain.model;

import java.util.UUID;
import org.hibernate.annotations.GenericGenerator; // Importación para Hibernate 5
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

// Importaciones corregidas para Spring Boot 2 (javax)
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue; // Importación necesaria
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "documents")
public class DocumentModel {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "document_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID document_id;

    @NotBlank(message = "Document number cannot be blank")
    @Size(max = 20, message = "CCF Number cannot exceed 20 characters")
    @Column(nullable = false, unique = true)
    private String documentNumber;

    @Column(nullable = false)
    @NotNull
    private Double amount;

    @NotBlank(message = "Issue Date cannot be blank")
    @Column(nullable = false)
    private String issueDate;

    @NotBlank(message = "Supplier name cannot be blank")
    @Column(nullable = false)
    private String supplierName;

    // UPLOADED/SELECTED/APPROVED/REJECTED/DISBURSED
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String statusUpdateDate;

    private String disbursementDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = true)
    private EntityModel supplier;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargadoPor_id")
    private UserModel uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seleccionadoPor_id")
    private UserModel selectedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobadoPor_id")
    private UserModel approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rechazadoPor_id")
    private UserModel rejectedBy;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acuerdo_id", nullable = false)
    private AgreementModel agreement;

}