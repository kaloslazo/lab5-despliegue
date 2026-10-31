# Pregunta 1 - Champions Predictor

Aplicacion web para ver los partidos de la Champions League y registrar predicciones.
Hecha en Java 21 con Spring Boot 4, Thymeleaf y PostgreSQL.

## Arquitectura en 3 capas

```mermaid
flowchart TB
    user([Usuario / navegador])

    subgraph P[Capa de presentacion - web]
        MC[MatchController<br/>/matches, /matches/id]
        PC[PredictionController<br/>/predictions]
        AC[ApiController<br/>/api/matches, /api/predictions]
        V[Vistas Thymeleaf<br/>matches.html, match.html, predictions.html]
    end

    subgraph N[Capa de logica de negocio - service]
        MS[MatchService<br/>sincroniza y lista partidos]
        PS[PredictionService<br/>valida y registra predicciones]
        SL[StartupLoader<br/>carga inicial al arrancar]
    end

    subgraph D[Capa de acceso a datos - repository + client]
        MR[(MatchRepository)]
        PR[(PredictionRepository)]
        SRC{{MatchSource}}
        FD[FootballDataClient]
        SM[SampleMatchSource<br/>JSON simulado]
    end

    DB[(PostgreSQL<br/>matches, predictions)]
    API[[football-data.org<br/>/v4/competitions/CL/matches]]

    user --> MC & PC & AC
    MC & PC --> V
    MC --> MS & PS
    PC --> PS
    AC --> MS & PS
    SL --> MS
    MS --> SRC
    MS --> MR
    PS --> PR & MR
    SRC -.token configurado.-> FD --> API
    SRC -.sin token.-> SM
    MR & PR --> DB
```

| Capa | Paquete | Responsabilidad |
|---|---|---|
| Presentacion | `pe.edu.utec.champions.web` | Controladores MVC, formularios, validacion de campos, vistas y endpoints JSON |
| Negocio | `pe.edu.utec.champions.service` | Reglas: no predecir partidos ya empezados, marcador coherente con el resultado, calculo de aciertos |
| Datos | `pe.edu.utec.champions.repository`, `client`, `model` | Entidades JPA, repositorios Spring Data, cliente HTTP de la API externa |

Cada capa solo conoce a la de abajo. Los controladores nunca tocan los repositorios directamente
y los servicios no saben nada de HTTP ni de las vistas.

## Modelo de datos

```mermaid
erDiagram
    MATCHES ||--o{ PREDICTIONS : tiene
    MATCHES {
        bigint id PK "id de football-data"
        timestamp utc_date
        varchar status
        varchar stage
        int matchday
        varchar home_team
        varchar away_team
        varchar home_crest
        varchar away_crest
        int home_score
        int away_score
    }
    PREDICTIONS {
        bigint id PK
        bigint match_id FK
        varchar author
        varchar outcome "HOME, DRAW, AWAY"
        int home_goals
        int away_goals
        timestamp created_at
    }
```

## Flujo de datos

### 1. Carga inicial (requerimientos 1 y 2)

```mermaid
sequenceDiagram
    participant Boot as Spring Boot
    participant SL as StartupLoader
    participant MS as MatchService
    participant SRC as MatchSource
    participant API as football-data.org
    participant DB as PostgreSQL

    Boot->>SL: run() al terminar de arrancar
    SL->>MS: syncMatches()
    MS->>SRC: fetchMatches()
    alt hay FOOTBALL_DATA_TOKEN
        SRC->>API: GET /v4/competitions/CL/matches (X-Auth-Token)
        API-->>SRC: JSON con los partidos
    else sin token
        SRC->>SRC: lee data/cl-matches.json
    end
    SRC-->>MS: List<ApiMatch>
    MS->>MS: convierte ApiMatch -> Match
    MS->>DB: saveAll (upsert por id)
```

Se usa el `id` que trae la API como llave primaria, asi al reiniciar la app los partidos se actualizan
(marcadores, estado) en vez de duplicarse. Si la API falla, la app arranca igual con lo que ya tenia la base de datos.

### 2. Listado de partidos (requerimiento 3)

`GET /matches?stage=...` -> `MatchController.list` -> `MatchService.list(stage)` -> `MatchRepository.findAllByOrderByUtcDateAsc()` -> vista `matches.html`.

### 3. Registrar prediccion (requerimientos 4 y 5)

```mermaid
sequenceDiagram
    actor U as Usuario
    participant C as MatchController
    participant PS as PredictionService
    participant DB as PostgreSQL

    U->>C: POST /matches/{id}/predictions (nombre, resultado, goles)
    C->>C: @Valid PredictionForm (campos obligatorios, 0..20)
    C->>PS: register(matchId, autor, outcome, local, visita)
    PS->>DB: findById(matchId)
    PS->>PS: partido no empezado? marcador coincide con outcome?
    alt valido
        PS->>DB: save(Prediction) con FK al partido
        C-->>U: redirect /matches/{id} + mensaje
    else invalido
        C-->>U: misma vista con el error
    end
```

### 4. Consultar predicciones (requerimiento 6)

`GET /predictions` muestra todas las predicciones con su partido. Cuando el partido termina
se calcula si acerto el resultado o el marcador exacto. Lo mismo en JSON en `GET /api/predictions`.

## Endpoints

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/matches` | Listado de partidos, filtro opcional `?stage=` |
| GET | `/matches/{id}` | Detalle, formulario y predicciones del partido |
| POST | `/matches/{id}/predictions` | Registra una prediccion |
| GET | `/predictions` | Todas las predicciones |
| GET | `/api/matches` | Partidos en JSON |
| GET | `/api/predictions` | Predicciones en JSON |
