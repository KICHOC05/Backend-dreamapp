# 🌐 Configuración de Conexión de Red

Este documento explica cómo cambiar entre diferentes entornos de desarrollo y producción para la aplicación.

## 🔧 Configuración Actual

### Archivos de Configuración

1. **`SleepApiService.kt`**: Contiene las URLs de conexión
2. **`network_security_config.xml`**: Permite conexiones HTTP locales
3. **`AndroidManifest.xml`**: Referencia la configuración de seguridad

## 🚀 Cambio Rápido de Entornos

### En `SleepApiService.kt`:

```kotlin
// ⚠️ CAMBIAR AQUÍ: Selecciona la URL activa
private const val BASE_URL = LOCAL_EMULATOR_URL  // <-- Cambiar esta línea
```

### Opciones Disponibles:

#### 1. 🔧 Desarrollo Local - Emulador
```kotlin
private const val BASE_URL = LOCAL_EMULATOR_URL
```
- **URL**: `http://127.0.0.1:5001/dream-34ed4/us-central1/`
- **Uso**: Emulador de Android con Firebase Emulator
- **Requisitos**: Firebase Emulator ejecutándose en localhost

#### 2. 📱 Desarrollo Local - Dispositivo Físico
```kotlin
private const val BASE_URL = LOCAL_DEVICE_URL
```
- **URL**: `http://192.168.1.100:5001/dream-34ed4/us-central1/`
- **Uso**: Dispositivo físico conectado a la misma red
- **Requisitos**: 
  - Ajustar la IP según tu red local
  - Firebase Emulator accesible desde la red

#### 3. ☁️ Producción - Firebase Cloud Functions
```kotlin
private const val BASE_URL = PRODUCTION_URL
```
- **URL**: `https://registerusersleepdata-nmry4bipxq-uc.a.run.app/`
- **Uso**: Entorno de producción
- **Requisitos**: Cloud Functions desplegadas

## 🔒 Configuración de Seguridad de Red

### `network_security_config.xml`

Este archivo permite conexiones HTTP (cleartext) para desarrollo local:

```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">127.0.0.1</domain>
    <domain includeSubdomains="true">localhost</domain>
    <domain includeSubdomains="true">10.0.2.2</domain>
    <!-- Más dominios locales... -->
</domain-config>
```

### Dominios Permitidos:
- `127.0.0.1` - Localhost para emulador
- `localhost` - Localhost alternativo
- `10.0.2.2` - IP especial del emulador Android
- `192.168.x.x` - Rangos de IP locales comunes
- `172.16.x.x` - Rango de IP privadas
- `10.0.x.x` - Otro rango de IP privadas

## 🛠️ Solución de Problemas

### Error: "CLEARTEXT communication not permitted"

**Causa**: Android bloquea conexiones HTTP por seguridad.

**Soluciones**:
1. ✅ Verificar que `network_security_config.xml` esté configurado
2. ✅ Confirmar que `AndroidManifest.xml` referencia el archivo
3. ✅ Añadir el dominio/IP específico al archivo de configuración

### Error: "Connection refused" o "Network unreachable"

**Causa**: El servidor local no está ejecutándose o la IP es incorrecta.

**Soluciones**:
1. ✅ Verificar que Firebase Emulator esté ejecutándose
2. ✅ Confirmar la IP correcta de tu red local
3. ✅ Verificar que el puerto 5001 esté abierto

### Para Dispositivos Físicos:

1. **Encontrar tu IP local**:
   ```bash
   # En Windows
   ipconfig
   
   # En macOS/Linux
   ifconfig
   ```

2. **Actualizar `LOCAL_DEVICE_URL`**:
   ```kotlin
   private const val LOCAL_DEVICE_URL = "http://TU_IP_LOCAL:5001/dream-34ed4/us-central1/"
   ```

3. **Agregar tu IP al `network_security_config.xml`** si no está incluida:
   ```xml
   <domain includeSubdomains="true">TU_IP_LOCAL</domain>
   ```

## 📋 Checklist de Cambio de Entorno

### Desarrollo Local → Producción:
- [ ] Cambiar `BASE_URL` a `PRODUCTION_URL`
- [ ] Verificar que las Cloud Functions estén desplegadas
- [ ] Probar conexión

### Emulador → Dispositivo Físico:
- [ ] Obtener IP local del desarrollo
- [ ] Actualizar `LOCAL_DEVICE_URL` con la IP correcta
- [ ] Agregar IP a `network_security_config.xml` si es necesario
- [ ] Cambiar `BASE_URL` a `LOCAL_DEVICE_URL`
- [ ] Verificar que Firebase Emulator sea accesible desde la red

### Producción → Desarrollo Local:
- [ ] Iniciar Firebase Emulator
- [ ] Cambiar `BASE_URL` a `LOCAL_EMULATOR_URL`
- [ ] Verificar conexión local

## 🔍 Debugging

Para verificar qué URL se está usando:
```kotlin
Log.d("SleepApiClient", "Using URL: ${SleepApiClient.getCurrentBaseUrl()}")
```

## ⚠️ Notas Importantes

1. **Nunca** commitear credenciales o IPs específicas en el repositorio
2. **Siempre** usar HTTPS en producción
3. **Solo** permitir HTTP para desarrollo local
4. **Revisar** que la configuración de seguridad sea apropiada para el entorno
