package com.davivienda.factoraje.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davivienda.factoraje.domain.dto.Auth.RegisterDTORequest;
import com.davivienda.factoraje.domain.dto.Auth.RegisterDTOResponse;
import com.davivienda.factoraje.domain.dto.Users.AssignRolesToUserRequestDTO;
import com.davivienda.factoraje.domain.dto.Users.CreateAdminRequestDTO;
import com.davivienda.factoraje.domain.dto.Users.CreateAdminResponseDTO;
import com.davivienda.factoraje.domain.dto.Users.GetAllUsersResponseDTO;
import com.davivienda.factoraje.domain.dto.Users.UserLoggedResponseDTO;
import com.davivienda.factoraje.domain.model.UserModel;
import com.davivienda.factoraje.exception.ResourceNotFoundException;
import com.davivienda.factoraje.service.AuthService;
import com.davivienda.factoraje.service.UserService;
import com.davivienda.factoraje.utils.JwtUtil;
import javax.servlet.http.Cookie;
import java.util.Arrays;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
        log.info("UserController initialized");
    }

    @PostMapping("/linkUser")
    public ResponseEntity<?> linkUser(@RequestBody RegisterDTORequest linkRequest) {
        log.info("POST /api/users/linkUser - link user request: {}", linkRequest);
        if (linkRequest == null) {
            log.warn("RegisterDTORequest is null");
            return ResponseEntity.badRequest().body("El cuerpo de la petición no puede ser vacío");
        }
        try {
            RegisterDTOResponse response = authService.linkUser(linkRequest);
            log.info("User linked successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid linkUser request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error linking user", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al vincular usuario");
        }
    }

    @PostMapping("/createPayer")
    public ResponseEntity<?> createPayer(@RequestBody UserModel payer) {
        log.info("POST /api/users/createPayer - create payer: {}", payer);
        if (payer == null) {
            log.warn("UserModel payer is null");
            return ResponseEntity.badRequest().body("El cuerpo de la petición no puede ser vacío");
        }
        try {
            UserModel created = userService.createPayer(payer);
            log.info("Payer created with id={}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid payer data: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error creating payer", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al crear pagador");
        }
    }

    @PostMapping("/assignRolesToUser")
    public ResponseEntity<?> assignRolesToUser(@RequestBody AssignRolesToUserRequestDTO dto) {
        log.info("POST /api/users/assignRolesToUser - request: {}", dto);
        if (dto == null || dto.getUserId() == null) {
            log.warn("AssignRolesToUserRequestDTO or userId is null");
            return ResponseEntity.badRequest().body("El cuerpo de la petición y el userId no pueden ser nulos");
        }
        try {
            UserModel updated = userService.assignRolesToUser(dto);
            log.info("Roles assigned to userId={}", dto.getUserId());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid assignRolesToUser data: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (ResourceNotFoundException ex) {
            log.warn("Resource not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error assigning roles to user", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al asignar roles al usuario");
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        log.info("GET /api/users/getAll - fetch all users");
        try {
            List<UserModel> users = userService.getAll();
            if (users.isEmpty()) {
                log.info("No users found");
                return ResponseEntity.noContent().build();
            }
            log.info("Found {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception ex) {
            log.error("Error fetching all users", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al obtener usuarios");
        }
    }

    @GetMapping("/getPayers")
    public ResponseEntity<?> getPayers() {
        log.info("GET /api/users/getPayers - fetch payer users");
        try {
            List<UserModel> payers = userService.getPayers();
            if (payers.isEmpty()) {
                log.info("No payers found");
                return ResponseEntity.noContent().build();
            }
            log.info("Found {} payers", payers.size());
            return ResponseEntity.ok(payers);
        } catch (Exception ex) {
            log.error("Error fetching payers", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al obtener pagadores");
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserData(HttpServletRequest request) {
        log.info("GET /api/users/user - fetch logged user data");
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                log.warn("No cookies found in request");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token cookie present");
            }
            Optional<String> tokenOpt = Arrays.stream(cookies)
                    .filter(c -> "token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst();
            if (!tokenOpt.isPresent() || !JwtUtil.validateToken(tokenOpt.get())) {
                log.warn("Invalid or missing JWT token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
            }
            String token = tokenOpt.get();
            String DUI = JwtUtil.getDUIFromToken(token);
            log.debug("DUI extracted from token: {}", DUI);

            UserModel user = userService.findUserByDUI(DUI)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con DUI " + DUI));
            log.info("User found for DUI {}", DUI);

            UserLoggedResponseDTO userResponse = new UserLoggedResponseDTO();
            userResponse.setId(user.getId());
            userResponse.setName(user.getName());
            userResponse.setEmail(user.getEmail());
            userResponse.setDui(user.getDui());
            userResponse.setEntityId(user.getEntity().getId());
            userResponse.setEntityName(user.getEntity().getName());
            userResponse.setEntityType(user.getEntity().getEntityType());
            userResponse.setAuthenticationMode(user.getEntity().getAuthenticationMode());
            userResponse.setRole(user.getRole());

            return ResponseEntity.ok(userResponse);
        } catch (ResourceNotFoundException ex) {
            log.warn(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error fetching user data", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al obtener datos de usuario");
        }
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        log.info("GET /api/users/getAllUsers - fetch all users via authService");
        try {
            List<GetAllUsersResponseDTO> users = authService.getAllUsers();
            if (users.isEmpty()) {
                log.info("No users found via authService");
                return ResponseEntity.noContent().build();
            }
            log.info("Found {} users via authService", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception ex) {
            log.error("Error fetching all users via authService", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al obtener usuarios");
        }
    }
}
