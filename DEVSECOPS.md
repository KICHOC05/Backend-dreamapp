# Lineamientos DevSecOps del backend

## Flujo de entrega

1. Cada cambio se desarrolla en una rama corta y se integra mediante pull request.
2. `main` no admite push directo, borrado ni force-push una vez concluida esta migración.
3. CODEOWNERS debe aprobar cambios sensibles y todas las conversaciones deben resolverse.
4. Los checks obligatorios validan artefactos sensibles, calidad, pruebas, cobertura, dependencias, secretos, SAST, IaC, SBOM y la imagen final.
5. La imagen aprobada se publica en GHCR con etiqueta inmutable `sha-<commit>`; `latest` es solo una referencia operativa.

## Controles automatizados

| Control | Herramienta | Ejecución |
|---|---|---|
| SAST y calidad | Detekt + CodeQL | push y pull request |
| Pruebas y cobertura | JUnit + JaCoCo | push y pull request |
| SCA | OWASP Dependency-Check + Dependabot | push/PR y semanal |
| Secretos | Gitleaks sobre historial completo | push y pull request |
| IaC y filesystem | Trivy | push y pull request |
| Contenedor | Docker Buildx + Trivy | push y pull request |
| Inventario | SBOM SPDX JSON | push y pull request |
| DAST | OWASP ZAP | semanal o manual contra URL HTTPS |

## Configuración requerida de GitHub

- Actions habilitado con permisos de lectura por defecto y acciones fijadas a SHA.
- Dependabot alerts y security updates habilitados.
- Protección de `main` con checks obligatorios, revisión de CODEOWNERS y una aprobación mínima.
- Variable opcional `NVD_DATAFEED_URL` para sustituir el mirror NVD 2.0 mantenido por Dependency-Check.
- Variable `DAST_TARGET_URL` con la URL HTTPS del API desplegado para activar el análisis ZAP semanal.

## Criterio de salida

No se libera un commit si falla un control obligatorio. Un hallazgo aceptado temporalmente debe quedar documentado con riesgo, mitigación compensatoria, propietario y fecha de expiración. Los artefactos de pruebas, análisis y SBOM se conservan en GitHub Actions según la retención del workflow.
