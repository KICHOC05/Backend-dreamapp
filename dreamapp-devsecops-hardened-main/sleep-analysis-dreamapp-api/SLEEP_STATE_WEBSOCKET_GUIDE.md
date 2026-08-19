# Sleep State WebSocket API

Este documento explica cómo usar el sistema de WebSockets para manejar estados de sueño en tiempo real.

## Endpoints Disponibles

### WebSockets
- `ws://localhost:7070/ws/sleep/mobile` - Para aplicaciones móviles (envío de estados)
- `ws://localhost:7070/ws/sleep/dashboard` - Para dashboard (recepción de estados)

### REST API
- `GET /sleep/states` - Obtener estados actuales de sueño
- `POST /sleep/states` - Cambiar estado de sueño (alternativa al WebSocket)
- `GET /sleep/connections` - Estadísticas de conexiones

## Estados de Sueño Disponibles

| Estado | Color | Descripción |
|--------|-------|-------------|
| REM    | #FF6B6B (Rojo) | Sueño REM |
| LIGHT  | #4ECDC4 (Verde azulado) | Sueño ligero |
| DEEP   | #45B7D1 (Azul) | Sueño profundo |
| AWAKE  | #96CEB4 (Verde) | Despierto |

## Uso desde la App Mobile (Android/Kotlin)

### 1. Conectar al WebSocket

```kotlin
// Dependencia: implementation 'org.java-websocket:Java-WebSocket:1.5.3'

val uri = URI("ws://tu-servidor:7070/ws/sleep/mobile")
val client = object : WebSocketClient(uri) {
    override fun onOpen(handshake: ServerHandshake?) {
        println("Conectado al servidor de estados de sueño")
    }
    
    override fun onMessage(message: String?) {
        // Manejar respuestas del servidor
        println("Respuesta del servidor: $message")
    }
    
    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("Conexión cerrada: $reason")
    }
    
    override fun onError(ex: Exception?) {
        println("Error: ${ex?.message}")
    }
}

client.connect()
```

### 2. Enviar cambio de estado desde los botones

```kotlin
// Función para cambiar estado de sueño
fun changeSleepState(sleepState: String) {
    val message = mapOf(
        "action" to "changeSleepState",
        "userId" to "user123", // ID único del usuario
        "userName" to "Juan Pérez", // Nombre del usuario
        "sleepState" to sleepState, // REM, LIGHT, DEEP, AWAKE
        "deviceId" to "device456", // ID del dispositivo (opcional)
        "location" to mapOf( // Ubicación (opcional)
            "latitude" to -34.6037,
            "longitude" to -58.3816,
            "address" to "Buenos Aires, Argentina"
        )
    )
    
    val json = Gson().toJson(message)
    client.send(json)
}

// Configurar botones
btnREM.setOnClickListener { changeSleepState("REM") }
btnLIGHT.setOnClickListener { changeSleepState("LIGHT") }
btnDEEP.setOnClickListener { changeSleepState("DEEP") }
btnAWAKE.setOnClickListener { changeSleepState("AWAKE") }
```

## Uso desde el Dashboard (JavaScript/Web)

### 1. Conectar al WebSocket

```javascript
const ws = new WebSocket('ws://localhost:7070/ws/sleep/dashboard');

ws.onopen = function(event) {
    console.log('Conectado al dashboard de estados de sueño');
};

ws.onmessage = function(event) {
    const data = JSON.parse(event.data);
    handleSleepStateUpdate(data);
};

ws.onclose = function(event) {
    console.log('Conexión cerrada');
};

ws.onerror = function(error) {
    console.error('Error:', error);
};
```

### 2. Manejar actualizaciones de estado

```javascript
function handleSleepStateUpdate(data) {
    switch(data.action) {
        case 'allStates':
            // Estados iniciales al conectar
            updateMapWithAllStates(data.states);
            break;
            
        case 'stateChange':
            // Cambio de estado individual
            updateUserStateOnMap(data.event);
            break;
    }
}

function updateUserStateOnMap(event) {
    const { userId, userName, sleepState, colorCode, location, timestamp } = event;
    
    // Actualizar punto en el mapa
    if (location && location.latitude && location.longitude) {
        // Ejemplo con Google Maps o cualquier librería de mapas
        updateMapMarker(userId, {
            lat: location.latitude,
            lng: location.longitude,
            color: colorCode,
            title: `${userName} - ${sleepState}`,
            timestamp: timestamp
        });
    }
    
    // Actualizar UI
    updateUserStateDisplay(userId, sleepState, colorCode, timestamp);
}

function updateMapMarker(userId, markerData) {
    // Implementación específica según la librería de mapas que uses
    // Por ejemplo, con Google Maps:
    /*
    const marker = new google.maps.Marker({
        position: { lat: markerData.lat, lng: markerData.lng },
        map: map,
        title: markerData.title,
        icon: {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 10,
            fillColor: markerData.color,
            fillOpacity: 0.8,
            strokeWeight: 2,
            strokeColor: '#ffffff'
        }
    });
    */
}
```

## Ejemplo de uso con REST API (alternativo)

### Cambiar estado desde la app mobile

```kotlin
// Con Retrofit o similar
data class SleepStateRequest(
    val userId: String,
    val userName: String,
    val sleepState: String,
    val deviceId: String? = null,
    val location: LocationData? = null
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

// Llamada HTTP POST
val request = SleepStateRequest(
    userId = "user123",
    userName = "Juan Pérez",
    sleepState = "LIGHT"
)

api.changeSleepState(request)
```

### Obtener estados actuales

```javascript
// Desde el dashboard
fetch('/sleep/states')
    .then(response => response.json())
    .then(data => {
        console.log('Estados actuales:', data.states);
        updateMapWithAllStates(data.states);
    });
```

## Estructura de mensajes

### Mensaje de cambio de estado (Mobile -> Servidor)
```json
{
    "action": "changeSleepState",
    "userId": "user123",
    "userName": "Juan Pérez",
    "sleepState": "LIGHT",
    "deviceId": "device456",
    "location": {
        "latitude": -34.6037,
        "longitude": -58.3816,
        "address": "Buenos Aires, Argentina"
    }
}
```

### Respuesta del servidor (Servidor -> Mobile)
```json
{
    "status": "success",
    "message": "Sleep state updated successfully",
    "sleepState": "LIGHT",
    "timestamp": "2025-08-14T10:30:00"
}
```

### Broadcast al dashboard (Servidor -> Dashboard)
```json
{
    "action": "stateChange",
    "event": {
        "userId": "user123",
        "userName": "Juan Pérez",
        "sleepState": "LIGHT",
        "sleepStateDisplay": "Light",
        "colorCode": "#4ECDC4",
        "timestamp": "2025-08-14T10:30:00",
        "deviceId": "device456",
        "location": {
            "latitude": -34.6037,
            "longitude": -58.3816,
            "address": "Buenos Aires, Argentina"
        }
    }
}
```

## Ventajas de esta implementación

1. **Tiempo Real**: Los cambios se reflejan instantáneamente en el dashboard
2. **Separación de Conexiones**: Mobile y Dashboard tienen endpoints separados para mejor organización
3. **Persistencia**: Los estados se mantienen en memoria para nuevos clientes
4. **Flexibilidad**: Soporta tanto WebSockets como REST API
5. **Información Rica**: Incluye ubicación, timestamp, colores para visualización
6. **Escalable**: Maneja múltiples conexiones de forma eficiente
