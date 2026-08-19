# Registro y verificación por correo

DreamApp crea cuentas personales con el rol `Cliente`. La cuenta se guarda definitivamente solo después de validar el código de seis dígitos enviado por correo.

## Variables de entorno en Render

Los servicios gratuitos de Render bloquean los puertos SMTP. Por eso el envío se realiza mediante una aplicación web de Google Apps Script por HTTPS.

Configura estas variables únicamente en el servicio `dreamapp-api`:

- `GOOGLE_APPS_SCRIPT_URL`: URL de la implementación, terminada en `/exec`.
- `GOOGLE_APPS_SCRIPT_SECRET`: secreto compartido entre Render y Apps Script.
- `EMAIL_VERIFICATION_SECRET`: secreto aleatorio de 32 caracteres o más. El Blueprint puede generarlo.

## Configurar Google Apps Script

1. Crea un proyecto en `script.google.com`.
2. Copia el contenido de `google-apps-script/Code.gs`.
3. En **Configuración del proyecto > Propiedades del script**, agrega `APP_SHARED_SECRET` con un secreto aleatorio largo.
4. Implementa como **Aplicación web**, ejecutar como **Yo** y permitir acceso a **Cualquier usuario**.
5. Autoriza el permiso para enviar correo y copia la URL terminada en `/exec`.
6. En Render, usa esa URL y el mismo secreto en las dos variables anteriores.

## Comportamiento de seguridad

- Los códigos caducan en 10 minutos.
- Se permiten cinco intentos por código.
- Solo se puede solicitar un código por correo cada 60 segundos.
- Se almacena el hash del código, nunca el código original.
- Las contraseñas se almacenan con BCrypt.
- Las rutas de métricas e IA siempre usan el identificador de la sesión autenticada e ignoran identificadores enviados por el navegador.
- El antiguo usuario bootstrap `admin` se elimina durante la migración y ya no se vuelve a crear.
