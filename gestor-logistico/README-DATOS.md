# 📊 Guía para Inicializar la Base de Datos Neo4j

## Opción 1: Desde la Aplicación Web (Recomendado) ✨

1. **Inicia la aplicación Spring Boot**
2. **Abre tu navegador** en `http://localhost:8080`
3. **Ve a la pestaña "Centros"**
4. **Haz clic en el botón "🚀 Inicializar Base de Datos"**
5. **Confirma la acción**
6. **¡Listo!** Los datos se cargarán automáticamente

### Botones disponibles:

- **🔄 Cargar Centros**: Muestra los centros existentes
- **🚀 Inicializar Base de Datos**: Crea datos de ejemplo
- **🗑️ Limpiar Datos**: Elimina todos los datos

---

## Opción 2: API REST con cURL 🔧

### Inicializar datos:

```bash
curl -X POST http://localhost:8080/data/init
```

### Limpiar datos:

```bash
curl -X DELETE http://localhost:8080/data/clear
```

### Ver centros:

```bash
curl http://localhost:8080/centros
```

---

## Opción 3: Neo4j Browser (Cypher directo) 🗄️

1. **Abre Neo4j Browser**: `http://localhost:7474`
2. **Ejecuta el script**: Copia y pega el contenido del archivo `init-neo4j.cypher`
3. **O ejecuta estos comandos paso a paso**:

### Limpiar datos:

```cypher
MATCH (n) DETACH DELETE n;
```

### Crear centros:

```cypher
CREATE (ca001:Centro {id: 'CA001', nombre: 'Centro Norte', ciudad: 'Buenos Aires', capacidad: 1000})
CREATE (ca002:Centro {id: 'CA002', nombre: 'Centro Sur', ciudad: 'Córdoba', capacidad: 800})
CREATE (ca003:Centro {id: 'CA003', nombre: 'Centro Este', ciudad: 'Rosario', capacidad: 600})
CREATE (ca004:Centro {id: 'CA004', nombre: 'Centro Oeste', ciudad: 'Mendoza', capacidad: 500})
CREATE (ca005:Centro {id: 'CA005', nombre: 'Centro Litoral', ciudad: 'Santa Fe', capacidad: 700})
CREATE (ca006:Centro {id: 'CA006', nombre: 'Centro Patagonia', ciudad: 'Neuquén', capacidad: 400});
```

### Crear rutas (ejemplo):

```cypher
// Buenos Aires -> Córdoba
MATCH (a:Centro {id: 'CA001'}), (b:Centro {id: 'CA002'})
CREATE (a)-[:RUTA {tiempoMin: 60, costo: 1500, distKm: 700}]->(b);

// Buenos Aires -> Rosario
MATCH (a:Centro {id: 'CA001'}), (b:Centro {id: 'CA003'})
CREATE (a)-[:RUTA {tiempoMin: 35, costo: 800, distKm: 300}]->(b);
```

Para todas las rutas, consulta el archivo `init-neo4j.cypher`

### Verificar datos:

```cypher
// Contar centros
MATCH (c:Centro) RETURN count(c) as TotalCentros;

// Contar rutas
MATCH ()-[r:RUTA]->() RETURN count(r) as TotalRutas;

// Ver el grafo completo
MATCH (c:Centro)-[r:RUTA]->(d:Centro)
RETURN c, r, d;
```

---

## 📋 Datos que se crearán:

### Centros:

| ID    | Nombre           | Ciudad       | Capacidad |
| ----- | ---------------- | ------------ | --------- |
| CA001 | Centro Norte     | Buenos Aires | 1000      |
| CA002 | Centro Sur       | Córdoba      | 800       |
| CA003 | Centro Este      | Rosario      | 600       |
| CA004 | Centro Oeste     | Mendoza      | 500       |
| CA005 | Centro Litoral   | Santa Fe     | 700       |
| CA006 | Centro Patagonia | Neuquén      | 400       |

### Rutas (Ejemplos):

- **CA001 → CA002**: 60 min / $1500 / 700 km
- **CA001 → CA003**: 35 min / $800 / 300 km
- **CA002 → CA004**: 90 min / $2000 / 600 km
- Y muchas más...

---

## 🔍 Consultas útiles en Neo4j:

### Ver todos los centros:

```cypher
MATCH (c:Centro) RETURN c;
```

### Ver todas las rutas desde un centro:

```cypher
MATCH (c:Centro {id: 'CA001'})-[r:RUTA]->(d:Centro)
RETURN c, r, d;
```

### Encontrar la ruta más corta (por tiempo):

```cypher
MATCH path = shortestPath(
  (start:Centro {id: 'CA001'})-[:RUTA*]-(end:Centro {id: 'CA006'})
)
RETURN path;
```

---

## ⚙️ Configuración de Neo4j

Asegúrate de que tu `application.yml` tenga:

```yaml
spring:
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: tu_password
```

---

## 🚀 ¡Ya estás listo!

Una vez inicializada la base de datos, puedes:

- ✅ Probar todos los algoritmos de grafos (BFS, DFS, Dijkstra, MST, etc.)
- ✅ Ejecutar el problema del viajante (TSP)
- ✅ Buscar rutas con backtracking
- ✅ Y mucho más...

¡Diviértete explorando los algoritmos! 🎉
