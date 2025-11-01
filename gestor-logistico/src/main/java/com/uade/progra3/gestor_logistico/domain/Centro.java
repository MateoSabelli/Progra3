package com.uade.progra3.gestor_logistico.domain;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Node("Centro")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Centro {
    @Id @GeneratedValue
    private Long internalId;

    @Property("id")
    private String id; // código externo: A,B,C...
    private String nombre;
    private String ciudad;
    private int capacidad;

    @Relationship(type = "RUTA", direction = Relationship.Direction.OUTGOING)
    private Set<Ruta> rutas = new HashSet<>();

    public Centro() {}
    public Centro(String id, String nombre, String ciudad, int capacidad) {
        this.id=id; this.nombre=nombre; this.ciudad=ciudad; this.capacidad=capacidad;
    }

    public Long getInternalId() { return internalId; }
    public void setInternalId(Long internalId) { this.internalId = internalId; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public Set<Ruta> getRutas() { return rutas; }
    public void setRutas(Set<Ruta> rutas) { this.rutas = rutas; }
}
