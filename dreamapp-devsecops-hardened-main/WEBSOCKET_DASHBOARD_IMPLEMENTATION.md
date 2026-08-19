# Implementación WebSocket Dashboard - Estados de Sueño

## Descripción General

Se ha implementado un sistema completo de WebSocket que permite mostrar en tiempo real los estados de sueño de los usuarios en el Dashboard. El sistema funciona de la siguiente manera:

1. **App Mobile**: Envía estados de sueño al servidor WebSocket
2. **Servidor**: Recibe y distribuye los estados a los dashboards conectados
3. **Dashboard App**: Recibe y muestra los estados de sueño en la lista de usuarios

## Arquitectura del Sistema

```
[App Mobile] ---(WebSocket)---> [Servidor] ---(WebSocket)---> [Dashboard App]
      |                             |                              |
   ProfileScreen              SleepStateController          DashboardScreen
      |                             |                              |
SleepWebSocketViewModel    ws/sleep/mobile & dashboard    SleepStateViewModel
```

## Componentes Implementados

### 1. App Mobile (appmobile)

#### ProfileScreen.kt
- **Funcionalidad**: Pantalla donde los usuarios pueden activar la sincronización y enviar estados de sueño
- **Características**:
  - Botón de "Activar/Desactivar sincronización"
  - Indicador de estado de conexión (verde/rojo)
  - Botones para cada fase de sueño (Despierto, Ligero, Profundo, REM)
  - Visualización del estado actual

#### SleepWebSocketClient.kt
- **Funcionalidad**: Cliente WebSocket para enviar estados al servidor
- **Endpoint**: `ws://192.168.0.8:7070/ws/sleep/mobile`
- **Datos enviados**:
  ```json
  {
    "action": "changeSleepState",
    "userId": "user_uid_123",
    "userName": "Nombre Usuario",
    "sleepState": "LIGHT",
    "deviceId": "android_device_1234567890"
  }
  ```

#### SleepWebSocketViewModel.kt
- **Funcionalidad**: Gestiona el estado de conexión y envío de datos
- **Estados**:
  - `isConnected`: Estado de conexión WebSocket
  - `isSyncEnabled`: Si la sincronización está activada
  - `currentSleepState`: Último estado enviado

### 2. Dashboard App (dashboardapp)

#### DashboardScreen.kt
- **Funcionalidad**: Pantalla principal que muestra la lista de usuarios con sus estados
- **Características**:
  - Conexión automática al WebSocket de dashboard
  - Actualización en tiempo real de estados de sueño
  - Integración con lista de usuarios existente

#### DashboardWebSocketClient.kt
- **Funcionalidad**: Cliente WebSocket para recibir estados del servidor
- **Endpoint**: `ws://192.168.0.8:7070/ws/sleep/dashboard`
- **Datos recibidos**:
  ```json
  {
    "action": "stateChange",
    "event": {
      "userId": "user_uid_123",
      "userName": "Nombre Usuario",
      "sleepState": "LIGHT",
      "sleepStateDisplay": "Sueño Ligero",
      "colorCode": "#4ECDC4",
      "timestamp": "2025-08-14T10:30:00"
    }
  }
  ```

#### SleepStateViewModel.kt
- **Funcionalidad**: Gestiona los estados de sueño recibidos del WebSocket
- **Estados**:
  - `sleepStates`: Map de userId -> SleepStateUpdate
  - `isConnected`: Estado de conexión del dashboard WebSocket

#### ListUsers.kt (Actualizado)
- **Funcionalidad**: Componente que muestra cada usuario en la lista
- **Nuevas características**:
  - Parámetros opcionales para estado de sueño
  - Muestra icono y color del estado de sueño
  - Fallback a "Activo/Inactivo" si no hay estado

### 3. Servidor (sleep-analysis-dreamapp-api)

