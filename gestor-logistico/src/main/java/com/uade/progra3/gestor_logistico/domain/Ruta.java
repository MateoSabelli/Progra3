package com.uade.progra3.gestor_logistico.domain;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RelationshipProperties
public class Ruta {
    @Id @GeneratedValue
    private Long id;

    @TargetNode
    @JsonIgnoreProperties({"rutas", "internalId"})
    private Centro destino;

    private int tiempoMin;
    private int costo;
    private int distKm;

    public Ruta() {}
    public Ruta(Centro destino, int tiempoMin, int costo, int distKm) {
        this.destino=destino; this.tiempoMin=tiempoMin; this.costo=costo; this.distKm=distKm;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Centro getDestino() { return destino; }
    public void setDestino(Centro destino) { this.destino = destino; }
    public int getTiempoMin() { return tiempoMin; }
    public void setTiempoMin(int tiempoMin) { this.tiempoMin = tiempoMin; }
    public int getCosto() { return costo; }
    public void setCosto(int costo) { this.costo = costo; }
    public int getDistKm() { return distKm; }
    public void setDistKm(int distKm) { this.distKm = distKm; }
}
