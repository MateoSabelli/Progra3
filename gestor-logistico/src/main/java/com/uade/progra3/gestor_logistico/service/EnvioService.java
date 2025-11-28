package com.uade.progra3.gestor_logistico.service; // declara el paquete donde vive esta clase

import java.util.ArrayList; // implementación de List de uso dinámico
import java.util.Comparator; // interfaz para comparar objetos (usada en sort)
import java.util.HashMap; // implementación de Map mutable
import java.util.HashSet; // implementación de Set mutable
import java.util.LinkedList; // implementación de List que también sirve como Queue
import java.util.List; // interfaz List
import java.util.Map; // interfaz Map
import java.util.Optional; // contenedor que puede o no contener un valor
import java.util.PriorityQueue; // cola de prioridad (Dijkstra)
import java.util.Queue; // interfaz Queue
import java.util.Set; // interfaz Set
import java.util.stream.Collectors; // utilidades para streams (p. ej. limitar, collect)

import org.springframework.stereotype.Service; // anotación para marcar componente Spring

import com.uade.progra3.gestor_logistico.domain.Centro; // entidad dominio Centro
import com.uade.progra3.gestor_logistico.domain.Ruta; // entidad dominio Ruta
import com.uade.progra3.gestor_logistico.repo.CentroRepository; // repositorio para acceder a centros

@Service // le indica a Spring que es un servicio gestionado (bean)
public class EnvioService { // inicio de la clase servicio

    private final CentroRepository repo; // campo final para el repositorio (inyección por constructor)

    public EnvioService(CentroRepository repo) { // constructor: Spring inyecta el repo
        this.repo = repo; // asigna el repo al campo de la clase
    }

    // Construir grafo con información completa de rutas
    private Map<String, List<RutaInfo>> buildGraph(String metric) { // método privado que construye un grafo en memoria
        List<Centro> centros = repo.findAllByOrderByIdAsc(); // obtiene todos los centros desde la BD ordenados por id
        Map<String, List<RutaInfo>> graph = new HashMap<>(); // inicializa el mapa: idCentro -> lista de aristas

        for (Centro c : centros) { // itera cada centro recuperado
            graph.putIfAbsent(c.getId(), new ArrayList<>()); // asegura que exista una lista para este id
            if (c.getRutas() != null) { // si el centro tiene rutas salientes
                for (Ruta r : c.getRutas()) { // itera cada ruta desde el centro
                    int peso = switch (metric != null ? metric : "tiempoMin") { // selecciona el peso según la métrica pedida
                        case "costo" -> r.getCosto(); // si la métrica es costo
                        case "distKm" -> r.getDistKm(); // si la métrica es distancia
                        default -> r.getTiempoMin(); // por defecto usa tiempo en minutos
                    };
                    graph.get(c.getId()).add(new RutaInfo( // construye y añade un objeto RutaInfo que representa la arista
                            r.getDestino().getId(), // id del centro destino
                            r.getTiempoMin(), // tiempo en minutos del tramo
                            r.getCosto(), // costo del tramo
                            r.getDistKm(), // distancia en km del tramo
                            peso)); // peso calculado según la métrica
                }
            }
        }
        return graph; // devuelve el grafo construido (lista de adyacencia con información extra)
    }