#### SleepStateController.kt
- **Endpoints WebSocket**:
  - `/ws/sleep/mobile`: Para recibir estados de apps móviles
  - `/ws/sleep/dashboard`: Para enviar estados a dashboards
- **Funcionalidades**:
  - Almacenamiento en memoria de estados actuales
  - Broadcast automático a dashboards conectados
  - Manejo de conexiones y desconexiones

## Estados de Sueño

| Estado | Nombre | Icono | Color |
|--------|--------|-------|-------|
| AWAKE | Despierto | 🟡 | #96CEB4 |
| LIGHT | Sueño Ligero | 🟢 | #4ECDC4 |
| DEEP | Sueño Profundo | 🔵 | #45B7D1 |
| REM | REM | 🔴 | #FF6B6B |

## Flujo de Datos

### 1. Envío de Estado (Mobile → Servidor)
1. Usuario presiona botón en ProfileScreen
2. SleepWebSocketViewModel.sendSleepState()
3. SleepWebSocketClient envía JSON al servidor
4. Servidor responde con confirmación

### 2. Distribución de Estado (Servidor → Dashboard)
1. Servidor recibe estado de mobile
2. Almacena en userSleepStates
3. Hace broadcast a todos los dashboards conectados
4. Dashboard actualiza la UI automáticamente

### 3. Visualización en Dashboard
1. DashboardWebSocketClient recibe mensaje
2. SleepStateViewModel actualiza sleepStates
3. DashboardScreen re-renderiza con nuevos datos
4. ListUsers muestra el estado actualizado

## Configuración de Red

### IP del Servidor
- **Actual**: `192.168.0.8:7070`
- **Para cambiar**: Modificar `serverUrl` en ambos ViewModels

### Requisitos
- Dispositivos en la misma red WiFi
- Servidor ejecutándose en puerto 7070
- Permisos de INTERNET en ambas apps

## Testing

### 1. Probar Conexión
```bash
# Verificar servidor HTTP
curl http://192.168.0.8:7070/

# Verificar WebSocket (usando herramienta como wscat)
wscat -c ws://192.168.0.8:7070/ws/sleep/dashboard
```

### 2. Logs de Debug
- **Mobile**: Tag `SleepWebSocket`
- **Dashboard**: Tag `DashboardWebSocket`
- **Servidor**: Tag `SleepStateController`

### 3. Flujo Completo
1. Iniciar servidor: `./gradlew run` en sleep-analysis-dreamapp-api
2. Abrir Dashboard App y verificar conexión
3. Abrir Mobile App, activar sincronización
4. Enviar estados desde Mobile
5. Verificar actualización en Dashboard

## Dependencias Agregadas

### Mobile App (appmobile/build.gradle.kts)
```kotlin
implementation("org.java-websocket:Java-WebSocket:1.5.3")
```

### Dashboard App (dashboardapp/build.gradle.kts)
```kotlin
implementation("org.java-websocket:Java-WebSocket:1.5.3")
```

## Archivos Modificados/Creados

### Mobile App
- ✅ **Nuevo**: `SleepStateWebSocketClient.kt`
- ✅ **Nuevo**: `SleepWebSocketViewModel.kt`
- ✅ **Modificado**: `ProfileScreen.kt`
- ✅ **Modificado**: `build.gradle.kts`

### Dashboard App
- ✅ **Nuevo**: `DashboardWebSocketClient.kt`
- ✅ **Nuevo**: `SleepStateViewModel.kt`
- ✅ **Modificado**: `DashboardScreen.kt`
- ✅ **Modificado**: `ListUsers.kt`
- ✅ **Modificado**: `build.gradle.kts`

## Próximas Mejoras

- [ ] Indicador de conexión en Dashboard
- [ ] Historial de cambios de estado
- [ ] Filtros por estado de sueño
- [ ] Notificaciones de cambios críticos
- [ ] Configuración de IP desde UI
- [ ] Reconexión automática mejorada
