package com.davivienda.factoraje.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.davivienda.factoraje.utils.JwtUtil;
import com.davivienda.factoraje.domain.dto.Auth.RegisterDTORequest;
import com.davivienda.factoraje.domain.dto.Auth.RegisterDTOResponse;
import com.davivienda.factoraje.domain.dto.Users.GetAllUsersResponseDTO;
import com.davivienda.factoraje.domain.model.EntityModel;
import com.davivienda.factoraje.domain.model.RoleModel;
import com.davivienda.factoraje.domain.model.UserModel;


@Service
public class AuthService {

    private final UserService userService;
    private final EntityService entityService;
    private final RoleService roleService;

    public AuthService(UserService userService, EntityService entityService, RoleService roleService) {
        this.userService = userService;
        this.entityService = entityService;
        this.roleService = roleService;
    }

    // Se asume que los usuarios poseen un usuario de BEL, por lo tanto solo se
    // vinculan a la aplicación
    public RegisterDTOResponse linkUser(RegisterDTORequest registerRequest) {

        RegisterDTOResponse response = new RegisterDTOResponse();

        // Validar con el email si el usuario ya está vinculado
        UserModel existingUser = userService.findByEmail(registerRequest.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("User already linked");
        }

        // Buscar la entidad por ID
        EntityModel entity = entityService.getEntityById(registerRequest.getEntityId());
        if (entity == null) {
            throw new RuntimeException("Entity not found");
        }

        RoleModel role = roleService.getById(registerRequest.getRoleId());

        if (role == null) {
            throw new RuntimeException("PAYER role not found");
        }

        UserModel user = new UserModel();
        user.setEmail(registerRequest.getEmail());
        user.setName(registerRequest.getName());
        user.setDui(registerRequest.getDui());
        user.setEntity(entity);
        user.setRole(role);

        userService.save(user);

        // Construcción de la respuesta
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setEntityId(user.getEntity().getId());
        response.setEntityName(user.getEntity().getName());
        response.setDui(user.getDui());
        return response;
    }

    public List<GetAllUsersResponseDTO> getAllUsers() {

        List<UserModel> users = userService.getAll();

        return users.stream()
                .map(user -> {
                    GetAllUsersResponseDTO dto = new GetAllUsersResponseDTO();
                    dto.setId(user.getId());
                    dto.setDui(user.getDui());
                    dto.setName(user.getName());
                    dto.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : "N/A");
                    dto.setEntityName(user.getEntity() != null ? user.getEntity().getName() : "N/A");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void login(String DUI, HttpServletResponse response) {
        
        String jwt = JwtUtil.generateToken(DUI);

        Cookie cookie = new Cookie("token", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Solo en producción con HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(300000);

        // 3. Añadir cookie a la respuesta
        response.addCookie(cookie);
    }

      public void logout(HttpServletResponse response) {
        // Invalidar cookie
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Elimina la cookie
        response.addCookie(cookie);
    }
}
