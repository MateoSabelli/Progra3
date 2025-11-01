package com.uade.progra3.gestor_logistico.service;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uade.progra3.gestor_logistico.domain.Centro;
import com.uade.progra3.gestor_logistico.domain.Ruta;
import com.uade.progra3.gestor_logistico.repo.CentroRepository;

@Service
public class GraphService {

    private final CentroRepository repo;
    public GraphService(CentroRepository repo){ this.repo = repo; }

    public Map<String, Map<String, Integer>> buildAdj(String metric) {
        List<Centro> centros = repo.findAllByOrderByIdAsc();
        Map<String, Map<String, Integer>> adj = new HashMap<>();
        String safeMetric = metric != null ? metric : "tiempoMin";
        for (Centro c : centros) {
            adj.putIfAbsent(c.getId(), new HashMap<>());
            if (c.getRutas() != null) {
                for (Ruta r : c.getRutas()) {
                    int peso = switch (safeMetric) {
                        case "costo" -> r.getCosto();
                        case "distKm" -> r.getDistKm();
                        default -> r.getTiempoMin();
                    };
                    adj.get(c.getId()).put(r.getDestino().getId(), peso);
                }
            }
        }
        return adj;
    }

    // BFS
    public List<String> bfs(String origen){
        var adj = buildAdj("tiempoMin");
        List<String> out = new ArrayList<>();
        if (!adj.containsKey(origen)) return out;
        Set<String> vis = new HashSet<>();
        Queue<String> q = new ArrayDeque<>();
        q.add(origen); vis.add(origen);
        while(!q.isEmpty()){
            String u = q.poll(); out.add(u);
            for (String v : adj.getOrDefault(u, Map.of()).keySet()){
                if (!vis.contains(v)){ vis.add(v); q.add(v); }
            }
        }
        return out;
    }

    // DFS
    public List<String> dfs(String origen){
        var adj = buildAdj("tiempoMin");
        List<String> out = new ArrayList<>();
        if (!adj.containsKey(origen)) return out;
        dfsRec(origen, adj, new HashSet<>(), out);
        return out;
    }
    private void dfsRec(String u, Map<String,Map<String,Integer>> adj, Set<String> vis, List<String> out){
        vis.add(u); out.add(u);
        for (String v : adj.getOrDefault(u, Map.of()).keySet()){
            if (!vis.contains(v)) dfsRec(v, adj, vis, out);
        }
    }

    // Dijkstra
    public Map<String,Object> dijkstra(String origen, String metric){
        var adj = buildAdj(metric);
        Map<String,Integer> dist = new HashMap<>();
        Map<String,String> prev = new HashMap<>();
        for (String v : adj.keySet()) dist.put(v, Integer.MAX_VALUE/4);
        if (!dist.containsKey(origen)) return Map.<String,Object>of("error","origen inexistente");
        dist.put(origen, 0);

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        pq.addAll(adj.keySet());

        while(!pq.isEmpty()){
            String u = pq.poll();
            for (var e : adj.getOrDefault(u, Map.of()).entrySet()){
                String v = e.getKey(); int w = e.getValue();
                if (dist.get(u) + w < dist.get(v)){
                    dist.put(v, dist.get(u) + w);
                    prev.put(v, u);
                    pq.remove(v); pq.add(v);
                }
            }
        }
        Map<String,Object> resp = new LinkedHashMap<>();
        resp.put("origen", origen);
        resp.put("metrica", metric);
        resp.put("distancias", dist);
        resp.put("predecesores", prev);
        return resp;
    }

    // Convertir a no dirigido (para MST)
    private Map<String,List<Edge>> toUndirected(String metric){
        var adj = buildAdj(metric);
        Map<String,List<Edge>> und = new HashMap<>();
        for (String u : adj.keySet()){
            und.putIfAbsent(u, new ArrayList<>());
            for (var e : adj.get(u).entrySet()){
                String v = e.getKey(); int w = e.getValue();
                und.get(u).add(new Edge(u,v,w));
                und.putIfAbsent(v, new ArrayList<>());
                und.get(v).add(new Edge(v,u,w));
            }
        }
        return und;
    }

