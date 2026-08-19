# Implementación WebSocket en ProfileScreen

## Descripción

Se ha implementado la funcionalidad de WebSocket en la pantalla de perfil (`ProfileScreen`) de la aplicación móvil para sincronizar los estados de sueño en tiempo real con el servidor.

## Funcionalidades Implementadas

### 1. Cliente WebSocket (`SleepStateWebSocketClient.kt`)
- Conexión WebSocket al servidor de estados de sueño
- Envío de estados de sueño (REM, LIGHT, DEEP, AWAKE)
- Manejo de conexión/desconexión
- Gestión de errores y reconexión automática

### 2. ViewModel (`SleepWebSocketViewModel.kt`)
- Gestión del estado de conexión
- Control de activación/desactivación de sincronización
- Envío de estados de sueño al servidor
- Gestión de lifecycle para liberar recursos

### 3. UI Actualizada (`ProfileScreen.kt`)
- **Botón de Activar Sincronización**: Permite conectar/desconectar del servidor WebSocket
- **Indicador de Estado**: Muestra si está conectado (verde) o desconectado (rojo)
- **Botones de Fases de Sueño**: Solo habilitados cuando la sincronización está activa
  - 🟡 Despierto (AWAKE)
  - 🟢 Sueño Ligero (LIGHT)
  - 🔵 Sueño Profundo (DEEP)
  - 🔴 REM
- **Estado Actual**: Muestra la última fase de sueño seleccionada

## Configuración del Servidor

### URL del Servidor
Por defecto, la aplicación intenta conectarse a:
```
ws://192.168.1.100:7070/ws/sleep/mobile
```

Para cambiar la IP del servidor, modifica la variable `serverUrl` en `SleepWebSocketViewModel.kt`:
```kotlin
private val serverUrl = "ws://TU_IP_SERVIDOR:7070/ws/sleep/mobile"
```

### Iniciar el Servidor
Antes de usar la funcionalidad, asegúrate de que el servidor WebSocket esté ejecutándose:
```bash
cd sleep-analysis-dreamapp-api
./gradlew run
```

## Cómo Usar

1. **Activar Sincronización**:
   - En la pantalla de perfil, presiona el botón "Activar sincronización"
   - Verifica que el indicador cambie a "Conectado" (verde)

2. **Enviar Estados de Sueño**:
   - Una vez conectado, aparecerán los botones de fases de sueño
   - Presiona cualquier botón para enviar ese estado al servidor
   - El estado actual se mostrará en la parte superior de la sección

3. **Desactivar Sincronización**:
   - Presiona "Desactivar sincronización" para desconectarte
   - Los botones de fases se ocultarán automáticamente

## Estructura de Mensajes

### Mensaje Enviado al Servidor
```json
{
    "action": "changeSleepState",
    "userId": "user123",
    "userName": "NombreUsuario",
    "sleepState": "LIGHT",
    "deviceId": "android_device_1234567890"
}
```

### Respuesta del Servidor
```json
{
    "status": "success",
    "message": "Sleep state updated successfully",
    "sleepState": "LIGHT",
    "timestamp": "2025-08-14T10:30:00"
}
```

## Estados de Sueño Disponibles

| Estado | Nombre | Icono | Color |
|--------|--------|-------|-------|
| AWAKE | Despierto | 🟡 | #96CEB4 |
| LIGHT | Sueño Ligero | 🟢 | #4ECDC4 |
| DEEP | Sueño Profundo | 🔵 | #45B7D1 |
| REM | REM | 🔴 | #FF6B6B |

## Dependencias Agregadas

Se agregó la dependencia WebSocket en `build.gradle.kts`:
```kotlin
implementation("org.java-websocket:Java-WebSocket:1.5.3")
```

## Archivos Modificados

1. **Nuevos archivos**:
   - `SleepStateWebSocketClient.kt`
   - `SleepWebSocketViewModel.kt`

2. **Archivos modificados**:
   - `ProfileScreen.kt`
   - `build.gradle.kts` (dependencias)

## Troubleshooting

### Problemas de Conexión
1. Verifica que el servidor esté ejecutándose
2. Confirma que la IP del servidor sea correcta
3. Asegúrate de que el puerto 7070 esté abierto
4. Verifica la conectividad de red

### Logs de Debug
Los logs se muestran en Android Studio con el tag `SleepWebSocket`:
```
D/SleepWebSocket: Conectado al servidor WebSocket
D/SleepWebSocket: Estado enviado: {"action":"changeSleepState",...}
```

## Próximas Mejoras

- [ ] Configuración de IP del servidor desde la UI
- [ ] Persistencia del último estado de sueño
- [ ] Notificaciones push para cambios de estado
- [ ] Historial de estados de sueño enviados
- [ ] Indicador de latencia de conexión
