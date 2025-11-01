package com.uade.progra3.gestor_logistico.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;

import com.uade.progra3.gestor_logistico.domain.Centro;
import com.uade.progra3.gestor_logistico.domain.Ruta;
import com.uade.progra3.gestor_logistico.repo.CentroRepository;

import reactor.core.publisher.Mono;

@Service
public class GraphServiceReactive {

  private final CentroRepository repo;
  public GraphServiceReactive(CentroRepository repo){ this.repo = repo; }

  private Map<String, Map<String,Integer>> buildAdj(List<Centro> centros, String metric){
    Map<String, Map<String,Integer>> adj = new HashMap<>();
    for (Centro c : centros){
      adj.putIfAbsent(c.getId(), new HashMap<>());
      if (c.getRutas()!=null){
        for (Ruta r : c.getRutas()){
          String safeMetric = metric != null ? metric : "tiempoMin";
          int w = switch (safeMetric) {
            case "costo" -> r.getCosto();
            case "distKm" -> r.getDistKm();
            default -> r.getTiempoMin(); // tiempoMin por defecto
          };
          adj.get(c.getId()).put(r.getDestino().getId(), w);
        }
      }
    }
    return adj;
  }

  // ---------- Dijkstra con reconstrucción de ruta ----------
  public Mono<Map<String,Object>> dijkstraPath(String origen, String destino, String metric){
    return Mono.fromCallable(() -> {
      List<Centro> list = repo.findAllByOrderByIdAsc();
      var adj = buildAdj(list, metric);
      if (!adj.containsKey(origen) || !adj.containsKey(destino)) {
        return Map.<String,Object>of("error", "origen/destino inexistente", "origen", origen, "destino", destino);
      }

      // distancias y predecesores
      Map<String,Integer> dist = new HashMap<>();
      Map<String,String> prev = new HashMap<>();
      for (String v : adj.keySet()) dist.put(v, Integer.MAX_VALUE/4);
      dist.put(origen, 0);

      PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
      pq.addAll(adj.keySet());

      while(!pq.isEmpty()){
        String u = pq.poll();
        if (dist.get(u) == Integer.MAX_VALUE/4) break;      // nodos inalcanzables
        if (u.equals(destino)) break;                        // ya llegamos
        for (var e : adj.getOrDefault(u, Map.of()).entrySet()){
          String v = e.getKey(); int w = e.getValue();
          if (dist.get(u) + w < dist.get(v)){
            dist.put(v, dist.get(u) + w);
            prev.put(v, u);
            pq.remove(v); pq.add(v);
          }
        }
      }

      // reconstrucción de camino
      List<String> path = new ArrayList<>();
      if (dist.get(destino) < Integer.MAX_VALUE/4){
        String cur = destino;
        while (cur != null){
          path.add(cur);
          cur = prev.get(cur);
        }
        Collections.reverse(path);
      }

      return Map.<String,Object>of(
        "origen", origen,
        "destino", destino,
        "metrica", metric,
        "distanciaTotal", dist.get(destino),
        "camino", path
      );
    });
  }

  // ---------- Simulación de envío ----------
  // Usa Dijkstra para obtener el camino y devuelve el costo/tiempo/distancia total + hops
  public Mono<Map<String,Object>> simularEnvio(String origen, String destino, String metric, Integer peso){
    return dijkstraPath(origen, destino, metric).map(res -> {
      if (res.containsKey("error")) return res;
      @SuppressWarnings("unchecked")
      List<String> camino = (List<String>) res.get("camino");
      int total = (int) res.get("distanciaTotal");

      Map<String,Object> out = new LinkedHashMap<>();
      out.put("origen", origen);
      out.put("destino", destino);
      out.put("metrica", metric);
      out.put("camino", camino);
      out.put("tramos", Math.max(0, camino.size()-1));
      out.put("total", total);

      // si te interesa usar el peso para un costo final estimado, ejemplo simple:
      if (peso != null && metric.equals("costo")) {
        out.put("costoEstimado", total * Math.max(1, peso)); // <--- ajustá a tu modelo real
      }
      return out;
    });
  }
}
