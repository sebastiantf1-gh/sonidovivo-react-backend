Proyecto de la Evaluación Parcial 3 de **Desarrollo Fullstack II (DSY1104)**, Duoc UC. Es la API REST completa de la tienda **Sonido Vivo**, desarrollada con **Spring Boot**, **MySQL 8 en Docker** con persistencia real, **Flyway**, **Spring Security + JWT** y documentación interactiva en **Swagger UI**.

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
Este backend provee la capa de persistencia relacional en MySQL, autenticación con tokens JWT, control de acceso por roles (`Administrador`, `Vendedor`, `Cliente`), CRUD completo de usuarios y catálogo (categorías y productos con ajuste de stock) y el flujo transaccional de pedidos con validación de stock que retorna `409 Conflict` cuando una solicitud supera el inventario disponible.

## Estado del proyecto

| Módulo / Funcionalidad | Estado | Detalle técnico |
|---|---|---|
| Base de datos MySQL y migraciones | Implementado | Contenedor `mysql:8.0` con volumen persistente `mysql_data` y migraciones Flyway `V1`–`V7` (3 roles, 10 categorías y 51 productos iniciales) |
| Autenticación y JWT | Implementado | Registro (`POST /v1/auth/register` con BCrypt y rol `Cliente` en servidor), login (`POST /v1/auth/login` con JWT `HS256`) y perfil (`GET /v1/auth/me`) |
| CRUD de usuarios | Implementado | `GET`, `GET /{id}`, `POST`, `PUT /{id}` y `DELETE /{id}` exclusivo de `Administrador` (impide auto-eliminación con `409`) |
| CRUD de categorías | Implementado | Lectura pública (`GET`); escritura (`POST`, `PUT`, `DELETE`) para `Administrador` y `Vendedor` con `409` por nombre duplicado o productos asociados |
| CRUD de productos y stock | Implementado | Lectura pública (`GET`, por `id`, por `codigo`, filtros de categoría y críticos); escritura y `PATCH /{id}/stock` para `Administrador` y `Vendedor` |
| Flujo de pedidos y regla `409` | Implementado | Creación transaccional con bloqueo pesimista, cálculo de precios en servidor, **`409 Conflict`** si supera stock, aislamiento por cliente (`403` ante pedidos ajenos) y actualización de estado con restitución de stock al cancelar |
| Manejo global de errores, CORS y Swagger | Implementado | `GlobalExceptionHandler` (`{ mensaje, detalles }`), orígenes desde `CORS_ORIGINS` y OpenAPI/Swagger UI con `bearerAuth` |

## Endpoints principales

Todas las rutas se sirven bajo `server.servlet.context-path=/api`:

**Autenticación y Usuarios**

| Método | Ruta | Acceso | Respuestas principales | Descripción |
|---|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Público | `201`, `400`, `409` | Registra un nuevo cliente (rol `Cliente` asignado por el servidor) |
| `POST` | `/api/v1/auth/login` | Público | `200`, `400`, `401` | Valida credenciales y emite `accessToken` JWT |
| `GET` | `/api/v1/auth/me` | Autenticado | `200`, `401` | Retorna los datos del usuario autenticado desde el JWT |
| `GET` | `/api/v1/usuarios` | `Administrador` | `200`, `401`, `403` | Lista todos los usuarios registrados |
| `GET` | `/api/v1/usuarios/{id}` | `Administrador` | `200`, `401`, `403`, `404` | Consulta un usuario por su ID |
| `POST` | `/api/v1/usuarios` | `Administrador` | `201`, `400`, `401`, `403`, `409` | Crea un usuario asignando su rol |
| `PUT` | `/api/v1/usuarios/{id}` | `Administrador` | `200`, `400`, `401`, `403`, `404` | Actualiza datos personales y/o rol de un usuario |
| `DELETE` | `/api/v1/usuarios/{id}` | `Administrador` | `204`, `401`, `403`, `404`, `409` | Elimina un usuario por ID (`409` ante auto-eliminación) |

**Catálogo (Categorías y Productos)**

