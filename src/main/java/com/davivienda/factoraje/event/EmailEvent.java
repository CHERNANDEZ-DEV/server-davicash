package com.davivienda.factoraje.event;

import java.util.UUID;

import com.davivienda.factoraje.domain.dto.Emails.EmailRequestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor 
@Data
public class EmailEvent {
    private final EmailRequestDTO request;
}
