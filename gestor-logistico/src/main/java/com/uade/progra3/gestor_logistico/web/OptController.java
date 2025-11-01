package com.uade.progra3.gestor_logistico.web;
import com.uade.progra3.gestor_logistico.service.OptService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/opt")
public class OptController {
    private final OptService opt;
    public OptController(OptService opt){ this.opt = opt; }

    @PostMapping("/mergesort")
    public Map<String,Object> mergesort(@RequestBody Map<String,List<Integer>> body){
        return Map.<String,Object>of("ordenado", opt.mergeSort(body.get("valores")));
    }

    @PostMapping("/greedy/fraccional")
    public Map<String,Object> greedyFrac(@RequestBody Map<String,Object> body){
        int capacidad = (int) body.get("capacidad");
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> itemsRaw = (List<Map<String,Object>>) body.get("items");
        List<OptService.Item> items = new ArrayList<>();
        for (Map<String,Object> m : itemsRaw){
            String id = (String) m.get("id");
            int peso = (int) m.get("peso");
            int beneficio = (int) m.get("beneficio");
            items.add(new OptService.Item(id, peso, beneficio));
        }
        return opt.mochilaFraccional(capacidad, items);
    }

    @PostMapping("/dp/mochila01")
    public Map<String,Object> dpMochila(@RequestBody Map<String,Object> body){
        int capacidad = (int) body.get("capacidad");
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> itemsRaw = (List<Map<String,Object>>) body.get("items");
        List<OptService.Item> items = new ArrayList<>();
        for (Map<String,Object> m : itemsRaw){
            String id = (String) m.get("id");
            int peso = (int) m.get("peso");
            int beneficio = (int) m.get("beneficio");
            items.add(new OptService.Item(id, peso, beneficio));
        }
        return opt.mochila01(capacidad, items);
    }
}
