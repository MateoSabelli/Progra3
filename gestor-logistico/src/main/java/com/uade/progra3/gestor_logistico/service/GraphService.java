package com.uade.progra3.gestor_logistico.service; // declara el paquete

import java.util.ArrayDeque; // deque para cola eficiente
import java.util.ArrayList; // lista mutable
import java.util.Collection; // interfaz Collection
import java.util.Collections; // utilidades de Collections
import java.util.Comparator; // comparadores
import java.util.HashMap; // mapa mutable
import java.util.HashSet; // conjunto mutable
import java.util.LinkedHashMap; // mapa con orden de inserción
import java.util.List; // interfaz List
import java.util.Map; // interfaz Map
import java.util.PriorityQueue; // cola de prioridad
import java.util.Queue; // interfaz Queue
import java.util.Set; // interfaz Set
import java.util.stream.Collectors; // collectors para streams

import org.springframework.stereotype.Service; // anotación Spring

import com.uade.progra3.gestor_logistico.domain.Centro; // entidad Centro
import com.uade.progra3.gestor_logistico.domain.Ruta; // entidad Ruta
import com.uade.progra3.gestor_logistico.repo.CentroRepository; // repositorio de centros

@Service // marca como servicio gestionado por Spring
public class GraphService { // inicio de la clase servicio

    private final CentroRepository repo; // repositorio inyectado
    public GraphService(CentroRepository repo){ this.repo = repo; } // constructor con inyección

    public Map<String, Map<String, Integer>> buildAdj(String metric) { // construye lista de adyacencia con pesos
        List<Centro> centros = repo.findAllByOrderByIdAsc(); // obtiene centros de BD ordenados
        Map<String, Map<String, Integer>> adj = new HashMap<>(); // mapa nodo -> (vecino -> peso)
        String safeMetric = metric != null ? metric : "tiempoMin"; // métrica segura por defecto
        for (Centro c : centros) { // por cada centro
            adj.putIfAbsent(c.getId(), new HashMap<>()); // asegura mapa para el nodo
            if (c.getRutas() != null) { // si tiene rutas salientes
                for (Ruta r : c.getRutas()) { // por cada ruta
                    int peso = switch (safeMetric) { // elige peso según métrica
                        case "costo" -> r.getCosto();
                        case "distKm" -> r.getDistKm();
                        default -> r.getTiempoMin();
                    };
                    adj.get(c.getId()).put(r.getDestino().getId(), peso); // agrega arista con peso
                }
            }
        }
        return adj; // retorna adyacencia
    }

    // BFS
    public List<String> bfs(String origen){ // recorrido en anchura desde origen
        var adj = buildAdj("tiempoMin"); // construye adyacencia (tiempo)
        List<String> out = new ArrayList<>(); // resultado de orden visita
        if (!adj.containsKey(origen)) return out; // si no existe origen, retorna vacío
        Set<String> vis = new HashSet<>(); // visitados
        Queue<String> q = new ArrayDeque<>(); // cola FIFO
        q.add(origen); vis.add(origen); // inicializa
        while(!q.isEmpty()){ // mientras haya nodos por procesar
            String u = q.poll(); out.add(u); // extrae y añade a salida
            for (String v : adj.getOrDefault(u, Map.of()).keySet()){ // vecinos
                if (!vis.contains(v)){ vis.add(v); q.add(v); } // encola vecinos no visitados
            }
        }
        return out; // devuelve orden BFS
    }

    // DFS
    public List<String> dfs(String origen){ // recorrido en profundidad desde origen
        var adj = buildAdj("tiempoMin"); // adyacencia
        List<String> out = new ArrayList<>(); // resultado
        if (!adj.containsKey(origen)) return out; // origen inexistente -> vacío
        dfsRec(origen, adj, new HashSet<>(), out); // llamada recursiva
        return out; // devuelve orden DFS
    }
    private void dfsRec(String u, Map<String,Map<String,Integer>> adj, Set<String> vis, List<String> out){
        vis.add(u); out.add(u); // marca y registra
        for (String v : adj.getOrDefault(u, Map.of()).keySet()){ // por cada vecino
            if (!vis.contains(v)) dfsRec(v, adj, vis, out); // recursión si no visitado
        }
    }

