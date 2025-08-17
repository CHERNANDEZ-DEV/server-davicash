package com.davivienda.factoraje.database;
import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jndi.JndiTemplate;
import org.springframework.stereotype.Component;

import com.davivienda.factoraje.utils.ProjectProperties;
import com.hsbc.sv.j2entorno.core.FabricaEntornoCore;

@Component
public class FabricaEntornoCoreSpring extends FabricaEntornoCore {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(FabricaEntornoCoreSpring.class);

    @Autowired
    private ProjectProperties projectProperties;

    @Override
    public Connection conexionDefinida() {
        return obtenerSpringDSConexion();
    }

    private Connection obtenerSpringDSConexion() {
        try {
            // Validar que projectProperties no sea null
            if (projectProperties == null) {
                logger.error("ProjectProperties es null - la inyección no funcionó correctamente");
                return null;
            }
            
            String nombreJNDY = projectProperties.getNombreJNDY();
            if (nombreJNDY == null || nombreJNDY.isEmpty()) {
                logger.error("El valor nombre.jndy no está configurado en application.properties");
                return null;
            }
            
            JndiTemplate jndi = new JndiTemplate();
            logger.info("parametro de conexion jndi: " + nombreJNDY);
            DataSource ds = jndi.lookup(nombreJNDY, DataSource.class);
            logger.info("FabricaEntornoCoreSpring||obtenerSpringDSConexion Exito: " + nombreJNDY);
            return ds.getConnection();
        } catch (Exception e) {
            logger.error("FabricaEntornoCoreSpring||obtenerSpringDSConexion error en la configuracion del jndy: " + 
                (projectProperties != null ? projectProperties.getNombreJNDY() : "projectProperties es null"), e);
            return null;
        }
    }

}
