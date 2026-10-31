# Laboratorio 05 - Despliegue

Diseño de Software - UTEC

| | Pregunta 1 | Pregunta 2 |
|---|---|---|
| App | Champions Predictor | Postea (marketing para empresas) |
| Carpeta | [`champions/`](champions) | [`marketing/`](marketing) |
| Imagen | `ghcr.io/kaloslazo/champions-predictor` | `ghcr.io/kaloslazo/marketing-app` |
| Documentacion | [arquitectura y flujo de datos](docs/pregunta1-arquitectura.md) | [arquitectura y flujo](docs/pregunta2-arquitectura.md) |

Despliegue en AWS: [docs/despliegue.md](docs/despliegue.md)

## Stack

- Java 21 + Spring Boot 4 (Web MVC, Data JPA, Validation, Thymeleaf)
- PostgreSQL 16
- Docker + Docker Compose
- GitHub Actions -> GitHub Container Registry
- AWS EC2 + Caddy (HTTPS)
- Pregunta 2: SDK de Anthropic para Java (guiones), Java2D (fotos), ffmpeg (video), Instagram Graph API

## Estructura

```
.
├── champions/          # Pregunta 1
├── marketing/          # Pregunta 2
├── deploy/             # compose de produccion, Caddyfile, script de la instancia
├── docs/               # arquitectura, flujo de datos y despliegue
└── .github/workflows/  # build y push de imagenes
```

## Requisitos

- Docker y Docker Compose
- (opcional, para correr sin Docker) JDK 21 y ffmpeg

## Instalacion y ejecucion

### Con Docker (recomendado)

Cada app trae su `docker-compose.yml` con la app y su base de datos. Las variables se pueden pasar
en la linea de comando o en un archivo `.env` dentro de la carpeta de la app.

**Pregunta 1**

```bash
cd champions
POSTGRES_PASSWORD=algo_seguro docker compose up -d --build
# http://localhost:8080
```

Variables:

| Variable | Obligatoria | Descripcion |
|---|---|---|
| `POSTGRES_PASSWORD` | si | Clave de la base de datos |
| `FOOTBALL_DATA_TOKEN` | no | Token gratis de [football-data.org](https://www.football-data.org/client/register). Sin token usa datos simulados |
| `APP_PORT` | no | Puerto local (8080 por defecto) |

**Pregunta 2**

```bash
cd marketing
POSTGRES_PASSWORD=algo_seguro docker compose up -d --build
# http://localhost:8081
```

Variables:

| Variable | Obligatoria | Descripcion |
|---|---|---|
| `POSTGRES_PASSWORD` | si | Clave de la base de datos |
| `ANTHROPIC_API_KEY` | no | Guiones generados con Claude a partir de la foto. Sin key usa plantillas |
| `INSTAGRAM_USER_ID` / `INSTAGRAM_TOKEN` | no | Publicacion real en Instagram. Sin esto la publicacion es simulada |
| `PUBLIC_BASE_URL` | no | URL publica de la app (necesaria para publicar en Instagram) |
| `APP_PORT` | no | Puerto local (8081 por defecto) |

Para detener: `docker compose down` (los datos quedan en los volumenes; `docker compose down -v` los borra).

### Sin Docker

```bash
cd champions && ./mvnw spring-boot:run      # usa H2 en memoria
cd marketing && ./mvnw spring-boot:run      # usa H2 en ./data, necesita ffmpeg en el PATH
```

### Tests

```bash
cd champions && ./mvnw test
cd marketing && ./mvnw test
```

## Imagenes

Cada push a `main` corre los tests, construye las dos imagenes y las publica en GHCR
([workflow](.github/workflows/images.yml)):

```bash
docker pull ghcr.io/kaloslazo/champions-predictor:latest
docker pull ghcr.io/kaloslazo/marketing-app:latest
```
