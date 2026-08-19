# 📊 Subida de Datos de Sueño a la Nube

## 🚀 Nueva Funcionalidad Implementada

Se agregó una nueva sección al **ProfileScreen** que permite subir datos de sueño simulados a Firebase Cloud Functions.

## ✨ Características de la Funcionalidad

### 📱 **Interfaz de Usuario**
- **Nueva sección:** "📊 Subir Datos a la Nube"
- **Tres botones de subida:**
  - 📅 **Datos de Hoy** - Sube datos con fecha actual
  - 📆 **Datos de Ayer** - Sube datos con fecha de ayer  
  - 🎲 **Datos Aleatorios** - Sube datos con fecha aleatoria (últimos 7 días)

### 🔔 **Indicadores de Estado**
- **Loading spinner** durante la subida
- **Mensajes de feedback:**
  - ✅ "Datos subidos exitosamente"
  - ❌ "Error: Ya existe una sesión de sueño para esta fecha"
  - ⚠️ "Debes iniciar sesión para subir datos"

### 🎯 **Datos Simulados Generados**

Cada subida incluye datos realistas como:

```json
{
  "uidUser": "usuario_uid_de_google",
  "deviceId": "android_mobile_1234567890",
  "date": "2025-08-17",
  "startTime": "2025-08-16T22:30:00.000Z",
  "endTime": "2025-08-17T06:45:00.000Z",
  "timezone": "America/Mexico_City",
  "totalDuration": 480,
  "sleepDuration": 420,
  "lightSleepMinutes": 200,
  "deepSleepMinutes": 120,
  "remSleepMinutes": 100,
  "awakeDuration": 60,
  "sleepEfficiency": 87.5,
  "awakeningsCount": 3,
  "quality": "GOOD",
  "avgHeartRate": 65,
  "minHeartRate": 48,
  "maxHeartRate": 78,
  "avgMovement": 15,
  "avgRmssd": 45.2,
  "avgSdnn": 52.1,
  "sleepPhaseData": [
    // Array de 26 mediciones con fases: AWAKE, LIGHT, DEEP, REM
    // Datos fisiológicos realistas según cada fase
  ],
  "createdAt": 1723334400000,
  "dataVersion": "1.0"
}
```

## 🛠️ Componentes Implementados

### **1. Modelos de Datos**
- `SleepDataUpload` - Estructura completa de datos de sueño
- `SleepPhaseData` - Datos de cada fase individual
- `SleepUploadResponse` - Respuesta del servidor
- `SleepUploadError` - Manejo de errores

### **2. API Service**
- `SleepApiService` - Interface Retrofit para llamadas HTTP
- `SleepApiClient` - Cliente configurado para la URL de producción
- **Endpoint:** `https://registerusersleepdata-nmry4bipxq-uc.a.run.app/registerUserSleepData`

### **3. Repositorio**
- `CloudSleepDataRepository` - Lógica de negocio para subida
- **Generación de datos simulados realistas**
- **Manejo de errores específicos (409, 400, 403, 500)**

### **4. ViewModel**
- `SleepUploadViewModel` - Gestión de estados de UI
- **Estados reactivos:**
  - `isUploading` - Indica si hay subida en progreso
  - `uploadResult` - Resultado de la última subida
  - `lastUploadMessage` - Mensaje de feedback para el usuario

### **5. UI Components**
- **Card Material Design** con elevación
- **Botones con estados:** Primary, Outlined, Disabled
- **Progress indicator** durante subida
- **Feedback visual** con colores semánticos
- **Auto-limpieza de mensajes** después de 5 segundos

## 📡 Manejo de Errores

### **Errores Específicos Manejados:**

#### **409 - Conflict**
```json
{
    "error": "Conflict",
    "message": "Sleep session already exists",
    "date": "2025-08-17",
    "startTime": "2025-08-16T22:30:00.000Z"
}
```
**UI:** ❌ "Ya existe una sesión de sueño para esta fecha: 2025-08-17"

#### **400 - Bad Request**
```json
{
    "error": "Validation error",
    "message": "Invalid sleep data format"
}
```
**UI:** ❌ "Datos inválidos: Invalid sleep data format"

#### **403 - Forbidden**
**UI:** ❌ "Permisos insuficientes"

#### **500 - Server Error**
**UI:** ❌ "Error del servidor: [mensaje]"

## 🔧 Configuración Técnica

### **Dependencias Agregadas:**
```kotlin
// Retrofit para API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.google.code.gson:gson:2.10.1")
```

### **Permisos Requeridos:**
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 🎮 Cómo Usar

### **Para el Usuario:**
1. **Abrir ProfileScreen**
2. **Scroll hacia abajo** hasta "📊 Subir Datos a la Nube"
3. **Seleccionar tipo de datos:**
   - **Datos de Hoy** - Para probar conflictos (puede existir)
   - **Datos de Ayer** - Más probable que sea único
   - **Datos Aleatorios** - Siempre único (fecha aleatoria)
4. **Ver feedback en tiempo real**
5. **Mensaje se auto-limpia** después de 5 segundos

### **Estados de la UI:**
- **Antes de subir:** Botones habilitados
- **Durante subida:** Loading spinner, botones deshabilitados
- **Después de subir:** Mensaje de éxito/error, botones habilitados
- **Sin login:** Mensaje de advertencia, botones deshabilitados

## 🔍 Datos Generados

### **Patrones Realistas:**
- **Fases de sueño:** AWAKE → LIGHT → DEEP → REM (cíclico)
- **Frecuencia cardíaca:** Varía según la fase
  - AWAKE: 70-78 BPM
  - LIGHT: 60-68 BPM  
  - DEEP: 48-55 BPM
  - REM: 65-72 BPM
- **HRV:** Valores realistas según investigación médica
- **Duración:** 8 horas totales, 7 horas de sueño efectivo
- **Eficiencia:** 87.5% (valor saludable)

### **Unicidad de Datos:**
- **Device ID:** `android_mobile_[timestamp]`
- **Fechas variables:** Hoy, ayer, o aleatoria
- **Timestamp:** UTC con zona horaria América/México

¡La funcionalidad está lista para probar la integración con tu Firebase Cloud Function! 🚀
