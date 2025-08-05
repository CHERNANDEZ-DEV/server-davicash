package com.davivienda.factoraje.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserModel {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Column(nullable = false)
    private String dui;

    @Column(nullable = false)
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentModel> uploadedDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "selectedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentModel> selectedDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "approvedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentModel> approvedDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "rejectedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentModel> rejectedDocuments = new ArrayList<>();

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", nullable = true)
    private EntityModel entity;

    // @JsonManagedReference
    // @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
    // CascadeType.MERGE })
    // @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
    // inverseJoinColumns = @JoinColumn(name = "role_id"))
    // private Set<RoleModel> roles = new HashSet<>();

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private RoleModel role;
}