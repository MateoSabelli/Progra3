package com.uade.progra3.gestor_logistico.web; // paquete donde vive el controlador

import java.util.ArrayList; // servicio que contiene la lógica de optimización
import java.util.List; // anotaciones Spring MVC (RestController, RequestMapping, PostMapping, RequestBody)
import java.util.Map; // utilidades Java (List, Map, ArrayList, etc.)

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.progra3.gestor_logistico.service.OptService;

@RestController // indica que esta clase expone endpoints REST y sus métodos devuelven el cuerpo de la respuesta
@RequestMapping("/opt") // prefijo común para todas las rutas de este controlador: /opt
public class OptController { // definición de la clase controlador

    private final OptService opt; // dependencia al servicio de optimización

    public OptController(OptService opt){ this.opt = opt; } // constructor: inyección por constructor del servicio

    @PostMapping("/mergesort") // endpoint POST /opt/mergesort
    public Map<String,Object> mergesort(@RequestBody Map<String,List<Integer>> body){
        // espera un JSON con clave "valores": [enteros...]
        // delega al servicio mergeSort y devuelve un Map con la lista ordenada
        return Map.<String,Object>of("ordenado", opt.mergeSort(body.get("valores")));
    }

    @PostMapping("/greedy/fraccional") // endpoint POST /opt/greedy/fraccional
    public Map<String,Object> greedyFrac(@RequestBody Map<String,Object> body){
        // extrae la capacidad del body (debe ser entero)
        int capacidad = (int) body.get("capacidad");

        // extrae la lista de items del body; cast inseguro por JSON -> suprime warning
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> itemsRaw = (List<Map<String,Object>>) body.get("items");

        // convierte la representación cruda a objetos Item del servicio
        List<OptService.Item> items = new ArrayList<>();
        for (Map<String,Object> m : itemsRaw){
            String id = (String) m.get("id"); // id del item
            int peso = (int) m.get("peso"); // peso del item
            int beneficio = (int) m.get("beneficio"); // beneficio del item
            items.add(new OptService.Item(id, peso, beneficio)); // construye y añade al listado
        }

        // llama al algoritmo voraz de OptService y retorna su resultado (Map con beneficio y selección)
        return opt.mochilaFraccional(capacidad, items);
    }

    @PostMapping("/dp/mochila01") // endpoint POST /opt/dp/mochila01
    public Map<String,Object> dpMochila(@RequestBody Map<String,Object> body){
        // extrae capacidad
        int capacidad = (int) body.get("capacidad");

        // extrae items crudos del body
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> itemsRaw = (List<Map<String,Object>>) body.get("items");

        // convierte a lista de Item
        List<OptService.Item> items = new ArrayList<>();
        for (Map<String,Object> m : itemsRaw){
            String id = (String) m.get("id");
            int peso = (int) m.get("peso");
            int beneficio = (int) m.get("beneficio");
            items.add(new OptService.Item(id, peso, beneficio));
        }

        // delega a la implementación DP de mochila 0/1 y devuelve el mapa resultado
        return opt.mochila01(capacidad, items);
    }
} // fin de la clase OptController
