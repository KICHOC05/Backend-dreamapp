# 📱 Lista de Usuarios en Dashboard - Estados WebSocket

## 🎯 **Cómo Funciona Ahora**

### **En la Lista de Usuarios se muestra:**

```
┌─────────────────────────────────────────────────┐
│ 👤 [Foto] Juan Pérez                          │
│             Galaxy Watch 4                     │
│             🟢 ● Sueño Ligero            📱    │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│ 👤 [Foto] María García                         │
│             Galaxy Watch 4                     │
│             🔴 ● REM                      📱    │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│ 👤 [Foto] Carlos López                         │
│             Galaxy Watch 4                     │
│             ⚫ ● Desconectado             📱    │
└─────────────────────────────────────────────────┘
```

## 🔄 **Estados Posibles**

### **✅ Usuario Conectado (Enviando Estados)**
- **Indicador**: 🟢 Verde
- **Estados**: 
  - 🟡 **Despierto** (#96CEB4)
  - 🟢 **Sueño Ligero** (#4ECDC4)  
  - 🔵 **Sueño Profundo** (#45B7D1)
  - 🔴 **REM** (#FF6B6B)

### **❌ Usuario Desconectado**
- **Indicador**: 🔴 Rojo
- **Estado**: ⚫ **Desconectado** (#FF5722)

## 🛠 **Lógica de Conexión**

### **Usuario se considera "Conectado" cuando:**
- Tiene un estado de sueño activo en el mapa `sleepStates[userId]`
- Ha enviado un estado recientemente (menos de 2 minutos)

### **Usuario se considera "Desconectado" cuando:**
- No existe en el mapa `sleepStates[userId]`
- Su último estado es muy antiguo (más de 2 minutos)

### **Limpieza Automática:**
- Si el WebSocket se desconecta → espera 30 segundos
- Limpia estados de más de 2 minutos de antigüedad
- Actualiza la UI automáticamente

## 📊 **Flujo Completo**

```
Mobile App                 Servidor                Dashboard App
    │                         │                         │
    │──── 🔄 Activa Sync ────▶│                         │
    │                         │                         │
    │──── 🟢 Envía "LIGHT" ───▶│                         │
    │                         │                         │
    │                         │──── 📡 Broadcast ──────▶│
    │                         │                         │
    │                         │                   ✅ Muestra: 
    │                         │                   "🟢 Sueño Ligero"
    │                         │                         │
    │──── ❌ Se Desconecta ───▶│                         │
    │                         │                         │
    │                         │──── ⏰ Después 2min ───▶│
    │                         │                         │
    │                         │                   ❌ Muestra:
    │                         │                   "⚫ Desconectado"
```

## 🎨 **Colores y Estilos**

### **Estados de Sueño**
- **Fuente**: `FontWeight.Medium`
- **Colores**: Según estado específico
- **Icono**: Emoji correspondiente

### **Estado Desconectado**  
- **Fuente**: `FontWeight.Normal`
- **Color**: `Color.Red` (#FF5722)
- **Icono**: ⚫ (círculo negro)

### **Indicador de Conexión**
- **Conectado**: 🟢 Círculo verde (8dp)
- **Desconectado**: 🔴 Círculo rojo (8dp)

## 🧪 **Testing**

### **Probar Conexión:**
1. Abrir Dashboard → Ver usuarios "Desconectado"
2. Abrir Mobile → Activar sincronización  
3. Enviar estado → Ver cambio en Dashboard
4. Cerrar Mobile → Esperar 2 minutos → Ver "Desconectado"

### **Estados de Prueba:**
- ✅ Usuario conectado con estado actual
- ❌ Usuario desconectado  
- 🔄 Transición conectado → desconectado
- 🔄 Transición desconectado → conectado

## 📝 **Logs para Debug**

```kotlin
// Dashboard
Log.d("SleepStateViewModel", "Estados iniciales cargados: ${states.size}")
Log.d("SleepStateViewModel", "Estado actualizado para ${userId}: ${sleepState}")
Log.d("SleepStateViewModel", "Estados antiguos limpiados")

// Mobile  
Log.d("SleepWebSocket", "Estado enviado: $jsonMessage")
Log.d("SleepWebSocket", "✅ Conectado al servidor WebSocket")
```

¡El sistema está completamente implementado y funcional! 🚀
