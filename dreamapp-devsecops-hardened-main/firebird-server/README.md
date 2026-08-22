
## Firebird Server para Autenticación

Este directorio contiene la configuración para desplegar un servidor de base de datos Firebird, utilizado para la autenticación en el sistema.

---

### Ejemplo de archivo docker-compose.yml

```yaml
services:
  firebird5-engine:
    image: firebirdsql/firebird:5.0.2-noble@sha256:ac37a37ae971b6006ea2648c3d28afd64b848465d5d8151b29d31e8a7cbf8d8b
    container_name: firebird5-engine
    ports:
      - 127.0.0.1:3051:3050
    restart: always
    security_opt:
      - no-new-privileges:true
    environment:
      - FIREBIRD_ROOT_PASSWORD=${FIREBIRD_ROOT_PASSWORD}
      #- FIREBIRD_USER=jerry
      #- FIREBIRD_PASSWORD=${FIREBIRD_PASSWORD}
      - FIREBIRD_DATABASE_DEFAULT_CHARSET=UTF8
      - TZ=America/Mexico_City
    volumes:
      - ./volumes/etc/firebird/databases.conf:/opt/firebird/databases.conf
      - ./volumes/opt/firebird/security5.fdb:/opt/firebird/security5.fdb
      - ./volumes/etc/firebird/firebird.conf:/opt/firebird/firebird.conf
      - ./volumes/var/lib/firebird/data:/var/lib/firebird/data
```

### Despliegue con Docker Compose (Windows y Linux)

#### 1. Crear manualmente las carpetas de volúmenes
Antes de iniciar el contenedor, crea manualmente las carpetas que se usarán como volúmenes para la base de datos y otros datos persistentes.

#### 2. Ejecutar docker-compose con los volúmenes comentados
Edita el archivo `docker-compose.yml` y comenta las líneas que montan los volúmenes. Luego inicia el contenedor:

```
docker-compose up -d
```

#### 3. Copiar los archivos de los volúmenes del contenedor a las carpetas locales
Usa el comando `docker cp` para copiar los archivos generados en el contenedor a las carpetas locales que creaste en el paso 1. Ejemplo:

```
docker cp <container_id>:/firebird/data ./db_data
docker cp <container_id>:/firebird/backups ./backups
```

O para los archivos de configuración y base de datos:

```
docker cp firebird5-engine:/opt/firebird/databases.conf ./volumes/etc/firebird/databases.conf
docker cp firebird5-engine:/opt/firebird/security5.fdb ./volumes/opt/firebird/security5.fdb
docker cp firebird5-engine:/opt/firebird/firebird.conf ./volumes/etc/firebird/firebird.conf
docker cp firebird5-engine:/var/lib/firebird/data ./volumes/var/lib/firebird/data
```

#### 4. Detener el servidor
Detén el contenedor:

```
docker-compose down
```


#### 5. Levantar docker-compose con los volúmenes sin comentar
Descomenta las líneas de los volúmenes en el archivo `docker-compose.yml` y vuelve a iniciar el contenedor:

```
docker-compose up -d
```

Ahora el servidor Firebird usará los volúmenes persistentes para almacenar los datos.

---

### Restaurar un backup (.fbk) de la base de datos en Firebird Docker

> **Seguridad:** los backups (`.fbk`) y bases de datos (`.fdb`) NUNCA deben commitearse al repositorio (están en `.gitignore`). Almacénalos en un almacenamiento privado (p. ej. bucket cifrado) y tráelos solo al host que ejecuta el contenedor.

1. Copia el archivo de backup (`db_dashboard.fbk`) desde tu almacenamiento privado a la carpeta compartida del contenedor:
   - En Windows, por ejemplo:
   ```
   copy db_dashboard.fbk c:\Firebase Container\volumes\var\lib\firebird\data\
   ```

2. Ejecuta el siguiente comando para restaurar el backup dentro del contenedor:
   ```
   docker exec -it firebird5-engine gbak -c -v /var/lib/firebird/data/db_dashboard.fbk /var/lib/firebird/data/db_dashboard.fdb -user SYSDBA -pass <tu-contraseña>
   ```

3. La base restaurada estará disponible en:
   ```
   /var/lib/firebird/data/db_dashboard.fdb
   ```

4. Conéctate a la base restaurada desde FlameRobin o tu aplicación usando la ruta anterior.

---

### Registrar el alias en databases.conf

Para registrar el alias directamente en el `databases.conf` del contenedor:

1. Accede al contenedor con una terminal:
   ```
   docker exec -it firebird5-engine bash
   ```

2. Edita el archivo de configuración con `vim` o `nano` (si están instalados):
   ```
   nano /opt/firebird/databases.conf
   # o
   vim /opt/firebird/databases.conf
   ```

3. Agrega la línea del alias al final del archivo. Ejemplo:
   ```
   db_dashboard = /var/lib/firebird/data/db_dashboard.fdb
   ```

4. Guarda los cambios y reinicia el contenedor:
   ```
   exit
   docker-compose restart
   ```

Ahora puedes conectar usando el alias `db_dashboard` desde tus aplicaciones o FlameRobin.
