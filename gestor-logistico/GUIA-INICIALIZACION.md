# 🚀 Guía de Inicialización de la Base de Datos

## ⚠️ Problema Conocido

La inicialización desde el backend Java tiene problemas de persistencia con Neo4j Aura (cloud). Las relaciones (rutas) no se guardan correctamente cuando se usan objetos de Spring Data Neo4j.

**Solución:** Ejecutar el script Cypher directamente en Neo4j.

---

## 📋 Pasos de Inicialización

### 1️⃣ Accede a Neo4j Aura

1. Abre tu navegador
2. Ve a: **https://console.neo4j.io**
3. Inicia sesión con tu cuenta
4. Selecciona tu instancia de Neo4j (la base de datos configurada en `application.yml`)

### 2️⃣ Abre el Query Browser

- Click en el botón **"Query"** de tu instancia
- O click en **"Open with Neo4j Browser"**
- Se abrirá una consola donde puedes ejecutar queries Cypher

### 3️⃣ Copia el Script de Inicialización

1. Abre el archivo: **`init-neo4j-completo.cypher`** (en la raíz del proyecto)
2. Selecciona TODO el contenido (Ctrl + A)
3. Copia (Ctrl + C)

### 4️⃣ Ejecuta el Script

1. Pega el contenido en la consola de Neo4j (Ctrl + V)
2. Presiona **Ctrl + Enter** o click en el botón **▶️ Run**
3. Espera a que termine la ejecución (puede tomar 2-3 segundos)

### 5️⃣ Verifica la Creación

Deberías ver dos tablas de resultados:

**Tabla 1: Centros Creados**

```
╔════════╦═══════════════════╦═════════════╦═══════════╗
║ Centro ║ Nombre            ║ Ciudad      ║ Capacidad ║
╠════════╬═══════════════════╬═════════════╬═══════════╣
║ CA001  ║ Centro Norte      ║ Buenos Aires║ 1000      ║
║ CA002  ║ Centro Sur        ║ Córdoba     ║ 800       ║
║ CA003  ║ Centro Este       ║ Rosario     ║ 600       ║
║ CA004  ║ Centro Oeste      ║ Mendoza     ║ 500       ║
║ CA005  ║ Centro Litoral    ║ Santa Fe    ║ 700       ║
║ CA006  ║ Centro Patagonia  ║ Neuquén     ║ 400       ║
╚════════╩═══════════════════╩═════════════╩═══════════╝
```

**Tabla 2: Rutas Creadas** (17 rutas en total)

```
Ejemplos:
CA001 → CA002: 60 min, $1500, 700 km
CA001 → CA003: 35 min, $800, 300 km
CA002 → CA004: 90 min, $2000, 600 km
...
```

### 6️⃣ Verifica en la Aplicación

1. Vuelve a tu aplicación: **http://localhost:8081**
2. Click en **"🔄 Cargar Centros"**
3. Deberías ver los 6 centros en la tabla con sus rutas

---

## 🎯 Estructura de Datos Creada

### Centros (Nodos)

- **CA001**: Centro Norte (Buenos Aires) - Capacidad: 1000
- **CA002**: Centro Sur (Córdoba) - Capacidad: 800
- **CA003**: Centro Este (Rosario) - Capacidad: 600
- **CA004**: Centro Oeste (Mendoza) - Capacidad: 500
- **CA005**: Centro Litoral (Santa Fe) - Capacidad: 700
- **CA006**: Centro Patagonia (Neuquén) - Capacidad: 400

### Rutas (Relaciones Dirigidas)

#### Desde CA001 (Buenos Aires)

- → CA002: 60 min, $1500, 700 km
- → CA003: 35 min, $800, 300 km
- → CA005: 70 min, $1200, 500 km

#### Desde CA002 (Córdoba)

- → CA001: 60 min, $1500, 700 km
- → CA003: 50 min, $1000, 400 km
- → CA004: 90 min, $2000, 600 km
- → CA005: 45 min, $900, 350 km

#### Desde CA003 (Rosario)

- → CA001: 35 min, $800, 300 km
- → CA002: 50 min, $1000, 400 km
- → CA005: 40 min, $700, 200 km

#### Desde CA004 (Mendoza)

- → CA002: 90 min, $2000, 600 km
- → CA006: 120 min, $2500, 800 km

#### Desde CA005 (Santa Fe)

- → CA001: 70 min, $1200, 500 km
- → CA002: 45 min, $900, 350 km
- → CA003: 40 min, $700, 200 km

#### Desde CA006 (Neuquén)

- → CA004: 120 min, $2500, 800 km

---

## 🔍 Queries de Verificación

### Ver todos los centros

```cypher
MATCH (c:Centro)
RETURN c.id, c.nombre, c.ciudad, c.capacidad
ORDER BY c.id;
```

### Ver todas las rutas

```cypher
MATCH (origen:Centro)-[r:RUTA]->(destino:Centro)
RETURN origen.id, destino.id, r.tiempoMin, r.costo, r.distKm
ORDER BY origen.id, destino.id;
```

### Contar centros y rutas

```cypher
MATCH (c:Centro)
WITH count(c) as totalCentros
MATCH ()-[r:RUTA]->()
RETURN totalCentros, count(r) as totalRutas;
```

### Ver grafo completo

```cypher
MATCH (c:Centro)-[r:RUTA]->(d:Centro)
RETURN c, r, d;
```

---

## 🗑️ Limpiar Base de Datos

Si necesitas borrar todo y empezar de nuevo:

```cypher
MATCH (n) DETACH DELETE n;
```

⚠️ **ADVERTENCIA:** Esto eliminará TODOS los nodos y relaciones de tu base de datos.

---

## 🐛 Solución de Problemas

### Problema: "No hay datos cargados"

**Solución:** Ejecuta el script `init-neo4j-completo.cypher` en Neo4j Browser

### Problema: "Centro origen o destino no encontrado"

**Solución:** Verifica que los IDs sean CA001-CA006 (no A-F)

### Problema: "rutas: []" en la consola

**Solución:** Las relaciones no se crearon. Ejecuta nuevamente el script completo.

### Problema: "Error de conexión a Neo4j"

**Solución:**

1. Verifica tu archivo `application.yml`
2. Asegúrate de que la instancia de Neo4j Aura esté activa
3. Verifica usuario/contraseña

---

## ✅ Checklist de Verificación

- [ ] Neo4j Aura está activo
- [ ] Script ejecutado sin errores
- [ ] 6 centros creados (CA001-CA006)
- [ ] 17 rutas creadas
- [ ] Aplicación Spring Boot corriendo
- [ ] Centros visibles en la tabla del frontend
- [ ] Columna "Rutas" muestra números > 0
- [ ] Algoritmos de envíos funcionan correctamente

---

## 📞 Contacto

Si sigues teniendo problemas, verifica:

1. Logs de la aplicación Spring Boot
2. Logs del navegador (F12 → Console)
3. Query history en Neo4j Browser
