# Sistema de Citas — Backend

API REST para gestión de citas médicas (tipo clínica), con pacientes, doctores, especialidades, horarios y un chatbot con IA para agendar citas por lenguaje natural. Proyecto de portafolio construido con un flujo profesional completo: modelado de datos, reglas de negocio, autenticación, testing, contenedores y CI/CD.

## Stack técnico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 25 (Temurin LTS) |
| Framework | Spring Boot 4.1.1 |
| Base de datos | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Seguridad | Spring Security + JWT (jjwt 0.13.0) |
| IA / Chatbot | Spring AI 2.0.1 + Google Gemini (gemini-3.6-flash) |
| Build | Maven |
| Utilidades | Lombok |

## Arquitectura

El proyecto sigue una arquitectura en capas estándar:

**Controller → Service → Repository → Entity (JPA)**

- Los **Controllers** reciben y devuelven **DTOs**, nunca entidades directamente
- Los **Services** contienen las reglas de negocio y validaciones
- Los **Repositories** son interfaces de Spring Data JPA sobre las **Entities**

- **Entity**: mapeo directo a las tablas de PostgreSQL (Usuario, Paciente, Doctor, Especialidad, HorarioDisponible, Cita)
- **Repository**: interfaces de Spring Data JPA, consultas derivadas por nombre de método
- **Service**: lógica de negocio (validaciones, reglas, transacciones)
- **DTO**: objetos de entrada/salida de la API, separados de las entidades
- **Controller**: endpoints REST, sin lógica de negocio
- **GlobalExceptionHandler**: manejo centralizado de errores con códigos HTTP consistentes
- **security/**: piezas de autenticación JWT (filtro, generación/validación de tokens, adaptador de `UserDetails`) y autorización (`CitaPermisos`)
- **chatbot/**: `CitasTools` (herramientas que el chatbot puede invocar) y `ChatbotController` (endpoint conversacional con memoria)

## Modelo de datos

- **Usuario**: identidad y acceso (username, contraseña hasheada, rol). Puede estar ligado opcionalmente a un Paciente o a un Doctor, o a ninguno (caso de un ADMIN puro)
- **Paciente**: puede ser invitado (sin cuenta, creado vía chatbot) o registrado
- **Doctor**: asociado a una o más Especialidades
- **Especialidad**: catálogo de especialidades médicas
- **HorarioDisponible**: franjas horarias en las que un doctor atiende, por día de la semana
- **Cita**: vincula Paciente + Doctor + fecha/hora, con estados (`PENDIENTE`, `CONFIRMADA`, `CANCELADA`, `COMPLETADA`)

## Requisitos previos

- JDK 25
- PostgreSQL 16+ corriendo localmente
- Maven (o usar el wrapper incluido `./mvnw`)
- Una API key de [Google AI Studio](https://aistudio.google.com/app/apikey) (gratuita) para el chatbot

## Cómo levantar el proyecto localmente

1. Clona el repositorio:
```bash
git clone https://github.com/JimboJ10/sistema-citas-backend.git
cd sistema-citas-backend
```

2. Crea la base de datos en PostgreSQL:
```sql
CREATE DATABASE sistema_citas;
```

3. Configura las variables de entorno necesarias:
```bash
DB_PASSWORD=tu_contraseña_de_postgres
JWT_SECRET=una-clave-secreta-de-al-menos-32-caracteres
GEMINI_API_KEY=tu_api_key_de_google_ai_studio
```

4. Ejecuta la aplicación:
```bash
./mvnw spring-boot:run
```

5. La API estará disponible en `http://localhost:8080`

## Autenticación

La API usa **JWT** (JSON Web Tokens). El flujo es:

1. Registrarse o iniciar sesión para obtener un token
2. Enviar el token en cada petición protegida, en el header:
```
Authorization: Bearer <token>
```

**Registro:**
```
POST /api/auth/registro
```
```json
{
  "username": "usuario.ejemplo",
  "password": "contraseñaSegura123",
  "rol": "PACIENTE",
  "pacienteId": null,
  "doctorId": null
}
```
`rol` puede ser `PACIENTE`, `DOCTOR` o `ADMIN`. `pacienteId`/`doctorId` son opcionales, para vincular la cuenta a un registro existente.

**Login:**
```
POST /api/auth/login
```
```json
{
  "username": "usuario.ejemplo",
  "password": "contraseñaSegura123"
}
```

Ambos devuelven:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "usuario.ejemplo",
  "rol": "PACIENTE"
}
```

### Reglas de autorización por rol

| Recurso | Regla |
|---|---|
| `GET` de Especialidades y Doctores | Público, sin autenticación |
| Crear/editar/eliminar Especialidades y Doctores | Solo `ADMIN` |
| Ver/gestionar una Cita específica | El `PACIENTE` dueño, el `DOCTOR` asignado, o `ADMIN` |
| Resto de endpoints | Requieren estar autenticado |

## Chatbot con IA

El endpoint `/api/chatbot` expone un asistente conversacional (Google Gemini vía Spring AI) capaz de:

- Listar especialidades y doctores disponibles
- Consultar horarios reales de un doctor
- Detectar conflictos de horario y sugerir alternativas
- Crear una cita real cuando tiene todos los datos necesarios

Todo esto usando datos reales de la base de datos (nunca inventados), gracias a un conjunto de *tools* (`CitasTools`) que el modelo puede invocar.

**Uso:**
```
POST /api/chatbot
```
```json
{
  "mensaje": "Quiero una cita con el dermatólogo el lunes a las 9am",
  "conversationId": "conv-001"
}
```

El `conversationId` debe mantenerse igual entre mensajes de una misma conversación para que el chatbot recuerde el contexto (gracias a `MessageChatMemoryAdvisor`). Un `conversationId` distinto inicia una conversación nueva, sin memoria de la anterior.

> **Nota sobre límites:** el tier gratuito de la API de Gemini permite 20 peticiones/día por modelo. Si se excede, el endpoint responde `503 Service Unavailable` con un mensaje claro en vez de un error crudo.

## Endpoints disponibles

### Autenticación
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/registro` | Público |
| POST | `/api/auth/login` | Público |

### Chatbot
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/chatbot` | Autenticado |

### Especialidades
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/especialidades` | Público | Lista todas las especialidades |
| GET | `/api/especialidades/{id}` | Público | Busca una especialidad por id |
| POST | `/api/especialidades` | ADMIN | Crea una especialidad |
| PUT | `/api/especialidades/{id}` | ADMIN | Actualiza una especialidad |
| DELETE | `/api/especialidades/{id}` | ADMIN | Elimina una especialidad |

### Doctores
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/doctores` | Público | Lista todos los doctores |
| GET | `/api/doctores/{id}` | Público | Busca un doctor por id |
| POST | `/api/doctores` | ADMIN | Crea un doctor (con especialidades) |
| PUT | `/api/doctores/{id}` | ADMIN | Actualiza un doctor |
| DELETE | `/api/doctores/{id}` | ADMIN | Elimina un doctor |

### Pacientes
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/pacientes` | Autenticado | Lista todos los pacientes |
| GET | `/api/pacientes/{id}` | Autenticado | Busca un paciente por id |
| POST | `/api/pacientes` | Autenticado | Crea un paciente (soporta invitados sin email) |
| PUT | `/api/pacientes/{id}` | Autenticado | Actualiza un paciente |
| DELETE | `/api/pacientes/{id}` | Autenticado | Elimina un paciente |

### Horarios disponibles
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/horarios/doctor/{doctorId}` | Autenticado | Lista los horarios de un doctor |
| POST | `/api/horarios` | Autenticado | Crea un horario (día en español, ej. `"lunes"`) |
| DELETE | `/api/horarios/{id}` | Autenticado | Elimina un horario |

### Citas
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/citas/paciente/{pacienteId}` | Autenticado | Lista las citas de un paciente |
| GET | `/api/citas/doctor/{doctorId}` | Autenticado | Lista las citas de un doctor |
| GET | `/api/citas/{id}` | Dueño / asignado / ADMIN | Busca una cita por id |
| POST | `/api/citas` | Autenticado | Crea una cita (valida horario y evita doble-reserva) |
| PATCH | `/api/citas/{id}/estado?nuevoEstado=CONFIRMADA` | Dueño / asignado / ADMIN | Cambia el estado de una cita |

## Manejo de errores

Todas las respuestas de error siguen este formato:

```json
{
  "timestamp": "2026-08-29T16:20:57.62",
  "status": 400,
  "error": "Mensaje descriptivo del error"
}
```

| Código | Cuándo ocurre |
|---|---|
| 400 | Datos inválidos o regla de negocio violada |
| 401 | No autenticado, o credenciales incorrectas |
| 403 | Autenticado, pero sin permiso para esta acción |
| 404 | Recurso no encontrado |
| 409 | Conflicto (recurso duplicado, doble-reserva) |
| 503 | El servicio de IA no está disponible (límite de cuota alcanzado) |

## Roadmap del proyecto

- [x] Modelado de datos y CRUD completo (Especialidad, Doctor, Paciente, HorarioDisponible, Cita)
- [x] Reglas de negocio (validación de horario, prevención de doble-reserva)
- [x] Autenticación con JWT y roles (PACIENTE, DOCTOR, ADMIN)
- [x] Autorización por rol y por propiedad de recurso (dueño de la cita)
- [x] Chatbot con IA para agendar citas por lenguaje natural (Spring AI + Gemini, con memoria de conversación)
- [ ] Frontend en React + Vite + Tailwind
- [ ] Tests unitarios y de integración (JUnit 5, Testcontainers)
- [ ] Dockerización
- [ ] CI/CD con GitHub Actions
- [ ] Despliegue en producción

## Autor

Jordy Jimbo — Universidad Técnica de Machala, Tecnologías de la Información