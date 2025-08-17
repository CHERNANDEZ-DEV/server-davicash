/*
package com.davivienda.factoraje.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.davivienda.factoraje.components.AppParameterLoader;
import com.davivienda.factoraje.domain.dto.calculate.CalculateDTORequest;
import com.davivienda.factoraje.domain.dto.calculate.CalculateDTOResponse;
import com.davivienda.factoraje.domain.dto.calculate.DetailDTOResponse;
import com.davivienda.factoraje.domain.dto.calculate.PercentageDTORequest;
import com.davivienda.factoraje.domain.dto.calculate.PercentageDTOResponse;
import com.davivienda.factoraje.domain.model.ParameterModel;

@Service
public class OperationsService {

    private final AppParameterLoader parameterLoader;
    private static final Logger log = LoggerFactory.getLogger(OperationsService.class);

    private static final String PARAM_KEY_INTEREST  = "param.key.interest";
    private static final String PARAM_KEY_COMISSION = "param.key.comission";
    private static final String PARAM_KEY_BASE      = "param.key.base";


    private BigDecimal interestRate   = null;    // p.ej. 0.18  → 18 %
    private BigDecimal commissionRate = null;    // p.ej. 0.0025 → 0.25 %
    private BigDecimal base           = null;    // p.ej. 360


    private static final BigDecimal DEFAULT_INTEREST_RATE   = new BigDecimal("0.0");
    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.0");
    private static final BigDecimal DEFAULT_BASE            = new BigDecimal("0");

    private static final int SCALE = 2; // centavos

    public OperationsService(AppParameterLoader parameterLoader) {
        this.parameterLoader = parameterLoader;
        log.info("OperationsService initialized");
    }

    public void loadParameters() {
        for (ParameterModel p : parameterLoader.getParameters()) {
            try {
                switch (p.getKey()) {
                    case PARAM_KEY_INTEREST:
                        interestRate = new BigDecimal(p.getValue());
                        break;
                    case PARAM_KEY_COMISSION:
                        commissionRate = new BigDecimal(p.getValue());
                        break;
                    case PARAM_KEY_BASE:
                        base = new BigDecimal(p.getValue());
                        break;
                    default:
                        // otro parámetro - ignorar
                }
            } catch (NumberFormatException ex) {
                log.error("Valor no numérico '{}' para la clave '{}'", p.getValue(), p.getKey(), ex);
            }
        }
        log.info("Parámetros cargados → interest={}, commission={}, base={}",
                 interestRate, commissionRate, base);
    }

    private BigDecimal getInterestRate()   { return interestRate   != null ? interestRate   : DEFAULT_INTEREST_RATE; }
    private BigDecimal getCommissionRate() { return commissionRate != null ? commissionRate : DEFAULT_COMMISSION_RATE; }
    private BigDecimal getBase()           { return base           != null ? base           : DEFAULT_BASE; }

    public PercentageDTOResponse getPercentage(PercentageDTORequest req) {

        BigDecimal dailyRate = req.getInterestRate()
                                  .divide(req.getBase(), 10, RoundingMode.HALF_UP);

        BigDecimal divisor = BigDecimal.ONE.add(
                dailyRate.multiply(BigDecimal.valueOf(req.getDays())));

        BigDecimal percentage = BigDecimal.ONE.divide(divisor, 10, RoundingMode.HALF_UP);

        return new PercentageDTOResponse(percentage);
    }

    public CalculateDTOResponse calculate(List<CalculateDTORequest> request) {

        log.info("Starting calculate() for {} items", request == null ? 0 : request.size());

        if (request == null || request.isEmpty()) {
            log.warn("Request list is null or empty");
            return new CalculateDTOResponse();
        }

        BigDecimal interestRate   = getInterestRate();
        BigDecimal commissionRate = getCommissionRate();
        BigDecimal base           = getBase();

        BigDecimal totFinance   = BigDecimal.ZERO.setScale(SCALE);
        BigDecimal totInterest  = BigDecimal.ZERO.setScale(SCALE);
        BigDecimal totCommission= BigDecimal.ZERO.setScale(SCALE);
        BigDecimal totDisburse  = BigDecimal.ZERO.setScale(SCALE);

        CalculateDTOResponse resp = new CalculateDTOResponse();

        for (CalculateDTORequest dto : request) {

            DetailDTOResponse d = new DetailDTOResponse();

            BigDecimal pct = getPercentage(new PercentageDTORequest(
                    interestRate, base, dto.getDiffDays())).getPercentage();

            BigDecimal amount = dto.getAmount();
            BigDecimal amountToFinance = amount.multiply(pct)
                                               .setScale(SCALE, RoundingMode.HALF_UP);

            BigDecimal dailyRate = interestRate.divide(base, 10, RoundingMode.HALF_UP);
            BigDecimal interest = dailyRate.multiply(amountToFinance)
                                           .multiply(BigDecimal.valueOf(dto.getDiffDays()))
                                           .setScale(SCALE, RoundingMode.HALF_UP);

            BigDecimal commission = amountToFinance.multiply(commissionRate)
                                                   .setScale(SCALE, RoundingMode.HALF_UP);

            BigDecimal disburse = amount.subtract(interest)
                                        .subtract(commission)
                                        .setScale(SCALE, RoundingMode.HALF_UP);

            d.setDocumentNumber(dto.getDocumentNumber());
            d.setCutOffDate(dto.getCutOffDate());
            d.setAmountToFinance(amountToFinance);
            d.setInterests(interest);
            d.setCommissions(commission);
            d.setAmountToBeDisbursed(disburse);

            resp.getDetail().add(d);

            totFinance    = totFinance.add(amountToFinance);
            totInterest   = totInterest.add(interest);
            totCommission = totCommission.add(commission);
            totDisburse   = totDisburse.add(disburse);
        }

        resp.setAmountToFinance(totFinance);
        resp.setInterests(totInterest);
        resp.setCommissions(totCommission);
        resp.setAmountToBeDisbursed(totDisburse);

        log.info("Totals → finance={}, interests={}, commission={}, disburse={}",
                 totFinance, totInterest, totCommission, totDisburse);

        return resp;
    }
}

*/

