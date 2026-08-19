# Servidor de Inteligencia Artificial - DreamApp

Este servidor proporciona capacidades de inteligencia artificial para el análisis avanzado de datos de sueño utilizando **Ollama** y **Open WebUI**.

## Descripción

El servidor de IA es responsable de:
- Análisis inteligente de patrones de sueño
- Generación de recomendaciones personalizadas
- Procesamiento de datos complejos de fases del sueño

## Características Nuevas

- ✅ **Descarga automática de modelos**: El modelo `qwen2.5:0.5b` se descarga automáticamente al iniciar
- ✅ **Sin comandos manuales**: No necesitas ejecutar comandos para descargar modelos
- ✅ **Healthcheck configurado**: Los servicios se aseguran de estar listos antes de iniciarse
- ✅ **Archivos ignorados**: Las carpetas de datos están incluidas en `.gitignore`

## Inicio Rápido

1. **Iniciar los servicios:**
   ```bash
   cd ia
   docker-compose up -d
   ```

2. **Verificar el estado:**
   ```bash
   docker-compose ps
   docker-compose logs ollama
   ```

3. **Acceder a las interfaces:**
   - Open WebUI: http://localhost:3000
   - API de Ollama: http://localhost:11434
- Interfaz conversacional para consultas sobre el sueño

## Arquitectura

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Javalin API   │────│   Ollama Server  │────│  Open WebUI     │
│  (Puerto 7000)  │    │  (Puerto 11434)  │    │  (Puerto 3000)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Tecnologías Utilizadas

- **[Ollama](https://ollama.ai/)**: Motor de IA local para modelos de lenguaje
- **[Open WebUI](https://openwebui.com/)**: Interfaz web para interactuar con modelos
- **Docker & Docker Compose**: Containerización y orquestación
- **Modelos LLM**: Llama2, Mistral, etc.

## Componentes

### 1. Ollama Server
- **Puerto**: `11434`
- **Función**: Ejecuta modelos de lenguaje localmente
- **Modelos soportados**: Llama2, Mistral, CodeLlama, etc.
- **Volumen**: `./ollama/models` para persistencia de modelos

### 2. Open WebUI
- **Puerto**: `3000`
- **Función**: Interfaz web para chat con IA
- **Características**:
  - Chat conversacional
  - Gestión de modelos
  - Historial de conversaciones
  - API REST integrada

## Instalación y Configuración

### Prerequisitos

- Docker Desktop instalado
- Docker Compose
- Mínimo 8GB RAM (recomendado 16GB)
- 10GB de espacio libre en disco

### 1. Clonar el repositorio
```bash
git clone <repository-url>
cd aplicativo-iot/ia-server
```

### 2. Crear volúmenes Docker
```bash
docker volume create ollama
docker volume create open-webui
```

### 3. Iniciar los servicios
```bash
cd ia
docker-compose up -d
```

### 4. Verificar que los servicios estén corriendo
```bash
docker-compose ps
```

## Uso

### Acceso a la Interfaz Web
- **Open WebUI**: http://localhost:3000
- **Ollama API**: http://localhost:11434

### Descargar Modelos

Los modelos no se descargan automáticamente. Debes ejecutar:

```bash
# Modelo básico Llama2
docker-compose exec ollama ollama pull llama2

# Modelo optimizado para código
docker-compose exec ollama ollama pull codellama

# Modelo Mistral (más eficiente)
docker-compose exec ollama ollama pull mistral
```

### Modelos Recomendados para Análisis de Sueño

```bash
# Para análisis de datos y recomendaciones
docker-compose exec ollama ollama pull mistral:7b

# Para procesamiento de texto médico
docker-compose exec ollama ollama pull llama2:13b

# Para consultas rápidas
docker-compose exec ollama ollama pull phi
```

## Configuración Avanzada

### Variables de Entorno

El `docker-compose.yaml` incluye las siguientes configuraciones:

```yaml
environment:
  - OLLAMA_MODELS=/ollama/models
  - RAG_EMBEDDING_ENGINE=ollama
  - AUDIO_STT_ENGINE=openai
```

### Personalización de Modelos

Para usar modelos específicos en tu aplicación:

```bash
# Listar modelos disponibles
docker-compose exec ollama ollama list

# Eliminar modelo no usado
docker-compose exec ollama ollama rm <modelo>
```

## Integración con la API

### Endpoints Principales

#### 1. Generar Respuesta
```http
POST http://localhost:11434/api/generate
Content-Type: application/json

{
  "model": "llama2",
  "prompt": "Analiza estos datos de sueño: {...}",
  "stream": false
}
```

#### 2. Chat Conversacional
```http
POST http://localhost:11434/api/chat
Content-Type: application/json

{
  "model": "llama2",
  "messages": [
    {
      "role": "user",
      "content": "¿Qué recomendaciones tienes para mejorar mi sueño?"
    }
  ]
}
```

### Ejemplo de Integración con Javalin

```kotlin
// En tu servidor Javalin
val response = client.post("http://localhost:11434/api/generate") {
    setBody(mapOf(
        "model" to "llama2",
        "prompt" to "Analiza estos datos de sueño: $sleepData",
        "stream" to false
    ))
}
```

## Casos de Uso para DreamApp

### 1. Análisis de Patrones de Sueño
```
Prompt: "Analiza estos datos de sueño de los últimos 7 días: 
- Hora de dormir: 23:30-00:15
- Duración: 7.5-8.2 horas  
- Despertares: 2-4 por noche
- Fases REM: 18-22%
¿Qué patrones identificas?"
```

### 2. Recomendaciones Personalizadas
```
Prompt: "Basándote en el análisis anterior, genera 5 recomendaciones 
específicas para mejorar la calidad del sueño."
```

### 3. Interpretación de Datos de Sensores
```
Prompt: "Interpreta estos datos del acelerómetro durante el sueño:
- Movimientos por hora: [datos]
- Variabilidad cardíaca: [datos]
- Temperatura corporal: [datos]"
```

## Solución de Problemas

### Problema: Contenedores no inician
```bash
# Verificar logs
docker-compose logs ollama
docker-compose logs open-webui

# Reiniciar servicios
docker-compose down
docker-compose up -d
```

### Problema: Modelos no se descargan
```bash
# Verificar conectividad
docker-compose exec ollama ollama list

# Descargar manualmente
docker-compose exec ollama ollama pull llama2
```

### Problema: Poco rendimiento
- Aumentar RAM asignada a Docker
- Usar modelos más pequeños (phi, mistral:7b)
- Verificar que no hay otros procesos pesados

## Logs y Monitoreo

### Ver logs en tiempo real
```bash
# Todos los servicios
docker-compose logs -f

# Solo Ollama
docker-compose logs -f ollama

# Solo Open WebUI
docker-compose logs -f open-webui
```

### Monitoreo de recursos
```bash
# Uso de recursos por contenedor
docker stats

# Espacio utilizado por volúmenes
docker system df
```

## Despliegue en Producción

Para despliegue en producción:

1. **Configurar reverse proxy** (nginx)
2. **Añadir certificados SSL**
3. **Configurar variables de entorno** para producción
4. **Implementar autenticación** en Open WebUI
5. **Configurar backups** de modelos y datos

## Licencia

Este proyecto es parte del sistema DreamApp desarrollado por el equipo de la Universidad Tecnológica Tula Tepeji.

---

**Desarrollado por**: UTT Team v2  
**Integrantes**: Xiadani Citlalli Vazquez Patiño, Monserrat Belen Flores Lopez, Alison Campos Aguilar, Fernando Leon Monroy