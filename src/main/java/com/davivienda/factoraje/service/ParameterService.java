package com.davivienda.factoraje.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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
}
