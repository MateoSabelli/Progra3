package com.uade.progra3.gestor_logistico.service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OptService {

    public List<Integer> mergeSort(List<Integer> a){
        if (a == null || a.size() <= 1) return a;
        int m = a.size()/2;
        List<Integer> L = mergeSort(new ArrayList<>(a.subList(0, m)));
        List<Integer> R = mergeSort(new ArrayList<>(a.subList(m, a.size())));
        return merge(L, R);
    }
    private List<Integer> merge(List<Integer> L, List<Integer> R){
        List<Integer> res = new ArrayList<>(L.size() + R.size());
        int i=0, j=0;
        while (i < L.size() || j < R.size()){
            if (j == R.size() || (i < L.size() && L.get(i) <= R.get(j))) res.add(L.get(i++));
            else res.add(R.get(j++));
        }
        return res;
    }

    public Map<String,Object> mochilaFraccional(int cap, List<Item> items){
        items.sort(Comparator.comparingDouble((Item it) -> (double) it.getBeneficio()/it.getPeso()).reversed());
        double beneficio = 0.0; int restante = cap;
        List<Map<String,Object>> seleccion = new ArrayList<>();

        for (Item it : items){
            if (restante == 0) break;
            int take = Math.min(it.getPeso(), restante);
            double frac = take / (double) it.getPeso();
            beneficio += it.getBeneficio() * frac;
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", it.getId());
            m.put("fraccion", frac);
            seleccion.add(m);
            restante -= take;
        }
        Map<String,Object> resp = new LinkedHashMap<>();
        resp.put("beneficioMax", beneficio);
        resp.put("seleccion", seleccion);
        return resp;
    }

    // DP mochila 0/1 O(n*W)
    public Map<String,Object> mochila01(int cap, List<Item> items){
        int n = items.size();
        int[][] dp = new int[n+1][cap+1];
        for (int i=1;i<=n;i++){
            Item it = items.get(i-1);
            for (int w=0; w<=cap; w++){
                dp[i][w] = dp[i-1][w];
                if (it.getPeso() <= w){
                    dp[i][w] = Math.max(dp[i][w], dp[i-1][w - it.getPeso()] + it.getBeneficio());
                }
            }
        }
        // reconstrucción
        List<String> pick = new ArrayList<>();
        int w = cap;
        for (int i=n;i>=1;i--){
            if (dp[i][w] != dp[i-1][w]){
                Item it = items.get(i-1);
                pick.add(it.getId());
                w -= it.getPeso();
            }
        }
        Collections.reverse(pick);

        Map<String,Object> resp = new LinkedHashMap<>();
        resp.put("valorMax", dp[n][cap]);
        resp.put("itemsSeleccionados", pick);
        return resp;
    }

    // DTO interno
    public static class Item {
        private String id;
        private int peso;
        private int beneficio;

        public Item() {}
        public Item(String id, int peso, int beneficio){ this.id=id; this.peso=peso; this.beneficio=beneficio; }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getPeso() { return peso; }
        public void setPeso(int peso) { this.peso = peso; }
        public int getBeneficio() { return beneficio; }
        public void setBeneficio(int beneficio) { this.beneficio = beneficio; }
    }
}
