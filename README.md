Proyecto del Parcial 3 de **Desarrollo Fullstack II (DSY1104)**, Duoc UC. Es la API REST de la tienda **Sonido Vivo**, desarrollada con **Spring Boot**, **MySQL 8 en Docker** con persistencia real, **Flyway**, **Spring Security + JWT** y documentación interactiva en **Swagger UI**.

- **Repositorio Frontend (React):** `https://github.com/benjav892/sonido-vivo-react`
- **URL Base API (EC2):** `http://<IP_EC2_BACKEND>:8080/api`
- **Documentación Swagger UI (EC2):** `http://<IP_EC2_BACKEND>:8080/api/swagger-ui.html`

## Integrantes
- Benjamin Vasquez (benjam.vasquezc@duocuc.cl)
- Camilo Vera (c.vera2@duocuc.cl)
- Sebastian Toro (se.torof@duocuc.cl)

## Caso
Sonido Vivo

## Descripción del caso
Sonido Vivo es una tienda dedicada a la venta de instrumentos musicales y equipos de sonido.
Este backend provee la capa de persistencia relacional, autenticación basada en tokens JWT y control de acceso por roles (`Administrador`, `Vendedor`, `Cliente`) consumida por la aplicación React.

## Estado del proyecto

| Módulo / Funcionalidad | Estado | Detalle técnico |
|---|---|---|
| Base de datos MySQL y migraciones | Implementado | Contenedor `mysql:8.0` con volumen persistente `mysql_data` y migraciones Flyway (`V1`–`V3`) |
| Registro de usuarios | Implementado | `POST /api/v1/auth/register` con hash BCrypt, validación de campos y rol `Cliente` asignado por el servidor |
| Inicio de sesión y JWT | Implementado | `POST /api/v1/auth/login` con firma `HS256` y claims `sub`, `rol`, `iss`, `aud`, `iat` y `exp` |
| Perfil del usuario autenticado | Implementado | `GET /api/v1/auth/me` valida el Bearer JWT y lee el ID desde el token |
| CRUD de usuarios | Implementado | `GET`, `GET /{id}`, `POST`, `PUT /{id}` (incluye cambio de rol) y `DELETE /{id}` solo para `Administrador` |
| Manejo global de errores y CORS | Implementado | `GlobalExceptionHandler` con respuestas `{ mensaje, detalles }` y orígenes vía `CORS_ORIGINS` |
| Documentación Swagger / OpenAPI | Implementado | Interfaz en `/api/swagger-ui.html` con esquema `bearerAuth` (botón **Authorize**) |
| Módulos de productos, categorías y pedidos | Pendiente | Planificados para la siguiente iteración de integración |

## Endpoints principales

Todas las rutas se sirven bajo el prefijo `server.servlet.context-path=/api`:

| Método | Ruta | Acceso | Respuestas principales | Descripción |
|---|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Público | `201`, `400`, `409` | Registra un cliente nuevo (el rol lo asigna el servidor) |
| `POST` | `/api/v1/auth/login` | Público | `200`, `400`, `401` | Valida credenciales y entrega `accessToken` JWT |
| `GET` | `/api/v1/auth/me` | Autenticado | `200`, `401` | Devuelve los datos del usuario autenticado |
| `GET` | `/api/v1/usuarios` | `Administrador` | `200`, `401`, `403` | Lista todos los usuarios registrados |
| `GET` | `/api/v1/usuarios/{id}` | `Administrador` | `200`, `401`, `403`, `404` | Consulta un usuario por su ID |
| `POST` | `/api/v1/usuarios` | `Administrador` | `201`, `400`, `401`, `403`, `409` | Crea un usuario asignando su rol |
| `PUT` | `/api/v1/usuarios/{id}` | `Administrador` | `200`, `400`, `401`, `403`, `404` | Actualiza datos personales y/o rol de un usuario |
| `DELETE` | `/api/v1/usuarios/{id}` | `Administrador` | `204`, `401`, `403`, `404`, `409` | Elimina un usuario por ID (impide auto-eliminación del administrador) |

