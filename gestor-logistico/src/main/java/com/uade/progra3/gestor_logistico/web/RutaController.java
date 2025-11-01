package com.uade.progra3.gestor_logistico.web;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.progra3.gestor_logistico.repo.CentroRepository;
import com.uade.progra3.gestor_logistico.service.GraphServiceReactive;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rutas")
public class RutaController {

  private final CentroRepository repo;
  private final GraphServiceReactive gs;

  public RutaController(CentroRepository repo, GraphServiceReactive gs){
    this.repo = repo; this.gs = gs;
  }

  // Crear/actualizar una ruta dirigida A -> B
  @PostMapping
  public Map<String,Object> crear(@RequestBody Map<String,Object> body){
    String from = (String) body.get("from");
    String to   = (String) body.get("to");
    int tiempo  = (int) body.getOrDefault("tiempoMin", 0);
    int costo   = (int) body.getOrDefault("costo", 0);
    int distKm  = (int) body.getOrDefault("distKm", 0);

    Integer ok = repo.crearRuta(from, to, tiempo, costo, distKm);
    return Map.<String,Object>of("ok", ok, "from", from, "to", to);
  }

  // Crear rutas A<->B (bidireccional) con los mismos pesos
  @PostMapping("/bidir")
  public Map<String,Object> crearBidir(@RequestBody Map<String,Object> body){
    String a = (String) body.get("a");
    String b = (String) body.get("b");
    int tiempo  = (int) body.getOrDefault("tiempoMin", 0);
    int costo   = (int) body.getOrDefault("costo", 0);
    int distKm  = (int) body.getOrDefault("distKm", 0);

    repo.crearRuta(a, b, tiempo, costo, distKm);
    repo.crearRuta(b, a, tiempo, costo, distKm);
    return Map.<String,Object>of("ok", 1, "a", a, "b", b, "bidireccional", true);
  }

  // Borrar una ruta dirigida A -> B
  @DeleteMapping
  public Map<String,Object> borrar(@RequestParam String from, @RequestParam String to){
    Integer ok = repo.borrarRuta(from, to);
    return Map.<String,Object>of("ok", ok, "from", from, "to", to, "deleted", true);
  }

  // ------ Simular Envío (camino óptimo) ------
  // metric: tiempoMin | costo | distKm
  @GetMapping("/simular")
  public Mono<Map<String,Object>> simular(@RequestParam String origen,
                                          @RequestParam String destino,
                                          @RequestParam(defaultValue = "tiempoMin") String metric,
                                          @RequestParam(required = false) Integer peso){
    return gs.simularEnvio(origen, destino, metric, peso);
  }
}

