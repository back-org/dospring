# dospring — Enterprise V4+ (Spring Boot)

Projet Spring Boot refait en **version “Enterprise V4+”** : performance + sécurité + bonnes pratiques.

## Stack
- Java **21 LTS**
- Spring Boot **3.5.4**
- Spring Security 6 (JWT stateless)
- PostgreSQL + Flyway (prod)
- H2 (dev)
- Bucket4j (rate limiting)
- Redis (rate limiting distribué, optionnel)
- OpenAPI / Swagger UI
- Actuator (observabilité)

## Démarrage rapide (DEV)
```bash
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Démarrage (PROD via Docker)
```bash
docker compose up --build
```

Par défaut, le `docker-compose.yml` démarre aussi **Redis** et active le rate limiting distribué.

## Auth API (V4)

### Register
`POST /api/auth/register`
```json
{ "username": "user1", "email": "user1@mail.com", "password": "StrongPwd#1234" }
```

### Login
`POST /api/auth/login` (header optionnel `X-Device-Id`)
```json
{ "username": "user1", "password": "StrongPwd#1234" }
```
Réponse: `accessToken` + `refreshToken`

### Refresh (rotation)
`POST /api/auth/refresh`
```json
{ "refreshToken": "<token>", "deviceId": "laptop-1" }
```

### Logout
`POST /api/auth/logout`
```json
{ "refreshToken": "<token>" }
```

### Logout all devices
`POST /api/auth/logout-all` (requiert `Authorization: Bearer <accessToken>`)

### Logout one device
`POST /api/auth/logout-device`
```json
{ "deviceId": "laptop-1" }
```

### Sessions actives (V4+)
`GET /api/auth/sessions` (requiert `Authorization: Bearer <accessToken>`)

Retourne la liste des sessions actives (sans jamais retourner le refresh token).

### Révoquer une session (V4+)
`POST /api/auth/revoke-session` (requiert `Authorization: Bearer <accessToken>`)
```json
{ "sessionId": 123 }
```

### Changer le mot de passe (V4+)
`POST /api/auth/change-password` (requiert `Authorization: Bearer <accessToken>`)
```json
{ "currentPassword": "OldPwd#1234", "newPassword": "NewStrongPwd#5678" }
```

Comportement sécurité : **révoque toutes les sessions** (re-login requis partout).

## Sécurité (ce qui est mis en place)
- JWT **Access** seulement en JWT
- **Refresh tokens** opaques stockés **hashés SHA-256**, avec **rotation** + **revocation**
- Multi-device: `deviceId`, user-agent, IP
- Brute-force mitigation: lock après N échecs (configurable)
- Password policy minimale (12 + upper/lower/digit/special + blocklist)
- Password history: blocage réutilisation (5 dernières)
- Chiffrement "at rest" (AES-256-GCM) pour champs sensibles (optionnel via `APP_CRYPTO_KEY_BASE64`)
- Audit JPA: created_at/updated_at/created_by/updated_by
- Headers de sécurité (HSTS, etc.)

## Rate limiting distribué (Redis)

Le filtre `RateLimitFilter` fonctionne :
- **Sans Redis** : buckets en mémoire (dev)
- **Avec Redis** : buckets partagés entre instances (prod)

Activer Redis:
```properties
app.redis.enabled=true
app.redis.host=localhost
app.redis.port=6379
```

## Chiffrement des colonnes (AES-256-GCM)

Activer le chiffrement en prod en fournissant une clé AES 256 bits:
```bash
export APP_CRYPTO_KEY_BASE64="$(openssl rand -base64 32)"
```

Sans clé, le convertisseur est **no-op** (pratique pour dev). 

## Bonnes pratiques
- `open-in-view: false`
- CORS centralisé via env `APP_CORS_ORIGINS`
- Flyway migrations en prod (`src/main/resources/db/migration`)

## Idées d’extensions (si tu veux version “banque++”)
- Support MySQL complet sur migrations Flyway (adaptation V2)
- Ajout de scoring mot de passe (zxcvbn)
- Tests API complets (RestAssured + Testcontainers)
