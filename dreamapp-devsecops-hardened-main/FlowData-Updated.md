# Flujo de Datos Actualizado - Aplicativo IoT Sleep Monitoring

## Arquitectura del Flujo de Datos

### **FLUJO COMPLETO:**
```
WEARABLE → APP MOBILE → FIREBASE FUNCTIONS → FIRESTORE
```

---

## **1. WEARABLE DEVICE**
**Responsabilidades:**
- Capturar datos de sensores cada 30 segundos (@AlisonCampos Aqui le cambias al tiempo segun lo que establescas en las ventanas de tiempo)
- **Calcular métricas HRV (RMSSD, SDNN) en tiempo real**
- **Detectar fases de sueño usando algoritmos locales**
- **Calcular métricas de resumen de sesión (eficiencia, promedios, calidad)**
- **Procesar y analizar datos fisiológicos sin depender de conectividad**
- Enviar datos ya procesados al teléfono móvil

**Procesamiento Local:**
1. **Sensores**: Heart Rate + Acelerómetro
2. **Calcula HRV**: RMSSD y SDNN a partir de intervalos RR
3. **Detecta Fase**: Algoritmo basado en tablas de referencia
4. **Calcula Métricas de Sesión**: Eficiencia, promedios, calidad, duraciones
5. **Envía**: Datos completos al móvil

**Experiencia del Usuario al Despertar:**
```
¡Cuando te despiertes, tus estadísticas ya están listas!
Abres el teléfono → Datos instantáneos
Sin esperas → Métricas calculadas durante la noche
```

**Datos que envía el weerable al telefono en tiempo real (@AlisonCampos Esto ya lo hace):**
```json
{
  "phase": "LIGHT|DEEP|REM|AWAKE",
  "datetime": "2025-01-15T22:30:00Z",
  "hr_bpm": 65,
  "hrv_rmssd": 42,
  "hrv_sdnn": 40
}
```

**Datos que envía el wearable al teléfono cuando ya terminó el ciclo (@AlisonCampos se envía como JSON usando WearOS Data Layer API):**

```json
{
  "date": "2025-01-15",
  "startTime": "2025-01-15T22:30:00Z",
  "endTime": "2025-01-16T06:30:00Z",
  "timezone": "America/New_York",
  "totalDuration": 480,
  "sleepDuration": 420,
  "lightSleepMinutes": 200,
  "deepSleepMinutes": 120,
  "remSleepMinutes": 100,
  "awakeDuration": 60,
  "sleepEfficiency": 87.5,
  "awakeningsCount": 3,
  "quality": "GOOD", // 'POOR', 'FAIR', 'GOOD', 'EXCELLENT'
  "avgHeartRate": 58,
  "minHeartRate": 52,
  "maxHeartRate": 72,
  "avgMovement": 15,
  "avgRmssd": 45,
  "avgSdnn": 42,
  "sleepPhaseData": [ // Todos los datos de la tabla SleepDataPhases
    {
      "id": 1,
      "phase": "LIGHT",
      "datetime": "2025-01-15T22:30:00Z",
      "hr_bpm": 65,
      "hrv_rmssd": 42,
      "hrv_sdnn": 40
    },
    {
      "id": 2,
      "phase": "DEEP",
      "datetime": "2025-01-15T22:30:30Z",
      "hr_bpm": 58,
      "hrv_rmssd": 48,
      "hrv_sdnn": 45
    }
    // ... todas las mediciones
  ]
}
```

**Algoritmos en el Wearable:**
- **HRV Calculation**: RMSSD y SDNN calculados a partir de 20 intervalos RR
- **Sleep Phase Detection**: Tabla de referencia con rangos por fase
- **Movement Analysis**: Magnitud calculada desde acelerómetro (X,Y,Z)
- **User Adjustment**: Tabla ajustada según edad del usuario
- **Real-time Processing**: Cálculos cada 30 segundos durante toda la noche
- **Data Quality**: Validación automática de señales fisiológicas
- **Session Metrics**: Eficiencia de sueño, promedios, calidad general
- **Duration Calculation**: Tiempo total, tiempo dormido, tiempo por fases

**Ventajas del Procesamiento en el Reloj:**
```
Sin esperas al despertar
Optimización de batería
Funciona sin internet
Métricas en tiempo real
Precisión especializada
Métricas de sesión completas
```


**Tabla Local (@AlisonCampos Esta ya esta): `SleepDataPhases`**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | INTEGER | ID secuencial |
| phase | TEXT | Fase del sueño |
| datetime | TEXT | Timestamp ISO 8601 |
| hr_bpm | REAL | Frecuencia cardíaca |
| hrv_rmssd | REAL | HRV RMSSD |
| hrv_sdnn | REAL | HRV SDNN |

---

## **2. APP MOBILE**
**Responsabilidades:**
- **Recibir datos ya procesados del wearable**
- Almacenar temporalmente en SQLite
- **Mostrar estadísticas instantáneas al usuario**
- Enviar datos completos a Firebase

**Lo que ve el usuario al despertar:**
```
Abre la app → Dashboard completo listo
Duración: 7h 45min
Eficiencia: 92.8%
Fases: Ligero 47% | Profundo 32% | REM 21%
HR promedio: 58 BPM
Calidad: EXCELENTE
Graficas: Grafica con la cantidad de horas dormidas por fase y cuantos ciclos se completaron en linea de tiempo
```

