# Datum API - Sistema de Autenticación con Keycloak

API de autenticación desarrollada con Quarkus y Keycloak para el proyecto Datum.

## Requisitos

- Docker
- Docker Compose

**Eso es todo.** No necesitas tener instalado Java, Maven, ni nada más. Docker se encarga de todo.

## Inicio Rápido

### 1. Clonar el repositorio o descargar los archivos

Asegúrate de tener esta estructura:
```
datum-api/
├── docker-compose.yml
├── keycloak-config/
│   └── datum-realm.json
└── datum-keycloak-api/
    ├── Dockerfile
    ├── pom.xml
    └── src/
```

### 2. Levantar todos los servicios

Abre una terminal en la carpeta `datum-api` y ejecuta:

```bash
docker-compose up --build
```

Este comando:
- Descarga las imágenes necesarias (PostgreSQL, Keycloak)
- Compila tu API de Quarkus
- Levanta todos los servicios
- Configura automáticamente Keycloak con usuarios de prueba

**Espera entre 2-3 minutos** la primera vez mientras descarga todo.

### 3. Verificar que todo esté funcionando

Cuando veas estos mensajes, todo está listo:

```
datum-keycloak    | Running the server in development mode. DO NOT use this configuration in production.
datum-api         | Listening on: http://0.0.0.0:8082
```

## URLs de Acceso

- **API de Autenticación**: http://localhost:8082
- **Keycloak Admin Console**: http://localhost:8080
  - Usuario: `admin`
  - Contraseña: `admin`
- **OpenKM (Gestión Documental)**: http://localhost:8090/OpenKM
  - Usuario por defecto: `okmAdmin`
  - Contraseña por defecto: `admin`

## Endpoints de la API

### 1. Health Check
```bash
GET http://localhost:8082/auth/hello
```

Respuesta:
```
Hello, world!
```

### 2. Login
```bash
POST http://localhost:8082/auth/login
Content-Type: application/json

{
  "username": "dev",
  "password": "dev123!"
}
```

Respuesta exitosa:
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userInfo": {
    "id": "user_12345",
    "username": "caleb",
    "email": "caleb@example.com",
    "roles": ["basic", "user"]
  }
}
```

## Usuarios de Prueba

El sistema viene con 3 usuarios pre-configurados:

| Username   | Password     | Rol            | Email              |
|------------|--------------|----------------|--------------------|
| admin      | admin123!    | administrator  | admin@datum.com    |
| dev        | dev123!      | employee       | dev@datum.com      |
| finance    | finance123!  | finance        | finance@datum.com  |

## Probar desde el Frontend

Tu compañero de frontend debe usar esta URL base:

```javascript
const API_URL = "http://localhost:8082";

// Ejemplo de login
const response = await fetch(`${API_URL}/auth/login`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'dev',
    password: 'dev123!'
  })
});

const data = await response.json();
console.log(data.accessToken); // Token JWT para usar en otras peticiones
```

## Comandos Útiles

### Detener los servicios
```bash
docker-compose down
```

### Detener y borrar todos los datos (reset completo)
```bash
docker-compose down -v
```

### Ver logs de un servicio específico
```bash
docker-compose logs -f datum-api     # Logs de la API
docker-compose logs -f keycloak      # Logs de Keycloak
```

### Reconstruir la API después de cambios en el código
```bash
docker-compose up --build datum-api
```

### Ver servicios corriendo
```bash
docker-compose ps
```

## Problemas Comunes

### El puerto 8080 o 8082 ya está en uso
Detén el servicio que esté usando ese puerto o cambia los puertos en [docker-compose.yml](docker-compose.yml):

```yaml
ports:
  - "8083:8082"  # Cambiar el primer número
```

### Keycloak no inicia o da error de conexión
Espera un poco más. Keycloak tarda en arrancar la primera vez. Si después de 5 minutos no funciona:

```bash
docker-compose down -v
docker-compose up --build
```

### La API no se conecta a Keycloak
Verifica que Keycloak esté corriendo:
```bash
curl http://localhost:8080/health
```

## Servicios Incluidos

El docker-compose levanta automáticamente:

1. **PostgreSQL (Keycloak)** - Base de datos para Keycloak en puerto 5432
2. **Keycloak** - Servidor de autenticación en http://localhost:8080
3. **Datum API** - Tu API de Quarkus en http://localhost:8082
4. **PostgreSQL (OpenKM)** - Base de datos para OpenKM en puerto 5433
5. **OpenKM** - Sistema de gestión documental en http://localhost:8090/OpenKM

## Estructura del Proyecto

```
datum-api/
├── docker-compose.yml              # Orquestación de servicios
├── keycloak-config/
│   └── datum-realm.json           # Configuración de Keycloak (realm, usuarios, clientes)
└── datum-keycloak-api/
    ├── Dockerfile                 # Imagen de la API
    ├── pom.xml                    # Dependencias de Maven
    └── src/
        └── main/
            ├── java/
            │   └── com/datum/auth/
            │       ├── AuthResource.java      # Endpoint de login
            │       ├── KeycloakClient.java    # Cliente REST para Keycloak
            │       ├── LoginRequest.java
            │       ├── AuthResponse.java
            │       └── ...
            └── resources/
                └── application.properties     # Configuración de Quarkus
```

## Para los Compañeros del Equipo

Solo necesitan:

1. Tener Docker instalado
2. Clonar/descargar este proyecto
3. Ejecutar: `docker-compose up --build`
4. Usar la API en `http://localhost:8082`

**No necesitan instalar Java, Maven, ni configurar nada más.**

## Próximos Pasos

- [ ] Agregar endpoints protegidos que requieran autenticación
- [ ] Integrar con base de datos Oracle para datos de negocio
- [ ] Agregar refresh token endpoint
- [ ] Agregar logout endpoint

## Soporte

Si tienes problemas, revisa:
1. Que Docker esté corriendo: `docker ps`
2. Los logs: `docker-compose logs`
3. Que los puertos 8080 y 8082 estén libres

---

Desarrollado por el equipo Datum
