package com.uade.progra3.gestor_logistico.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.progra3.gestor_logistico.service.EnvioService;

@RestController
@RequestMapping("/envios")
public class EnvioController {
    
    private final EnvioService envioService;
    
    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }
    
    /**
     * Encuentra la ruta con menos transbordos usando BFS
     * GET /envios/menos-transbordos?origen=CA001&destino=CA006
     */
    @GetMapping("/menos-transbordos")
    public Map<String, Object> menosTransbordos(
            @RequestParam String origen,
            @RequestParam String destino) {
        return envioService.encontrarRutaMenosTransbordos(origen, destino);
    }
    
    /**
     * Explora la red de distribución desde un centro usando DFS
     * GET /envios/explorar-red?origen=CA001&profundidad=3
     */
    @GetMapping("/explorar-red")
    public Map<String, Object> explorarRed(
            @RequestParam String origen,
            @RequestParam(defaultValue = "3") int profundidad) {
        return envioService.explorarRedDistribucion(origen, profundidad);
    }
    
    /**
     * Encuentra todas las rutas posibles con restricciones usando Backtracking
     * GET /envios/rutas-con-restricciones?origen=CA001&destino=CA006&metrica=costo&valorMaximo=3000&maxTransbordos=3
     */
    @GetMapping("/rutas-con-restricciones")
    public Map<String, Object> rutasConRestricciones(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam(defaultValue = "tiempoMin") String metrica,
            @RequestParam(required = false) Integer valorMaximo,
            @RequestParam(required = false) Integer maxTransbordos) {
        return envioService.encontrarRutasConRestricciones(
            origen, destino, metrica, valorMaximo, maxTransbordos);
    }
    
    /**
     * Encuentra la ruta óptima según una métrica usando Dijkstra
     * GET /envios/ruta-optima?origen=CA001&destino=CA006&metrica=costo
     */
    @GetMapping("/ruta-optima")
    public Map<String, Object> rutaOptima(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam(defaultValue = "tiempoMin") String metrica) {
        return envioService.encontrarRutaOptima(origen, destino, metrica);
    }
}
