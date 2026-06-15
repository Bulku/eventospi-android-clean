# EventosPI Android

Aplicación Android desarrollada para el Proyecto Integrador 2026-1.
La app permite consultar, crear, gestionar e inscribirse a eventos desde un dispositivo Android, usando un backend desplegado en Render.

## Estado actual del proyecto

La aplicación se encuentra en una versión funcional conectada al backend desplegado.

Actualmente permite:

* Registro de usuarios.
* Inicio de sesión.
* Consulta del usuario autenticado.
* Cambio de contraseña.
* Carga de eventos desde el backend.
* Creación de eventos públicos y privados.
* Edición de eventos.
* Eliminación de eventos creados por el usuario.
* Inscripción a eventos públicos.
* Solicitud de inscripción a eventos privados.
* Gestión de solicitudes pendientes para eventos privados.
* Aprobación y rechazo de solicitudes por parte del creador del evento.
* Cancelación de inscripción.
* Manejo local de inscripciones canceladas para evitar que se muestren como activas si el backend las conserva en el historial.
* Visualización de eventos en mapa.
* Visualización de información del evento en tarjetas.
* Reacciones a eventos.
* Consulta de participantes inscritos.
* Carga de imagen para eventos.
* Carga de imagen de perfil.

## Conexión con backend

La app consume el backend desplegado en Render:

```text
https://backend-pi-2p40.onrender.com/
```

El archivo encargado de la configuración de conexión es:

```text
app/src/main/java/com/leonvelez/eventospi/data/remote/RetrofitInstance.kt
```

La URL base configurada es:

```kotlin
private const val BASE_URL = "https://backend-pi-2p40.onrender.com/"
```

No se debe usar la URL de Swagger como base de Retrofit. Swagger solo se usa para revisar y probar endpoints desde el navegador.

## Endpoints actualizados

Después del ajuste del backend, las rutas quedaron organizadas por controlador:

```text
/User
/Event
/EventParticipant
/Reaction
```

Por eso la app fue actualizada para consumir rutas como:

```text
User/Login
User/Register
Event/GetEvents
Event/Create
EventParticipant/RegisterToEvent
EventParticipant/CancelRegistration
Reaction/ReactToEvent
```

También se ajustaron los métodos que reciben objetos para enviarlos como JSON en el cuerpo de la petición.

## Eventos públicos y privados

Al crear un evento, el usuario puede escoger si será público o privado.

En un evento público, la inscripción queda realizada directamente.

En un evento privado, el usuario envía una solicitud de inscripción y el creador del evento debe aprobarla o rechazarla desde la pantalla de solicitudes pendientes.


## Tecnologías usadas

* Kotlin
* Jetpack Compose
* Retrofit
* OkHttp
* MapLibre
* Android Studio
* Backend .NET desplegado en Render

## Estructura general del proyecto

La app fue organizada por capas para facilitar el mantenimiento:

```text
data/
  model/
  remote/

ui/
  components/
  navigation/
  screens/
    auth/
    dashboard/
    events/
    map/
    profile/
  theme/

utils/
```

Esta estructura separa modelos, consumo de API, pantallas, componentes reutilizables y funciones auxiliares.

## Requisitos para ejecutar la app

* Android Studio.
* Emulador Android o dispositivo físico.
* Conexión a internet.
* Backend desplegado disponible en Render.

Ya no es necesario ejecutar el backend localmente para probar la app Android.

## Ejecución

1. Abrir el proyecto en Android Studio.
2. Verificar que el emulador esté iniciado.
3. Ejecutar:

```text
Build > Clean Project
Build > Assemble Project
```

4. Presionar el botón Run.

## Flujo recomendado de prueba

Para validar la app completa se recomienda probar:

1. Crear una cuenta.
2. Iniciar sesión.
3. Crear un evento público.
4. Inscribirse al evento público.
5. Cancelar la inscripción.
6. Confirmar que aparece como “Inscripción cancelada”.
7. Crear un evento privado.
8. Iniciar sesión con otro usuario.
9. Solicitar inscripción al evento privado.
10. Volver al usuario creador.
11. Aprobar o rechazar la solicitud pendiente.
12. Probar reacciones a eventos.
13. Editar un evento creado.
14. Eliminar un evento creado.
15. Cerrar y volver a abrir la app para verificar persistencia local de cancelaciones.

## Nota final

Esta versión deja la aplicación preparada para la demo técnica, con conexión al backend desplegado y con los principales flujos funcionales integrados.
