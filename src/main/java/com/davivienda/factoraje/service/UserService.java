package com.davivienda.factoraje.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.davivienda.factoraje.domain.dto.Users.AssignRolesToUserRequestDTO;
import com.davivienda.factoraje.domain.model.RoleModel;
import com.davivienda.factoraje.domain.model.UserModel;
import com.davivienda.factoraje.exception.ResourceNotFoundException;
import com.davivienda.factoraje.repository.UserRepository;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RoleService roleService;

    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        log.info("UserService initialized");
    }

    public List<UserModel> getAll() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    public List<UserModel> getPayers() {
        log.debug("Fetching payers");
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "PAYER".equals(u.getRole().getRoleName()))
                .collect(Collectors.toList());
    }

    public UserModel createPayer(UserModel payer) {
        if (payer == null) {
            throw new IllegalArgumentException("El cuerpo de la petición no puede ser nulo");
        }
        RoleModel payerRole = roleService.getByRoleName("PAYER");
        if (payerRole == null) {
            throw new ResourceNotFoundException("Rol PAYER no existe");
        }
        payer.setRole(payerRole);
        log.debug("Saving new payer {} with role PAYER", payer.getEmail());
        return userRepository.save(payer);
    }

    public UserModel findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserModel save(UserModel user) {

        return userRepository.save(user);
    }

    public UserModel getUserById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser nulo");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id=" + id));
    }

    public UserModel assignRolesToUser(AssignRolesToUserRequestDTO dto) {
        if (dto == null || dto.getUserId() == null ||
                dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            throw new IllegalArgumentException("userId y roleIds son obligatorios");
        }

        UserModel user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id=" + dto.getUserId()));

        for (UUID roleId : dto.getRoleIds()) {
            RoleModel role = roleService.getById(roleId);
            user.setRole(role); // tu modelo usa un solo rol, no lista
        }
        log.debug("Roles assigned to user {}", user.getId());
        return userRepository.save(user);
    }

    public Optional<UserModel> findUserByDUI(String dui) {
        return userRepository.findByDui(dui);
    }

}
