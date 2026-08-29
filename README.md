# Sistema de Citas — Backend

API REST para gestión de citas médicas (tipo clínica), con pacientes, doctores, especialidades, horarios y un chatbot con IA para agendar citas por lenguaje natural. Proyecto de portafolio construido con un flujo profesional completo: modelado de datos, reglas de negocio, autenticación, testing, contenedores y CI/CD.

## Stack técnico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 25 (Temurin LTS) |
| Framework | Spring Boot 4.1.1 |
| Base de datos | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Seguridad | Spring Security (Basic Auth por ahora, JWT en progreso) |
| Build | Maven |
| Utilidades | Lombok |

## Arquitectura

El proyecto sigue una arquitectura en capas estándar:

**Controller → Service → Repository → Entity (JPA)**

- Los **Controllers** reciben y devuelven **DTOs**, nunca entidades directamente
- Los **Services** contienen las reglas de negocio y validaciones
- Los **Repositories** son interfaces de Spring Data JPA sobre las **Entities**

- **Entity**: mapeo directo a las tablas de PostgreSQL (Paciente, Doctor, Especialidad, HorarioDisponible, Cita)
- **Repository**: interfaces de Spring Data JPA, consultas derivadas por nombre de método
- **Service**: lógica de negocio (validaciones, reglas, transacciones)
- **DTO**: objetos de entrada/salida de la API, separados de las entidades
- **Controller**: endpoints REST, sin lógica de negocio
- **GlobalExceptionHandler**: manejo centralizado de errores con códigos HTTP consistentes

## Modelo de datos

- **Paciente**: puede ser invitado (sin cuenta, creado vía chatbot) o registrado
- **Doctor**: asociado a una o más Especialidades
- **Especialidad**: catálogo de especialidades médicas
- **HorarioDisponible**: franjas horarias en las que un doctor atiende, por día de la semana
- **Cita**: vincula Paciente + Doctor + fecha/hora, con estados (`PENDIENTE`, `CONFIRMADA`, `CANCELADA`, `COMPLETADA`)

## Requisitos previos

- JDK 25
- PostgreSQL 16+ corriendo localmente
- Maven (o usar el wrapper incluido `./mvnw`)

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
```
4. Ejecuta la aplicación:
```bash
   ./mvnw spring-boot:run
```

5. La API estará disponible en `http://localhost:8080`

### Autenticación (desarrollo)

Actualmente la API usa Basic Auth con credenciales fijas de desarrollo:
- Usuario: `admin`
- Contraseña: `admin123`

> Esto es temporal. Se está migrando a autenticación con JWT y roles (`PACIENTE`, `DOCTOR`, `ADMIN`).

## Endpoints disponibles

### Especialidades
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/especialidades` | Lista todas las especialidades |
| GET | `/api/especialidades/{id}` | Busca una especialidad por id |
| POST | `/api/especialidades` | Crea una especialidad |
| PUT | `/api/especialidades/{id}` | Actualiza una especialidad |
| DELETE | `/api/especialidades/{id}` | Elimina una especialidad |

### Doctores
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/doctores` | Lista todos los doctores |
| GET | `/api/doctores/{id}` | Busca un doctor por id |
| POST | `/api/doctores` | Crea un doctor (con especialidades) |
| PUT | `/api/doctores/{id}` | Actualiza un doctor |
| DELETE | `/api/doctores/{id}` | Elimina un doctor |

### Pacientes
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/pacientes` | Lista todos los pacientes |
| GET | `/api/pacientes/{id}` | Busca un paciente por id |
| POST | `/api/pacientes` | Crea un paciente (soporta invitados sin email) |
| PUT | `/api/pacientes/{id}` | Actualiza un paciente |
| DELETE | `/api/pacientes/{id}` | Elimina un paciente |

### Horarios disponibles
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/horarios/doctor/{doctorId}` | Lista los horarios de un doctor |
| POST | `/api/horarios` | Crea un horario (día en español, ej. `"lunes"`) |
| DELETE | `/api/horarios/{id}` | Elimina un horario |

### Citas
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/citas/paciente/{pacienteId}` | Lista las citas de un paciente |
| GET | `/api/citas/doctor/{doctorId}` | Lista las citas de un doctor |
| GET | `/api/citas/{id}` | Busca una cita por id |
| POST | `/api/citas` | Crea una cita (valida horario y evita doble-reserva) |
| PATCH | `/api/citas/{id}/estado?nuevoEstado=CONFIRMADA` | Cambia el estado de una cita |

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
| 404 | Recurso no encontrado |
| 409 | Conflicto (recurso duplicado, doble-reserva) |

## Roadmap del proyecto

- [x] Modelado de datos y CRUD completo (Especialidad, Doctor, Paciente, HorarioDisponible, Cita)
- [x] Reglas de negocio (validación de horario, prevención de doble-reserva)
- [ ] Autenticación con JWT y roles
- [ ] Chatbot con IA para agendar citas por lenguaje natural
- [ ] Frontend en React + Vite + Tailwind
- [ ] Tests unitarios y de integración (JUnit 5, Testcontainers)
- [ ] Dockerización
- [ ] CI/CD con GitHub Actions
- [ ] Despliegue en producción

## Autor

Jordy Jimbo — Universidad Técnica de Machala, Tecnologías de la Información


