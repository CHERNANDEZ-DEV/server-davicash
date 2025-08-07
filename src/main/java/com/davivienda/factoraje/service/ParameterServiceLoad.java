package com.davivienda.factoraje.service;

import java.util.Collections;                 // ⬅️ usa el de java.util
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.davivienda.factoraje.domain.model.ParameterModel;
import com.davivienda.factoraje.repository.ParameterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParameterServiceLoad implements InitializingBean {

    private final ParameterRepository repository;

    // “Constantes” accesibles desde cualquier parte
    public static String INTEREST_RATE;
    public static String BASE;
    public static String COMISSION;

    // Almacén inmutable para el resto de parámetros
    private Map<String, String> cache = Collections.emptyMap();

    @Override
    @Transactional(readOnly = true)
    public void afterPropertiesSet() {
        // Carga todos los registros
        cache = repository.findAll().stream()
                .collect(Collectors.toMap(
                        ParameterModel::getKey,
                        ParameterModel::getValue
                ));

        // Asigna los que quieres como campos individuales
        INTEREST_RATE = cache.get("param.key.interest");
        BASE  = cache.get("param.key.base");
        COMISSION = cache.get("param.key.comission");
    }

    /** Acceso genérico al resto de parámetros */
    public String get(String key) {
        return cache.get(key);
    }
}