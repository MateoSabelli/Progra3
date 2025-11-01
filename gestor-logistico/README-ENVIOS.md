# 📦 Sistema de Envíos Logísticos

## Nuevas Funcionalidades

Se ha agregado una nueva pestaña **"📦 Envíos"** con algoritmos específicamente diseñados para el contexto logístico:

---

## 🚚 1. Ruta con Menos Transbordos (BFS)

**Problema que resuelve:** Minimizar el número de centros intermedios en una entrega.

**Algoritmo:** BFS (Breadth-First Search)

**Uso práctico:**

- Envíos de productos frágiles que requieren menos manipulación
- Reducir riesgo de pérdida o daño
- Menor tiempo de procesamiento en centros intermedios

**Endpoint:** `GET /envios/menos-transbordos?origen=CA001&destino=CA006`

**Ejemplo de respuesta:**

```json
{
  "tipo": "BFS - Menos Transbordos",
  "origen": "CA001",
  "destino": "CA006",
  "ruta": ["CA001", "CA002", "CA004", "CA006"],
  "transbordos": 3,
  "totales": {
    "tiempoTotal": 270,
    "costoTotal": 4200,
    "distanciaTotal": 1300
  }
}
```

---

## 🌐 2. Explorar Red de Distribución (DFS)

**Problema que resuelve:** Identificar qué centros son alcanzables desde un punto con limitación de saltos.

**Algoritmo:** DFS (Depth-First Search)

**Uso práctico:**

- Planificar rutas de distribución con vehículos de autonomía limitada
- Identificar cobertura de servicio desde un centro
- Análisis de conectividad de la red logística

**Endpoint:** `GET /envios/explorar-red?origen=CA001&profundidad=3`

**Ejemplo de respuesta:**

```json
{
  "tipo": "DFS - Exploración de Red",
  "origen": "CA001",
  "profundidadMaxima": 3,
  "centrosAlcanzables": ["CA001", "CA002", "CA003", "CA004", "CA005", "CA006"],
  "totalCentros": 6
}
```

---

## 🎯 3. Buscar Rutas con Restricciones (Backtracking)

**Problema que resuelve:** Encontrar TODAS las rutas posibles que cumplan restricciones específicas.

**Algoritmo:** Backtracking con poda

**Uso práctico:**

- Planificar envíos con presupuesto limitado
- Encontrar rutas alternativas en caso de bloqueos
- Comparar múltiples opciones de entrega
- Considerar restricciones de tiempo o distancia

**Endpoint:** `GET /envios/rutas-con-restricciones?origen=CA001&destino=CA006&metrica=costo&valorMaximo=3000&maxTransbordos=4`

**Parámetros:**

- `origen`: Centro de origen (requerido)
- `destino`: Centro de destino (requerido)
- `metrica`: `tiempoMin` | `costo` | `distKm` (default: `tiempoMin`)
- `valorMaximo`: Valor máximo permitido según la métrica (opcional)
- `maxTransbordos`: Número máximo de transbordos (opcional)

**Ejemplo de respuesta:**

```json
{
  "tipo": "Backtracking - Rutas con Restricciones",
  "origen": "CA001",
  "destino": "CA006",
  "metrica": "costo",
  "restriccionValor": 3000,
  "restriccionTransbordos": 4,
  "rutasEncontradas": 5,
  "rutas": [
    {
      "centros": ["CA001", "CA002", "CA004", "CA006"],
      "tiempoTotal": 270,
      "costoTotal": 2100,
      "distanciaTotal": 5000,
      "valorTotal": 2100
    },
    {
      "centros": ["CA001", "CA003", "CA002", "CA004", "CA006"],
      "tiempoTotal": 305,
      "costoTotal": 2500,
      "distanciaTotal": 5700,
      "valorTotal": 2500
    }
  ]
}
```

---

## ⚡ 4. Ruta Óptima (Dijkstra)

**Problema que resuelve:** Encontrar la ruta que minimiza una métrica específica (tiempo, costo o distancia).

**Algoritmo:** Dijkstra