| Método | Ruta | Acceso | Respuestas principales | Descripción |
|---|---|---|---|---|
| `GET` | `/api/v1/categorias` | Público | `200` | Lista todas las categorías |
| `GET` | `/api/v1/categorias/{id}` | Público | `200`, `404` | Obtiene una categoría por ID |
| `POST` | `/api/v1/categorias` | `Administrador`, `Vendedor` | `201`, `400`, `401`, `403`, `409` | Crea una categoría (`409` si el nombre ya existe) |
| `PUT` | `/api/v1/categorias/{id}` | `Administrador`, `Vendedor` | `200`, `400`, `401`, `403`, `404`, `409` | Actualiza una categoría existente |
| `DELETE` | `/api/v1/categorias/{id}` | `Administrador`, `Vendedor` | `204`, `401`, `403`, `404`, `409` | Elimina una categoría (`409` si tiene productos asociados) |
| `GET` | `/api/v1/productos` | Público | `200` | Lista productos (filtros opcionales: `categoriaId`, `categoria`, `criticos=true`) |
| `GET` | `/api/v1/productos/{id}` | Público | `200`, `404` | Obtiene un producto por ID |
| `GET` | `/api/v1/productos/codigo/{codigo}` | Público | `200`, `404` | Obtiene un producto por código SKU |
| `POST` | `/api/v1/productos` | `Administrador`, `Vendedor` | `201`, `400`, `401`, `403`, `404`, `409` | Crea un producto (`409` si el código SKU ya existe) |
| `PUT` | `/api/v1/productos/{id}` | `Administrador`, `Vendedor` | `200`, `400`, `401`, `403`, `404`, `409` | Actualiza un producto existente |
| `PATCH` | `/api/v1/productos/{id}/stock` | `Administrador`, `Vendedor` | `200`, `400`, `401`, `403`, `404` | Actualiza directamente el stock disponible de un producto |
| `DELETE` | `/api/v1/productos/{id}` | `Administrador`, `Vendedor` | `204`, `401`, `403`, `404`, `409` | Elimina un producto (`409` si figura en pedidos registrados) |

**Pedidos (Flujo Principal y Regla de Negocio `409`)**

| Método | Ruta | Acceso | Respuestas principales | Descripción |
|---|---|---|---|---|
| `POST` | `/api/v1/pedidos` | Autenticado | `201`, `400`, `401`, `404`, `409` | Crea un pedido descontando stock en transacción; devuelve **`409 Conflict`** si la cantidad supera el stock disponible |
| `GET` | `/api/v1/pedidos` | Autenticado | `200`, `401` | `Cliente` ve solo sus propios pedidos; `Vendedor` y `Administrador` ven todos |
| `GET` | `/api/v1/pedidos/{id}` | Autenticado | `200`, `401`, `403`, `404` | Detalle de un pedido (`403` si un `Cliente` consulta un pedido ajeno) |
| `PATCH` | `/api/v1/pedidos/{id}/estado` | `Administrador`, `Vendedor` | `200`, `400`, `401`, `403`, `404`, `409` | Cambia estado (`En preparación`, `Despachado`, `Entregado`, `Cancelado`); restituir stock al cancelar; `409` si el pedido ya estaba finalizado |

## Estructura del proyecto

