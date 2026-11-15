# TP Integrador · DAW1 — Sistema de venta de entradas para eventos

**Alumno/a:** Bacur, Betsabe · **Legajo:** 510017
**Enunciado elegido:** eventos


## Descripción

API REST para una productora de eventos. Permite administrar eventos, asistentes y entradas, y vende entradas sin superar la capacidad de cada evento. Está pensada para el personal de la productora que gestiona las ventas.

## Cómo ejecutarlo

Requisitos: Java 21 (Maven viene incluido con `mvnw`).

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080/api`. La base H2 se guarda en `data/` (no se sube al repositorio).
Consola H2: `http://localhost:8080/h2-console`.

## Modelo

| Entidad | Atributos principales | Relaciones |
|---|---|---|
| Evento | id, nombre, fecha, capacidad, precio | 1 a N con Entrada |
| Asistente | id, nombre, email | 1 a N con Entrada |
| Entrada | id, estado, codigo, fechaReserva, importePagado | N a 1 con Evento y Asistente |

Estados de Entrada: `RESERVADA → PAGADA | ANULADA`, `PAGADA → USADA | ANULADA`. `USADA` y `ANULADA` son finales.

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | /api/eventos | Lista eventos (filtro opcional `?nombre=`) | 200 |
| GET | /api/eventos/{id} | Busca un evento por id | 200, 400, 404 |
| POST | /api/eventos | Crea un evento | 201, 400 |
| PUT | /api/eventos/{id} | Modifica un evento | 200, 400, 404 |
| DELETE | /api/eventos/{id} | Borra un evento (si no tiene entradas) | 204, 404, 409 |
| GET | /api/eventos/{id}/entradas | Entradas de un evento | 200, 404 |
| GET | /api/eventos/{id}/ocupacion | Vendidas, disponibles y porcentaje ocupado | 200, 404 |
| GET | /api/asistentes | Lista asistentes | 200 |
| POST | /api/asistentes | Crea un asistente | 201, 400 |
| GET | /api/entradas | Lista entradas | 200 |
| GET | /api/entradas/{id} | Busca una entrada por id | 200, 400, 404 |
| POST | /api/entradas | Reserva una entrada | 201, 400, 404, 409 |
| PATCH | /api/entradas/{id}/estado | Cambia el estado de la entrada | 200, 400, 404, 409 |
| POST | /api/entradas/validar | Valida un código y pasa la entrada a USADA | 200, 400, 404, 409 |
| PATCH | /api/entradas/{id}/transferir | Transfiere una entrada pagada a otro asistente | 200, 400, 404, 409 |

## Reglas de negocio

1. No se venden más entradas no anuladas que la capacidad del evento (409).
2. Un asistente no puede tener dos entradas activas para el mismo evento (409).
3. Un evento que tiene entradas no se puede borrar (409).
4. Las transiciones de estado inválidas devuelven 409.

## Extras implementados

- **Código de entrada:** cada entrada recibe un `codigo` único (UUID) y `POST /api/entradas/validar` la pasa a USADA.
- **Preventa:** las entradas pagadas más de 7 días antes del evento cuestan un 20 % menos; el importe queda en `importePagado`.
- **Ocupación:** `GET /api/eventos/{id}/ocupacion`.
- **Transferencia:** `PATCH /api/entradas/{id}/transferir`.
- **Anulación automática:** las entradas RESERVADAS que no se pagan en 48 h se anulan solas.

## Pruebas

En `requests.http` están las peticiones de ejemplo (se ejecutan desde IntelliJ).