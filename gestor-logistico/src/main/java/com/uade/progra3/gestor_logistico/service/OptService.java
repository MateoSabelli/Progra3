package com.uade.progra3.gestor_logistico.service; // declara el paquete del archivo

import java.util.ArrayList; // clase para listas dinámicas
import java.util.Collections; // utilidades de colección (p.ej. reverse)
import java.util.Comparator; // interfaz para comparar objetos (usada en sort)
import java.util.LinkedHashMap; // Map que mantiene orden de inserción
import java.util.List; // interfaz List
import java.util.Map; // interfaz Map

import org.springframework.stereotype.Service; // anotación Spring para marcar un bean de servicio

@Service // marca la clase como servicio gestionado por Spring
public class OptService { // comienzo de la clase OptService

    // Método público que ordena una lista de enteros usando Merge Sort (divide y vencerás)
    public List<Integer> mergeSort(List<Integer> a){
        if (a == null || a.size() <= 1) return a; // caso base: nulo o 0/1 elemento => ya está ordenado
        int m = a.size()/2; // calcula el punto medio
        List<Integer> L = mergeSort(new ArrayList<>(a.subList(0, m))); // ordena recursivamente la mitad izquierda
        List<Integer> R = mergeSort(new ArrayList<>(a.subList(m, a.size()))); // ordena recursivamente la mitad derecha
        return merge(L, R); // combina (merge) las dos mitades ordenadas y retorna
    }

    // Función auxiliar que mezcla dos listas ordenadas en una sola ordenada
    private List<Integer> merge(List<Integer> L, List<Integer> R){
        List<Integer> res = new ArrayList<>(L.size() + R.size()); // lista resultado con capacidad inicial
        int i=0, j=0; // índices para recorrer L y R
        while (i < L.size() || j < R.size()){ // mientras queden elementos en alguna de las dos listas
            // si R se agotó o el elemento actual de L <= elemento actual de R, tomar de L
            if (j == R.size() || (i < L.size() && L.get(i) <= R.get(j))) res.add(L.get(i++));
            else res.add(R.get(j++)); // en caso contrario tomar de R
        }
        return res; // devuelve la lista fusionada ordenada
    }

    // Algoritmo voraz para la mochila fraccional
    public Map<String,Object> mochilaFraccional(int cap, List<Item> items){
        // ordena los items por ratio beneficio/peso descendente (los mejores primeros)
        items.sort(Comparator.comparingDouble((Item it) -> (double) it.getBeneficio()/it.getPeso()).reversed());
        double beneficio = 0.0; int restante = cap; // inicializa beneficio acumulado y capacidad restante
        List<Map<String,Object>> seleccion = new ArrayList<>(); // lista de selecciones (id + fraccion tomada)

        for (Item it : items){ // recorre items ordenados
            if (restante == 0) break; // si no queda capacidad, salir
            int take = Math.min(it.getPeso(), restante); // cuánto tomar del item (puede ser parcial)
            double frac = take / (double) it.getPeso(); // fracción tomada del item
            beneficio += it.getBeneficio() * frac; // acumula beneficio proporcional
            Map<String,Object> m = new LinkedHashMap<>(); // registra la toma (mantiene orden)
            m.put("id", it.getId()); // id del item
            m.put("fraccion", frac); // fracción tomada
            seleccion.add(m); // añade registro a la selección
            restante -= take; // reduce capacidad restante
        }
        Map<String,Object> resp = new LinkedHashMap<>(); // prepara respuesta (mantiene orden)
        resp.put("beneficioMax", beneficio); // beneficio máximo aproximado obtenido
        resp.put("seleccion", seleccion); // lista de items/fracciones seleccionadas
        return resp; // retorna el resultado
    }

    // Programación dinámica para la mochila 0/1 (solución exacta, complejidad O(n * cap))
    public Map<String,Object> mochila01(int cap, List<Item> items){
        int n = items.size(); // número de items
        int[][] dp = new int[n+1][cap+1]; // tabla DP; dp[i][w] = mejor beneficio con primeros i items y capacidad w
        for (int i=1;i<=n;i++){ // iterar items (1..n)
            Item it = items.get(i-1); // item i (ajuste por índice 0-based)
            for (int w=0; w<=cap; w++){ // iterar todas las capacidades desde 0 hasta cap
                dp[i][w] = dp[i-1][w]; // por defecto no tomar el item i
                if (it.getPeso() <= w){ // si cabe el item i en capacidad w
                    dp[i][w] = Math.max(dp[i][w], dp[i-1][w - it.getPeso()] + it.getBeneficio()); // comparar tomar vs no tomar
                }
            }
        }
        // Reconstrucción de la solución: ver qué items fueron tomados
        List<String> pick = new ArrayList<>(); // lista de ids seleccionados
        int w = cap; // capacidad restante para reconstrucción
        for (int i=n;i>=1;i--){ // retroceder por la tabla desde el último item
            if (dp[i][w] != dp[i-1][w]){ // si el valor cambió, significa que el item i fue tomado
                Item it = items.get(i-1); // obtener el item correspondiente
                pick.add(it.getId()); // agregar id a la lista de seleccionados
                w -= it.getPeso(); // reducir capacidad restante
            }
        }
        Collections.reverse(pick); // invertir para mantener orden original de items

        Map<String,Object> resp = new LinkedHashMap<>(); // respuesta final
        resp.put("valorMax", dp[n][cap]); // beneficio máximo obtenido
        resp.put("itemsSeleccionados", pick); // lista de ids seleccionados
        return resp; // retorna resultado
    }

    // DTO interno que representa un item con id, peso y beneficio
    public static class Item {
        private String id; // identificador del item
        private int peso; // peso del item (entero)
        private int beneficio; // beneficio (valor) del item

        public Item() {} // constructor vacío (útil para serialización / frameworks)
        public Item(String id, int peso, int beneficio){ this.id=id; this.peso=peso; this.beneficio=beneficio; } // constructor con campos

        public String getId() { return id; } // getter id
        public void setId(String id) { this.id = id; } // setter id
        public int getPeso() { return peso; } // getter peso
        public void setPeso(int peso) { this.peso = peso; } // setter peso
        public int getBeneficio() { return beneficio; } // getter beneficio
        public void setBeneficio(int beneficio) { this.beneficio = beneficio; } // setter beneficio
    }
} // fin de la clase OptService
