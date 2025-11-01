package com.uade.progra3.gestor_logistico.repo;
import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import com.uade.progra3.gestor_logistico.domain.Centro;

public interface CentroRepository extends Neo4jRepository<Centro, Long> {

  // Buscar todos los centros ordenados por id (String)
  @Query("""
    MATCH (c:Centro)
    OPTIONAL MATCH (c)-[r:RUTA]->(d:Centro)
    WITH c, r, d ORDER BY c.id
    RETURN c, collect(r), collect(d)
  """)
  List<Centro> findAllByOrderByIdAsc();

  // Buscar por id (String property, no Long neo4j id)
  @Query("""
    MATCH (c:Centro {id: $id})
    OPTIONAL MATCH (c)-[r:RUTA]->(d:Centro)
    RETURN c, collect(r), collect(d)
  """)
  Optional<Centro> findByIdProperty(String id);

  // Crear/actualizar una ruta dirigida
  @Query("""
    MERGE (o:Centro {id:$from})
    MERGE (d:Centro {id:$to})
    MERGE (o)-[r:RUTA]->(d)
    SET r.tiempoMin = $tiempoMin, r.costo = $costo, r.distKm = $distKm
    RETURN 1
  """)
  Integer crearRuta(String from, String to, int tiempoMin, int costo, int distKm);

  // Borrar una ruta dirigida
  @Query("""
    MATCH (:Centro {id:$from})-[r:RUTA]->(:Centro {id:$to})
    DELETE r
    RETURN 1
  """)
  Integer borrarRuta(String from, String to);
}