    public Map<String, Object> encontrarRutaMenosTransbordos(String origen, String destino) { // BFS para minimizar transbordos
        var graph = buildGraph("tiempoMin"); // reconstruye grafo usando tiempo como métrica auxiliar

        if (!graph.containsKey(origen) || !graph.containsKey(destino)) { // valida que ambos centros existan en el grafo
            return Map.of("error", "Centro origen o destino no encontrado"); // devuelve error en forma de Map si falta alguno
        }

        Queue<String> queue = new LinkedList<>(); // cola para BFS
        Map<String, String> parent = new HashMap<>(); // mapa padre para reconstruir la ruta final
        Set<String> visited = new HashSet<>(); // conjunto de visitados para evitar ciclos

        queue.add(origen); // encola el origen
        visited.add(origen); // marca origen como visitado
        parent.put(origen, null); // origen no tiene padre (null)

        while (!queue.isEmpty()) { // bucle estándar de BFS hasta agotar la cola
            String current = queue.poll(); // extrae el siguiente nodo de la cola

            if (current.equals(destino)) { // si el nodo actual es el destino
                // Reconstruir ruta desde destino hacia origen usando parent
                List<String> path = new ArrayList<>(); // lista para contener la ruta reconstruida
                String node = destino; // empezamos en destino
                while (node != null) { // subimos por la cadena de padres hasta llegar a null
                    path.add(0, node); // insertamos al inicio para obtener orden origen->destino
                    node = parent.get(node); // avanzamos al padre
                }

                // Calcular totales (tiempo/costo/distancia) para la ruta encontrada
                Map<String, Integer> totales = calcularTotalesRuta(path, graph);

                return Map.of( // retorna un Map con la información de la ruta hallada
                        "tipo", "BFS - Menos Transbordos",
                        "origen", origen,
                        "destino", destino,
                        "ruta", path,
                        "transbordos", path.size() - 1, // número de aristas = nodos - 1
                        "totales", totales);
            }

            // expandir vecinos del nodo actual
            for (RutaInfo info : graph.getOrDefault(current, List.of())) { // obtiene lista de rutas salientes o lista vacía
                if (!visited.contains(info.destino)) { // si el destino no fue visitado
                    visited.add(info.destino); // lo marca como visitado
                    parent.put(info.destino, current); // registra el padre para reconstrucción
                    queue.add(info.destino); // encola el vecino para procesarlo más tarde
                }
            }
        }

        return Map.of("error", "No existe ruta entre " + origen + " y " + destino); // si la cola queda vacía sin alcanzar destino
    }

    public Map<String, Object> explorarRedDistribucion(String origen, int profundidad) { // DFS limitado por profundidad
        var graph = buildGraph("tiempoMin"); // construye grafo (usa tiempo como métrica)

        if (!graph.containsKey(origen)) { // valida que el origen exista en el grafo
            return Map.of("error", "Centro origen no encontrado"); // devuelve error si no existe
        }

        List<String> recorrido = new ArrayList<>(); // lista donde se irá guardando el orden de visita (preorder)
        Set<String> visitados = new HashSet<>(); // conjunto global de nodos visitados para evitar re-procesar
        dfsExplorar(origen, graph, visitados, recorrido, profundidad, 0); // llamada inicial a la recursión (profundidadActual = 0)

        return Map.of( // construye y retorna el resumen de la exploración
                "tipo", "DFS - Exploración de Red",
                "origen", origen,
                "profundidadMaxima", profundidad,
                "centrosAlcanzables", recorrido,
                "totalCentros", recorrido.size());
    }

    private void dfsExplorar(String nodo, Map<String, List<RutaInfo>> graph,
            Set<String> visitados, List<String> recorrido,
            int profundidadMax, int profundidadActual) { // método recursivo DFS con tope de profundidad
        if (profundidadActual > profundidadMax) // poda por profundidad: si ya pasamos el tope
            return; // no procesar ni descender más

        visitados.add(nodo); // marca el nodo actual como visitado (globalmente)
        recorrido.add(nodo); // "procesa" el nodo: aquí registramos el orden de visita

        for (RutaInfo info : graph.getOrDefault(nodo, List.of())) { // itera vecinos salientes
            if (!visitados.contains(info.destino)) { // si el vecino no fue visitado
                // llama recursivamente incrementando la profundidad actual
                dfsExplorar(info.destino, graph, visitados, recorrido, profundidadMax, profundidadActual + 1);
            }
        }
        // Nota: no se desmarca visitado ni se remueve de recorrido aquí porque esta función hace un recorrido único,
        // no enumera todas las rutas; visitados se mantiene para evitar reprocesar nodos.
    }

