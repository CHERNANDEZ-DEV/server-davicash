package com.davivienda.factoraje.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RolesAllowed {
    String[] value();              // p.e. { "MANAGER", "OPERATOR" }
}


