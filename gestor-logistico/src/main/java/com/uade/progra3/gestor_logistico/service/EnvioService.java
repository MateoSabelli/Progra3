package com.uade.progra3.gestor_logistico.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uade.progra3.gestor_logistico.domain.Centro;
import com.uade.progra3.gestor_logistico.domain.Ruta;
import com.uade.progra3.gestor_logistico.repo.CentroRepository;

@Service
public class EnvioService {

    private final CentroRepository repo;

    public EnvioService(CentroRepository repo) {
        this.repo = repo;
    }

    // Construir grafo con información completa de rutas
    private Map<String, List<RutaInfo>> buildGraph(String metric) {
        List<Centro> centros = repo.findAllByOrderByIdAsc();
        Map<String, List<RutaInfo>> graph = new HashMap<>();

        for (Centro c : centros) {
            graph.putIfAbsent(c.getId(), new ArrayList<>());
            if (c.getRutas() != null) {
                for (Ruta r : c.getRutas()) {
                    int peso = switch (metric != null ? metric : "tiempoMin") {
                        case "costo" -> r.getCosto();
                        case "distKm" -> r.getDistKm();
                        default -> r.getTiempoMin();
                    };
                    graph.get(c.getId()).add(new RutaInfo(
                            r.getDestino().getId(),
                            r.getTiempoMin(),
                            r.getCosto(),
                            r.getDistKm(),
                            peso));
                }
            }
        }
        return graph;
    }

    public Map<String, Object> encontrarRutaMenosTransbordos(String origen, String destino) {
        var graph = buildGraph("tiempoMin");

        if (!graph.containsKey(origen) || !graph.containsKey(destino)) {
            return Map.of("error", "Centro origen o destino no encontrado");
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(origen);
        visited.add(origen);
        parent.put(origen, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(destino)) {
                // Reconstruir ruta
                List<String> path = new ArrayList<>();
                String node = destino;
                while (node != null) {
                    path.add(0, node);
                    node = parent.get(node);
                }

                // Calcular totales
                Map<String, Integer> totales = calcularTotalesRuta(path, graph);

                return Map.of(
                        "tipo", "BFS - Menos Transbordos",
                        "origen", origen,
                        "destino", destino,
                        "ruta", path,
                        "transbordos", path.size() - 1,
                        "totales", totales);
            }

            for (RutaInfo info : graph.getOrDefault(current, List.of())) {
                if (!visited.contains(info.destino)) {
                    visited.add(info.destino);
                    parent.put(info.destino, current);
                    queue.add(info.destino);
                }
            }
        }

        return Map.of("error", "No existe ruta entre " + origen + " y " + destino);
    }

    public Map<String, Object> explorarRedDistribucion(String origen, int profundidad) {
        var graph = buildGraph("tiempoMin");

        if (!graph.containsKey(origen)) {
            return Map.of("error", "Centro origen no encontrado");
        }

        List<String> recorrido = new ArrayList<>();
        Set<String> visitados = new HashSet<>();
        dfsExplorar(origen, graph, visitados, recorrido, profundidad, 0);

        return Map.of(
                "tipo", "DFS - Exploración de Red",
                "origen", origen,
                "profundidadMaxima", profundidad,
                "centrosAlcanzables", recorrido,
                "totalCentros", recorrido.size());
    }

    private void dfsExplorar(String nodo, Map<String, List<RutaInfo>> graph,
            Set<String> visitados, List<String> recorrido,
            int profundidadMax, int profundidadActual) {
        if (profundidadActual > profundidadMax)
            return;

        visitados.add(nodo);
        recorrido.add(nodo);

        for (RutaInfo info : graph.getOrDefault(nodo, List.of())) {
            if (!visitados.contains(info.destino)) {
                dfsExplorar(info.destino, graph, visitados, recorrido, profundidadMax, profundidadActual + 1);
            }
        }
    }

    public Map<String, Object> encontrarRutasConRestricciones(
            String origen,
            String destino,
            String metrica,
            Integer valorMaximo,
            Integer maxTransbordos) {

        var graph = buildGraph(metrica);

        if (!graph.containsKey(origen) || !graph.containsKey(destino)) {
            return Map.of("error", "Centro origen o destino no encontrado");
        }

        List<RutaDetallada> rutasEncontradas = new ArrayList<>();
        List<String> rutaActual = new ArrayList<>();
        Set<String> visitados = new HashSet<>();

        backtrackingRutas(origen, destino, graph, visitados, rutaActual,
                0, valorMaximo, maxTransbordos, rutasEncontradas);

        // Ordenar por valor de métrica
        rutasEncontradas.sort(Comparator.comparingInt(r -> r.valorTotal));

        return Map.of(
                "tipo", "Backtracking - Rutas con Restricciones",
                "origen", origen,
                "destino", destino,
                "metrica", metrica,
                "restriccionValor", valorMaximo != null ? valorMaximo : "sin límite",
                "restriccionTransbordos", maxTransbordos != null ? maxTransbordos : "sin límite",
                "rutasEncontradas", rutasEncontradas.size(),
                "rutas", rutasEncontradas.stream().limit(10).collect(Collectors.toList()) // Top 10
        );
    }

