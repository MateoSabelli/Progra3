// Script para inicializar Neo4j con datos de ejemplo
// Puedes ejecutar esto directamente en Neo4j Browser (http://localhost:7474)
// 1. Limpiar todos los datos existentes
MATCH (n)
DETACH DELETE n;

// 2. Crear los centros logísticos
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

// 3. Crear las rutas (relaciones con propiedades)
// Desde Buenos Aires (CA001)
MATCH (a:Centro {id: 'CA001'}), (b:Centro {id: 'CA002'})
CREATE (a)-[:RUTA {tiempoMin: 60, costo: 1500, distKm: 700}]->(b);

MATCH (a:Centro {id: 'CA001'}), (b:Centro {id: 'CA003'})
CREATE (a)-[:RUTA {tiempoMin: 35, costo: 800, distKm: 300}]->(b);

MATCH (a:Centro {id: 'CA001'}), (b:Centro {id: 'CA005'})
CREATE (a)-[:RUTA {tiempoMin: 70, costo: 1200, distKm: 500}]->(b);

// Desde Córdoba (CA002)
MATCH (a:Centro {id: 'CA002'}), (b:Centro {id: 'CA001'})
CREATE (a)-[:RUTA {tiempoMin: 60, costo: 1500, distKm: 700}]->(b);

MATCH (a:Centro {id: 'CA002'}), (b:Centro {id: 'CA003'})
CREATE (a)-[:RUTA {tiempoMin: 50, costo: 1000, distKm: 400}]->(b);

MATCH (a:Centro {id: 'CA002'}), (b:Centro {id: 'CA004'})
CREATE (a)-[:RUTA {tiempoMin: 90, costo: 2000, distKm: 600}]->(b);

MATCH (a:Centro {id: 'CA002'}), (b:Centro {id: 'CA005'})
CREATE (a)-[:RUTA {tiempoMin: 45, costo: 900, distKm: 350}]->(b);

// Desde Rosario (CA003)
MATCH (a:Centro {id: 'CA003'}), (b:Centro {id: 'CA001'})
CREATE (a)-[:RUTA {tiempoMin: 35, costo: 800, distKm: 300}]->(b);

MATCH (a:Centro {id: 'CA003'}), (b:Centro {id: 'CA002'})
CREATE (a)-[:RUTA {tiempoMin: 50, costo: 1000, distKm: 400}]->(b);

MATCH (a:Centro {id: 'CA003'}), (b:Centro {id: 'CA005'})
CREATE (a)-[:RUTA {tiempoMin: 40, costo: 700, distKm: 200}]->(b);

// Desde Mendoza (CA004)
MATCH (a:Centro {id: 'CA004'}), (b:Centro {id: 'CA002'})
CREATE (a)-[:RUTA {tiempoMin: 90, costo: 2000, distKm: 600}]->(b);

MATCH (a:Centro {id: 'CA004'}), (b:Centro {id: 'CA006'})
CREATE (a)-[:RUTA {tiempoMin: 120, costo: 2500, distKm: 800}]->(b);

// Desde Santa Fe (CA005)
MATCH (a:Centro {id: 'CA005'}), (b:Centro {id: 'CA001'})
CREATE (a)-[:RUTA {tiempoMin: 70, costo: 1200, distKm: 500}]->(b);

MATCH (a:Centro {id: 'CA005'}), (b:Centro {id: 'CA002'})
CREATE (a)-[:RUTA {tiempoMin: 45, costo: 900, distKm: 350}]->(b);

MATCH (a:Centro {id: 'CA005'}), (b:Centro {id: 'CA003'})
CREATE (a)-[:RUTA {tiempoMin: 40, costo: 700, distKm: 200}]->(b);

// Desde Neuquén (CA006)
MATCH (a:Centro {id: 'CA006'}), (b:Centro {id: 'CA004'})
CREATE (a)-[:RUTA {tiempoMin: 120, costo: 2500, distKm: 800}]->(b);

// 4. Verificar la creación
MATCH (c:Centro)
RETURN count(c) AS TotalCentros;
MATCH ()-[r:RUTA]->()
RETURN count(r) AS TotalRutas;

// 5. Ver el grafo completo
MATCH (c:Centro)-[r:RUTA]->(d:Centro)
RETURN c, r, d;