## Estructura del proyecto

El proyecto sigue una arquitectura por capas dentro del paquete `cl.duoc.sonidovivo_backend`: configuración, controladores, DTOs, seguridad JWT y servicios bajo `auth/`, junto con las entidades JPA en `model/`, repositorios en `repository/` y migraciones SQL versionadas en `resources/db/migration/`.

```
sonidovivo-react-backend/
├─ .mvn/
│  └─ wrapper/
│     └─ maven-wrapper.properties
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  └─ cl/
│  │  │     └─ duoc/
│  │  │        └─ sonidovivo_backend/
│  │  │           ├─ auth/
│  │  │           │  ├─ config/
│  │  │           │  │  ├─ DataSeeder.java
│  │  │           │  │  ├─ GlobalExceptionHandler.java
│  │  │           │  │  ├─ OpenApiConfig.java
│  │  │           │  │  └─ SecurityConfig.java
│  │  │           │  ├─ controller/
│  │  │           │  │  ├─ AuthController.java
│  │  │           │  │  └─ UsuarioController.java
│  │  │           │  ├─ dto/
│  │  │           │  │  ├─ AuthResponse.java
│  │  │           │  │  ├─ LoginRequest.java
│  │  │           │  │  ├─ RegistroRequest.java
│  │  │           │  │  ├─ UsuarioCreateRequest.java
│  │  │           │  │  ├─ UsuarioResponse.java
│  │  │           │  │  └─ UsuarioUpdateRequest.java
│  │  │           │  ├─ security/
│  │  │           │  │  ├─ JwtAuthenticationFilter.java
│  │  │           │  │  └─ JwtService.java
│  │  │           │  └─ service/
│  │  │           │     ├─ AuthService.java
│  │  │           │     └─ UsuarioService.java
│  │  │           ├─ model/
│  │  │           │  ├─ Rol.java
│  │  │           │  └─ Usuario.java
│  │  │           ├─ repository/
│  │  │           │  ├─ RolRepository.java
│  │  │           │  └─ UsuarioRepository.java
│  │  │           └─ SonidovivoBackendApplication.java
│  │  └─ resources/
│  │     ├─ db/
│  │     │  └─ migration/
│  │     │     ├─ V1__create_table_rol.sql
│  │     │     ├─ V2__create_table_usuario.sql
│  │     │     └─ V3__insert_roles.sql
│  │     ├─ static/
│  │     ├─ templates/
│  │     └─ application.properties
│  └─ test/
│     └─ java/
│        └─ cl/
│           └─ duoc/
│              └─ sonidovivo_backend/
│                 └─ SonidovivoBackendApplicationTests.java
├─ target/
│  ├─ classes/
│  │  ├─ cl/
│  │  │  └─ duoc/
│  │  │     └─ sonidovivo_backend/
│  │  │        ├─ auth/
│  │  │        │  ├─ config/
│  │  │        │  │  ├─ DataSeeder.class
│  │  │        │  │  ├─ GlobalExceptionHandler.class
│  │  │        │  │  ├─ GlobalExceptionHandler$ErrorResponse.class
│  │  │        │  │  ├─ OpenApiConfig.class
│  │  │        │  │  └─ SecurityConfig.class
│  │  │        │  ├─ controller/
│  │  │        │  │  ├─ AuthController.class
│  │  │        │  │  └─ UsuarioController.class
│  │  │        │  ├─ dto/
│  │  │        │  │  ├─ AuthResponse.class
│  │  │        │  │  ├─ AuthResponse$AuthResponseBuilder.class
│  │  │        │  │  ├─ LoginRequest.class
│  │  │        │  │  ├─ RegistroRequest.class
│  │  │        │  │  ├─ UsuarioCreateRequest.class
│  │  │        │  │  ├─ UsuarioResponse.class
│  │  │        │  │  ├─ UsuarioResponse$UsuarioResponseBuilder.class
│  │  │        │  │  └─ UsuarioUpdateRequest.class
│  │  │        │  ├─ security/
│  │  │        │  │  ├─ JwtAuthenticationFilter.class
│  │  │        │  │  └─ JwtService.class
│  │  │        │  └─ service/
│  │  │        │     ├─ AuthService.class
│  │  │        │     └─ UsuarioService.class
│  │  │        ├─ model/
│  │  │        │  ├─ Rol.class
│  │  │        │  ├─ Rol$RolBuilder.class
│  │  │        │  ├─ Usuario.class
│  │  │        │  └─ Usuario$UsuarioBuilder.class
│  │  │        ├─ repository/
│  │  │        │  ├─ RolRepository.class
│  │  │        │  └─ UsuarioRepository.class
│  │  │        └─ SonidovivoBackendApplication.class
│  │  ├─ db/
│  │  │  └─ migration/
│  │  │     ├─ V1__create_table_rol.sql
│  │  │     ├─ V2__create_table_usuario.sql
│  │  │     └─ V3__insert_roles.sql
│  │  └─ application.properties
│  ├─ generated-sources/
│  │  └─ annotations/
│  ├─ generated-test-sources/
│  │  └─ test-annotations/
│  ├─ maven-status/
│  │  └─ maven-compiler-plugin/
│  │     ├─ compile/
│  │     │  └─ default-compile/
│  │     │     ├─ createdFiles.lst
│  │     │     └─ inputFiles.lst
│  │     └─ testCompile/
│  │        └─ default-testCompile/
│  │           ├─ createdFiles.lst
│  │           └─ inputFiles.lst
│  └─ test-classes/
│     └─ cl/
│        └─ duoc/
│           └─ sonidovivo_backend/
│              └─ SonidovivoBackendApplicationTests.class
├─ .env
├─ .env.example
├─ .gitattributes
├─ .gitignore
├─ compose.yaml
├─ HELP.md
├─ init-db.sql
├─ mvnw
├─ mvnw.cmd
├─ pom.xml
└─ README.md
```

