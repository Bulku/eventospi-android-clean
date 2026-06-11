# Refactor frontend Android - EventosPI

## Objetivo del cambio

Este refactor divide el antiguo `MainActivity.kt` monolítico en archivos por responsabilidades sin cambiar los contratos actuales del backend ni migrar la navegación simple por estado.

`MainActivity.kt` queda reducido a la entrada de la app y sigue cargando `MapRootScreen()` dentro del tema actual.

## Nueva estructura principal

```text
app/src/main/java/com/leonvelez/eventospi/
  MainActivity.kt
  data/
    TokenManager.kt
    model/
    remote/
  ui/
    components/
    model/
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

## Pantallas separadas

- `ui/screens/auth/LoginScreen.kt`
- `ui/screens/auth/RegisterScreen.kt`
- `ui/screens/events/CreateEventScreen.kt`
- `ui/screens/events/EventsListScreen.kt`
- `ui/screens/events/UpdateEventScreen.kt`
- `ui/screens/events/RegisteredEventsScreen.kt`
- `ui/screens/events/PendingRequestsScreen.kt`
- `ui/screens/dashboard/DashboardScreen.kt`
- `ui/screens/profile/ProfileHubScreen.kt`
- `ui/screens/profile/ProfileImageScreen.kt`
- `ui/screens/profile/ChangePasswordScreen.kt`
- `ui/screens/map/MapShellScreen.kt`
- `ui/screens/map/MapTestScreen.kt`
- `ui/navigation/MapRootScreen.kt`

## Componentes separados

- contenedores/formularios: `ui/components/Containers.kt`
- botones/info/avatar: `ui/components/ButtonsAndInfo.kt`
- tarjetas e imágenes de eventos: `ui/components/EventCards.kt`
- reacciones: `ui/components/Reactions.kt`
- tarjeta y filtros del mapa: `ui/components/MapEventCard.kt`
- tarjetas del dashboard: `ui/components/DashboardCards.kt`

## Utilidades separadas

- imágenes multipart: `utils/ImageUploadUtils.kt`
- fechas/formato: `utils/DateTimePickerUtils.kt`
- ownership/usuario actual: `utils/EventOwnershipUtils.kt`
- labels visuales: `utils/EventDisplayUtils.kt`
- mapa/filtros/marcadores: `utils/MapUtils.kt`
- reacciones/backend IDs: `utils/ReactionUtils.kt`
- inscripción/cancelación temporal: `utils/RegistrationSessionState.kt`

## Nota importante sobre inscripción/cancelación

La lógica visual temporal de cancelación quedó centralizada en `utils/RegistrationSessionState.kt`.

Esto conserva el comportamiento actual:

- cancelar marca el evento como cancelado en la sesión actual;
- el mapa puede pintar el estado cancelado en rojo;
- registrarse exitosamente limpia ese estado local;
- cerrar sesión limpia el estado local.

No se inventó una solución definitiva para la persistencia entre sesiones porque depende del contrato final del backend. Cuando el backend devuelva el estado final esperado para cancelación/reinscripción, el punto principal de cambio debe ser `RegistrationSessionState.kt` y las llamadas cercanas en `MapRootScreen.kt` / `RegisteredEventsScreen.kt`.

## Validación local realizada

Se intentó ejecutar:

```bash
./gradlew :app:assembleDebug --no-daemon
```

El entorno no pudo completar la compilación porque el wrapper necesitó descargar Gradle desde `services.gradle.org` y no había acceso de red. El wrapper quedó con permiso ejecutable para facilitar la validación local.
