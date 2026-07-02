# Persistencia Local de Ruta y Puntos (Modo Offline)

Este plan soluciona la pérdida de datos cuando la app falla al actualizar por falta de internet, permitiendo que el conductor siga trabajando con la última ruta conocida.

## Cambios Propuestos

### Base de Datos Local (Android)

#### [ConexionSQLite.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/database/ConexionSQLite.java)

- Incrementar `DATABASE_VERSION` a 4.
- Actualizar `puntos_reciclaje`: Añadir columnas `latitud` y `longitud` (REAL).
- Implementar `guardarRutaLocal(int rutaActivaId, String nombreRuta, String patente, JsonArray puntos)`:
    - Borrar la caché anterior.
    - Guardar los metadatos en `SharedPreferences`.
    - Insertar los puntos en la tabla local.
- Implementar `obtenerPuntosLocales()`: Devolver un Cursor con los puntos guardados.

### Lógica de Actividades (Android Java)

#### [RegistrarRetiroActivity.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/RegistrarRetiroActivity.java)

- En `cargarDatosRuta`:
    - Si la petición al servidor es exitosa, llamar a `dbHelper.guardarRutaLocal`.
    - Si la petición falla (onError), intentar cargar desde la caché local usando `dbHelper`.
    - Mostrar un mensaje (Toast) indicando que se están usando "Datos Offline" si el servidor no responde.

#### [MapaActivity.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/MapaActivity.java)

- En `cargarPuntosDeServidor`:
    - Lógica similar: Si falla la red, cargar marcadores desde la base de datos local.

## Plan de Verificación

### Pruebas Manuales
1.  **Carga Inicial:** Abrir la app con internet y cargar la ruta.
2.  **Modo Avión:** Activar modo avión y presionar el botón de "Actualizar" en la pantalla principal o el mapa.
3.  **Verificación:** Confirmar que los datos se mantienen y aparece un aviso de "Modo Offline".
4.  **Cierre y Reapertura:** Cerrar la app en modo avión y volver a entrar. Verificar que la ruta sigue ahí.
5.  **Actualización Real:** Volver a conectar internet, cambiar un dato en la web y verificar que al actualizar en la app, la caché local se renueva.