    private void backtrackingRutas(String nodoActual, String destino,
            Map<String, List<RutaInfo>> graph,
            Set<String> visitados, List<String> rutaActual,
            int valorAcumulado, Integer valorMaximo,
            Integer maxTransbordos, List<RutaDetallada> resultado) {

        // Restricción de transbordos
        if (maxTransbordos != null && rutaActual.size() > maxTransbordos + 1) {
            return;
        }

        // Restricción de valor (costo, tiempo o distancia)
        if (valorMaximo != null && valorAcumulado > valorMaximo) {
            return;
        }

        visitados.add(nodoActual);
        rutaActual.add(nodoActual);

        // ¿Llegamos al destino?
        if (nodoActual.equals(destino)) {
            // Calcular detalles completos
            Map<String, Integer> totales = calcularTotalesRuta(new ArrayList<>(rutaActual), graph);
            resultado.add(new RutaDetallada(
                    new ArrayList<>(rutaActual),
                    totales.get("tiempoTotal"),
                    totales.get("costoTotal"),
                    totales.get("distanciaTotal"),
                    valorAcumulado));
        } else {
            // Explorar vecinos
            for (RutaInfo info : graph.getOrDefault(nodoActual, List.of())) {
                if (!visitados.contains(info.destino)) {
                    backtrackingRutas(info.destino, destino, graph, visitados, rutaActual,
                            valorAcumulado + info.pesoMetrica, valorMaximo,
                            maxTransbordos, resultado);
                }
            }
        }

        // Backtrack
        rutaActual.remove(rutaActual.size() - 1);
        visitados.remove(nodoActual);
    }

    public Map<String, Object> encontrarRutaOptima(String origen, String destino, String metrica) {
        var graph = buildGraph(metrica);

        if (!graph.containsKey(origen) || !graph.containsKey(destino)) {
            return Map.of("error", "Centro origen o destino no encontrado");
        }

        Map<String, Integer> distancias = new HashMap<>();
        Map<String, String> predecesores = new HashMap<>();
        PriorityQueue<Nodo> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distancia));
        Set<String> visitados = new HashSet<>();

        // Inicializar
        for (String centro : graph.keySet()) {
            distancias.put(centro, Integer.MAX_VALUE / 2);
        }
        distancias.put(origen, 0);
        pq.add(new Nodo(origen, 0));

        while (!pq.isEmpty()) {
            Nodo actual = pq.poll();

            if (visitados.contains(actual.id))
                continue;
            visitados.add(actual.id);

            if (actual.id.equals(destino))
                break;

            for (RutaInfo info : graph.getOrDefault(actual.id, List.of())) {
                int nuevaDist = distancias.get(actual.id) + info.pesoMetrica;
                if (nuevaDist < distancias.get(info.destino)) {
                    distancias.put(info.destino, nuevaDist);
                    predecesores.put(info.destino, actual.id);
                    pq.add(new Nodo(info.destino, nuevaDist));
                }
            }
        }

        List<String> ruta = new ArrayList<>();
        String nodo = destino;
        while (nodo != null) {
            ruta.add(0, nodo);
            nodo = predecesores.get(nodo);
        }

        if (ruta.isEmpty() || !ruta.get(0).equals(origen)) {
            return Map.of("error", "No existe ruta entre " + origen + " y " + destino);
        }

        Map<String, Integer> totales = calcularTotalesRuta(ruta, graph);

        return Map.of(
                "tipo", "Dijkstra - Ruta Óptima",
                "origen", origen,
                "destino", destino,
                "metrica", metrica,
                "valorOptimo", distancias.get(destino),
                "ruta", ruta,
                "transbordos", ruta.size() - 1,
                "totales", totales);
    }

    // Calcular totales de una ruta
    private Map<String, Integer> calcularTotalesRuta(List<String> path, Map<String, List<RutaInfo>> graph) {
        int tiempoTotal = 0, costoTotal = 0, distanciaTotal = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);

            Optional<RutaInfo> ruta = graph.getOrDefault(from, List.of())
                    .stream()
                    .filter(r -> r.destino.equals(to))
                    .findFirst();

            if (ruta.isPresent()) {
                tiempoTotal += ruta.get().tiempoMin;
                costoTotal += ruta.get().costo;
                distanciaTotal += ruta.get().distKm;
            }
        }

        return Map.of(
                "tiempoTotal", tiempoTotal,
                "costoTotal", costoTotal,
                "distanciaTotal", distanciaTotal);
    }

    // Clases auxiliares
    private record RutaInfo(String destino, int tiempoMin, int costo, int distKm, int pesoMetrica) {
    }

    private record Nodo(String id, int distancia) {
    }

    private record RutaDetallada(
            List<String> centros,
            int tiempoTotal,
            int costoTotal,
            int distanciaTotal,
            int valorTotal) {
    }
}
