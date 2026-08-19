# 🌙 Monitoreo de Sueño en Segundo Plano

## 🚀 ¿Qué se implementó?

Se agregó un **Foreground Service** que permite que tu aplicación móvil siga enviando datos de sueño al dashboard **incluso cuando la app está en segundo plano**.

## ✨ Características Nuevas

### 📱 **Funciona en Segundo Plano**
- ✅ La app sigue enviando datos cuando cambias de aplicación
- ✅ Funciona aunque bloquees la pantalla
- ✅ Mantiene la conexión activa durante navegación entre apps
- ✅ Muestra notificación permanente mientras está activo

### 🔔 **Notificación Inteligente**
- 🌙 **Título:** "Monitoreo de Sueño Activo"
- 📊 **Estado actual:** Muestra el estado de sueño en tiempo real
- 🔴 **Botón "Detener":** Para parar el monitoreo desde la notificación
- 📱 **Toca la notificación:** Abre la app directamente

### 🔄 **Estados de la Notificación**
- **"Conectando..."** - Estableciendo conexión WebSocket
- **"Estado: Despierto"** - Mostrando estado actual de sueño
- **"Estado: Sueño Ligero"** - Actualizaciones en tiempo real
- **"Esperando datos..."** - Conectado pero sin datos de sueño

## 🛠️ Cómo Usar

### 1. **Activar Monitoreo**
1. Abre la app móvil
2. Ve a la pantalla **Perfil**
3. Presiona **"Activar Sincronización"**
4. ✅ Aparecerá una notificación permanente
5. 🎉 **¡Listo!** Ahora funciona en segundo plano

### 2. **Enviar Estados de Sueño**
- Toca cualquier botón de estado (Despierto, Ligero, Profundo, REM)
- Los datos se envían **inmediatamente** al dashboard
- La notificación se actualiza con el nuevo estado

### 3. **Desactivar Monitoreo**
**Opción 1:** Desde la app
- Abre la app → Perfil → "Desactivar Sincronización"

**Opción 2:** Desde la notificación
- Toca "Detener" en la notificación

## 🔧 Detalles Técnicos

### **Componentes Implementados:**

1. **SleepMonitoringService**
   - Servicio en primer plano que mantiene WebSocket activo
   - Maneja reconexiones automáticas
   - Gestiona notificaciones dinámicas

2. **SleepServiceManager**
   - Administra el ciclo de vida del servicio
   - Proporciona interfaz simple para la UI

3. **BackgroundSleepViewModel**
   - Reemplaza al ViewModel anterior
   - Conecta la UI con el servicio de segundo plano
   - Mantiene estados sincronizados

### **Permisos Agregados:**
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## 📊 Comportamiento de Conexión

### **Antes (ViewModel normal):**
- ❌ Se desconectaba al cambiar de app
- ❌ Perdía conexión al bloquear pantalla
- ❌ No funcionaba en segundo plano

### **Ahora (Foreground Service):**
- ✅ Mantiene conexión activa en segundo plano
- ✅ Funciona aunque la app esté minimizada
- ✅ Reconexión automática si se pierde la red
- ✅ Notificación visual del estado

## ⚡ Optimización de Batería

### **Consumo Inteligente:**
- 🔋 **Conexión eficiente:** Solo mantiene WebSocket activo
- 📡 **Sin GPS:** No usa ubicación continuamente  
- 🎯 **Targeted wake-up:** Solo se activa para enviar datos
- 💤 **Sleep-friendly:** Diseñado para monitoreo nocturno

### **Gestión de Recursos:**
- Cierre automático al detener sincronización
- Liberación de memoria al cerrar la app
- Reconexión inteligente en caso de errores

## 🐛 Solución al Problema Original

### **Error Previo:**
```
Error: WebSocket error: Software caused connections abort
```

### **Causa:**
- Múltiples conexiones WebSocket concurrentes
- Desconexiones abruptas al cambiar de app
- Falta de gestión del ciclo de vida

### **Solución Implementada:**
- ✅ **Una sola conexión activa** por vez
- ✅ **Desconexión limpia** con mensajes apropiados
- ✅ **Gestión robusta de errores** con timeouts
- ✅ **Reconexión automática** cuando sea necesario

## 🎯 Resultado Final

Tu aplicación móvil ahora puede:

1. **📱 Enviar datos continuamente** - Incluso en segundo plano
2. **🔔 Mostrar estado actual** - A través de notificaciones
3. **🔄 Reconectarse automáticamente** - Si hay problemas de red
4. **🎛️ Control total** - Activar/desactivar desde app o notificación
5. **📊 Dashboard actualizado** - Sin errores de "connection abort"

¡Tu sistema de monitoreo de sueño ahora es completamente robusto y funciona 24/7! 🌙✨
