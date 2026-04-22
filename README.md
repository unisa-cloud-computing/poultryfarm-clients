# poultryfarm-clienti

Microservizio Spring Boot per la gestione clienti del SaaS PoultryFarm.

## Panoramica

Il servizio espone API REST per:
- creazione cliente
- ricerca clienti (paginata e non paginata)
- lettura cliente per id
- aggiornamento cliente
- cancellazione logica (soft delete)

Il progetto usa **multi-tenancy**: il tenant viene estratto dal JWT (`claim tenantId`) e usato per risolvere dinamicamente il database tenant tramite catalogo.

## Stack tecnologico

- Java 17
- Spring Boot 3.4.x
- Spring Web
- Spring Data JPA (Hibernate)
- SQL Server JDBC Driver
- Dapr Java SDK
- MapStruct
- Lombok
- Bean Validation

## Struttura progetto (principale)

- `src/main/java/com/poultryfarm/application/rest/ClienteRestController.java`  
  Controller REST con endpoint `/clienti`
- `src/main/java/com/poultryfarm/business/service/ClientService.java`  
  Logica applicativa
- `src/main/java/com/poultryfarm/persistence/entity/Cliente.java`  
  Entità JPA `CLIENTE`
- `src/main/java/com/poultryfarm/application/multitenancy/`  
  Componenti multi-tenant (`JwtTenantFilter`, `SchemaTenantResolver`, `SchemaMultiTenantConnectionProvider`, `CatalogRepository`)
- `src/main/resources/application.yml`  
  Config base app
- `src/main/resources/application-azure.yml`  
  Config Azure / datasource

## Requisiti

- JDK 17+
- Maven 3.9+
- Accesso a SQL Server/Azure SQL
- Managed Identity (in ambiente Azure) per autenticazione DB
- (Opzionale) runtime Dapr per endpoint di test metadati

## Configurazione

### Config base

In `application.yml`:
- `server.port: 8080`
- `server.servlet.context-path: /api/v1`
- nome app: `cliente-service`
- dialect Hibernate SQL Server

### Config Azure

In `application-azure.yml`:
- `app.datasource.server-name`: hostname SQL Server
- autoconfigurazione datasource Spring disabilitata (gestione custom multi-tenant)

> Nota: la risoluzione tenant avviene dal `tenantId` nel JWT (`Authorization: Bearer ...`).

## Avvio locale

```bash
mvn clean spring-boot:run
```

Build jar:

```bash
mvn clean package
java -jar target/poultryfarm-clienti-2.0.1-SNAPSHOT.jar
```

## Base URL

Con config attuale:
- `http://localhost:8080/api/v1`

## API

### 1) Test metadati Dapr

- **GET** `/clienti/test-dapr-meta`
- Ritorna l’ID istanza Dapr (se disponibile)

Esempio:

```bash
curl -X GET "http://localhost:8080/api/v1/clienti/test-dapr-meta" \
  -H "Authorization: Bearer <JWT_CON_CLAIM_tenantId>"
```

---

### 2) Cliente per ID

- **GET** `/clienti/{id}`
- `200 OK` con `Cliente`
- `404 Not Found` se non presente o eliminato logicamente

```bash
curl -X GET "http://localhost:8080/api/v1/clienti/1" \
  -H "Authorization: Bearer <JWT_CON_CLAIM_tenantId>"
```

---

### 3) Ricerca clienti

- **POST** `/clienti/ricerca`
- Query params opzionali:
    - `isPaginated` (default `false`)
    - `pageNumber` (default `0`, usato se paginato)
    - `pageSize` (default `10`, usato se paginato)
- Body: `SearchData`

Body esempio:

```json
{
  "nome": "Mario",
  "cognome": "Rossi",
  "indirizzo": "Roma"
}
```

Chiamata paginata:

```bash
curl -X POST "http://localhost:8080/api/v1/clienti/ricerca?isPaginated=true&pageNumber=0&pageSize=10" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_CON_CLAIM_tenantId>" \
  -d '{"nome":"Mario","cognome":"Rossi","indirizzo":"Roma"}'
```

---

### 4) Creazione cliente

- **POST** `/clienti`
- Body: `CreateDtoClient`
- `201 Created` se ok
- `400 Bad Request` con header `X-Error-Message` se codice fiscale non valido

Esempio body:

```json
{
  "nome": "Mario",
  "cognome": "Rossi",
  "cellulare": "+393331112233",
  "codiceFiscale": "RSSMRA80A01H501U",
  "email": "mario.rossi@example.com",
  "indirizzo": "Via Roma 10",
  "provincia": "RM",
  "comune": "Roma",
  "codiceIdentificativoAsl": "ASL123",
  "dataNascita": "1980-01-01"
}
```

```bash
curl -X POST "http://localhost:8080/api/v1/clienti" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_CON_CLAIM_tenantId>" \
  -d '{
    "nome":"Mario",
    "cognome":"Rossi",
    "cellulare":"+393331112233",
    "codiceFiscale":"RSSMRA80A01H501U",
    "email":"mario.rossi@example.com",
    "indirizzo":"Via Roma 10",
    "provincia":"RM",
    "comune":"Roma",
    "codiceIdentificativoAsl":"ASL123",
    "dataNascita":"1980-01-01"
  }'
```

---

### 5) Aggiornamento cliente

- **PUT** `/clienti`
- Body: `UpdateDtoClient` (campo `id` obbligatorio)
- `200 OK` se aggiornato
- `404 Not Found` se cliente non trovato

```bash
curl -X PUT "http://localhost:8080/api/v1/clienti" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_CON_CLAIM_tenantId>" \
  -d '{
    "id":1,
    "email":"nuova.email@example.com",
    "indirizzo":"Via Milano 25"
  }'
```

---

### 6) Cancellazione logica cliente

- **DELETE** `/clienti/{id}`
- `204 No Content` se eliminato logicamente (`eliminato=true`)
- `404 Not Found` se non trovato

```bash
curl -X DELETE "http://localhost:8080/api/v1/clienti/1" \
  -H "Authorization: Bearer <JWT_CON_CLAIM_tenantId>"
```

## Validazioni principali

Da DTO applicativi:
- `nome`, `cognome`, `cellulare`, `indirizzo`, `provincia`, `comune` obbligatori in creazione
- `cellulare` conforme regex italiana mobile (`+39` opzionale)
- `email` valida (se valorizzata)
- `id` obbligatorio in aggiornamento
- `codiceFiscale` validato con regex formato CF italiano

## Multi-tenancy (come funziona)

1. `JwtTenantFilter` legge `Authorization Bearer`
2. Estrae il claim `tenantId` dal payload JWT
3. Salva `tenantId` in `TenantContext` (`ThreadLocal`)
4. `SchemaTenantResolver` fornisce il tenant corrente a Hibernate
5. `SchemaMultiTenantConnectionProvider` risolve il DB del tenant via `CatalogRepository`
6. Viene usata una cache in memoria `tenantId -> DataSource`

## Note operative

- Se il JWT non contiene `tenantId`, la richiesta viene rifiutata (`401`).
- La firma JWT non viene verificata dal microservizio (delegata a gateway/APIM, come da commento nel filtro).
- Le query includono automaticamente il filtro su record non eliminati (`eliminato = false`) nelle ricerche gestite da `ClientSpecification`.

## Test

Attualmente non risultano test in `src/test/java/com/poultryfarm`.

## Licenza

Definire la licenza del progetto (es. MIT, Apache-2.0, Proprietary).
```