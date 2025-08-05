package com.davivienda.factoraje.controller;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.davivienda.factoraje.domain.dto.Auth.LoginDTORequest;
import com.davivienda.factoraje.exception.InvalidCredentialsException;
import com.davivienda.factoraje.service.AuthService;
import com.davivienda.factoraje.service.UserService;

@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
        log.info("AuthController initialized");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTORequest loginRequest, HttpServletResponse response) {

        if (!true) {
            // logger.info("Inicio de sesión fallido por credenciales invalidas");
            throw new InvalidCredentialsException("Credenciales invalidas");
        }

        authService.login(loginRequest.getDui(), response);
        return ResponseEntity.ok("Login exitoso");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        log.info("POST /api/auth/logout - logout attempt");
        try {
            authService.logout(response);
            log.info("Logout successful");
            return ResponseEntity.ok("Sesión cerrada");
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al cerrar sesión");
        }
    }

}
