# Parcial #2 – Sistema de Gestión y Control de Eventos Académicos
### ICC-352 Programación Web – PUCMM



##  Créditos

    Almy Ventura – 10153712
    Maria Jose Cruz – 10154963

    Fecha: 11 de Marzo 2026
    Profesor: Ing. Carlos Camacho
    Grupo: 5488


## 1. Descripción del Proyecto

Sistema web para la gestión de eventos académicos (charlas, talleres, seminarios, congresos), implementando:
    - Gestión de usuarios con tres roles: Administrador, Organizador y Participante
    - Creación, edición, publicación y cancelación de eventos
    - Inscripción a eventos con validación de cupo y duplicados
    - Control de asistencia mediante código QR generado del lado del servidor
    - Estadísticas visuales por evento con gráficos en tiempo real
    - Panel de administración para gestión de usuarios y eventos
    - Despliegue en producción con Docker y certificado SSL/TLS

## 2. Tecnologías Utilizadas

    - Javalin 7.0.1 – Framework web y servidor HTTP
    - Hibernate 6.4.4 – ORM para persistencia de datos
    - H2 2.1.214 – Base de datos en modo servidor (puerto 9092)
    - Thymeleaf – Motor de plantillas HTML
    - Bootstrap 5.3 – Diseño responsivo y componentes UI
    - Google ZXing 3.5.3 – Generación de códigos QR del lado del servidor
    - html5-qrcode – Escaneo de QR desde el navegador
    - Chart.js – Gráficos de estadísticas
    - FullCalendar 6 – Vista de eventos en calendario
    - jBCrypt 0.4 – Hash de contraseñas
    - Jackson 2.16.1 – Serialización JSON para las APIs
    - Docker – Contenedorización con imagen multistage
    - Let's Encrypt – Certificado SSL/TLS en producción

## 3. Estructura del Proyecto

    src/main/java/com/pucmm/csti19105488/
    ├── Main.java
    ├── controller/
    │   ├── UsuarioController.java
    │   ├── EventoController.java
    │   └── RegistroController.java
    ├── service/
    │   ├── UsuarioService.java
    │   ├── EventoService.java
    │   ├── RegistroService.java
    │   └── EstadisticaService.java
    ├── dao/
    │   ├── UsuarioDAO.java
    │   ├── EventoDAO.java
    │   ├── RegistroDAO.java
    │   └── CodigoQRDAO.java
    ├── model/
    │   ├── Usuario.java
    │   ├── Evento.java
    │   ├── Registro.java
    │   ├── CodigoQR.java
    │   ├── ResumenEvento.java
    │   └── enums/
    │       ├── TipoUsuario.java
    │       ├── TipoEvento.java
    │       └── EstadoEvento.java
    └── util/
        └── HibernateUtil.java

## 4. Requisitos Previos

    - Java 25
    - Gradle 9.2+
    - Docker y Docker Compose

## 5. Ejecución Local

### Con Gradle

    ./gradlew shadowJar
    java -jar build/libs/*-all.jar

    La aplicación estará disponible en http://localhost:7000

### Con Docker

    docker build -t parcial2-eventos .
    docker-compose up -d

## 6. Usuario Administrador por Defecto

Al iniciar la aplicación por primera vez se crea automáticamente un usuario administrador:

        Email:      admin@ce.pucmm.edu.do
        Contraseña: admin123

Este usuario no puede ser eliminado ni bloqueado desde el sistema.

## 7. Roles del Sistema

    - Administrador – Gestiona usuarios, asigna roles, elimina eventos, accede a todo
    - Organizador – Crea, edita, publica y cancela sus eventos, escanea QR de asistencia
    - Participante – Ve eventos publicados, se inscribe, cancela inscripción, presenta QR

## 8. Endpoints Principales

### Vistas

GET /login                            – Pantalla de inicio de sesión
GET /registro                         – Registro de nuevo participante
GET /eventos                          – Lista pública de eventos
GET /mis-inscripciones                – Inscripciones del participante con QR
GET /organizador/eventos              – Panel del organizador
GET /organizador/eventos/{id}/scanner – Escáner QR de asistencia
GET /organizador/eventos/{id}/resumen – Estadísticas del evento
GET /dashboard                        – Dashboard con gráficos y filtros
GET /admin/usuarios                   – Gestión de usuarios (admin)
GET /admin/eventos                    – Gestión de todos los eventos (admin)

### API REST

POST /api/eventos/{id}/inscribir            – Inscribirse a un evento
POST /api/eventos/{id}/cancelar-inscripcion – Cancelar inscripción
POST /api/asistencia/validar                – Validar QR y marcar asistencia
GET  /api/eventos/{id}/resumen              – Estadísticas del evento (JSON)
GET  /api/dashboard/estadisticas            – Estadísticas del dashboard (JSON)

## 9. Generación de Código QR

Al inscribirse, el sistema genera automáticamente un QR con ZXing que codifica:

eventoId=<id>&usuarioId=<id>&token=<UUID>

El QR se almacena como imagen Base64 en la base de datos y se muestra en Mis Inscripciones.

## 10. Docker

### Dockerfile (multistage)

FROM gradle:9.2.0-jdk25 AS builder
WORKDIR /app
COPY . .
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar app.jar
EXPOSE 7000
CMD ["java", "-jar", "app.jar"]

### docker-compose.yml

services:
  eventos:
    image: parcial2-eventos
    container_name: parcial2-eventos-container
    ports:
      - "7000:7000"
    restart: always

## 11. Producción

La aplicación está desplegada en un servidor con IP pública vía Docker Compose,
con certificado SSL emitido por Let's Encrypt y redirección automática de HTTP a HTTPS.

## 12. Conclusión

Se implementó un sistema web completo aplicando todos los conceptos fundamentales
del curso: arquitectura MVC en capas, ORM con Hibernate, APIs REST consumidas con
Fetch API, generación y escaneo de QR, estadísticas visuales con Chart.js y
despliegue en producción con Docker y HTTPS.