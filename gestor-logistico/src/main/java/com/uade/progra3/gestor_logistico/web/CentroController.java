package com.uade.progra3.gestor_logistico.web;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.progra3.gestor_logistico.domain.Centro;
import com.uade.progra3.gestor_logistico.repo.CentroRepository;


@RestController
@RequestMapping("/centros")
public class CentroController {
    private final CentroRepository repo;
    public CentroController(CentroRepository repo){ this.repo = repo; }

    @PutMapping
    public Centro upsert(@RequestBody Centro centro){
        return repo.save(centro);
    }

    @GetMapping
    public List<Centro> all(){
        // Spring Data Neo4j carga automáticamente las relaciones
        return repo.findAllByOrderByIdAsc();
    }
}
