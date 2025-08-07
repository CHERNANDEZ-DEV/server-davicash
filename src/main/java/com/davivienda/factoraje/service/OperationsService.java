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

    /* Valores leídos desde BD (cargados en loadParameters) */
    private BigDecimal interestRate   = null;    // p.ej. 0.18  → 18 %
    private BigDecimal commissionRate = null;    // p.ej. 0.0025 → 0.25 %
    private BigDecimal base           = null;    // p.ej. 360

    /* Defaults por si aún no se han cargado los parámetros */
    private static final BigDecimal DEFAULT_INTEREST_RATE   = new BigDecimal("0.0");
    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.0");
    private static final BigDecimal DEFAULT_BASE            = new BigDecimal("0");

    private static final int SCALE = 2; // centavos

    public OperationsService(AppParameterLoader parameterLoader) {
        this.parameterLoader = parameterLoader;
        log.info("OperationsService initialized");
    }

    /** Carga parámetros desde la BD (el controlador invoca este método). */
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

    /* ─── Getters con fallback ─── */
    private BigDecimal getInterestRate()   { return interestRate   != null ? interestRate   : DEFAULT_INTEREST_RATE; }
    private BigDecimal getCommissionRate() { return commissionRate != null ? commissionRate : DEFAULT_COMMISSION_RATE; }
    private BigDecimal getBase()           { return base           != null ? base           : DEFAULT_BASE; }

    /* ════════════════════════════════════════════════════════════════
     * PORCENTAJE = 1 / (1 + r/base * days)
     * ════════════════════════════════════════════════════════════════
     */
    public PercentageDTOResponse getPercentage(PercentageDTORequest req) {

        BigDecimal dailyRate = req.getInterestRate()
                                  .divide(req.getBase(), 10, RoundingMode.HALF_UP);

        BigDecimal divisor = BigDecimal.ONE.add(
                dailyRate.multiply(BigDecimal.valueOf(req.getDays())));

        BigDecimal percentage = BigDecimal.ONE.divide(divisor, 10, RoundingMode.HALF_UP);

        return new PercentageDTOResponse(percentage);
    }

    /* ════════════════════════════════════════════════════════════════
     * CÁLCULO PRINCIPAL
     * ════════════════════════════════════════════════════════════════
     */
    public CalculateDTOResponse calculate(List<CalculateDTORequest> request) {

        log.info("Starting calculate() for {} items", request == null ? 0 : request.size());

        if (request == null || request.isEmpty()) {
            log.warn("Request list is null or empty");
            return new CalculateDTOResponse();
        }

        /* Obtiene parámetros (ya deben haber sido cargados) */
        BigDecimal interestRate   = getInterestRate();
        BigDecimal commissionRate = getCommissionRate();
        BigDecimal base           = getBase();

        /* ─── acumuladores ─── */
        BigDecimal totFinance   = BigDecimal.ZERO.setScale(SCALE);
        BigDecimal totInterest  = BigDecimal.ZERO.setScale(SCALE);
        BigDecimal totCommission= BigDecimal.ZERO.setScale(SCALE);
        BigDecimal totDisburse  = BigDecimal.ZERO.setScale(SCALE);

        CalculateDTOResponse resp = new CalculateDTOResponse();

        /* ─── proceso fila a fila ─── */
        for (CalculateDTORequest dto : request) {

            DetailDTOResponse d = new DetailDTOResponse();

            /* porcentaje */
            BigDecimal pct = getPercentage(new PercentageDTORequest(
                    interestRate, base, dto.getDiffDays())).getPercentage();

            /* monto a financiar */
            BigDecimal amount = dto.getAmount();
            BigDecimal amountToFinance = amount.multiply(pct)
                                               .setScale(SCALE, RoundingMode.HALF_UP);

            /* intereses */
            BigDecimal dailyRate = interestRate.divide(base, 10, RoundingMode.HALF_UP);
            BigDecimal interest = dailyRate.multiply(amountToFinance)
                                           .multiply(BigDecimal.valueOf(dto.getDiffDays()))
                                           .setScale(SCALE, RoundingMode.HALF_UP);

            /* comisión */
            BigDecimal commission = amountToFinance.multiply(commissionRate)
                                                   .setScale(SCALE, RoundingMode.HALF_UP);

            /* a abonar */
            BigDecimal disburse = amount.subtract(interest)
                                        .subtract(commission)
                                        .setScale(SCALE, RoundingMode.HALF_UP);

            /* detalle */
            d.setDocumentNumber(dto.getDocumentNumber());
            d.setCutOffDate(dto.getCutOffDate());
            d.setAmountToFinance(amountToFinance);
            d.setInterests(interest);
            d.setCommissions(commission);
            d.setAmountToBeDisbursed(disburse);

            resp.getDetail().add(d);

            /* acumuladores */
            totFinance    = totFinance.add(amountToFinance);
            totInterest   = totInterest.add(interest);
            totCommission = totCommission.add(commission);
            totDisburse   = totDisburse.add(disburse);
        }

        /* totales */
        resp.setAmountToFinance(totFinance);
        resp.setInterests(totInterest);
        resp.setCommissions(totCommission);
        resp.setAmountToBeDisbursed(totDisburse);

        log.info("Totals → finance={}, interests={}, commission={}, disburse={}",
                 totFinance, totInterest, totCommission, totDisburse);

        return resp;
    }
}
