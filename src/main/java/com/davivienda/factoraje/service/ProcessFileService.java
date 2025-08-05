package com.davivienda.factoraje.service;

import java.io.IOException;
import java.util.UUID;

import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.davivienda.factoraje.domain.model.AgreementModel;
import com.davivienda.factoraje.domain.model.DocumentModel;
import com.davivienda.factoraje.domain.model.EntityModel;
import com.davivienda.factoraje.domain.model.UserModel;

@Service
public class ProcessFileService {

    private static final Logger log = LoggerFactory.getLogger(ProcessFileService.class);

    private final DocumentService documentService;
    private final UserService userService;
    private final AgreementService agreementService;
    private final EntityService entityService;

    UserModel operatorUser = new UserModel();

    private static final String[] COLUMNAS_ESPERADAS = {
            "fechaEmision",
            "monto",
            "numeroDocumento",
            "nombreProveedor",
            "nit",
            "correoProveedor",
            "cuentaBancaria",
            "codigo"
    };

    public ProcessFileService(DocumentService documentService, UserService userService,
            AgreementService agreementService, EntityService entityService) {
        this.documentService = documentService;
        this.userService = userService;
        this.agreementService = agreementService;
        this.entityService = entityService;
    }

    private void validarCabeceras(Row headerRow) {
        if (headerRow == null) {
            throw new IllegalArgumentException("El archivo no tiene fila de cabeceras");
        }

        for (int i = 0; i < COLUMNAS_ESPERADAS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String headerValue = (cell != null) ? cell.getStringCellValue().trim() : "";

            if (!COLUMNAS_ESPERADAS[i].equalsIgnoreCase(headerValue)) {
                throw new IllegalArgumentException(
                        String.format("Columna %d esperada: '%s', encontrada: '%s'",
                                i + 1, COLUMNAS_ESPERADAS[i], headerValue));
            }
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    @Transactional
    public String procesarYGuardarExcelDos(MultipartFile file, UUID payerId, UUID userId) throws IOException {

        // Instancias para la lectura del archivo Excel
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        int filasProcesadas = 0;
        int filasDuplicadas = 0;

        // Validación de cabeceras del archivo Excel
        Row headerRow = sheet.getRow(0);
        validarCabeceras(headerRow);

        // Procesamiento de filas del archivo Excel
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            // Obtener la fila actual
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            // Obtener el número de documento y validar si es nulo o vacío
            String documentNumber = getStringCellValue(row.getCell(2));

            if (documentNumber == null || documentNumber.isEmpty()) {
                continue;
            }

            // Creación de instancias
            DocumentModel documento = new DocumentModel(); // Documento que va a representar una factura/ccf/etc
            AgreementModel agreement = new AgreementModel(); // Acuerdo que representa la relación entre el pagador y el
                                                             // proveedor

            EntityModel supplierEntity = new EntityModel(); // Proveedor de la factura/ccf/etc
            EntityModel payerEntity = new EntityModel(); // Pagador de la factura/ccf/etc

            // Obtenemos el codigo del proveedor para validar si existe
            String supplierCode = getStringCellValue(row.getCell(7));

            documento.setDocumentNumber(getStringCellValue(row.getCell(2))); // Número de documento
            documento.setAmount(Double.parseDouble(getStringCellValue(row.getCell(1)))); // Monto del documento
            documento.setIssueDate(getStringCellValue(row.getCell(0))); // Fecha de emisión del documento
            documento.setStatus("SELECTED"); // Estado del documento (asumimos que es true por defecto)
            documento.setStatusUpdateDate(getStringCellValue(row.getCell(0))); // Fecha de actualización del estado
                                                                               // (asumimos que es la misma fecha de
                                                                               // emisión)
            documento.setDisbursementDate(getStringCellValue(row.getCell(0))); // Fecha de desembolso (asumimos que es
                                                                               // la misma fecha de emisión)
            documento.setSupplierName(getStringCellValue(row.getCell(3))); // Nombre del proveedor

            // Con el id del pagador recibido, buscamos al pagador

            if (payerId == null) {
                throw new IllegalArgumentException("El ID del pagador recibido no puede ser nulo.");
            }

            if (entityService.getEntityById(payerId) == null) {
                throw new IllegalArgumentException("El pagador con ID " + payerId + " no existe.");
            } else {
                // Buscamos al pagador por ID y asignamos al acuerdo
                payerEntity = entityService.getEntityById(payerId);
                agreement.setPayer(payerId); // Asignar el pagador al acuerdo
            }

            // Validaciones para el proveedor
            if (supplierCode == null || supplierCode.isEmpty()) {
                throw new IllegalArgumentException("El código del proveedor no puede ser nulo o vacío.");
            }

            if (entityService.findByCode(supplierCode) == null) {

                supplierEntity.setName(getStringCellValue(row.getCell(3)));
                supplierEntity.setEmail(getStringCellValue(row.getCell(5)));
                supplierEntity.setCode(getStringCellValue(row.getCell(7)));
                supplierEntity.setNit(getStringCellValue(row.getCell(4)));
                supplierEntity.setAccountBank(getStringCellValue(row.getCell(6)));
                supplierEntity.setEntityType(false); // Asumimos que es un proveedor

                entityService.createEntity(supplierEntity); // Guardar el proveedor en la base de datos

                agreement.setSupplier(supplierEntity.getId()); // Asignar el proveedor al acuerdo

            } else {

                // Si el proveedor ya existe, lo buscamos por código y lo asignamos al acuerdo
                supplierEntity = entityService.findByCode(supplierCode);

                agreement.setSupplier(supplierEntity.getId());
                // documento.setSupplier(supplier);
            }

            // Verificar si el acuerdo ya existe
            String agreementIdentifier = payerId.toString().concat(supplierEntity.getId().toString());

            if (agreementService.findByIdentifier(agreementIdentifier) == null) {

                agreement.setIdentifier(agreementIdentifier);
                agreement.setName("Convenio entre " + payerEntity.getName() + " y " + supplierEntity.getName());
                agreement.setPayer(entityService.getEntityById(payerId).getId());
                agreement.setSupplier(supplierEntity.getId());

                operatorUser = userService.getUserById(userId);

                // Agregar el documento al convenio nuevo
                documento.setAgreement(agreement);
                documento.setUploadedBy(operatorUser);

                // guardar
                agreementService.save(agreement);

            } else {
                agreement = agreementService.findByIdentifier(agreementIdentifier);
                // Agregar el documento al convenio existente
                documento.setAgreement(agreement);

                // guardar
                agreementService.save(agreement);
            }

            documentService.createDocument(documento); // Guardar el documento en la base de datos

            filasProcesadas++;
        }

        workbook.close();
        String mensaje = "Archivo procesado. " + filasProcesadas + " registros guardados en la base de datos.";
        if (filasDuplicadas > 0) {
            mensaje += " " + filasDuplicadas + " registros duplicados encontrados y omitidos.";
        }

        return mensaje;
    }

    // Se asume que ya existe un pagador con el ID recibido
    @Transactional
    public String procesarYGuardarExcel(MultipartFile file, UUID payerId, UUID userId) throws IOException {

        // Instancias para la lectura del archivo Excel
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        int filasProcesadas = 0;
        int filasDuplicadas = 0;

        // Validación de cabeceras del archivo Excel
        Row headerRow = sheet.getRow(0);
        validarCabeceras(headerRow);

        // Procesamiento de filas del archivo Excel
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            // Obtener la fila actual
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            // Obtener el número de documento y validar si es nulo o vacío
            String documentNumber = getStringCellValue(row.getCell(2));

            if (documentNumber == null || documentNumber.isEmpty()) {
                continue;
            }

            // Creación de instancias
            DocumentModel documento = new DocumentModel(); // Documento que va a representar una factura/ccf/etc
            AgreementModel agreement = new AgreementModel(); // Acuerdo que representa la relación entre el pagador y el
                                                             // proveedor

            EntityModel supplierEntity = new EntityModel(); // Proveedor de la factura/ccf/etc
            EntityModel payerEntity = new EntityModel(); // Pagador de la factura/ccf/etc

            // Obtenemos el codigo del proveedor para validar si existe
            String supplierCode = getStringCellValue(row.getCell(7));

            documento.setDocumentNumber(getStringCellValue(row.getCell(2))); // Número de documento
            documento.setAmount(Double.parseDouble(getStringCellValue(row.getCell(1)))); // Monto del documento
            documento.setIssueDate(getStringCellValue(row.getCell(0))); // Fecha de emisión del documento
            documento.setStatus("UPLOADED"); // Estado del documento (asumimos que es true por defecto)
            documento.setStatusUpdateDate(getStringCellValue(row.getCell(0))); // Fecha de actualización del estado
                                                                               // (asumimos que es la misma fecha de
                                                                               // emisión)
            documento.setDisbursementDate(getStringCellValue(row.getCell(0))); // Fecha de desembolso (asumimos que es
                                                                               // la misma fecha de emisión)
            documento.setSupplierName(getStringCellValue(row.getCell(3))); // Nombre del proveedor

            // Con el id del pagador recibido, buscamos al pagador

            if (payerId == null) {
                throw new IllegalArgumentException("El ID del pagador recibido no puede ser nulo.");
            }

            if (entityService.getEntityById(payerId) == null) {
                throw new IllegalArgumentException("El pagador con ID " + payerId + " no existe.");
            } else {
                // Buscamos al pagador por ID y asignamos al acuerdo
                payerEntity = entityService.getEntityById(payerId);
                agreement.setPayer(payerId); // Asignar el pagador al acuerdo
            }

            // Validaciones para el proveedor
            if (supplierCode == null || supplierCode.isEmpty()) {
                throw new IllegalArgumentException("El código del proveedor no puede ser nulo o vacío.");
            }

            if (entityService.findByCode(supplierCode) == null) {

                supplierEntity.setName(getStringCellValue(row.getCell(3)));
                supplierEntity.setEmail(getStringCellValue(row.getCell(5)));
                supplierEntity.setCode(getStringCellValue(row.getCell(7)));
                supplierEntity.setNit(getStringCellValue(row.getCell(4)));
                supplierEntity.setAccountBank(getStringCellValue(row.getCell(6)));
                supplierEntity.setEntityType(false); // Asumimos que es un proveedor

                entityService.createEntity(supplierEntity); // Guardar el proveedor en la base de datos

                agreement.setSupplier(supplierEntity.getId()); // Asignar el proveedor al acuerdo

            } else {

                // Si el proveedor ya existe, lo buscamos por código y lo asignamos al acuerdo
                supplierEntity = entityService.findByCode(supplierCode);

                agreement.setSupplier(supplierEntity.getId());
                // documento.setSupplier(supplier);
            }

            // Verificar si el acuerdo ya existe
            String agreementIdentifier = payerId.toString().concat(supplierEntity.getId().toString());

            if (agreementService.findByIdentifier(agreementIdentifier) == null) {

                agreement.setIdentifier(agreementIdentifier);
                agreement.setName("Convenio entre " + payerEntity.getName() + " y " + supplierEntity.getName());
                agreement.setPayer(entityService.getEntityById(payerId).getId());
                agreement.setSupplier(supplierEntity.getId());

                operatorUser = userService.getUserById(userId);

                // Agregar el documento al convenio nuevo
                documento.setAgreement(agreement);
                documento.setUploadedBy(operatorUser);

                // guardar
                agreementService.save(agreement);

            } else {
                agreement = agreementService.findByIdentifier(agreementIdentifier);
                // Agregar el documento al convenio existente
                documento.setAgreement(agreement);

                // guardar
                agreementService.save(agreement);
            }

            documentService.createDocument(documento); // Guardar el documento en la base de datos

            filasProcesadas++;
        }

        workbook.close();
        String mensaje = "Archivo procesado. " + filasProcesadas + " registros guardados en la base de datos.";
        if (filasDuplicadas > 0) {
            mensaje += " " + filasDuplicadas + " registros duplicados encontrados y omitidos.";
        }

        return mensaje;
    }
}