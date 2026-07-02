# Soporte para Múltiples Registros Offline

Este plan permite que la aplicación guarde cada retiro como un registro individual, evitando que se sobrescriban o sumen datos de un mismo punto antes de sincronizar.

## Cambios Propuestos

### Base de Datos Local (Android)

#### [ConexionSQLite.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/database/ConexionSQLite.java)

- Modificar la función `guardarPesajeOffline`:
    - Eliminar la lógica de búsqueda de registros existentes (`SELECT ... WHERE sincronizado = 0`).
    - Hacer que cada llamada realice un `INSERT` directo en la tabla `registros_retiro`.
    - Esto garantiza que si un conductor visita el mismo punto dos veces (o registra dos cargas distintas), ambas se guarden con su propia hora y fotos.

### Lógica de Sincronización (Android)

#### [SyncManager.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/network/SyncManager.java)

- La función `sincronizarDatos` ya recorre todos los registros pendientes con un bucle `while(cursor.moveToNext())`, por lo que enviará el array completo de registros individuales al servidor automáticamente.

## Plan de Verificación

### Pruebas Manuales
1.  **Múltiples Registros:** Realizar 3 retiros distintos en la app (pueden ser del mismo punto o distintos) en modo offline.
2.  **Sincronizar:** Conectarse a internet y presionar "Sincronizar con la nube".
3.  **Verificación Web:** Confirmar en la plataforma administrativa que aparecen los 3 registros independientes en la sección de "Reportes de Terreno", cada uno con su peso, hora y fotos respectivas.
4.  **Limpieza:** Verificar que tras la sincronización, los 3 archivos físicos de fotos se eliminan del celular.