**Uso práctico:**

- Entregas urgentes (minimizar tiempo)
- Optimización de costos operativos (minimizar costo)
- Reducir emisiones de CO2 (minimizar distancia)

**Endpoint:** `GET /envios/ruta-optima?origen=CA001&destino=CA006&metrica=tiempoMin`

**Ejemplo de respuesta:**

```json
{
  "tipo": "Dijkstra - Ruta Óptima",
  "origen": "CA001",
  "destino": "CA006",
  "metrica": "tiempoMin",
  "valorOptimo": 270,
  "ruta": ["CA001", "CA002", "CA004", "CA006"],
  "transbordos": 3,
  "totales": {
    "tiempoTotal": 270,
    "costoTotal": 2100,
    "distanciaTotal": 5000
  }
}
```

---

## 🔄 Comparación de Algoritmos

| Algoritmo        | Caso de Uso            | Complejidad            | ¿Encuentra todas las rutas?    |
| ---------------- | ---------------------- | ---------------------- | ------------------------------ |
| **BFS**          | Menos transbordos      | O(V+E)                 | No, solo la primera encontrada |
| **DFS**          | Exploración de red     | O(V+E)                 | No, explora en profundidad     |
| **Backtracking** | Múltiples alternativas | Exponencial (con poda) | ✅ Sí, con restricciones       |
| **Dijkstra**     | Ruta óptima única      | O((V+E)log V)          | No, solo la óptima             |

---

## 📊 Métricas Disponibles

Todos los algoritmos pueden optimizar según:

- **`tiempoMin`**: Tiempo en minutos (ideal para entregas urgentes)
- **`costo`**: Costo en pesos (ideal para optimización financiera)
- **`distKm`**: Distancia en kilómetros (ideal para eficiencia de combustible)

---

## 🎯 Casos de Uso Reales

### Caso 1: Envío Urgente de Medicamentos

```
Usar: Ruta Óptima con métrica=tiempoMin
Resultado: Entrega más rápida posible
```

### Caso 2: Paquete Frágil

```
Usar: Menos Transbordos
Resultado: Menor manipulación, menor riesgo de daño
```

### Caso 3: Presupuesto Limitado

```
Usar: Rutas con Restricciones con valorMaximo=1500 y metrica=costo
Resultado: Todas las opciones dentro del presupuesto
```

### Caso 4: Planificación de Cobertura

```
Usar: Explorar Red con profundidad=2
Resultado: Qué centros puedo alcanzar con máximo 2 saltos
```

---

## 🚀 Cómo Usar

1. **Inicializa la base de datos** desde la pestaña "Centros"
2. **Carga los centros** para poblar los selectores
3. Ve a la pestaña **"📦 Envíos"**
4. Selecciona el algoritmo según tu necesidad
5. Configura origen, destino y restricciones
6. Haz clic en el botón correspondiente
7. Analiza los resultados en formato JSON

---

## 🛠️ Arquitectura Técnica

### Backend

- **`EnvioService.java`**: Lógica de negocio con algoritmos adaptados
- **`EnvioController.java`**: Endpoints REST para cada algoritmo
- **Spring Boot 3.5.6** + **Neo4j** (base de datos de grafos)

### Frontend

- HTML5 + Vanilla JavaScript
- Interfaz con pestañas para organizar funcionalidades
- Selectores auto-populados con centros disponibles
- Resultados en formato JSON legible

---

## 📌 Notas Importantes

- Los algoritmos de **grafos** originales siguen disponibles en la pestaña "Grafos"
- Los nuevos algoritmos de **envíos** están optimizados para el contexto logístico
- El **Backtracking** incluye poda para mejorar performance
- Todas las respuestas incluyen totales de tiempo, costo y distancia

---

## 🔮 Próximas Mejoras

- [ ] Agregar visualización gráfica de rutas en mapa
- [ ] Exportar resultados a CSV/Excel
- [ ] Considerar capacidad de vehículos
- [ ] Integrar restricciones de horarios
- [ ] Algoritmo genético para TSP con múltiples entregas
