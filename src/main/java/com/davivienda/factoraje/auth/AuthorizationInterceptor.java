package com.davivienda.factoraje.auth;

import com.davivienda.factoraje.domain.model.RoleModel;
import com.davivienda.factoraje.domain.model.UserModel;
import com.davivienda.factoraje.service.UserService;
import com.davivienda.factoraje.utils.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        /* ─ 1 · Solo métodos de controlador ─────────────────────── */
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;

        /* ─ 2 · Leer y validar el JWT de la cookie «token» ──────── */
        String token = extractToken(request);
        if (token == null || !JwtUtil.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                               "Token inválido o expirado");
            return false;
        }

        /* ─ 3 · Consultar usuario y su único rol ───────────────── */
        String dui = JwtUtil.getDUIFromToken(token);
        UserModel user = userService.findUserByDUI(dui).orElse(null);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                               "Usuario no encontrado");
            return false;
        }

        RoleModel role = user.getRole();                // relación Many-to-One EAGER
        String roleName = role.getRoleName();           // p. ej. "MANAGER"

        /* ─ 4 · Verificar anotación @RolesAllowed ──────────────── */
        if (!isRoleAllowed(hm, roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                               "Acceso denegado");
            return false;
        }

        /* ─ 5 · Exponer el usuario al controlador (opcional) ──── */
        request.setAttribute("loggedUser", user);
        return true;
    }

    /* ============ Helpers ====================================== */

    /** Devuelve el valor de la cookie «token» o null si no existe. */
    private String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                     .filter(c -> "token".equals(c.getName()))
                     .map(Cookie::getValue)
                     .findFirst()
                     .orElse(null);
    }

    /** Comprueba si el rol del usuario está permitido por la anotación. */
    private boolean isRoleAllowed(HandlerMethod hm, String role) {

        RolesAllowed ra = hm.getMethodAnnotation(RolesAllowed.class);
        if (ra == null) {
            ra = hm.getBeanType().getAnnotation(RolesAllowed.class);
        }

        // Si hay anotación y NINGUNO de los roles requeridos coincide → denegar
        return ra == null || Arrays.stream(ra.value()).anyMatch(role::equals);
    }
}