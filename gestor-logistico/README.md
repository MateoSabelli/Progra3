# Gestor Logístico — Trabajo Práctico

## Resumen

Aplicación Spring Boot que modela centros de distribución y rutas en Neo4j. Incluye algoritmos de grafos (BFS, DFS, Dijkstra, Prim, Kruskal, backtracking, Branch & Bound/TSP) y algoritmos de optimización (MergeSort, mochila fraccional y 0/1).

## Requisitos

- Java 17 (requerido por el proyecto)
- Maven (se incluye `mvnw`)
- Neo4j Aura o instancia local (URI y credenciales en `src/main/resources/application.yml`)

## Estructura

- `src/main/java/.../web` — Controladores REST (`/centros`, `/grafos`, `/opt`)
- `src/main/java/.../service` — `GraphService` y `OptService` (algoritmos)
- `src/main/java/.../repo` — `CentroRepository` (consulta para cargar el grafo)
- `src/main/resources/static/index.html` — UI mínima que consume `/centros`

## Endpoints principales

GET /centros

- Devuelve todos los centros y sus rutas (JSON)

PUT /centros

- Crea o actualiza un centro (body JSON con propiedades de `Centro` y opcionalmente sus `rutas`)

## Grafo

GET /grafos/bfs?origen=A
GET /grafos/dfs?origen=A
GET /grafos/dijkstra?origen=A&metrica=tiempoMin
GET /grafos/mst/prim?metrica=distKm
GET /grafos/mst/kruskal?metrica=distKm
GET /grafos/backtracking/rutas?origen=A&destino=D&maxSaltos=4
POST /grafos/bnb/tsp (body: {"centros": ["A","B","C"], "metrica": "distKm"})

## Optimización

POST /opt/mergesort (body: {"valores": [5,1,3,2]})
POST /opt/greedy/fraccional (body: {"capacidad": 50, "items": [{"id":"i1","peso":10,"beneficio":60}, ...]})
POST /opt/dp/mochila01 (igual estructura que anterior)

## Datos de ejemplo (Cypher)

CREATE CONSTRAINT centro_id IF NOT EXISTS FOR (c:Centro) REQUIRE c.id IS UNIQUE;

MERGE (a:Centro {id:'A', nombre:'Depósito Norte', ciudad:'Rosario', capacidad:120})
MERGE (b:Centro {id:'B', nombre:'Depósito Sur', ciudad:'La Plata', capacidad:90})
MERGE (c:Centro {id:'C', nombre:'CD Oeste', ciudad:'Moreno', capacidad:100})
MERGE (d:Centro {id:'D', nombre:'CD Cuyo', ciudad:'Mendoza', capacidad:70});

MERGE (a)-[:RUTA {tiempoMin:50, costo:1200, distKm:300}]->(b)
MERGE (a)-[:RUTA {tiempoMin:40, costo:1000, distKm:250}]->(c)
MERGE (b)-[:RUTA {tiempoMin:60, costo:1400, distKm:350}]->(d)
MERGE (c)-[:RUTA {tiempoMin:55, costo:1100, distKm:320}]->(d)
MERGE (b)-[:RUTA {tiempoMin:35, costo:900, distKm:200}]->(c);

## Cómo ejecutar (local)

1. Instalar JDK 17 y configurar `JAVA_HOME`.
2. (Opcional) Confirmar que `java -version` devuelve Java 17.
3. Desde la carpeta del proyecto ejecutar:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

4. Abrir `http://localhost:8081/` (puerto definido en `application.yml`) para ver la UI.

## Comprobaciones y notas

- Si ves `Cannot invoke "...TransactionTemplate" because "this.txTemplate" is null` revisa `Neo4jConfig` — el proyecto incluye un `Neo4jTransactionManager` bean para entornos imperativos.
- Si tu entorno tiene Java 11 y no puedes instalar Java 17, puedes (temporalmente) cambiar la propiedad `java.version` en `pom.xml` a `11` para compilar localmente, pero el TP pide Java 17.

## Siguientes pasos que puedo hacer por vos

- Probar un build aquí si me autorizás a cambiar `pom.xml` temporalmente a `11` para compilar con tu Java actual.
- Añadir tests unitarios mínimos para `GraphService` y `OptService`.
- Mejorar la UI para mostrar también resultados de `grafos` y `opt`.

Si querés que siga, decime cuál de las acciones preferís (ejecutar build aquí, agregar tests, mejorar UI, etc.).
