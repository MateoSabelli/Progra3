package com.uade.progra3.gestor_logistico.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.progra3.gestor_logistico.domain.Centro;
import com.uade.progra3.gestor_logistico.repo.CentroRepository;

@Service
public class DataInitService {

    private static final Logger log = LoggerFactory.getLogger(DataInitService.class);
    private final CentroRepository repo;

    public DataInitService(CentroRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public String initializeData() {
        try {
            // Limpiar datos existentes
            repo.deleteAll();
            
            log.info("Iniciando creación de centros...");

            // Crear centros logísticos SIN rutas primero
            Centro ca001 = new Centro("CA001", "Centro Norte", "Buenos Aires", 1000);
            Centro ca002 = new Centro("CA002", "Centro Sur", "Córdoba", 800);
            Centro ca003 = new Centro("CA003", "Centro Este", "Rosario", 600);
            Centro ca004 = new Centro("CA004", "Centro Oeste", "Mendoza", 500);
            Centro ca005 = new Centro("CA005", "Centro Litoral", "Santa Fe", 700);
            Centro ca006 = new Centro("CA006", "Centro Patagonia", "Neuquén", 400);
            
            // Guardar SOLO los nodos primero
            ca001 = repo.save(ca001);
            ca002 = repo.save(ca002);
            ca003 = repo.save(ca003);
            ca004 = repo.save(ca004);
            ca005 = repo.save(ca005);
            ca006 = repo.save(ca006);
            
            log.info("Centros creados. Esperando persistencia...");
            Thread.sleep(500);
            
            log.info("Creando rutas usando queries Cypher directas...");
            
            // Crear rutas usando el método del repositorio que usa Cypher
            // Desde CA001 (Buenos Aires) -> CA002, CA003, CA005
            repo.crearRuta("CA001", "CA002", 60, 1500, 700);
            repo.crearRuta("CA001", "CA003", 35, 800, 300);
            repo.crearRuta("CA001", "CA005", 70, 1200, 500);

            // Desde CA002 (Córdoba) -> CA001, CA003, CA004, CA005
            repo.crearRuta("CA002", "CA001", 60, 1500, 700);
            repo.crearRuta("CA002", "CA003", 50, 1000, 400);
            repo.crearRuta("CA002", "CA004", 90, 2000, 600);
            repo.crearRuta("CA002", "CA005", 45, 900, 350);

            // Desde CA003 (Rosario) -> CA001, CA002, CA005
            repo.crearRuta("CA003", "CA001", 35, 800, 300);
            repo.crearRuta("CA003", "CA002", 50, 1000, 400);
            repo.crearRuta("CA003", "CA005", 40, 700, 200);

            // Desde CA004 (Mendoza) -> CA002, CA006
            repo.crearRuta("CA004", "CA002", 90, 2000, 600);
            repo.crearRuta("CA004", "CA006", 120, 2500, 800);

            // Desde CA005 (Santa Fe) -> CA001, CA002, CA003
            repo.crearRuta("CA005", "CA001", 70, 1200, 500);
            repo.crearRuta("CA005", "CA002", 45, 900, 350);
            repo.crearRuta("CA005", "CA003", 40, 700, 200);

            // Desde CA006 (Neuquén) -> CA004
            repo.crearRuta("CA006", "CA004", 120, 2500, 800);

            log.info("Base de datos inicializada correctamente con 6 centros y 17 rutas");
            return "Base de datos inicializada con 6 centros y 17 rutas bidireccionales";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error al inicializar la base de datos", e);
            throw new RuntimeException("Error al inicializar la base de datos: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error al inicializar la base de datos", e);
            throw new RuntimeException("Error al inicializar la base de datos: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String clearData() {
        repo.deleteAll();
        return "Todos los datos han sido eliminados de la base de datos";
    }
}
