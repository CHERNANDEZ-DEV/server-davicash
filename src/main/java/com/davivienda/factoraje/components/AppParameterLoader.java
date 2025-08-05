package com.davivienda.factoraje.components;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.davivienda.factoraje.domain.model.ParameterModel;
import com.davivienda.factoraje.repository.ParameterRepository;

@Component
public class AppParameterLoader implements ApplicationRunner {

    private final ParameterRepository parameterRepository;
    private final List<ParameterModel> requiredParameters;

    @Autowired
    public AppParameterLoader(ParameterRepository parameterRepository, List<ParameterModel> requiredParameters){
        this.parameterRepository = parameterRepository;
        this.requiredParameters = requiredParameters;
    }

    @Override
    public void run(ApplicationArguments args){
        parameterRepository.findAll().forEach(p -> {
            requiredParameters.add(p);
        });
    }

    public List<ParameterModel> getParameters(){
        return requiredParameters;
    }
}
