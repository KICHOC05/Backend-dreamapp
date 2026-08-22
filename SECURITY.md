# Política de seguridad

## Reporte responsable

No publiques credenciales, datos de pacientes ni detalles explotables en un issue. Reporta la vulnerabilidad mediante **Security > Advisories > New draft security advisory** en GitHub. Incluye impacto, pasos mínimos de reproducción y versión afectada.

## Datos y secretos

- Nunca se versionan `.env`, cuentas de servicio, llaves privadas, tokens, contraseñas ni backups Firebird (`.fbk`, `.fdb`, `.gbk`).
- Los secretos de ejecución se almacenan en GitHub Actions Secrets y en el gestor de secretos del entorno de despliegue.
- Ante una exposición, se revoca o rota el secreto antes de limpiar el historial. La limpieza del backup anterior está documentada en `docs/security/FIREBIRD_BACKUP_INCIDENT.md`.
- Los logs no deben incluir tokens, contraseñas, prompts completos, identificadores personales ni métricas médicas sin anonimizar.

## Dependencias y vulnerabilidades

Dependabot revisa Gradle, Docker y GitHub Actions. El pipeline bloquea vulnerabilidades que superan los umbrales configurados en OWASP Dependency-Check y Trivy. Las excepciones requieren justificación, alcance, responsable y fecha de expiración.

## Ciclo de corrección

| Severidad | Inicio de atención | Objetivo de corrección |
|---|---:|---:|
| Crítica | 24 horas | 72 horas |
| Alta | 3 días | 14 días |
| Media | 7 días | 30 días |
| Baja | 30 días | 90 días |

## Alcance

Esta política cubre exclusivamente el backend Kotlin/Javalin, su imagen Docker y su automatización CI/CD.
