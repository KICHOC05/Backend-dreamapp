# Deploy de DreamApp API en Render

El backend usa una API de IA compatible con OpenAI. La configuración predeterminada apunta a Groq; Ollama ya no es necesario.

## Variables obligatorias en Render

- `AI_API_KEY`: clave creada en Groq Console.
- `AI_BASE_URL`: `https://api.groq.com/openai/v1`.
- `AI_MODEL`: `openai/gpt-oss-20b`.
- `FIREBASE_FUNCTIONS_URL`: URL base de las Cloud Functions, terminada en `/`.

Render proporciona `PORT` automáticamente y la aplicación lo respeta.

## Base de cuentas Firebird

El Blueprint deja `DB_ENABLED=false`, porque Firebird no está incluido en el servicio web de Render. Así funcionan salud, usuarios Firebase, estadísticas, IA y WebSockets, pero los endpoints `/auth` y `/account` requieren una instancia Firebird accesible públicamente.

Para habilitarla, configura `DB_ENABLED=true` y agrega `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` y `DB_ENCODING`.

## Despliegue

1. Sube el repositorio a GitHub.
2. En Render elige **New > Blueprint** y selecciona el repositorio.
3. Cuando Render lo solicite, captura `AI_API_KEY` como secreto.
4. Espera el build y comprueba `https://TU-SERVICIO.onrender.com/health`.

No subas `serviceAccountKey.json`, archivos `.env` ni claves API al repositorio.
