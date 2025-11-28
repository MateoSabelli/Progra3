package com.uade.progra3.gestor_logistico.service; // paquete de la clase

import java.util.ArrayList; // lista mutable
import java.util.Collections; // utilidades de colección (p.ej. reverse)
import java.util.Comparator; // comparadores
import java.util.HashMap; // mapa mutable
import java.util.LinkedHashMap; // mapa con orden de inserción
import java.util.List; // interfaz List
import java.util.Map; // interfaz Map
import java.util.PriorityQueue; // cola de prioridad (aunque no se usa explícita en este archivo)

import org.springframework.stereotype.Service; // anotación de servicio Spring

import com.uade.progra3.gestor_logistico.domain.Centro; // entidad Centro
import com.uade.progra3.gestor_logistico.domain.Ruta; // entidad Ruta
import com.uade.progra3.gestor_logistico.repo.CentroRepository; // repositorio de centros

import reactor.core.publisher.Mono; // Mono de Reactor para respuestas reactivas

@Service // marca la clase como componente de Spring
public class GraphServiceReactive { // inicio de la clase servicio reactivo

  private final CentroRepository repo; // repositorio inyectado para acceder a centros
  public GraphServiceReactive(CentroRepository repo){ this.repo = repo; } // constructor con inyección por constructor

  private Map<String, Map<String,Integer>> buildAdj(List<Centro> centros, String metric){ // construye adyacencia desde lista de centros y métrica
    Map<String, Map<String,Integer>> adj = new HashMap<>(); // mapa nodo -> (vecino -> peso)
    for (Centro c : centros){ // por cada centro recibido
      adj.putIfAbsent(c.getId(), new HashMap<>()); // asegura entrada para el centro
      if (c.getRutas()!=null){ // si el centro tiene rutas salientes
        for (Ruta r : c.getRutas()){ // por cada ruta saliente
          String safeMetric = metric != null ? metric : "tiempoMin"; // métrica segura por defecto
          int w = switch (safeMetric) { // selecciona peso según métrica
            case "costo" -> r.getCosto(); // si métrica es costo
            case "distKm" -> r.getDistKm(); // si métrica es distancia
            default -> r.getTiempoMin(); // por defecto tiempo en minutos
          };
          adj.get(c.getId()).put(r.getDestino().getId(), w); // registra arista con peso w
        }
      }
    }
    return adj; // devuelve la estructura de adyacencia
  }

  // ---------- Dijkstra con reconstrucción de ruta ----------
  public Mono<Map<String,Object>> dijkstraPath(String origen, String destino, String metric){ // método reactivo que devuelve Mono con resultado
    return Mono.fromCallable(() -> { // envuelve la ejecución síncrona en un Mono
      List<Centro> list = repo.findAllByOrderByIdAsc(); // obtiene todos los centros (consulta síncrona al repo)
      var adj = buildAdj(list, metric); // construye adyacencia usando la lista y métrica
      if (!adj.containsKey(origen) || !adj.containsKey(destino)) { // valida existencia de origen y destino
        return Map.<String,Object>of("error", "origen/destino inexistente", "origen", origen, "destino", destino); // retorna map de error
      }

      // distancias y predecesores
      Map<String,Integer> dist = new HashMap<>(); // mapa de distancias mínimas conocidas
      Map<String,String> prev = new HashMap<>(); // predecesores para reconstruir camino
      for (String v : adj.keySet()) dist.put(v, Integer.MAX_VALUE/4); // inicializa distancias a "infinito" (para evitar overflow)
      dist.put(origen, 0); // distancia al origen = 0

      PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get)); // pq ordenada por dist conocida
      pq.addAll(adj.keySet()); // inicialmente contiene todos los nodos

      while(!pq.isEmpty()){ // bucle principal de Dijkstra
        String u = pq.poll(); // extrae nodo con menor distancia conocida
        if (dist.get(u) == Integer.MAX_VALUE/4) break;      // si el nodo es inalcanzable, rompe (resto inalcanzables)
        if (u.equals(destino)) break;                        // si ya es el destino, puede terminar antes
        for (var e : adj.getOrDefault(u, Map.of()).entrySet()){ // itera aristas salientes de u
          String v = e.getKey(); int w = e.getValue(); // vecino y peso de arista
          if (dist.get(u) + w < dist.get(v)){ // si encontró mejor camino a v
            dist.put(v, dist.get(u) + w); // actualiza distancia
            prev.put(v, u); // guarda predecesor
            pq.remove(v); pq.add(v); // reordena pq actualizando prioridad de v
          }
        }
      }

      // reconstrucción de camino
      List<String> path = new ArrayList<>(); // lista para el camino final
      if (dist.get(destino) < Integer.MAX_VALUE/4){ // si destino es alcanzable
        String cur = destino; // empieza desde destino
        while (cur != null){ // sigue predecesores hasta null
          path.add(cur); // añade nodo actual
          cur = prev.get(cur); // avanza al predecesor
        }
        Collections.reverse(path); // invierte para obtener orden origen->destino
      }

      return Map.<String,Object>of( // construye mapa de salida con datos relevantes
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
    return dijkstraPath(origen, destino, metric).map(res -> { // reutiliza dijkstraPath y transforma el resultado
      if (res.containsKey("error")) return res; // propaga error si ocurrió
      @SuppressWarnings("unchecked")
      List<String> camino = (List<String>) res.get("camino"); // extrae camino (cast seguro si no hay error)
      int total = (int) res.get("distanciaTotal"); // distancia/costo/tiempo total según métrica

      Map<String,Object> out = new LinkedHashMap<>(); // mapa de salida con orden de inserción
      out.put("origen", origen);
      out.put("destino", destino);
      out.put("metrica", metric);
      out.put("camino", camino);
      out.put("tramos", Math.max(0, camino.size()-1)); // número de tramos (hops)
      out.put("total", total); // valor total acumulado

      // si te interesa usar el peso para un costo final estimado, ejemplo simple:
      if (peso != null && metric.equals("costo")) { // si se pidió costo y se suministró peso
        out.put("costoEstimado", total * Math.max(1, peso)); // cálculo simplificado de costo estimado
      }
      return out; // retorna mapa con simulación
    });
  }
}
