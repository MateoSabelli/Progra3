// ============================================
// SCRIPT DE INICIALIZACIÓN COMPLETA
// Gestor Logístico - Neo4j
// ============================================
// 1. LIMPIAR DATOS EXISTENTES
MATCH (n)
DETACH DELETE n;

// 2. CREAR CENTROS LOGÍSTICOS
CREATE
  (ca001:Centro
    {
      id: 'CA001',
      nombre: 'Centro Norte',
      ciudad: 'Buenos Aires',
      capacidad: 1000
    })
CREATE
  (ca002:Centro
    {id: 'CA002', nombre: 'Centro Sur', ciudad: 'Córdoba', capacidad: 800})
CREATE
  (ca003:Centro
    {id: 'CA003', nombre: 'Centro Este', ciudad: 'Rosario', capacidad: 600})
CREATE
  (ca004:Centro
    {id: 'CA004', nombre: 'Centro Oeste', ciudad: 'Mendoza', capacidad: 500})
CREATE
  (ca005:Centro
    {id: 'CA005', nombre: 'Centro Litoral', ciudad: 'Santa Fe', capacidad: 700})
CREATE
  (ca006:Centro
    {
      id: 'CA006',
      nombre: 'Centro Patagonia',
      ciudad: 'Neuquén',
      capacidad: 400
    });

// 3. CREAR RUTAS (RELACIONES DIRIGIDAS)

// Desde CA001 (Buenos Aires) hacia CA002, CA003, CA005
MATCH (origen:Centro {id: 'CA001'}), (destino:Centro {id: 'CA002'})
CREATE (origen)-[:RUTA {tiempoMin: 60, costo: 1500, distKm: 700}]->(destino);

MATCH (origen:Centro {id: 'CA001'}), (destino:Centro {id: 'CA003'})
CREATE (origen)-[:RUTA {tiempoMin: 35, costo: 800, distKm: 300}]->(destino);

MATCH (origen:Centro {id: 'CA001'}), (destino:Centro {id: 'CA005'})
CREATE (origen)-[:RUTA {tiempoMin: 70, costo: 1200, distKm: 500}]->(destino);

// Desde CA002 (Córdoba) hacia CA001, CA003, CA004, CA005
MATCH (origen:Centro {id: 'CA002'}), (destino:Centro {id: 'CA001'})
CREATE (origen)-[:RUTA {tiempoMin: 60, costo: 1500, distKm: 700}]->(destino);

MATCH (origen:Centro {id: 'CA002'}), (destino:Centro {id: 'CA003'})
CREATE (origen)-[:RUTA {tiempoMin: 50, costo: 1000, distKm: 400}]->(destino);

MATCH (origen:Centro {id: 'CA002'}), (destino:Centro {id: 'CA004'})
CREATE (origen)-[:RUTA {tiempoMin: 90, costo: 2000, distKm: 600}]->(destino);

MATCH (origen:Centro {id: 'CA002'}), (destino:Centro {id: 'CA005'})
CREATE (origen)-[:RUTA {tiempoMin: 45, costo: 900, distKm: 350}]->(destino);

// Desde CA003 (Rosario) hacia CA001, CA002, CA005
MATCH (origen:Centro {id: 'CA003'}), (destino:Centro {id: 'CA001'})
CREATE (origen)-[:RUTA {tiempoMin: 35, costo: 800, distKm: 300}]->(destino);

MATCH (origen:Centro {id: 'CA003'}), (destino:Centro {id: 'CA002'})
CREATE (origen)-[:RUTA {tiempoMin: 50, costo: 1000, distKm: 400}]->(destino);

MATCH (origen:Centro {id: 'CA003'}), (destino:Centro {id: 'CA005'})
CREATE (origen)-[:RUTA {tiempoMin: 40, costo: 700, distKm: 200}]->(destino);

// Desde CA004 (Mendoza) hacia CA002, CA006
MATCH (origen:Centro {id: 'CA004'}), (destino:Centro {id: 'CA002'})
CREATE (origen)-[:RUTA {tiempoMin: 90, costo: 2000, distKm: 600}]->(destino);

MATCH (origen:Centro {id: 'CA004'}), (destino:Centro {id: 'CA006'})
CREATE (origen)-[:RUTA {tiempoMin: 120, costo: 2500, distKm: 800}]->(destino);

// Desde CA005 (Santa Fe) hacia CA001, CA002, CA003
MATCH (origen:Centro {id: 'CA005'}), (destino:Centro {id: 'CA001'})
CREATE (origen)-[:RUTA {tiempoMin: 70, costo: 1200, distKm: 500}]->(destino);

MATCH (origen:Centro {id: 'CA005'}), (destino:Centro {id: 'CA002'})
CREATE (origen)-[:RUTA {tiempoMin: 45, costo: 900, distKm: 350}]->(destino);

MATCH (origen:Centro {id: 'CA005'}), (destino:Centro {id: 'CA003'})
CREATE (origen)-[:RUTA {tiempoMin: 40, costo: 700, distKm: 200}]->(destino);

// Desde CA006 (Neuquén) hacia CA004
MATCH (origen:Centro {id: 'CA006'}), (destino:Centro {id: 'CA004'})
CREATE (origen)-[:RUTA {tiempoMin: 120, costo: 2500, distKm: 800}]->(destino);

// 4. VERIFICAR CREACIÓN
MATCH (c:Centro)
RETURN
  c.id AS Centro,
  c.nombre AS Nombre,
  c.ciudad AS Ciudad,
  c.capacidad AS Capacidad
ORDER BY c.id;

// 5. VERIFICAR RUTAS
MATCH (origen:Centro)-[r:RUTA]->(destino:Centro)
RETURN
  origen.id AS Origen,
  destino.id AS Destino,
  r.tiempoMin AS Tiempo,
  r.costo AS Costo,
  r.distKm AS Distancia
ORDER BY origen.id, destino.id;