    // Prim
    public Map<String,Object> prim(String metric){
        var g = toUndirected(metric);
        if (g.isEmpty()) return Map.<String,Object>of("metrica", metric, "aristas", List.of(), "pesoTotal", 0);
        String start = g.keySet().iterator().next();
        Set<String> in = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.w));
        List<Edge> mst = new ArrayList<>(); int total = 0;

        in.add(start); pq.addAll(g.get(start));
        while (mst.size() < g.size()-1 && !pq.isEmpty()){
            Edge e = pq.poll();
            if (in.contains(e.v)) continue;
            mst.add(e); total += e.w;
            in.add(e.v);
            for (Edge nx : g.getOrDefault(e.v, List.of())) if (!in.contains(nx.v)) pq.add(nx);
        }
        List<Map<String,Object>> edges = mst.stream()
                .map(e -> Map.<String,Object>of("u", e.u, "v", e.v, "peso", e.w))
                .collect(Collectors.toList());
        return Map.<String,Object>of("metrica", metric, "aristas", edges, "pesoTotal", total);
    }

    // Kruskal
    public Map<String,Object> kruskal(String metric){
        var g = toUndirected(metric);
        List<Edge> edges = new ArrayList<>();
        for (var lst : g.values()) edges.addAll(lst);
        edges = edges.stream()
                .filter(e -> e.u.compareTo(e.v) < 0)
                .sorted(Comparator.comparingInt(e -> e.w))
                .collect(Collectors.toList());

        UnionFind uf = new UnionFind(g.keySet());
        List<Edge> mst = new ArrayList<>(); int total = 0;
        for (Edge e : edges){
            if (uf.union(e.u, e.v)){ mst.add(e); total += e.w; }
            if (mst.size() == g.size()-1) break;
        }
        List<Map<String,Object>> out = mst.stream()
                .map(e -> Map.<String,Object>of("u", e.u, "v", e.v, "peso", e.w))
                .collect(Collectors.toList());
        return Map.<String,Object>of("metrica", metric, "aristas", out, "pesoTotal", total);
    }

    // Backtracking rutas simples (cota por saltos)
    public Map<String,Object> rutasBacktracking(String origen, String destino, int maxSaltos, String metric){
        var adj = buildAdj(metric);
        List<List<String>> rutas = new ArrayList<>();
        dfsPaths(origen, destino, maxSaltos, adj, new HashSet<>(), new ArrayList<>(), rutas);
        return Map.<String,Object>of("rutas", rutas, "total", rutas.size());
    }
    private void dfsPaths(String u, String dst, int left, Map<String,Map<String,Integer>> adj,
                          Set<String> vis, List<String> path, List<List<String>> out){
        if (left < 0 || !adj.containsKey(u)) return;
        vis.add(u); path.add(u);
        if (u.equals(dst)) out.add(new ArrayList<>(path));
        else for (String v : adj.getOrDefault(u, Map.of()).keySet())
            if (!vis.contains(v)) dfsPaths(v, dst, left-1, adj, vis, path, out);
        path.remove(path.size()-1); vis.remove(u);
    }

    // Branch & Bound TSP (poda simple)
    public Map<String,Object> tspBnB(List<String> centros, String metric){
        var base = buildAdj(metric);
        Map<String,Map<String,Integer>> g = new HashMap<>();
        for (String u : centros) g.put(u, new HashMap<>(base.getOrDefault(u, Map.of())));

        List<String> bestPath = new ArrayList<>(); int[] bestCost = {Integer.MAX_VALUE};
        String start = centros.get(0);
        List<String> cur = new ArrayList<>(); cur.add(start);
        Set<String> used = new HashSet<>(); used.add(start);
        tspDfs(g, start, start, used, cur, 0, bestCost, bestPath);
        return Map.<String,Object>of("mejorRuta", bestPath, "costo", bestCost[0]);
    }
    private void tspDfs(Map<String,Map<String,Integer>> g, String u, String start,
                        Set<String> used, List<String> cur, int cost, int[] best, List<String> bestPath){
        if (used.size() == g.size()){
            Integer back = g.getOrDefault(u, Map.of()).get(start);
            if (back == null) return;
            int total = cost + back;
            if (total < best[0]){ best[0] = total; bestPath.clear(); bestPath.addAll(cur); bestPath.add(start); }
            return;
        }
        int bound = cost + lowerBound(g, used);
        if (bound >= best[0]) return;

        for (var e : g.getOrDefault(u, Map.of()).entrySet()){
            String v = e.getKey(); int w = e.getValue();
            if (used.contains(v)) continue;
            used.add(v); cur.add(v);
            tspDfs(g, v, start, used, cur, cost + w, best, bestPath);
            cur.remove(cur.size()-1); used.remove(v);
        }
    }
    private int lowerBound(Map<String,Map<String,Integer>> g, Set<String> used){
        int lb = 0;
        for (String v : g.keySet()){
            if (!used.contains(v)){
                var vs = g.getOrDefault(v, Map.of()).values();
                if (!vs.isEmpty()) {
                    lb += Collections.min(new ArrayList<>(vs));
                }
            }
        }
        return lb;
    }

    public record Edge(String u, String v, int w) {}
    static class UnionFind {
        Map<String,String> p = new HashMap<>();
        Map<String,Integer> r = new HashMap<>();
        UnionFind(Collection<String> vs){ for (var v:vs){ p.put(v,v); r.put(v,0);} }
        String find(String x){ if (!p.get(x).equals(x)) p.put(x, find(p.get(x))); return p.get(x); }
        boolean union(String a, String b){
            String pa = find(a), pb = find(b); if (pa.equals(pb)) return false;
            int ra = r.get(pa), rb = r.get(pb);
            if (ra < rb) p.put(pa, pb);
            else if (ra > rb) p.put(pb, pa);
            else { p.put(pb, pa); r.put(pa, ra+1); }
            return true;
        }
    }
}
