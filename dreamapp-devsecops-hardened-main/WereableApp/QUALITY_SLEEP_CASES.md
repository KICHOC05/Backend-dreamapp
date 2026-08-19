# 🎯 Casos de Uso por Calidad de Sueño

Este documento describe la implementación de botones específicos para generar datos de sueño simulados según la calidad definida en el esquema `sleepDataUserSchema.quality`.

## � Validación de Fechas Duplicadas

### Sistema de Control de Envíos
Se implementó un sistema de validación para evitar enviar datos duplicados:

- **TODAY**: Solo se permite un envío por día actual
- **YESTERDAY**: Solo se permite un envío para el día anterior  
- **RANDOM_PAST y QUALITY**: Permiten múltiples envíos (para testing)

### Características:
- Los botones se deshabilitan automáticamente si ya se envió para esa fecha
- El texto del botón cambia a "Ya enviado hoy/ayer" cuando no está disponible
- Se mantiene un registro por usuario de las fechas ya enviadas
- Error descriptivo si se intenta enviar una fecha duplicada

## � Valores de Calidad Implementados

Según el esquema TypeScript, el campo `quality` puede tener los siguientes valores:
```typescript
* @field quality - Overall sleep quality rating (POOR, FAIR, GOOD, EXCELLENT)
```

## 🎨 Interfaz de Usuario Mejorada

### Iconos en lugar de Emojis
Se reemplazaron todos los emojis por iconos de Material Design:

#### Sección "Subir Datos a la Nube":
- **DateRange**: Datos de Hoy
- **CalendarToday**: Datos de Ayer  
- **Shuffle**: Datos Aleatorios
- **CloudUpload**: Ícono del título

#### Sección "Casos de Uso por Calidad":
- **SentimentVeryDissatisfied**: Calidad POBRE
- **SentimentNeutral**: Calidad REGULAR
- **SentimentSatisfied**: Calidad BUENA
- **SentimentVerySatisfied**: Calidad EXCELENTE
- **Assessment**: Ícono del título

### Estados Visuales:
- Botones deshabilitados con color gris cuando la fecha ya fue enviada
- Indicadores de progreso durante las subidas
- Colores específicos por tipo de calidad
- Mensajes informativos claros

## 🔧 Implementación Técnica

### 1. SleepUploadViewModel Mejorado
```kotlin
// Control de fechas enviadas por usuario
private val _uploadedDates = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

// Métodos de validación
fun canUploadForDate(userId: String, dataType: SleepDataType): Boolean
private fun isDateAlreadyUploaded(userId: String, date: String): Boolean
private fun markDateAsUploaded(userId: String, date: String)
```

### 2. Validación en UI
```kotlin
// Verificar disponibilidad de fechas
val canUploadToday = userData?.let { 
    sleepUploadViewModel.canUploadForDate(it.userId, SleepDataType.TODAY) 
} ?: false

val canUploadYesterday = userData?.let { 
    sleepUploadViewModel.canUploadForDate(it.userId, SleepDataType.YESTERDAY) 
} ?: false
```

### 3. Botones Adaptativos
```kotlin
enabled = !isUploading && userData != null && canUploadToday,
colors = ButtonDefaults.buttonColors(
    containerColor = if (canUploadToday) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.outline
)
```

## 📋 Uso Actualizado

1. **Iniciar sesión** en la aplicación
2. Navegar a la pantalla de **Perfil**
3. **Sección "Subir Datos a la Nube":**
   - Los botones "Hoy" y "Ayer" se deshabilitan después del primer envío
   - "Datos Aleatorios" siempre disponible
4. **Sección "Casos de Uso por Calidad":**
   - Todos los botones siempre disponibles (para testing)
   - Iconos claros representando cada calidad

## ⚠️ Mensajes de Error Específicos

- **Fecha duplicada**: "Ya existe una sesión de sueño para la fecha YYYY-MM-DD"
- **Estado visual**: "Ya enviado hoy/ayer" en botones deshabilitados
- **Validación previa**: Evita llamadas innecesarias al servidor

## � Beneficios de la Validación

1. **Prevención de duplicados**: Evita datos inconsistentes en Firebase
2. **Mejor UX**: Feedback visual inmediato del estado
3. **Optimización**: Reduce llamadas innecesarias al servidor
4. **Consistencia**: Mantiene la integridad de los datos por fecha
5. **Claridad**: Iconos universales en lugar de emojis dependientes del sistema

## 🚀 Testing Recomendado

### Para validar la funcionalidad:
1. Enviar datos de "Hoy" → verificar que se deshabilite
2. Enviar datos de "Ayer" → verificar que se deshabilite  
3. Cerrar y abrir app → verificar que se mantenga el estado
4. Usar diferentes calidades → verificar que siempre estén disponibles
5. Intentar envío duplicado → verificar mensaje de error

### Casos de Uso de Calidad:
- **POOR**: Eficiencias bajas, muchos despertares
- **FAIR**: Métricas moderadas  
- **GOOD**: Buenos patrones de sueño
- **EXCELLENT**: Patrones óptimos

Todos los datos siguen cumpliendo con las validaciones del `sleepDataUserSchema` y proporcionan experiencias de testing realistas.