## Tecnologías
- Java 21 + Spring Boot 4.1.2
- Spring WebMVC, Spring Data JPA y Spring Validation
- Spring Security (sesiones `STATELESS`, `BCryptPasswordEncoder`) + JJWT 0.13.0 (`HS256`)
- MySQL 8.0 ejecutado en Docker Compose con volumen persistente (`mysql_data`)
- Flyway (versionamiento de esquema y datos iniciales de roles)
- Springdoc OpenAPI 3 / Swagger UI 3.0.3
- Lombok

## Seguridad y decisiones técnicas
- **Persistencia en Docker sin exponer la base de datos:** MySQL corre en la misma instancia EC2 mediante `compose.yaml` con volumen nombrado `mysql_data` y política `restart: unless-stopped`, por lo que los datos sobreviven a reinicios del backend y del servidor. El puerto `3306` está enlazado únicamente a `127.0.0.1` y cerrado en el Security Group de AWS.
- **Sin credenciales en el repositorio:** Las claves (`DB_PASS`, `JWT_SECRET`) y orígenes (`CORS_ORIGINS`) se leen desde un archivo `.env` local excluido en `.gitignore`, importado automáticamente por Spring Boot (`spring.config.import=optional:file:.env[.properties]`).
- **Autenticación y autorización:** Las contraseñas se almacenan con hash BCrypt y nunca se devuelven en las respuestas JSON. Los tokens JWT validan firma `HS256`, expiración (`exp`), emisor (`iss`) y destinatario (`aud`). Las rutas sin token o con token inválido devuelven `401`; las rutas con rol insuficiente devuelven `403`.

## Cómo ejecutar el proyecto

### 1. Requisitos previos en EC2 (Ubuntu)
- Java 21 (JDK)
- Docker y Docker Compose v2 (`docker.io`, `docker-compose-v2`)
- Security Group de EC2: abrir puertos de entrada `22` (SSH) y `8080` (TCP). **No abrir el puerto `3306`.**

### 2. Configurar variables de entorno
Copia `.env.example` a `.env` en la raíz del proyecto:

```bash
cp .env.example .env
```

