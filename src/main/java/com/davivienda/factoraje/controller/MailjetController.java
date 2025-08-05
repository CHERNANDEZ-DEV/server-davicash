package com.davivienda.factoraje.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davivienda.factoraje.domain.dto.Emails.EmailRequestDTO;
import com.davivienda.factoraje.service.MailjetEmailService;

@RestController
@RequestMapping("/api/mailjet")
public class MailjetController {

    public final MailjetEmailService mailjetEmailService;

    public MailjetController(MailjetEmailService mailjetEmailService) {
        this.mailjetEmailService = mailjetEmailService;
    }

    @PostMapping("/send")
    public String sendEmail(@RequestBody EmailRequestDTO request) {
        try {

            mailjetEmailService.loadParameters();
            mailjetEmailService.sendEmail(request.getDestinatarios(), request.getTipoHtml(),
                    request.getHtmlVariables());
            return "Correo enviado correctamente con HTML tipo " + request.getTipoHtml();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al enviar el correo.";
        }
    }
}
