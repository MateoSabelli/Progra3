package com.uade.progra3.gestor_logistico.web; // paquete: organización del código en el proyecto

import java.util.List; // import del servicio que contiene la lógica de grafos
import java.util.Map; // import de anotaciones Spring MVC (RestController, RequestMapping, GetMapping, PostMapping, RequestParam, RequestBody)

import org.springframework.web.bind.annotation.GetMapping; // tipo List usado en el cuerpo de peticiones
import org.springframework.web.bind.annotation.PostMapping;  // tipo Map usado para respuestas y cuerpo JSON
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.progra3.gestor_logistico.service.GraphService;


@RestController // indica que esta clase responde solicitudes REST y que los métodos retornan el cuerpo directamente (no vistas)
@RequestMapping("/grafos") // prefijo común de ruta para todos los endpoints de este controlador
public class GrafosController { // definición del controlador

    private final GraphService gs; // dependencia: el servicio que ejecuta los algoritmos de grafos

    public GrafosController(GraphService gs){ this.gs = gs; } // constructor: Spring inyecta GraphService (inyección por constructor)

    @GetMapping("/bfs") // endpoint GET /grafos/bfs
    public Map<String,Object> bfs(@RequestParam String origen){ // parámetro query 'origen' obligatorio
        return Map.<String,Object>of("recorrido", gs.bfs(origen)); // llama al servicio bfs y devuelve un Map con la clave "recorrido"
    }

    @GetMapping("/dfs") // endpoint GET /grafos/dfs
    public Map<String,Object> dfs(@RequestParam String origen){ // parámetro query 'origen'
        return Map.<String,Object>of("recorrido", gs.dfs(origen)); // llama a gs.dfs y empaqueta resultado
    }

    @GetMapping("/dijkstra") // endpoint GET /grafos/dijkstra
    public Map<String,Object> dijkstra(@RequestParam String origen,
                                       @RequestParam(defaultValue = "tiempoMin") String metrica){ 
        // 'origen' obligatorio; 'metrica' opcional con valor por defecto "tiempoMin"
        return gs.dijkstra(origen, metrica); // delega a servicio: devuelve mapa con distancias/predecesores
    }

    @GetMapping("/mst/prim") // endpoint GET /grafos/mst/prim
    public Map<String,Object> prim(@RequestParam(defaultValue = "distKm") String metrica){
        // métrica por defecto "distKm" (se usa para calcular pesos y generar el MST)
        return gs.prim(metrica); // delega algoritmo de Prim al servicio
    }

    @GetMapping("/mst/kruskal") // endpoint GET /grafos/mst/kruskal
    public Map<String,Object> kruskal(@RequestParam(defaultValue = "distKm") String metrica){
        // métrica por defecto "distKm"
        return gs.kruskal(metrica); // delega algoritmo de Kruskal al servicio
    }

    @GetMapping("/backtracking/rutas") // endpoint GET /grafos/backtracking/rutas
    public Map<String,Object> rutas(@RequestParam String origen,
                                    @RequestParam String destino,
                                    @RequestParam(defaultValue = "4") int maxSaltos,
                                    @RequestParam(defaultValue = "tiempoMin") String metrica){
        // parámetros:
        // - origen: id del centro origen
        // - destino: id del centro destino
        // - maxSaltos: cota máxima de saltos (aristas) por defecto 4
        // - metrica: métrica utilizada para pesos (por defecto tiempoMin)
        return gs.rutasBacktracking(origen, destino, maxSaltos, metrica); // delega búsqueda por backtracking
    }

    @PostMapping("/bnb/tsp") // endpoint POST /grafos/bnb/tsp (recibe JSON en el body)
    public Map<String,Object> tsp(@RequestBody Map<String,Object> body){
        // espera un JSON con al menos "centros": [id1, id2, ...] y opcional "metrica"
        @SuppressWarnings("unchecked")
        List<String> centros = (List<String>) body.get("centros"); 
        // convierte el valor del body a List<String> (cast); @SuppressWarnings evita el warning por el cast no verificado
        String metrica = (String) body.getOrDefault("metrica", "distKm"); // obtiene métrica o usa "distKm" por defecto
        return gs.tspBnB(centros, metrica); // delega el TSP Branch & Bound al servicio
    }
}