Arquitectura modular por dominio y capas dentro de `cl.duoc.sonidovivo_backend`: `auth/` (seguridad, JWT y usuarios), `catalogo/` (categorías y productos), `pedido/` (flujo de compra y regla `409`), junto con entidades JPA en `model/`, repositorios en `repository/` y migraciones versionadas en `resources/db/migration/`.

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
│  │  │           ├─ catalogo/
│  │  │           │  ├─ controller/
│  │  │           │  │  ├─ CategoriaController.java
│  │  │           │  │  └─ ProductoController.java
│  │  │           │  ├─ dto/
│  │  │           │  │  ├─ CategoriaRequest.java
│  │  │           │  │  ├─ CategoriaResponse.java
│  │  │           │  │  ├─ ProductoRequest.java
│  │  │           │  │  ├─ ProductoResponse.java
│  │  │           │  │  └─ StockUpdateRequest.java
│  │  │           │  └─ service/
│  │  │           │     ├─ CategoriaService.java
│  │  │           │     └─ ProductoService.java
│  │  │           ├─ model/
│  │  │           │  ├─ Categoria.java
│  │  │           │  ├─ DetallePedido.java
│  │  │           │  ├─ Pedido.java
│  │  │           │  ├─ Producto.java
│  │  │           │  ├─ Rol.java
│  │  │           │  └─ Usuario.java
│  │  │           ├─ pedido/
│  │  │           │  ├─ controller/
│  │  │           │  │  └─ PedidoController.java
│  │  │           │  ├─ dto/
│  │  │           │  │  ├─ DetallePedidoResponse.java
│  │  │           │  │  ├─ PedidoCreateRequest.java
│  │  │           │  │  ├─ PedidoEstadoRequest.java
│  │  │           │  │  ├─ PedidoItemRequest.java
│  │  │           │  │  └─ PedidoResponse.java
│  │  │           │  └─ service/
│  │  │           │     └─ PedidoService.java
│  │  │           ├─ repository/
│  │  │           │  ├─ CategoriaRepository.java
│  │  │           │  ├─ DetallePedidoRepository.java
│  │  │           │  ├─ PedidoRepository.java
│  │  │           │  ├─ ProductoRepository.java
│  │  │           │  ├─ RolRepository.java
│  │  │           │  └─ UsuarioRepository.java
│  │  │           └─ SonidovivoBackendApplication.java
│  │  └─ resources/
│  │     ├─ db/
│  │     │  └─ migration/
│  │     │     ├─ V1__create_table_rol.sql
│  │     │     ├─ V2__create_table_usuario.sql
│  │     │     ├─ V3__insert_roles.sql
│  │     │     ├─ V4__create_table_categoria.sql
│  │     │     ├─ V5__create_table_producto.sql
│  │     │     ├─ V6__create_table_pedido_y_detalle.sql
│  │     │     └─ V7__insert_catalogo_inicial.sql
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
│  │  │        ├─ catalogo/
│  │  │        │  ├─ controller/
│  │  │        │  │  ├─ CategoriaController.class
│  │  │        │  │  └─ ProductoController.class
│  │  │        │  ├─ dto/
│  │  │        │  │  ├─ CategoriaRequest.class
│  │  │        │  │  ├─ CategoriaResponse.class
│  │  │        │  │  ├─ ProductoRequest.class
│  │  │        │  │  ├─ ProductoResponse.class
│  │  │        │  │  └─ StockUpdateRequest.class
│  │  │        │  └─ service/
│  │  │        │     ├─ CategoriaService.class
│  │  │        │     └─ ProductoService.class
│  │  │        ├─ model/
│  │  │        │  ├─ Categoria.class
│  │  │        │  ├─ Categoria$CategoriaBuilder.class
│  │  │        │  ├─ DetallePedido.class
│  │  │        │  ├─ DetallePedido$DetallePedidoBuilder.class
│  │  │        │  ├─ Pedido.class
│  │  │        │  ├─ Pedido$PedidoBuilder.class
│  │  │        │  ├─ Producto.class
│  │  │        │  ├─ Producto$ProductoBuilder.class
│  │  │        │  ├─ Rol.class
│  │  │        │  ├─ Rol$RolBuilder.class
│  │  │        │  ├─ Usuario.class
│  │  │        │  └─ Usuario$UsuarioBuilder.class
│  │  │        ├─ pedido/
│  │  │        │  ├─ controller/
│  │  │        │  │  └─ PedidoController.class
│  │  │        │  ├─ dto/
│  │  │        │  │  ├─ DetallePedidoResponse.class
│  │  │        │  │  ├─ PedidoCreateRequest.class
│  │  │        │  │  ├─ PedidoEstadoRequest.class
│  │  │        │  │  ├─ PedidoItemRequest.class
│  │  │        │  │  └─ PedidoResponse.class
│  │  │        │  └─ service/
│  │  │        │     └─ PedidoService.class
│  │  │        ├─ repository/
│  │  │        │  ├─ CategoriaRepository.class
│  │  │        │  ├─ DetallePedidoRepository.class
│  │  │        │  ├─ PedidoRepository.class
│  │  │        │  ├─ ProductoRepository.class
│  │  │        │  ├─ RolRepository.class
│  │  │        │  └─ UsuarioRepository.class
│  │  │        └─ SonidovivoBackendApplication.class
│  │  ├─ db/
│  │  │  └─ migration/
│  │  │     ├─ V1__create_table_rol.sql
│  │  │     ├─ V2__create_table_usuario.sql
│  │  │     ├─ V3__insert_roles.sql
│  │  │     ├─ V4__create_table_categoria.sql
│  │  │     ├─ V5__create_table_producto.sql
│  │  │     ├─ V6__create_table_pedido_y_detalle.sql
│  │  │     └─ V7__insert_catalogo_inicial.sql
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
- MySQL 8.0 en Docker Compose con volumen persistente (`mysql_data`)
- Flyway (migraciones versionadas `V1`–`V7`)
- Springdoc OpenAPI 3 / Swagger UI 3.0.3
- Lombok

