# DevSecOps de DreamApp

## Controles implementados

- CI para Kotlin, TypeScript, Android Lint y CodeQL.
- Dependabot semanal para Gradle, npm y GitHub Actions.
- `npm audit --audit-level=high` como puerta de calidad.
- Secretos excluidos de Git y administrados por Render/Firebase.
- Contenedores sin root, sin capacidades Linux y con filesystem de solo lectura cuando es compatible.
- HTTPS/WSS obligatorio en Android, release minificado y logs sensibles eliminados por R8.
- Cloud Functions autenticadas con Firebase ID tokens; operaciones globales requieren claim `admin`.
- Comunicación Render → Firebase mediante `FUNCTIONS_INTERNAL_KEY` almacenada como secreto.
- API con roles, CORS restringido, límite de solicitudes, tamaño máximo de cuerpo y cabeceras de seguridad.
- WebSockets deshabilitados por defecto hasta incorporar autenticación por conexión.

## Configuración obligatoria

1. Generar un valor aleatorio de al menos 32 bytes para `FUNCTIONS_INTERNAL_KEY`.
2. Guardar exactamente el mismo valor en Firebase y Render:
   - `firebase functions:secrets:set FUNCTIONS_INTERNAL_KEY`
   - Render: variable secreta `FUNCTIONS_INTERNAL_KEY`.
3. Configurar `AI_API_KEY`, credenciales de base de datos y `ALLOWED_ORIGINS` en Render.
4. Compilar Android con `DREAMAPP_API_URL=https://<servicio>.onrender.com/`.
5. Restringir la clave web de Firebase/Google por nombre de paquete y huellas SHA en Google Cloud Console.
6. Mantener `WEBSOCKETS_ENABLED=false` en producción hasta implementar Firebase ID token o sesión autenticada durante el handshake.

## Puertas de entrega

No se debe fusionar o desplegar si falla compilación, pruebas, Android Lint, CodeQL o `npm audit`. Habilita protección de rama y exige el workflow **Security and quality gates**.

## Respuesta ante incidentes

Si un secreto entra al historial, revócalo primero, reemplázalo en los servicios y después limpia el historial. No publiques datos fisiológicos, ubicaciones, tokens o cuerpos completos de solicitudes en logs.