Completa `DB_PASS`, `JWT_SECRET` (mínimo 32 caracteres) y reemplaza `IP_EC2_FRONTEND` por la IPv4 pública actual de la instancia frontend:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sonidovivo
DB_USER=root
DB_PASS=
JWT_SECRET=
CORS_ORIGINS=http://localhost:5173,http://IP_EC2_FRONTEND:5173
```

### 3. Levantar base de datos y aplicación

```bash
# Levantar MySQL 8 con volumen persistente en segundo plano
docker compose up -d

# Verificar que el contenedor sonidovivo-mysql esté activo
docker ps

# Dar permisos al wrapper de Maven y ejecutar Spring Boot
chmod +x mvnw
./mvnw spring-boot:run
```

Una vez iniciado Tomcat en el puerto `8080`, abre Swagger UI en:

```text
http://<IP_EC2_BACKEND>:8080/api/swagger-ui.html
```

**Cuentas ficticias de prueba (insertadas por `DataSeeder` si no existen):**

| Correo | Contraseña | Rol |
|---|---|---|
| `admin@duoc.cl` | `Admin1234` | Administrador |
| `vendedor@duoc.cl` | `Vendedor1234` | Vendedor |
| `cliente@gmail.com` | `Cliente1234` | Cliente |

> Nota: Si se detiene e inicia la instancia frontend en EC2 y cambia su IPv4 pública, actualiza `CORS_ORIGINS` en `.env` y reinicia Spring Boot.

## Bitácora de problemas

| # | Problema | Causa | Solución | Lección |
|---|---|---|---|---|
| 1 | Flyway fallaba al arrancar y dos usuarios no podían compartir nombre ni apellido | Dos migraciones usaban el prefijo `V1__` y la tabla `usuario` tenía `UNIQUE` en `nombre` y `apellido` | Se versionaron las migraciones como `V1__`, `V2__` y `V3__`, dejando `UNIQUE` solo en `email` | Cada archivo de Flyway debe tener una versión única y las restricciones deben reflejar el dominio real |
| 2 | Spring Boot no tomaba las variables del archivo `.env` al ejecutar `./mvnw spring-boot:run` | Docker Compose lee `.env` automáticamente, pero Spring Boot no lo hace por defecto | Se agregó `spring.config.import=optional:file:.env[.properties]` en `application.properties` | Separar secretos en `.env` requiere indicar explícitamente cómo los carga la aplicación |
| 3 | Los endpoints de usuarios en Swagger devolvían `401` incluso tras usar el botón Authorize | `UsuarioController` no tenía la anotación `@SecurityRequirement(name = "bearerAuth")` | Se añadieron `@Tag`, `@SecurityRequirement`, `@Operation` y `@ApiResponses` al controlador | Definir el esquema en `OpenApiConfig` no basta; cada controlador protegido debe referenciarlo |
| 4 | Los commits hechos desde EC2 figuraban a nombre de `Ubuntu` | Git en la instancia no tenía configurados `user.name` ni `user.email` | Se configuró la identidad global de Git por integrante en cada instancia EC2 | Configurar la identidad de Git antes de crear los primeros commits en un servidor nuevo |

## Límites conocidos y mejoras
- Solo el bloque de autenticación y usuarios está persistido en base de datos; productos, categorías y pedidos quedan pendientes para la siguiente iteración.
- En el laboratorio de demostración el puerto `8080` se mantiene accesible para pruebas en vivo; en producción se restringiría tras un proxy inverso con HTTPS.
- `PUT /api/v1/usuarios/{id}` actualiza nombre, apellido, teléfono y rol; no incluye cambio de correo ni restablecimiento de contraseña desde el panel administrador.
- No se implementó renovación automática de tokens (`refresh token`); al expirar el JWT (`1 hora`) el usuario debe iniciar sesión nuevamente.

## Material complementario
Carpeta de Drive con documentos del semestre (ERS y otros):

https://drive.google.com/drive/folders/1DWg9AXXB8mmtMBFIYkqMC4ctOOw1t0hQ?usp=sharing