/*

package com.davivienda.factoraje.service;

import com.davivienda.factoraje.components.AppParameterLoader;
import com.davivienda.factoraje.domain.dto.calculate.CalculateDTORequest;
import com.davivienda.factoraje.domain.dto.calculate.CalculateDTOResponse;
import com.davivienda.factoraje.domain.dto.calculate.DetailDTOResponse;
import com.davivienda.factoraje.domain.dto.calculate.PercentageDTORequest;
import com.davivienda.factoraje.domain.dto.calculate.PercentageDTOResponse;
import com.davivienda.factoraje.domain.model.ParameterModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OperationsService {

    private final AppParameterLoader parameterLoader;
    private static final Logger log = LoggerFactory.getLogger(OperationsService.class);

    private static final String PARAM_KEY_INTEREST  = "param.key.interest";
    private static final String PARAM_KEY_COMISSION = "param.key.comission";
    private static final String PARAM_KEY_BASE      = "param.key.base";

    private BigDecimal interestRate;
    private BigDecimal commissionRate;
    private BigDecimal base;

    private static final BigDecimal DEFAULT_INTEREST_RATE   = new BigDecimal("0.0");
    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.0");
    private static final BigDecimal DEFAULT_BASE            = new BigDecimal("0");

    private static final int SCALE = 2; // Precisión de 2 decimales para los cálculos monetarios

    public OperationsService(AppParameterLoader parameterLoader) {
        this.parameterLoader = parameterLoader;
        log.info("OperationsService ha sido instanciado.");
    }

    @PostConstruct
    private void init() {
        log.info("Iniciando carga de parámetros de la aplicación...");
        for (ParameterModel p : parameterLoader.getParameters()) {
            try {
                switch (p.getKey()) {
                    case PARAM_KEY_INTEREST:
                        interestRate = new BigDecimal(p.getValue());
                        break;
                    case PARAM_KEY_COMISSION:
                        commissionRate = new BigDecimal(p.getValue());
                        break;
                    case PARAM_KEY_BASE:
                        base = new BigDecimal(p.getValue());
                        break;
                    default:
                        // Ignorar otros parámetros que no sean relevantes para este servicio
                }
            } catch (NumberFormatException ex) {
                log.error("Valor no numérico '{}' encontrado para la clave '{}'. Se usará el valor por defecto.", p.getValue(), p.getKey());
            }
        }
        log.info("Parámetros cargados y listos → Interés={}, Comisión={}, Base={}",
                 getInterestRate(), getCommissionRate(), getBase());
    }
    
    private BigDecimal getInterestRate()   {
        return interestRate != null ? interestRate : DEFAULT_INTEREST_RATE;
    }

    private BigDecimal getCommissionRate() {
        return commissionRate != null ? commissionRate : DEFAULT_COMMISSION_RATE;
    }
    
    private BigDecimal getBase() {
        return base != null ? base : DEFAULT_BASE;
    }

    public PercentageDTOResponse getPercentage(PercentageDTORequest req) {
        BigDecimal dailyRate = req.getInterestRate().divide(req.getBase(), 10, RoundingMode.HALF_UP);
        BigDecimal daysFactor = dailyRate.multiply(BigDecimal.valueOf(req.getDays()));
        BigDecimal divisor = BigDecimal.ONE.add(daysFactor);

        // Prevenir división por cero si el divisor es 0
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            return new PercentageDTOResponse(BigDecimal.ZERO);
        }

        BigDecimal percentage = BigDecimal.ONE.divide(divisor, 10, RoundingMode.HALF_UP);
        return new PercentageDTOResponse(percentage);
    }

    public CalculateDTOResponse calculate(List<CalculateDTORequest> request) {
        log.info("Iniciando cálculo para {} item(s) usando parámetros en memoria.", request.size());

        BigDecimal currentInterestRate = getInterestRate();
        BigDecimal currentCommissionRate = getCommissionRate();
        BigDecimal currentBase = getBase();

        BigDecimal totalAmountToFinance = BigDecimal.ZERO.setScale(SCALE);
        BigDecimal totalInterests = BigDecimal.ZERO.setScale(SCALE);
        BigDecimal totalCommissions = BigDecimal.ZERO.setScale(SCALE);
        BigDecimal totalAmountToDisburse = BigDecimal.ZERO.setScale(SCALE);

        CalculateDTOResponse response = new CalculateDTOResponse();

        for (CalculateDTORequest dto : request) {
            DetailDTOResponse detail = new DetailDTOResponse();

            // 1. Calcular porcentaje de financiación
            BigDecimal percentage = getPercentage(new PercentageDTORequest(
                currentInterestRate, currentBase, dto.getDiffDays())).getPercentage();

            // 2. Calcular monto a financiar
            BigDecimal amount = dto.getAmount();
            BigDecimal amountToFinance = amount.multiply(percentage).setScale(SCALE, RoundingMode.HALF_UP);

            // 3. Calcular intereses
            BigDecimal dailyRate = currentInterestRate.divide(currentBase, 10, RoundingMode.HALF_UP);
            BigDecimal interest = dailyRate.multiply(amountToFinance)
                                           .multiply(BigDecimal.valueOf(dto.getDiffDays()))
                                           .setScale(SCALE, RoundingMode.HALF_UP);

            // 4. Calcular comisión
            BigDecimal commission = amountToFinance.multiply(currentCommissionRate).setScale(SCALE, RoundingMode.HALF_UP);

            // 5. Calcular monto a desembolsar (abonar)
            BigDecimal disburse = amount.subtract(interest).subtract(commission).setScale(SCALE, RoundingMode.HALF_UP);

            detail.setDocumentNumber(dto.getDocumentNumber());
            detail.setCutOffDate(dto.getCutOffDate());
            detail.setAmountToFinance(amountToFinance);
            detail.setInterests(interest);
            detail.setCommissions(commission);
            detail.setAmountToBeDisbursed(disburse);
            response.getDetail().add(detail);

            totalAmountToFinance = totalAmountToFinance.add(amountToFinance);
            totalInterests = totalInterests.add(interest);
            totalCommissions = totalCommissions.add(commission);
            totalAmountToDisburse = totalAmountToDisburse.add(disburse);
        }

        response.setAmountToFinance(totalAmountToFinance);
        response.setInterests(totalInterests);
        response.setCommissions(totalCommissions);
        response.setAmountToBeDisbursed(totalAmountToDisburse);

        log.info("Cálculo finalizado. Total a desembolsar: {}", totalAmountToDisburse);
        return response;
    }
}
*/

