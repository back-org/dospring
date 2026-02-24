# dospring — Enterprise V4 (Spring Boot)

Projet Spring Boot refait en **version “Enterprise V4”** : performance + sécurité + bonnes pratiques.

## Stack
- Java **21 LTS**
- Spring Boot **3.5.4**
- Spring Security 6 (JWT stateless)
- PostgreSQL + Flyway (prod)
- H2 (dev)
- Bucket4j (rate limiting)
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

## Sécurité (ce qui est mis en place)
- JWT **Access** seulement en JWT
- **Refresh tokens** opaques stockés **hashés SHA-256**, avec **rotation** + **revocation**
- Multi-device: `deviceId`, user-agent, IP
- Brute-force mitigation: lock après N échecs (configurable)
- Password policy minimale (12 + upper/lower/digit/special + blocklist)
- Audit JPA: created_at/updated_at/created_by/updated_by
- Headers de sécurité (HSTS, etc.)

## Bonnes pratiques
- `open-in-view: false`
- CORS centralisé via env `APP_CORS_ORIGINS`
- Flyway migrations en prod (`src/main/resources/db/migration`)

## TODO (si tu veux version “banque++”)
- bucket4j Redis (multi-instance)
- Refresh token “logout device only” + session list
- Mot de passe : dictionnaire + zxcvbn scoring
- Chiffrement champs sensibles (JPA AttributeConverter)
- Tests API complets (RestAssured)