    public Map<String, Object> encontrarRutasConRestricciones(
            String origen,
            String destino,
            String metrica,
            Integer valorMaximo,
            Integer maxTransbordos) { // busca rutas aplicando restricciones (backtracking)
        var graph = buildGraph(metrica); // construye grafo usando la métrica solicitada

        if (!graph.containsKey(origen) || !graph.containsKey(destino)) { // valida nodos
            return Map.of("error", "Centro origen o destino no encontrado"); // error si alguno falta
        }

        List<RutaDetallada> rutasEncontradas = new ArrayList<>(); // lista para guardar rutas válidas encontradas
        List<String> rutaActual = new ArrayList<>(); // ruta en construcción (pila de nodos del camino actual)
        Set<String> visitados = new HashSet<>(); // conjunto local para evitar ciclos en la ruta actual

        backtrackingRutas(origen, destino, graph, visitados, rutaActual,
                0, valorMaximo, maxTransbordos, rutasEncontradas); // inicia la búsqueda recursiva

        // Ordenar por valor de la métrica acumulada (valorTotal)
        rutasEncontradas.sort(Comparator.comparingInt(r -> r.valorTotal)); // orden ascendente por valorTotal

        return Map.of( // construye resumen y devuelve (incluye top 10 rutas)
                "tipo", "Backtracking - Rutas con Restricciones",
                "origen", origen,
                "destino", destino,
                "metrica", metrica,
                "restriccionValor", valorMaximo != null ? valorMaximo : "sin límite",
                "restriccionTransbordos", maxTransbordos != null ? maxTransbordos : "sin límite",
                "rutasEncontradas", rutasEncontradas.size(),
                "rutas", rutasEncontradas.stream().limit(10).collect(Collectors.toList()) // devuelve máximo 10 rutas
        );
    }

    private void backtrackingRutas(String nodoActual, String destino,
            Map<String, List<RutaInfo>> graph,
            Set<String> visitados, List<String> rutaActual,
            int valorAcumulado, Integer valorMaximo,
            Integer maxTransbordos, List<RutaDetallada> resultado) { // método recursivo con backtracking

        // Restricción de transbordos: si la ruta actual (nodos) excede maxTransbordos+1 cortar
        if (maxTransbordos != null && rutaActual.size() > maxTransbordos + 1) {
            return; // poda por transbordos
        }

        // Restricción por valor acumulado (costo/tiempo/distancia) según la métrica
        if (valorMaximo != null && valorAcumulado > valorMaximo) {
            return; // poda por valor
        }

        visitados.add(nodoActual); // "colocar": marca nodo en la ruta actual (evita ciclos en esta rama)
        rutaActual.add(nodoActual); // añade nodo a la ruta en construcción

        // Caso base: si alcanzamos el destino, calculamos totales y guardamos la ruta
        if (nodoActual.equals(destino)) {
            Map<String, Integer> totales = calcularTotalesRuta(new ArrayList<>(rutaActual), graph); // calcula tiempo/costo/distancia
            resultado.add(new RutaDetallada( // crea y añade objeto RutaDetallada con los acumulados
                    new ArrayList<>(rutaActual), // copia de la ruta actual
                    totales.get("tiempoTotal"), // tiempo total
                    totales.get("costoTotal"), // costo total
                    totales.get("distanciaTotal"), // distancia total
                    valorAcumulado)); // valor acumulado según la métrica
        } else {
            // Explorar vecinos no visitados
            for (RutaInfo info : graph.getOrDefault(nodoActual, List.of())) { // por cada arista saliente
                if (!visitados.contains(info.destino)) { // si el vecino no está en la ruta actual
                    // recursión acumulando el peso de la métrica para el vecino
                    backtrackingRutas(info.destino, destino, graph, visitados, rutaActual,
                            valorAcumulado + info.pesoMetrica, valorMaximo,
                            maxTransbordos, resultado);
                }
            }
        }

        // Backtrack: deshacer la "colocación" del nodo actual para permitir explorar otras ramas
        rutaActual.remove(rutaActual.size() - 1); // elimina último elemento de la rutaActual
        visitados.remove(nodoActual); // desmarca el nodo como no visitado en esta rama
    }

