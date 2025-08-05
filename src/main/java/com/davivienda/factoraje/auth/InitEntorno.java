package com.davivienda.factoraje.auth;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.ComponentScan;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;

// import com.davivienda.factoraje.database.FabricaEntornoCoreSpring;
// import com.davivienda.factoraje.utils.ProjectProperties;
// import com.hsbc.sv.desarrollo.J2Entorno;
// import com.hsbc.sv.desarrollo.contenedores.Peticion;
// import com.hsbc.sv.desarrollo.contenedores.Respuesta;
// import com.hsbc.sv.desarrollo.interconexion.FabricaServicios;


// @Configuration
// @ComponentScan("sv.com.davivienda.auth")
// public class InitEntorno {

//     private static final Logger logger = LoggerFactory.getLogger(InitEntorno.class);	
    
//     @Autowired
//     private ProjectProperties projectProperties;
    
//     @Autowired
//     private FabricaEntornoCoreSpring fabricaEntornoCoreSpring;

//     @Primary
//     @Bean(name = "arrancarJ2EntornoCliente")
//     public String arrancarJ2EntornoCliente(){
//         logger.info("Inicio--------------");
//         J2Entorno.e().setIniciado(false);
//         J2Entorno.e().fabricas().clear();
        
//         try {
//             logger.info("------- Establenciendo conexion --------");
//             fabricaEntornoCoreSpring.conexionDefinida();
//             fabricaEntornoCoreSpring.arrancarJ2EntornoCliente(projectProperties.getCodigoAppEntorno());
//             logger.info("Codigo App Entorno: " + projectProperties.getCodigoAppEntorno());
//             logger.info("TotalFabricas:::" + J2Entorno.e().fabricas().size());
//             logger.info("Fin-----------------");
//         } catch (Exception e) {
//             logger.error("# Error tratando de arrancar el cliente de J2Entorno: " + e.getMessage(), e);
//         }
        
//         return "exito";
//     }

		
//     public String obtenerXMLDeFabrica(String nombreFabrica, String nombreServicio, String definicionXML){
//         Peticion p = new Peticion();
//         p.setDatosXML(definicionXML);

//         FabricaServicios fabrica = null;		    	
//         Respuesta r = null;
    	
//         fabrica = J2Entorno.e().obtenerFabrica(nombreFabrica);   	
//         r = fabrica.obtenerDatos(p, null, nombreServicio, null);
//         r.primerContenedor();
//         return r.getDatosXML();
//     }
	
// }