## Seguridad y decisiones técnicas
- **Persistencia real sin exponer la base de datos:** MySQL corre en la misma instancia EC2 mediante `compose.yaml` con volumen `mysql_data` y política `restart: unless-stopped`. El puerto `3306` está enlazado únicamente a `127.0.0.1` y cerrado en el Security Group de AWS.
- **Sin credenciales en Git:** `DB_PASS`, `JWT_SECRET` y `CORS_ORIGINS` se cargan desde un archivo `.env` local excluido en `.gitignore` (`spring.config.import=optional:file:.env[.properties]`).
- **Integridad de negocio en pedidos (`409 Conflict`):** `PedidoService.crear` lee el propietario desde el JWT (`Authentication`), calcula precios y totales en el servidor, consolida cantidades por producto y bloquea cada fila con `@Lock(LockModeType.PESSIMISTIC_WRITE)` en una transacción `@Transactional`. Si algún producto no cuenta con stock suficiente, retorna `409 Conflict` y revierte toda la operación.
- **Aislamiento de datos entre clientes:** `GET /api/v1/pedidos` filtra automáticamente por el usuario del token cuando el rol es `Cliente`, y `GET /api/v1/pedidos/{id}` devuelve `403 Forbidden` ante intentos de acceso horizontal (`IDOR`).

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

# Dar permisos al wrapper de Maven (solo la primera vez) y ejecutar Spring Boot
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

> Nota: Si se reinicia la instancia frontend en EC2 y cambia su IPv4 pública, actualiza `CORS_ORIGINS` en `.env` y reinicia Spring Boot.

## Bitácora de problemas

| # | Problema | Causa | Solución | Lección |
|---|---|---|---|---|
| 1 | Flyway fallaba al arrancar y dos usuarios no podían compartir nombre ni apellido | Dos migraciones usaban el prefijo `V1__` y `usuario` tenía `UNIQUE` en `nombre` y `apellido` | Se versionaron las migraciones (`V1`–`V7`), dejando `UNIQUE` solo en `email` | Cada migración de Flyway debe ser única e inmutable una vez aplicada |
| 2 | Spring Boot no tomaba las variables de `.env` al ejecutar `./mvnw spring-boot:run` | Spring Boot no importa archivos `.env` por defecto | Se agregó `spring.config.import=optional:file:.env[.properties]` en `application.properties` | Configurar explícitamente la carga de variables externas |
| 3 | Los endpoints protegidos en Swagger devolvían `401` tras usar Authorize | Faltaba `@SecurityRequirement(name = "bearerAuth")` en los controladores | Se agregó la anotación en los controladores de usuarios, catálogo y pedidos | Registrar el esquema en `OpenApiConfig` requiere referenciarlo en cada controlador protegido |
| 4 | Los commits hechos desde EC2 figuraban a nombre de `Ubuntu` | Git en la instancia no tenía configurados `user.name` ni `user.email` | Se configuró la identidad global de Git por integrante en cada EC2 | Verificar `git config` antes de crear commits en una instancia nueva |

## Límites conocidos y mejoras
- En el entorno académico el puerto `8080` se expone directamente sobre HTTP; en producción se desplegaría detrás de un proxy inverso con TLS/HTTPS.
- `PUT /api/v1/usuarios/{id}` actualiza datos personales y rol; no incluye cambio de correo ni restablecimiento de contraseña desde el panel administrador.
- No se implementó renovación automática de tokens (`refresh token`); al expirar el JWT (`1 hora`) el usuario debe volver a iniciar sesión.

## Material complementario
Carpeta de Drive con documentos del semestre (ERS, Manual de Usuario y evidencias):

https://drive.google.com/drive/folders/1DWg9AXXB8mmtMBFIYkqMC4ctOOw1t0hQ?usp=sharing