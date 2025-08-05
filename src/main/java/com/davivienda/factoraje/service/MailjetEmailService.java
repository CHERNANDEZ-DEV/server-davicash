package com.davivienda.factoraje.service;

import org.springframework.stereotype.Service;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import com.davivienda.factoraje.components.AppParameterLoader;
import com.davivienda.factoraje.domain.dto.Emails.DestinatarioRequestDTO;
import com.davivienda.factoraje.domain.dto.Emails.HTMLVariablesDTO;
import com.davivienda.factoraje.domain.model.ParameterModel;
import com.mailjet.client.ClientOptions;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MailjetEmailService {

    private final AppParameterLoader parameterLoader;

    private static final String PARAM_KEY_API_KEY = "mailjet.api.key";
    private static final String PARAM_KEY_API_SECRET = "mailjet.api.secret";
    private static final String PARAM_KEY_EMAIL = "mailjet.email.sender";

    private String paramValueApiKey;
    private String paramValueApiSecret;
    private String paramValueEmailSender;

    private MailjetClient mailjetClient;

    @Autowired
    public MailjetEmailService(AppParameterLoader parameterLoader) {
        this.parameterLoader = parameterLoader;
    }

    public void loadParameters() {
        for (ParameterModel p : parameterLoader.getParameters()) {

            switch (p.getKey()) {
                case PARAM_KEY_API_KEY:
                    paramValueApiKey = p.getValue();
                    break;
                case PARAM_KEY_API_SECRET:
                    paramValueApiSecret = p.getValue();
                    break;
                case PARAM_KEY_EMAIL:
                    paramValueEmailSender = p.getValue();
                default:
                    break;
            }
        }

        this.mailjetClient = new MailjetClient(
                paramValueApiKey,
                paramValueApiSecret,
                new ClientOptions("v3.1"));
    }

    private String obtenerHtmlPorTipo(int tipoHtml) {
        String html;
        switch (tipoHtml) {
            case 1:
                html = "<!DOCTYPE html>" +
                        "<html lang=\"es\">" +
                        "<head>" +
                        "    <meta charset=\"UTF-8\" />" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                        "    <title>Autorización de Desembolso</title>" +
                        "    <style>" +
                        "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #ffffff; color: #000000; margin: 40px; }"
                        +
                        "        .container { max-width: 800px; margin: auto; padding: 30px; border-radius: 10px; background-color: #ffffff; }"
                        +
                        "        .asunto { font-weight: bold; margin-top: 20px; }" +
                        "        .firma { margin-top: 50px; }" +
                        "        ul { margin-top: 10px; }" +
                        "        li { margin-bottom: 10px; }" +
                        "        .footer { margin-top: 40px; font-style: italic; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class=\"container\">" +
                        "        <p><strong>Señores</strong><br>" +
                        "            <strong>BANCO DAVIVIENDA SALVADOREÑO S.A.</strong><br>" +
                        "            <strong>Presente.</strong>" +
                        "        </p>" +
                        "        <p class=\"asunto\">Asunto: Autorización de desembolso de Línea de Crédito para Pago a Proveedores</p>"
                        +
                        "        <p><strong>Estimados señores de Banco Davivienda Salvadoreño, S.A.:</strong></p>" +
                        "        <p>" +
                        "            Por medio de la presente, <strong>[Nombre de la Empresa]</strong> (en adelante, “la Empresa”), con Número de "
                        +
                        "            Identificación Tributaria (NIT), solicita y autoriza formalmente el desembolso de nuestra Línea de Crédito "
                        +
                        "            número <strong>[Número de Línea de Crédito]</strong> para el pago de proveedores, según el detalle que se "
                        +
                        "            especifica a continuación:" +
                        "        </p>" +
                        "        <p><strong>Detalles del Desembolso:</strong></p>" +
                        "        <ul>" +
                        "            <li><strong>Nombre del Proveedor:</strong> [Nombre del Proveedor]</li>" +
                        "            <li><strong>Número de Cuenta del Proveedor:</strong> [Número de Cuenta del Proveedor]</li>"
                        +
                        "            <li><strong>Monto a Desembolsar:</strong> [Monto a Desembolsar]</li>" +
                        "            <p>...................................</p>" +
                        "        </ul>" +
                        "        <p>" +
                        "            Agradecemos poder atender las solicitudes de adelanto de pago a nuestros proveedores, quienes dan por "
                        +
                        "            entendido que el desembolso que se les hará llevará inmerso un descuento por pronto pago según la fecha que "
                        +
                        "            ellos estimen anticipar su cuenta por cobrar, todo lo anterior en común acuerdo de pagador y proveedor."
                        +
                        "        </p>" +
                        "        <p><strong>Cláusulas Legales:</strong></p>" +
                        "        <ul>" +
                        "            <li><strong>Responsabilidad de la Empresa:</strong> La Empresa se hace totalmente responsable por la "
                        +
                        "                exactitud y veracidad de la información proporcionada en esta solicitud y en los documentos enviados "
                        +
                        "                para dicho fin.</li>" +
                        "            <li><strong>Conformidad con el Contrato:</strong> Esta autorización se otorga en conformidad con los "
                        +
                        "                términos y condiciones del contrato de la línea de crédito número <strong>[Número de Línea de "
                        +
                        "                Crédito]</strong>, el cual permanece en pleno vigor y efecto.</li>" +
                        "            <li><strong>Liberación de Responsabilidad:</strong> La Empresa libera a Banco Davivienda Salvadoreño, S.A. de toda "
                        +
                        "                responsabilidad por cualquier disputa o reclamo que pueda surgir entre la Empresa y sus proveedores en "
                        +
                        "                relación con los desembolsos realizados bajo esta autorización.</li>" +
                        "        </ul>" +
                        "        <p class=\"footer\">Agradecemos se pueda atender la presente solicitud a la brevedad posible.</p>"
                        +
                        "        <div class=\"firma\">" +
                        "            <p>Atentamente,</p>" +
                        "            <p><strong>[Nombre de la Empresa]</strong></p>" +
                        "        </div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>";
                break;
            case 2: html =
                        "<!DOCTYPE html>" +
                        "<html lang=\"es\">" +
                        "<head>" +
                        "  <meta charset=\"UTF-8\">" +
                        "  <title>Notificación de documentos</title>" +
                        "</head>" +
                        // Fondo blanco en todo el mensaje
                        "<body style=\"font-family: Verdana, sans-serif; background-color:#ffffff; margin:0; padding:0;\">" +

                        /* --- CONTENEDOR PRINCIPAL (sin borde) --- */
                        "  <table align=\"center\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" " +
                        "         style=\"background-color:#ffffff;\">" +

                        /* --- CABECERA --- */
                        "    <tr>" +
                        "      <td style=\"padding:24px 32px; text-align:center;\">" +
                        "        <h2 style=\"color:#b40000; margin:0 0 16px 0;\">Confirmación de carga de documentos</h2>" +
                        "        <p style=\"color:#333333; font-size:14px; line-height:1.5; margin:0 0 24px 0;\">" +
                        "          Estimado usuario,<br><br>" +
                        "          Se le notifica que existen nuevos documentos disponibles para su financiamiento. " +
                        "          Ingrese a la plataforma para revisarlos y continuar con el proceso." +
                        "        </p>" +

                        /* --- BOTÓN CTA --- */
                        "        <a href=\"https://www.facebook.com\" " + 
                        "           style=\"display:inline-block; padding:12px 24px; background-color:#b40000; " +
                        "                  color:#ffffff; text-decoration:none; border-radius:4px; font-weight:bold;\">" +
                        "          Acceder a la plataforma" +
                        "        </a>" +
                        "      </td>" +
                        "    </tr>" +

                        /* --- PIE DE PÁGINA --- */
                        "    <tr>" +
                        "      <td style=\"padding:16px 32px; font-size:12px; color:#777777; text-align:center;\">" +
                        "        Si tiene alguna consulta, comuníquese con su ejecutivo de cuenta. <br><br>" +
                        "         <strong>BANCO DAVIVIENDA SALVADOREÑO S.A.</strong>" +
                        "      </td>" +
                        "    </tr>" +
                        "  </table>" +
                        "</body>" +
                        "</html>";
                break;
            case 3:
                html =
                        "<!DOCTYPE html>" +
                        "<html lang=\"es\">" +
                        "<head>" +
                        "  <meta charset=\"UTF-8\">" +
                        "  <title>Documentos listos para aprobación</title>" +
                        "</head>" +
                        "<body style=\"font-family: Verdana, sans-serif; background-color:#ffffff; margin:0; padding:0;\">" +

                        /* --- CONTENEDOR PRINCIPAL (sin borde) --- */
                        "  <table align=\"center\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" " +
                        "         style=\"background-color:#ffffff;\">" +

                        /* --- CABECERA --- */
                        "    <tr>" +
                        "      <td style=\"padding:24px 32px; text-align:center;\">" +
                        "        <h2 style=\"color:#b40000; margin:0 0 16px 0;\">Documentos listos para aprobación</h2>" +
                        "        <p style=\"color:#333333; font-size:14px; line-height:1.5; margin:0 0 24px 0;\">" +
                        "          Estimado usuario,<br><br>" +
                        "          Se ha solicitado el financiamiento de nuevos documentos y se encuentran listos para su aprobación. " +
                        "          Ingrese a la plataforma para revisarlos y completar el proceso." +
                        "        </p>" +

                        /* --- BOTÓN CTA --- */
                        "        <a href=\"https://www.facebook.com\" " +
                        "           style=\"display:inline-block; padding:12px 24px; background-color:#b40000; " +
                        "                  color:#ffffff; text-decoration:none; border-radius:4px; font-weight:bold;\">" +
                        "          Revisar y aprobar documentos" +
                        "        </a>" +
                        "      </td>" +
                        "    </tr>" +

                        /* --- PIE DE PÁGINA --- */
                        "    <tr>" +
                        "      <td style=\"padding:16px 32px; font-size:12px; color:#777777; text-align:center;\">" +
                        "        Si tiene alguna consulta, responda a este correo o comuníquese con su ejecutivo de cuenta. <br><br>" +
                        "         <strong>BANCO DAVIVIENDA SALVADOREÑO S.A.</strong>" +
                        "      </td>" +
                        "    </tr>" +
                        "  </table>" +
                        "</body>" +
                        "</html>";
                break;
            case 4:
                html = "<!DOCTYPE html>" +
                        "<html lang=\"es\">" +
                        "<head>" +
                        "    <meta charset=\"UTF-8\" />" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                        "    <title>Autorización de Desembolso</title>" +
                        "    <style>" +
                        "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #ffffff; color: #000000; margin: 40px; }"
                        +
                        "        .container { max-width: 800px; margin: auto; padding: 30px; border-radius: 10px; background-color: #ffffff; }"
                        +
                        "        .asunto { font-weight: bold; margin-top: 20px; }" +
                        "        .firma { margin-top: 50px; }" +
                        "        ul { margin-top: 10px; }" +
                        "        li { margin-bottom: 10px; }" +
                        "        .footer { margin-top: 40px; font-style: italic; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class=\"container\">" +
                        "        <p><strong>Señores</strong><br>" +
                        "            <strong>BANCO DAVIVIENDA SALVADOREÑO S.A.</strong><br>" +
                        "            <strong>Presente.</strong>" +
                        "        </p>" +
                        "        <p class=\"asunto\">Asunto: Autorización de desembolso de Línea de Crédito para Pago a Proveedores</p>"
                        +
                        "        <p><strong>Estimados señores de Banco Davivienda Salvadoreño, S.A.:</strong></p>" +
                        "        <p>" +
                        "            Por medio de la presente, <strong>[Nombre de la Empresa]</strong> (en adelante, “la Empresa”), con Número de "
                        +
                        "            Identificación Tributaria (NIT), solicita y autoriza formalmente el desembolso de nuestra Línea de Crédito "
                        +
                        "            número <strong>[Número de Línea de Crédito]</strong> para el pago de proveedores, según el detalle que se "
                        +
                        "            especifica a continuación:" +
                        "        </p>" +
                        "        <p><strong>Detalles del Desembolso:</strong></p>" +
                        "        <ul>" +
                        "            <li><strong>Nombre del Proveedor:</strong> [Nombre del Proveedor]</li>" +
                        "            <li><strong>Número de Cuenta del Proveedor:</strong> [Número de Cuenta del Proveedor]</li>"
                        +
                        "            <li><strong>Monto a Desembolsar:</strong> [Monto a Desembolsar]</li>" +
                        "            <p>...................................</p>" +
                        "        </ul>" +
                        "        <p>" +
                        "            Agradecemos poder atender las solicitudes de adelanto de pago a nuestros proveedores, quienes dan por "
                        +
                        "            entendido que el desembolso que se les hará llevará inmerso un descuento por pronto pago según la fecha que "
                        +
                        "            ellos estimen anticipar su cuenta por cobrar, todo lo anterior en común acuerdo de pagador y proveedor."
                        +
                        "        </p>" +
                        "        <p><strong>Cláusulas Legales:</strong></p>" +
                        "        <ul>" +
                        "            <li><strong>Responsabilidad de la Empresa:</strong> La Empresa se hace totalmente responsable por la "
                        +
                        "                exactitud y veracidad de la información proporcionada en esta solicitud y en los documentos enviados "
                        +
                        "                para dicho fin.</li>" +
                        "            <li><strong>Conformidad con el Contrato:</strong> Esta autorización se otorga en conformidad con los "
                        +
                        "                términos y condiciones del contrato de la línea de crédito número <strong>[Número de Línea de "
                        +
                        "                Crédito]</strong>, el cual permanece en pleno vigor y efecto.</li>" +
                        "            <li><strong>Liberación de Responsabilidad:</strong> La Empresa libera a Banco Davivienda Salvadoreño, S.A. de toda "
                        +
                        "                responsabilidad por cualquier disputa o reclamo que pueda surgir entre la Empresa y sus proveedores en "
                        +
                        "                relación con los desembolsos realizados bajo esta autorización.</li>" +
                        "        </ul>" +
                        "        <p class=\"footer\">Agradecemos se pueda atender la presente solicitud a la brevedad posible.</p>"
                        +
                        "        <div class=\"firma\">" +
                        "            <p>Atentamente,</p>" +
                        "            <p><strong>[Nombre de la Empresa]</strong></p>" +
                        "        </div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>";
                break;
            default:
                html = "<!DOCTYPE html><html><body><p>Error: tipo de HTML inválido.</p></body></html>";
                break;
        }
        return html;
    }

    private String reemplazarVariablesHtml(String html, HTMLVariablesDTO vars) {
        return html
                .replace("[Nombre de la Empresa]", vars.getNombreEmpresa())
                .replace("[Número de Línea de Crédito]", vars.getNumeroLineaCredito())
                .replace("[Nombre del Proveedor]", vars.getNombreProveedor())
                .replace("[Número de Cuenta del Proveedor]", vars.getNumeroCuentaProveedor())
                .replace("[Monto a Desembolsar]", vars.getMontoDesembolsar());
    }

    public void sendEmail(List<DestinatarioRequestDTO> destinatarios, int tipoHtml, HTMLVariablesDTO variables)
            throws Exception {
        JSONArray toArray = new JSONArray();
        for (DestinatarioRequestDTO dest : destinatarios) {
            toArray.put(new JSONObject()
                    .put("Email", dest.getEmail())
                    .put("Name", dest.getName()));
        }

        String htmlSeleccionado = obtenerHtmlPorTipo(tipoHtml);
        htmlSeleccionado = reemplazarVariablesHtml(htmlSeleccionado, variables);

        JSONObject mensaje = new JSONObject()
                .put("From", new JSONObject()
                        .put("Email", paramValueEmailSender)
                        .put("Name", "Davicash"))
                .put("To", toArray)
                .put("Subject", "Notificaciones automáticas")
                .put("HTMLPart", htmlSeleccionado);

        MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray().put(mensaje));

        MailjetResponse response = mailjetClient.post(request);

        System.out.println("Status: " + response.getStatus());
        System.out.println("Respuesta: " + response.getData());
    }
}
