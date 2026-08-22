# Respuesta al incidente del backup Firebird

## Estado y alcance

`firebird-server/db_dashboard.fbk` fue incluido en el historial Git. La copia
analizada tenía 9,216 bytes y SHA-256
`3b4575d136e384fd3c4f7b740a8de4ffcc6761b13db1c7332ca42d79a346de66`.

La inspección estática, realizada fuera del árbol de trabajo y sin imprimir
valores, encontró marcadores de cuentas, nombres de usuario, campos de
contraseña, hashes BCrypt y correo electrónico. Por ello, el incidente debe
tratarse como exposición de datos personales y de autenticación.

El backup no debe copiarse a tickets, chats, artefactos de CI ni equipos de
desarrollo. Solo el responsable del incidente puede conservar una copia
cifrada y con acceso auditado mientras dure la investigación.

## 1. Contención y rotación

Realizar estas acciones antes de considerar cerrado el incidente:

1. Deshabilitar temporalmente restauraciones y accesos no esenciales a la base.
2. Rotar `FIREBIRD_ROOT_PASSWORD` y cualquier credencial usada por el API para
   conectarse a esa instancia. Actualizar los secretos en el proveedor de
   despliegue; nunca escribir los valores en Git.
3. Forzar el restablecimiento de las contraseñas de todas las cuentas presentes
   en `USER_ACCOUNT`. Aunque estén protegidas con BCrypt, el backup permite
   ataques de fuerza bruta fuera de línea.
4. Invalidar sesiones y tokens emitidos antes de la rotación. Si no existe un
   mecanismo global de revocación, cambiar la clave de firma/pepper aplicable o
   implementar una fecha mínima de emisión.
5. Rotar cualquier credencial reutilizada en otros entornos y revisar los logs
   de autenticación desde la primera publicación del commit comprometido.
6. Registrar responsable, fecha, secreto rotado y evidencia de validación en el
   sistema privado de incidentes. No registrar el valor anterior ni el nuevo.

La rotación queda completa únicamente cuando una conexión con las credenciales
anteriores falla y la aplicación funciona con las nuevas.

## 2. Inspección fuera del repositorio

La inspección lógica debe hacerse en un host aislado, sin red y con disco
cifrado:

1. Obtener la copia desde el almacén de evidencias, no desde el repositorio.
2. Verificar su SHA-256 contra la huella indicada arriba.
3. Restaurarla en una instancia Firebird efímera sin puertos publicados y con
   una contraseña raíz aleatoria de un solo uso.
4. Inventariar únicamente esquemas, tablas, número de filas y tipos de datos.
   No exportar nombres, correos, teléfonos ni hashes.
5. Confirmar, como mínimo, el alcance de `USER_ACCOUNT` y cualquier tabla con
   tokens, sesiones, registros pendientes o datos personales.
6. Destruir la base restaurada, el volumen y la copia temporal; conservar solo
   el informe de alcance sin datos personales.

Si el análisis requiere ver valores, debe aprobarlo el responsable de
privacidad/seguridad y documentar la justificación y las personas con acceso.

## 3. Limpieza del historial Git

Antes de reescribir, congelar temporalmente los pushes y avisar a todos los
colaboradores. Con las credenciales ya rotadas:

```bash
git filter-repo --force \
  --path dreamapp-devsecops-hardened-main/firebird-server/db_dashboard.fbk \
  --invert-paths
git log --all -- dreamapp-devsecops-hardened-main/firebird-server/db_dashboard.fbk
git rev-list --objects --all | grep db_dashboard.fbk
git push --force-with-lease origin main
```

Los dos comandos de verificación no deben devolver resultados. Después del
push forzado:

- Solicitar a los colaboradores que eliminen sus clones o ejecuten una
  resincronización segura; un merge desde una rama antigua puede reintroducir
  el objeto.
- Eliminar ramas, tags, releases y artefactos que todavía apunten al commit
  afectado.
- Revisar forks y caches del proveedor Git; solicitar su purga si corresponde.
- Ejecutar Gitleaks sobre todo el historial limpio y guardar el resultado.

## 4. Prevención y criterio de cierre

- `.gitignore` debe bloquear `*.fbk`, `*.fdb` y `*.gbk`.
- CI debe ejecutar `pwsh ./scripts/security/check-sensitive-artifacts.ps1`.
- Los backups deben residir en almacenamiento privado, cifrado, con retención,
  acceso mínimo y logs de auditoría.
- Secret scanning y push protection deben estar habilitados en el proveedor Git.

El incidente puede cerrarse cuando exista evidencia de: rotación validada,
sesiones revocadas, alcance documentado, historial remoto limpio, clones
coordinados y control preventivo activo.