    // Dijkstra
    public Map<String,Object> dijkstra(String origen, String metric){ // calcula distancias mínimas
        var adj = buildAdj(metric); // adyacencia con la métrica dada
        Map<String,Integer> dist = new HashMap<>(); // distancias calculadas
        Map<String,String> prev = new HashMap<>(); // predecesores para reconstruir camino
        for (String v : adj.keySet()) dist.put(v, Integer.MAX_VALUE/4); // inicializa a "infinito"
        if (!dist.containsKey(origen)) return Map.<String,Object>of("error","origen inexistente"); // valida origen
        dist.put(origen, 0); // distancia al origen = 0

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get)); // pq ordenada por dist conocida
        pq.addAll(adj.keySet()); // inserta todos los nodos

        while(!pq.isEmpty()){ // mientras haya nodos
            String u = pq.poll(); // extrae el de menor distancia conocida
            for (var e : adj.getOrDefault(u, Map.of()).entrySet()){ // recorre aristas salientes
                String v = e.getKey(); int w = e.getValue(); // vecino y peso
                if (dist.get(u) + w < dist.get(v)){ // si mejora la distancia
                    dist.put(v, dist.get(u) + w); // actualiza distancia
                    prev.put(v, u); // registra predecesor
                    pq.remove(v); pq.add(v); // reordena pq (actualiza prioridad)
                }
            }
        }
        Map<String,Object> resp = new LinkedHashMap<>(); // respuesta con orden estable
        resp.put("origen", origen); // origen
        resp.put("metrica", metric); // métrica usada
        resp.put("distancias", dist); // distancias mínimas
        resp.put("predecesores", prev); // predecesores
        return resp; // retorna resultado
    }

    // Convertir a no dirigido (para MST)
    private Map<String,List<Edge>> toUndirected(String metric){ // transforma grafo dirigido a no dirigido duplicando aristas
        var adj = buildAdj(metric); // adyacencia dirigida
        Map<String,List<Edge>> und = new HashMap<>(); // grafo no dirigido
        for (String u : adj.keySet()){
            und.putIfAbsent(u, new ArrayList<>()); // asegura lista para u
            for (var e : adj.get(u).entrySet()){
                String v = e.getKey(); int w = e.getValue(); // vecino y peso
                und.get(u).add(new Edge(u,v,w)); // agrega arista u->v
                und.putIfAbsent(v, new ArrayList<>()); // asegura lista para v
                und.get(v).add(new Edge(v,u,w)); // agrega arista v->u (duplicada)
            }
        }
        return und; // retorna grafo no dirigido
    }

    // Prim
    public Map<String,Object> prim(String metric){ // algoritmo de Prim para MST
        var g = toUndirected(metric); // grafo no dirigido
        if (g.isEmpty()) return Map.<String,Object>of("metrica", metric, "aristas", List.of(), "pesoTotal", 0); // caso vacío
        String start = g.keySet().iterator().next(); // nodo inicial (cualquiera)
        Set<String> in = new HashSet<>(); // nodos ya incluidos en MST
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.w)); // pq de aristas por peso
        List<Edge> mst = new ArrayList<>(); int total = 0; // aristas del MST y peso total

        in.add(start); pq.addAll(g.get(start)); // inicia con aristas desde start
        while (mst.size() < g.size()-1 && !pq.isEmpty()){ // hasta tener n-1 aristas o no haber más
            Edge e = pq.poll(); // toma la arista mínima
            if (in.contains(e.v)) continue; // si el extremo ya está, la omite
            mst.add(e); total += e.w; // agrega arista al MST
            in.add(e.v); // marca nuevo nodo incluido
            for (Edge nx : g.getOrDefault(e.v, List.of())) if (!in.contains(nx.v)) pq.add(nx); // añade aristas incidentes
        }
        List<Map<String,Object>> edges = mst.stream() // prepara salida legible
                .map(e -> Map.<String,Object>of("u", e.u, "v", e.v, "peso", e.w))
                .collect(Collectors.toList());
        return Map.<String,Object>of("metrica", metric, "aristas", edges, "pesoTotal", total); // devuelve MST
    }

    // Kruskal
    public Map<String,Object> kruskal(String metric){ // algoritmo de Kruskal para MST
        var g = toUndirected(metric); // grafo no dirigido
        List<Edge> edges = new ArrayList<>(); // lista de aristas
        for (var lst : g.values()) edges.addAll(lst); // recoge todas las aristas
        edges = edges.stream() // filtra y ordena para evitar duplicados (u<v) y ordena por peso
                .filter(e -> e.u.compareTo(e.v) < 0)
                .sorted(Comparator.comparingInt(e -> e.w))
                .collect(Collectors.toList());

        UnionFind uf = new UnionFind(g.keySet()); // estructura para componentes
        List<Edge> mst = new ArrayList<>(); int total = 0;
        for (Edge e : edges){
            if (uf.union(e.u, e.v)){ mst.add(e); total += e.w; } // si une componentes, la toma
            if (mst.size() == g.size()-1) break; // parada si ya completó MST
        }
        List<Map<String,Object>> out = mst.stream() // formato de salida
                .map(e -> Map.<String,Object>of("u", e.u, "v", e.v, "peso", e.w))
                .collect(Collectors.toList());
        return Map.<String,Object>of("metrica", metric, "aristas", out, "pesoTotal", total); // resultado
    }

    // Backtracking rutas simples (cota por saltos)
    public Map<String,Object> rutasBacktracking(String origen, String destino, int maxSaltos, String metric){
        var adj = buildAdj(metric); // adyacencia con métrica
        List<List<String>> rutas = new ArrayList<>(); // lista de rutas encontradas
        dfsPaths(origen, destino, maxSaltos, adj, new HashSet<>(), new ArrayList<>(), rutas); // inicia búsqueda
        return Map.<String,Object>of("rutas", rutas, "total", rutas.size()); // devuelve rutas y total
    }
    private void dfsPaths(String u, String dst, int left, Map<String,Map<String,Integer>> adj,
                          Set<String> vis, List<String> path, List<List<String>> out){
        if (left < 0 || !adj.containsKey(u)) return; // poda por saltos o inexistencia
        vis.add(u); path.add(u); // marca y añade
        if (u.equals(dst)) out.add(new ArrayList<>(path)); // si llegó, copia ruta a salida
        else for (String v : adj.getOrDefault(u, Map.of()).keySet())
            if (!vis.contains(v)) dfsPaths(v, dst, left-1, adj, vis, path, out); // recursión en vecinos
        path.remove(path.size()-1); vis.remove(u); // backtrack
    }

    // Branch & Bound TSP (poda simple)
    public Map<String,Object> tspBnB(List<String> centros, String metric){ // TSP por Branch & Bound
        var base = buildAdj(metric); // base con todos los pesos
        Map<String,Map<String,Integer>> g = new HashMap<>(); // subgrafo con sólo centros solicitados
        for (String u : centros) g.put(u, new HashMap<>(base.getOrDefault(u, Map.of()))); // copia adyacencia relevante

        List<String> bestPath = new ArrayList<>(); int[] bestCost = {Integer.MAX_VALUE}; // mejor solución global
        String start = centros.get(0); // punto de partida elegido
        List<String> cur = new ArrayList<>(); cur.add(start); // ruta actual
        Set<String> used = new HashSet<>(); used.add(start); // nodos usados
        tspDfs(g, start, start, used, cur, 0, bestCost, bestPath); // llamada recursiva
        return Map.<String,Object>of("mejorRuta", bestPath, "costo", bestCost[0]); // retorna mejor ruta y costo
    }
    private void tspDfs(Map<String,Map<String,Integer>> g, String u, String start,
                        Set<String> used, List<String> cur, int cost, int[] best, List<String> bestPath){
        if (used.size() == g.size()){ // si visitó todos
            Integer back = g.getOrDefault(u, Map.of()).get(start); // costo de volver al inicio
            if (back == null) return; // si no hay arco de regreso, descarta
            int total = cost + back; // costo total del ciclo
            if (total < best[0]){ best[0] = total; bestPath.clear(); bestPath.addAll(cur); bestPath.add(start); } // actualiza mejor
            return;
        }
        int bound = cost + lowerBound(g, used); // calcula cota inferior
        if (bound >= best[0]) return; // poda si la cota no mejora

        for (var e : g.getOrDefault(u, Map.of()).entrySet()){
            String v = e.getKey(); int w = e.getValue();
            if (used.contains(v)) continue; // salta ya usados
            used.add(v); cur.add(v); // elige siguiente
            tspDfs(g, v, start, used, cur, cost + w, best, bestPath); // recursión
            cur.remove(cur.size()-1); used.remove(v); // deshace elección
        }
    }
    private int lowerBound(Map<String,Map<String,Integer>> g, Set<String> used){ // heurística simple de cota
        int lb = 0;
        for (String v : g.keySet()){
            if (!used.contains(v)){
                var vs = g.getOrDefault(v, Map.of()).values(); // pesos salientes
                if (!vs.isEmpty()) {
                    lb += Collections.min(new ArrayList<>(vs)); // suma el mínimo posible por nodo no usado
                }
            }
        }
        return lb; // devuelve cota inferior
    }

    public record Edge(String u, String v, int w) {} // record para aristas
    static class UnionFind { // estructura union-find para Kruskal
        Map<String,String> p = new HashMap<>(); // padre
        Map<String,Integer> r = new HashMap<>(); // rango
        UnionFind(Collection<String> vs){ for (var v:vs){ p.put(v,v); r.put(v,0);} } // inicializa
        String find(String x){ if (!p.get(x).equals(x)) p.put(x, find(p.get(x))); return p.get(x); } // find con compresión
        boolean union(String a, String b){
            String pa = find(a), pb = find(b); if (pa.equals(pb)) return false; // si ya en mismo conjunto, no une
            int ra = r.get(pa), rb = r.get(pb);
            if (ra < rb) p.put(pa, pb);
            else if (ra > rb) p.put(pb, pa);
            else { p.put(pb, pa); r.put(pa, ra+1); } // unión por rango
            return true; // unión exitosa
        }
    }
}
