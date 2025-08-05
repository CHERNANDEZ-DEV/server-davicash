package com.davivienda.factoraje.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.davivienda.factoraje.domain.dto.calculate.CalculateDTORequest;
import com.davivienda.factoraje.domain.dto.calculate.CalculateDTOResponse;
import com.davivienda.factoraje.domain.dto.calculate.DetailDTOResponse;
import com.davivienda.factoraje.domain.dto.calculate.PercentageDTORequest;
import com.davivienda.factoraje.domain.dto.calculate.PercentageDTOResponse;

@Service
public class OperationsService {

    private static final Logger logger = LoggerFactory.getLogger(OperationsService.class);

    public PercentageDTOResponse getPercentage(PercentageDTORequest request) {
        logger.info("Calculating percentage for interestRate={}, base={}, days={}",
                request.getInterestRate(), request.getBase(), request.getDays());

        double percentage = 1.0 / (1.0 + ((request.getInterestRate() / request.getBase()) * request.getDays()));
        PercentageDTOResponse response = new PercentageDTOResponse(percentage);

        logger.debug("Percentage calculated: {}", response.getPercentage());
        return response;
    }

    public CalculateDTOResponse calculate(List<CalculateDTORequest> request) {
        int itemCount = request != null ? request.size() : 0;
        logger.info("Starting calculate() for {} items", itemCount);

        if (request == null || request.isEmpty()) {
            logger.warn("Request list is null or empty");
            return new CalculateDTOResponse();
        }

        double totalAmountToFinance = 0.0;
        double totalInterests = 0.0;
        double totalCommissions = 0.0;
        double totalAmountToBeDisbursed = 0.0;

        double interestRate = 0.18;
        double commissionRate = 0.0025;
        double base = 360;

        CalculateDTOResponse calculateResponse = new CalculateDTOResponse();

        for (CalculateDTORequest dtoReq : request) {
            logger.debug("Processing item: {}", dtoReq);
            DetailDTOResponse detail = new DetailDTOResponse();

            double percentage = getPercentage(
                    new PercentageDTORequest(interestRate, base, dtoReq.getDiffDays())).getPercentage();

            double amount = dtoReq.getAmount();
            double amountToFinance = amount * percentage;
            double interests = (interestRate / base) * amountToFinance * dtoReq.getDiffDays();
            double commissions = amountToFinance * commissionRate;
            double amountToDisburse = amount - interests - commissions;

            detail.setAmountToFinance(amountToFinance);
            detail.setInterests(interests);
            detail.setCommissions(commissions);
            detail.setAmountToBeDisbursed(amountToDisburse);
            detail.setCutOffDate(dtoReq.getCutOffDate());
            detail.setDocumentNumber(dtoReq.getDocumentNumber());

            totalAmountToFinance += amountToFinance;
            totalInterests += interests;
            totalCommissions += commissions;
            totalAmountToBeDisbursed += amountToDisburse;

            calculateResponse.getDetail().add(detail);
        }

        calculateResponse.setAmountToFinance(totalAmountToFinance);
        calculateResponse.setInterests(totalInterests);
        calculateResponse.setCommissions(totalCommissions);
        calculateResponse.setAmountToBeDisbursed(totalAmountToBeDisbursed);

        logger.info(
                "Calculation completed: totals - amountToFinance={}, interests={}, commissions={}, amountToBeDisbursed={}",
                totalAmountToFinance, totalInterests, totalCommissions, totalAmountToBeDisbursed);

        return calculateResponse;
    }
}
