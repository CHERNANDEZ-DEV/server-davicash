package com.davivienda.factoraje.service;

import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.stereotype.Service;

import com.davivienda.factoraje.domain.model.ParameterModel;
import com.davivienda.factoraje.repository.ParameterRepository;

@Service
public class ParameterService {

    private final ParameterRepository parameterRepository;

    public ParameterService(ParameterRepository parameterRepository) {
        this.parameterRepository = parameterRepository;
    }

    public ParameterModel getParameterById(UUID parameterId) {
        return parameterRepository.findById(parameterId).get();
    }

    public void saveParameter(ParameterModel parameter) {
        parameterRepository.save(parameter);
    }

    /**
     * Obtiene el valor de un parámetro usando su 'key'.
     * El resultado se almacena en la caché "parameters".
    */
    @Cacheable(value = "parameters", key = "#key")
    public String getValueByKey(String key) {
        System.out.println("--- BUSCANDO EN BD EL PARÁMETRO CON KEY: " + key + " ---");

        // Usamos el nuevo método findByKey y el getter getValue
        return parameterRepository.findByKey(key)
                .map(ParameterModel::getValue)
                .orElseThrow(() -> new RuntimeException("Parámetro no encontrado: " + key));
    }
}