package com.davivienda.factoraje.service;

import com.davivienda.factoraje.domain.dto.calculate.CalculateDTORequest;
import com.davivienda.factoraje.domain.dto.calculate.CalculateDTOResponse;
import com.davivienda.factoraje.domain.dto.calculate.DetailDTOResponse;
import com.davivienda.factoraje.domain.dto.calculate.PercentageDTORequest;
import com.davivienda.factoraje.domain.dto.calculate.PercentageDTOResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OperationsService {

    private final ParameterService parameterService; // <-- 1. Nueva y única dependencia
    private static final Logger log = LoggerFactory.getLogger(OperationsService.class);

    // Las llaves para buscar los parámetros se mantienen
    private static final String PARAM_KEY_INTEREST  = "param.key.interest";
    private static final String PARAM_KEY_COMISSION = "param.key.comission";
    private static final String PARAM_KEY_BASE      = "param.key.base";

    // Los valores por defecto siguen siendo útiles por si un parámetro no está en la BD
    private static final BigDecimal DEFAULT_INTEREST_RATE   = new BigDecimal("0.18");
    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.0025");
    private static final BigDecimal DEFAULT_BASE            = new BigDecimal("360");

    private static final int SCALE = 2;

    // El constructor ahora inyecta el ParameterService
    public OperationsService(ParameterService parameterService) {
        this.parameterService = parameterService;
        log.info("OperationsService inicializado y listo para usar el servicio de parámetros en caché.");
    }

    /**
     * Orquesta el cálculo principal.
     * Ahora los parámetros se obtienen "bajo demanda" al inicio del método.
     */
    public CalculateDTOResponse calculate(List<CalculateDTORequest> request) {
        log.info("Iniciando cálculo para {} item(s).", request.size());

        // 2. Obtener parámetros usando el servicio. La caché hace su magia aquí.
        BigDecimal interestRate   = getParameterAsBigDecimal(PARAM_KEY_INTEREST, DEFAULT_INTEREST_RATE);
        BigDecimal commissionRate = getParameterAsBigDecimal(PARAM_KEY_COMISSION, DEFAULT_COMMISSION_RATE);
        BigDecimal base           = getParameterAsBigDecimal(PARAM_KEY_BASE, DEFAULT_BASE);

        log.info("Parámetros para esta transacción -> Interés={}, Comisión={}, Base={}", interestRate, commissionRate, base);

        BigDecimal totalAmountToFinance  = BigDecimal.ZERO;
        BigDecimal totalInterests        = BigDecimal.ZERO;
        BigDecimal totalCommissions      = BigDecimal.ZERO;
        BigDecimal totalAmountToDisburse = BigDecimal.ZERO;

        CalculateDTOResponse response = new CalculateDTOResponse();

        for (CalculateDTORequest dto : request) {
            DetailDTOResponse detail = new DetailDTOResponse();
            
            BigDecimal percentage = getPercentage(new PercentageDTORequest(interestRate, base, dto.getDiffDays()))
                                        .getPercentage();

            BigDecimal amount = dto.getAmount();
            BigDecimal amountToFinance = amount.multiply(percentage).setScale(SCALE, RoundingMode.HALF_UP);

            BigDecimal dailyRate = interestRate.divide(base, 10, RoundingMode.HALF_UP);
            BigDecimal interest = dailyRate.multiply(amountToFinance)
                                           .multiply(BigDecimal.valueOf(dto.getDiffDays()))
                                           .setScale(SCALE, RoundingMode.HALF_UP);

            BigDecimal commission = amountToFinance.multiply(commissionRate).setScale(SCALE, RoundingMode.HALF_UP);
            BigDecimal disburse = amount.subtract(interest).subtract(commission).setScale(SCALE, RoundingMode.HALF_UP);

            detail.setDocumentNumber(dto.getDocumentNumber());
            detail.setCutOffDate(dto.getCutOffDate());
            detail.setAmountToFinance(amountToFinance);
            detail.setInterests(interest);
            detail.setCommissions(commission);
            detail.setAmountToBeDisbursed(disburse);
            response.getDetail().add(detail);

            totalAmountToFinance  = totalAmountToFinance.add(amountToFinance);
            totalInterests        = totalInterests.add(interest);
            totalCommissions      = totalCommissions.add(commission);
            totalAmountToDisburse = totalAmountToDisburse.add(disburse);
        }

        response.setAmountToFinance(totalAmountToFinance.setScale(SCALE, RoundingMode.HALF_UP));
        response.setInterests(totalInterests.setScale(SCALE, RoundingMode.HALF_UP));
        response.setCommissions(totalCommissions.setScale(SCALE, RoundingMode.HALF_UP));
        response.setAmountToBeDisbursed(totalAmountToDisburse.setScale(SCALE, RoundingMode.HALF_UP));

        log.info("Cálculo finalizado. Total a desembolsar: {}", totalAmountToDisburse);
        return response;
    }

    /**
     * Método de utilidad para obtener un parámetro y convertirlo a BigDecimal de forma segura.
     * @param key La clave del parámetro a buscar.
     * @param defaultValue El valor a retornar si el parámetro no existe o no es un número.
     * @return El valor del parámetro como BigDecimal.
     */
    private BigDecimal getParameterAsBigDecimal(String key, BigDecimal defaultValue) {
        try {
            // Llama a nuestro servicio cacheable
            String value = parameterService.getValueByKey(key); 
            return new BigDecimal(value);
        } catch (RuntimeException e) {
            log.warn("No se pudo obtener o convertir el parámetro '{}'. Usando valor por defecto: {}. Causa: {}",
                     key, defaultValue, e.getMessage());
            return defaultValue;
        }
    }

    // El método getPercentage se mantiene igual
    public PercentageDTOResponse getPercentage(PercentageDTORequest req) {
        if (req.getBase().compareTo(BigDecimal.ZERO) == 0) {
            log.error("División por cero evitada: la base no puede ser cero.");
            return new PercentageDTOResponse(BigDecimal.ZERO);
        }
        BigDecimal dailyRate = req.getInterestRate().divide(req.getBase(), 10, RoundingMode.HALF_UP);
        BigDecimal daysFactor = dailyRate.multiply(BigDecimal.valueOf(req.getDays()));
        BigDecimal divisor = BigDecimal.ONE.add(daysFactor);

        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            return new PercentageDTOResponse(BigDecimal.ZERO);
        }
        BigDecimal percentage = BigDecimal.ONE.divide(divisor, 10, RoundingMode.HALF_UP);
        return new PercentageDTOResponse(percentage);
    }
}
