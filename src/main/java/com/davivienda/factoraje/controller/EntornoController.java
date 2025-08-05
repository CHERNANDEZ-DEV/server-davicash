package com.davivienda.factoraje.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;

// import com.davivienda.factoraje.auth.InitEntorno;
// import com.davivienda.factoraje.domain.dto.Auth.PeticionEntornoRequest;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// @RestController
// @RequestMapping("/api/entorno")
// public class EntornoController {

//     private static final Logger logger = LoggerFactory.getLogger(EntornoController.class);
//     private final InitEntorno initEntorno;

//     @Autowired
//     public EntornoController(InitEntorno initEntorno) {
//         logger.info("EntornoController initialized");
//         this.initEntorno = initEntorno;
//     }

//     @PostMapping("/peticion")
//     public String recibirPeticion(@RequestBody PeticionEntornoRequest request) {
//         logger.info("Recibiendo petición para fábrica: {}, servicio: {}", request.getFabrica(), request.getServicio());
//         try {
//             initEntorno.arrancarJ2EntornoCliente();
//             StringBuilder xmlBuilder = new StringBuilder();
//             xmlBuilder.append("<peticionEntorno>\n")
//                   .append("    <header>\n")
//                   .append("        <fabrica>").append(request.getFabrica()).append("</fabrica>\n")
//                   .append("        <servicio>").append(request.getServicio()).append("</servicio>\n")
//                   .append("    </header>\n")
//                   .append("    <body>\n")
//                   .append("        <usuario>").append(request.getUsuario()).append("</usuario>\n")
//                   .append("        <clave>").append(request.getClave()).append("</clave>\n")
//                   .append("        <tipoPersona>").append(request.getTipoPersona()).append("</tipoPersona>\n")
//                   .append("    </body>\n")
//                   .append("</peticionEntorno>");
//             String xmlPeticion = xmlBuilder.toString();

//             logger.debug("XML de petición generado: {}", xmlPeticion);

//             String respuestaXml = initEntorno.obtenerXMLDeFabrica(
//                 request.getFabrica(),
//                 request.getServicio(),
//                 xmlPeticion
//             );
//             logger.info("Respuesta generada correctamente para fábrica: {}, servicio: {}", request.getFabrica(), request.getServicio());
//             return respuestaXml;
//         } catch (Exception e) {
//             logger.error("Error procesando la petición para fábrica: {}, servicio: {}. Detalle: {}", request.getFabrica(), request.getServicio(), e.getMessage(), e);
//             return "<error>Ocurrió un error procesando la petición</error>";
//         }
//     }
// }