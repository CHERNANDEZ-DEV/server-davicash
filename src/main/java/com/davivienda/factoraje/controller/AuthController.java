package com.davivienda.factoraje.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.davivienda.factoraje.domain.dto.Auth.LoginDTORequest;
import com.davivienda.factoraje.domain.dto.Auth.PeticionEntornoRequest;
import com.davivienda.factoraje.domain.model.UserModel;
import com.davivienda.factoraje.exception.InvalidCredentialsException;
import com.davivienda.factoraje.service.AuthService;
import com.davivienda.factoraje.service.EntornoService;
import com.davivienda.factoraje.service.UserService;

@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EntornoService entornoService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, UserService userService, EntornoService entornoService) {
        this.authService = authService;
        this.userService = userService;
        this.entornoService = entornoService;
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

    /* 
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTORequest loginRequest, HttpServletResponse response) {

        try {
            PeticionEntornoRequest entornoRequest = new PeticionEntornoRequest();
            entornoRequest.setFabrica("FabricaESBBancaRegional5");
            entornoRequest.setServicio("ON_BOARDING_INICIO_SESION_WEB_EMPRESA");
            entornoRequest.setUsuario(loginRequest.getDui());
            entornoRequest.setClave(loginRequest.getPassword());
            entornoRequest.setTipoPersona("2");

            String respuestaXml = entornoService.recibirPeticion(entornoRequest);
            System.out.println(respuestaXml);

            if (respuestaXml == null || respuestaXml.contains("<error>")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                    .header(HttpHeaders.CONTENT_TYPE, "application/xml; charset=utf-8")
                                    .body(respuestaXml);
            }

            authService.login(loginRequest.getDui(), response);

            return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, "application/xml; charset=utf-8")
                                .body(respuestaXml);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("<error><mensaje>Error interno al procesar la solicitud de login.</mensaje></error>");
        }
    }

    */
    
    /* 
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTORequest loginRequest, HttpServletResponse response) {

        // Optional<UserModel> optionalUser = userService.findUserByDUI(loginRequest.getDui());

        // if (!optionalUser.isPresent()) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos.");
        // }

        // 3. Validación externa
        try {
            PeticionEntornoRequest entornoRequest = new PeticionEntornoRequest();
            entornoRequest.setFabrica("FabricaESBBancaRegional5");
            entornoRequest.setServicio("ON_BOARDING_INICIO_SESION_WEB_EMPRESA");
            entornoRequest.setUsuario(loginRequest.getDui());
            entornoRequest.setClave(loginRequest.getPassword());
            entornoRequest.setTipoPersona("2");

            String respuestaXml = entornoService.recibirPeticion(entornoRequest);
            System.out.println(respuestaXml);

            // Valida la respuesta del servicio externo
            if (respuestaXml == null || respuestaXml.contains("<error>")) {
                throw new InvalidCredentialsException("Credenciales inválidas según el servicio externo.");
            }
            // --- Fin del bloque que puede fallar ---

            // Si todo en el try tiene éxito, continúa con el login
            authService.login(loginRequest.getDui(), response);
            return ResponseEntity.ok("Login exitoso");

        } catch (InvalidCredentialsException ex) {
            // --- Captura la excepción específica ---
            // Si se lanza InvalidCredentialsException, se ejecuta este bloque.
            // Aquí devuelves una respuesta de error en lugar de dejar que la app crashee.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas.");
        }
    }
    */

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