**Procesamiento:**
1. **Recibe**: Datos ya procesados del wearable (HR, HRV, fase detectada, métricas de sesión)
2. **Almacena**: Datos en SQLite local como respaldo
3. **Visualiza**: Estadísticas instantáneas para el usuario
4. **Estructura**: Datos según schema de Firebase
5. **Envía**: POST a Firebase Function

**@xia204 Datos que envía a Firebase: **
```json
{
  "uidUser": "firebase_user_uid", // Aqui es el uid del usuario que inicio sesion
  "deviceId": "smartwatch_001", // Agregas el ui del smartwatch
  "date": "2025-01-15", // De aqui para abajo ya lo debe de mandar el reloj
  "startTime": "2025-01-15T22:30:00Z",
  "endTime": "2025-01-16T06:30:00Z",
  "timezone": "America/New_York",
  "totalDuration": 480,
  "sleepDuration": 420,
  "lightSleepMinutes": 200,
  "deepSleepMinutes": 120,
  "remSleepMinutes": 100,
  "awakeDuration": 60,
  "sleepEfficiency": 87.5,
  "awakeningsCount": 3,
  "quality": "GOOD", // 'POOR', 'FAIR', 'GOOD', 'EXCELLENT'
  "avgHeartRate": 58,
  "minHeartRate": 52,
  "maxHeartRate": 72,
  "avgMovement": 15,
  "avgRmssd": 45,
  "avgSdnn": 42,
  "sleepPhaseData": [
    {
      "id": 1,
      "phase": "LIGHT",
      "datetime": "2025-01-15T22:30:00Z",
      "hr_bpm": 65,
      "hrv_rmssd": 42,
      "hrv_sdnn": 40
    }
    // ... más mediciones
  ],
  "createdAt": 1705456800000,  // Automatico
  "dataVersion": "1.0"
}
```

**Tabla Local: `SQLITE (MOBILE)`**
- Recibe datos ya procesados del wearable via JSON (convierte de json a sqlite)
- Almacena temporalmente hasta envío exitoso a Firebase
- Respaldo en caso de fallos de red
- Solo presenta datos, no calcula métricas adicionales
- Estructura idéntica al schema de Firebase

---

## **3. CLOUD COMPUTING (Firebase)**

### **Firebase Function: `registerUserSleepData`**
**Endpoint:** `POST /registerUserSleepData`

**Proceso:**
1. **Valida**: Datos contra schema Zod
2. **Genera**: ID único: `{uidUser}_{date}_{timestamp}`
3. **Verifica**: No duplicados
4. **Calcula**: Métricas adicionales (calidad de datos)
5. **Guarda**: En colección `user_data_sleep`

**Validaciones:**
- Formato de fechas ISO 8601
- Rangos de HR (30-200 BPM)
- Rangos de HRV (0-200 ms)
- Consistencia de duraciones
- Fases de sueño válidas

**Respuesta Exitosa:**
```json
{
  "success": true,
  "message": "Sleep data registered successfully",
  "data": {
    "documentId": "user_abc123_2025-01-15_1705456200000",
    "date": "2025-01-15",
    "startTime": "2025-01-15T22:30:00Z",
    "userId": "user_abc123",
    "totalMeasurements": 960,
    "sleepDuration": 420,
    "sleepEfficiency": 87.5,
    "quality": "GOOD"
  }
}
```

---

## **4. FIRESTORE DATABASE**

### **Colección: `user_data_sleep`**
**Estructura del Documento:**
```
user_data_sleep/
├── user_abc123_2025-01-15_1705456200000
├── user_abc123_2025-01-16_1705542600000
├── user_def456_2025-01-15_1705456200000
└── ...
```

**Contenido por Documento:**
- **Identificación**: uidUser, deviceId
- **Tiempo**: date, startTime, endTime, timezone
- **Duraciones**: totalDuration, sleepDuration, fases
- **Calidad**: sleepEfficiency, awakeningsCount, quality
- **Métricas**: avgHeartRate, min/max HR, HRV promedios
- **Datos Raw**: sleepPhaseData[] (todas las mediciones)
- **Metadata**: createdAt, updatedAt, dataQuality, dataVersion

---

## **RESUMEN:**

### **EL RELOJ ES EL CEREBRO DEL SISTEMA**
- Calcula HRV (RMSSD, SDNN) en tiempo real
- Detecta fases de sueño usando algoritmos avanzados
- Procesa datos cada 30 segundos durante toda la noche
- Almacena resultados localmente
- **Calcula TODAS las métricas de sesión (eficiencia, promedios, calidad)**

### **EL MÓVIL ES LA VENTANA AL USUARIO**
- Recibe datos ya procesados del reloj
- Muestra estadísticas instantáneas al despertar
- Sincroniza con Firebase en segundo plano
- Presenta dashboard completo sin esperas
- **NO calcula métricas, solo presenta datos**

### **EL USUARIO VE RESULTADOS INMEDIATOS**
- Se despierta → Toma teléfono → Estadísticas listas
- Sin tiempos de espera o procesamiento
- Métricas completas calculadas durante la noche
- Experiencia fluida y profesional

¡El nuevo flujo está optimizado para tu aplicación de monitoreo de sueño!