    public Map<String, Object> encontrarRutaOptima(String origen, String destino, String metrica) { // Dijkstra: ruta óptima según métrica
        var graph = buildGraph(metrica); // construye grafo con la métrica pedida

        if (!graph.containsKey(origen) || !graph.containsKey(destino)) { // valida existencia de nodos
            return Map.of("error", "Centro origen o destino no encontrado"); // error si falta alguno
        }

        Map<String, Integer> distancias = new HashMap<>(); // mapa de distancias mínimas conocidas
        Map<String, String> predecesores = new HashMap<>(); // para reconstruir camino óptimo
        PriorityQueue<Nodo> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distancia)); // cola por distancia
        Set<String> visitados = new HashSet<>(); // nodos ya procesados por Dijkstra

        // Inicializar distancias con "infinito" excepto origen
        for (String centro : graph.keySet()) {
            distancias.put(centro, Integer.MAX_VALUE / 2); // usa división para evitar overflow al sumar
        }
        distancias.put(origen, 0); // distancia al origen = 0
        pq.add(new Nodo(origen, 0)); // agrega origen a la cola de prioridad

        while (!pq.isEmpty()) { // ciclo principal de Dijkstra
            Nodo actual = pq.poll(); // extrae nodo con menor distancia estimada

            if (visitados.contains(actual.id)) // si ya lo procesamos, lo ignoramos
                continue;
            visitados.add(actual.id); // marca como procesado

            if (actual.id.equals(destino)) // si extraemos el destino, podemos terminar antes
                break;

            for (RutaInfo info : graph.getOrDefault(actual.id, List.of())) { // para cada vecino
                int nuevaDist = distancias.get(actual.id) + info.pesoMetrica; // calcula distancia alternativa
                if (nuevaDist < distancias.get(info.destino)) { // si mejora la mejor conocida
                    distancias.put(info.destino, nuevaDist); // actualiza distancia
                    predecesores.put(info.destino, actual.id); // registra predecesor para reconstrucción
                    pq.add(new Nodo(info.destino, nuevaDist)); // encola vecino con nueva prioridad
                }
            }
        }

        // Reconstrucción de la ruta óptima desde destino hacia origen
        List<String> ruta = new ArrayList<>();
        String nodo = destino;
        while (nodo != null) { // subimos por predecesores hasta llegar a null (origen tiene null)
            ruta.add(0, nodo); // insertamos al inicio para tener orden origen->destino
            nodo = predecesores.get(nodo); // avanzamos al predecesor
        }

        // Si la ruta no comienza en origen, no hay camino válido
        if (ruta.isEmpty() || !ruta.get(0).equals(origen)) {
            return Map.of("error", "No existe ruta entre " + origen + " y " + destino);
        }

        Map<String, Integer> totales = calcularTotalesRuta(ruta, graph); // calcula tiempo/costo/distancia reales de la ruta

        return Map.of( // devuelve información completa de la ruta óptima
                "tipo", "Dijkstra - Ruta Óptima",
                "origen", origen,
                "destino", destino,
                "metrica", metrica,
                "valorOptimo", distancias.get(destino), // valor óptimo según la métrica
                "ruta", ruta,
                "transbordos", ruta.size() - 1, // cantidad de transbordos
                "totales", totales);
    }

    // Calcular totales de una ruta (suma de atributos de cada arista)
    private Map<String, Integer> calcularTotalesRuta(List<String> path, Map<String, List<RutaInfo>> graph) {
        int tiempoTotal = 0, costoTotal = 0, distanciaTotal = 0; // acumuladores inicializados

        for (int i = 0; i < path.size() - 1; i++) { // recorre pares consecutivos (from -> to)
            String from = path.get(i); // nodo origen del tramo
            String to = path.get(i + 1); // nodo destino del tramo

            Optional<RutaInfo> ruta = graph.getOrDefault(from, List.of()) // busca la arista que conecte from -> to
                    .stream()
                    .filter(r -> r.destino.equals(to))
                    .findFirst(); // toma la primera coincidencia si existe

            if (ruta.isPresent()) { // si encontró la arista, acumula sus atributos
                tiempoTotal += ruta.get().tiempoMin; // suma tiempo
                costoTotal += ruta.get().costo; // suma costo
                distanciaTotal += ruta.get().distKm; // suma distancia
            }
        }

        return Map.of( // retorna un Map con los totales calculados
                "tiempoTotal", tiempoTotal,
                "costoTotal", costoTotal,
                "distanciaTotal", distanciaTotal);
    }

    // Clases auxiliares (records) para almacenar datos de aristas y nodos
    private record RutaInfo(String destino, int tiempoMin, int costo, int distKm, int pesoMetrica) { // representa una arista
    }

    private record Nodo(String id, int distancia) { // elemento de la cola de prioridad (Dijkstra)
    }

    private record RutaDetallada(
            List<String> centros,
            int tiempoTotal,
            int costoTotal,
            int distanciaTotal,
            int valorTotal) { // almacena una ruta completa con sus totales y el valor de la métrica
    }
}
