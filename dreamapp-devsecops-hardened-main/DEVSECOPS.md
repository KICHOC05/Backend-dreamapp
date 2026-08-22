# DevSecOps de DreamApp

## Controles implementados

- CI para Kotlin, TypeScript (web y Functions), Android Lint y CodeQL (`security-extended`).
- Escaneo de secretos con Gitleaks en cada push/PR (config en `.gitleaks.toml`).
- Escaneo de vulnerabilidades, secretos y mala configuración de contenedores/IaC con Trivy (`fs`: lockfiles npm/pnpm, Dockerfiles, docker-compose, render.yaml).
- SBOM en formato SPDX generado en cada ejecución del pipeline y conservado 90 días como artefacto.
- DAST continuo: escaneo base OWASP ZAP contra el sitio web publicado (solo en la ejecución programada semanal).
- Dependabot semanal para Gradle, npm, GitHub Actions y ecosistema Docker (Dockerfiles y docker-compose).
- `npm audit --audit-level=high` (Functions) y `pnpm audit --audit-level high` (web) como puertas de calidad.
  - Dependabot no soporta lockfiles de pnpm; `dreamapp-web` queda cubierto por `pnpm audit` en CI y por Trivy sobre `pnpm-lock.yaml`.
- Secretos excluidos de Git (`.gitignore` cubre `.env`, keystores, service accounts y backups `.fbk`/`.fdb`) y administrados por Render/Firebase.
- Contenedores sin root, sin capacidades Linux y con filesystem de solo lectura cuando es compatible; `no-new-privileges` en compose; Firebird y Ollama/Open WebUI publicados solo en `127.0.0.1`.
- Imagen de Firebird pineada por digest; el resto de imágenes se actualiza vía Dependabot (evitar tags flotantes al renovarlas).
- HTTPS/WSS obligatorio en Android, release minificado y logs sensibles eliminados por R8.
- Cloud Functions autenticadas con Firebase ID tokens; operaciones globales requieren claim `admin`.
- Comunicación Render → Firebase mediante `FUNCTIONS_INTERNAL_KEY` almacenada como secreto.
- API con roles, CORS restringido, límite de solicitudes, tamaño máximo de cuerpo y cabeceras de seguridad.
- WebSockets deshabilitados por defecto hasta incorporar autenticación por conexión.
- Firestore con reglas deny-all (solo acceso vía Admin SDK).

## Configuración obligatoria

1. Generar un valor aleatorio de al menos 32 bytes para `FUNCTIONS_INTERNAL_KEY`.
2. Guardar exactamente el mismo valor en Firebase y Render:
   - `firebase functions:secrets:set FUNCTIONS_INTERNAL_KEY`
   - Render: variable secreta `FUNCTIONS_INTERNAL_KEY`.
3. Configurar `AI_API_KEY`, credenciales de base de datos y `ALLOWED_ORIGINS` en Render.
4. Compilar Android con `DREAMAPP_API_URL=https://<servicio>.onrender.com/`.
5. Restringir la clave web de Firebase/Google por nombre de paquete y huellas SHA-1/SHA-256 en Google Cloud Console y verificarlo:
   - Google Cloud Console → APIs & Services → Credentials → clave usada en `google-services.json` → Application restrictions = Android apps.
   - Añadir el paquete `com.example...` (el real del módulo `appmobile`) con sus huellas SHA-1 y SHA-256 de debug y release.
   - Confirmar que la clave NO tiene acceso libre a APIs sensibles (Maps, etc.): solo las API de Firebase necesarias.
6. Mantener `WEBSOCKETS_ENABLED=false` en producción hasta implementar Firebase ID token o sesión autenticada durante el handshake.
7. Si el repositorio es privado, Gitleaks Action requiere `GITLEAKS_LICENSE` como secret del repo (en repos públicos funciona solo con `GITHUB_TOKEN`).

## Protección de rama (pendiente de habilitar en GitHub)

En GitHub → Settings → Branches → Add branch protection rule para `main`:

- Require a pull request before merging → Require approvals: 1.
- Require status checks to pass before merging → Require branches to be up to date → marcar el workflow **Security and quality gates** (todos sus jobs).
- Require conversation resolution before merging.
- Do not allow bypassing the above settings.
- En Settings → Code security: activar Secret scanning y Push protection, Dependabot alerts/security updates, y Code scanning alerts.

Equivalente con `gh`:

```bash
gh api -X PUT repos/KICHOC05/dreamapp-backend-devsecops/branches/main/protection \
  -f required_status_checks.strict=true \
  -f required_status_checks.contexts='backend' -f required_status_checks.contexts='firebase-functions' \
  -f required_status_checks.contexts='dreamapp-web' -f required_status_checks.contexts='android-lint' \
  -f required_status_checks.contexts='secret-scan' -f required_status_checks.contexts='trivy' \
  -f required_status_checks.contexts='sbom' -f required_status_checks.contexts='codeql' \
  -F required_pull_request_reviews.required_approving_review_count=1 \
  -F enforce_admins=true
```

## Puertas de entrega

No se debe fusionar o desplegar si falla compilación, pruebas, Android Lint, CodeQL, Gitleaks, Trivy o las auditorías `npm audit`/`pnpm audit`. Habilita la protección de rama y exige el workflow **Security and quality gates**.

## Respuesta ante incidentes

Si un secreto entra al historial, revócalo primero, reemplázalo en los servicios y después limpia el historial. No publiques datos fisiológicos, ubicaciones, tokens o cuerpos completos de solicitudes en logs. Los hallazgos del DAST semanal (ZAP) y de Trivy deben triagearse: los CRITICAL/HIGH bloquean el merge; el resto se agenda.
