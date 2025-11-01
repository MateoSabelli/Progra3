package com.uade.progra3.gestor_logistico.web;
import com.uade.progra3.gestor_logistico.service.GraphService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/grafos")
public class GrafosController {
    private final GraphService gs;
    public GrafosController(GraphService gs){ this.gs = gs; }

    @GetMapping("/bfs")
    public Map<String,Object> bfs(@RequestParam String origen){
        return Map.<String,Object>of("recorrido", gs.bfs(origen));
    }

    @GetMapping("/dfs")
    public Map<String,Object> dfs(@RequestParam String origen){
        return Map.<String,Object>of("recorrido", gs.dfs(origen));
    }

    @GetMapping("/dijkstra")
    public Map<String,Object> dijkstra(@RequestParam String origen,
                                       @RequestParam(defaultValue = "tiempoMin") String metrica){
        return gs.dijkstra(origen, metrica);
    }

    @GetMapping("/mst/prim")
    public Map<String,Object> prim(@RequestParam(defaultValue = "distKm") String metrica){
        return gs.prim(metrica);
    }

    @GetMapping("/mst/kruskal")
    public Map<String,Object> kruskal(@RequestParam(defaultValue = "distKm") String metrica){
        return gs.kruskal(metrica);
    }

    @GetMapping("/backtracking/rutas")
    public Map<String,Object> rutas(@RequestParam String origen,
                                    @RequestParam String destino,
                                    @RequestParam(defaultValue = "4") int maxSaltos,
                                    @RequestParam(defaultValue = "tiempoMin") String metrica){
        return gs.rutasBacktracking(origen, destino, maxSaltos, metrica);
    }

    @PostMapping("/bnb/tsp")
    public Map<String,Object> tsp(@RequestBody Map<String,Object> body){
        @SuppressWarnings("unchecked")
        List<String> centros = (List<String>) body.get("centros");
        String metrica = (String) body.getOrDefault("metrica", "distKm");
        return gs.tspBnB(centros, metrica);
    }
}
