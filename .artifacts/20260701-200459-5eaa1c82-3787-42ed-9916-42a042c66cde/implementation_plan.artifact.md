# Persistencia de Imágenes Offline

Este plan corrige la pérdida de imágenes al cerrar la aplicación antes de sincronizar, asegurando que las fotos se guarden físicamente en el almacenamiento interno del teléfono.

## Cambios Propuestos

### App Móvil (Android Java)

#### [RegistrarRetiroActivity.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/RegistrarRetiroActivity.java)

- Modificar el flujo de `botonGuardar`:
    - Antes de guardar en la base de datos, copiar las imágenes seleccionadas desde la galería/cámara a una carpeta privada de la aplicación (`files/fotos_pendientes/`).
    - Guardar en SQLite la ruta del **archivo interno** en lugar del URI temporal.
    - Implementar una función `copiarImagenInterna(Uri uri, String nombreDestino)` para realizar la copia física.

#### [SyncManager.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/network/SyncManager.java)

- Actualizar `convertImageToBase64`:
    - Ahora debe ser capaz de leer archivos desde rutas de archivos internos (`/data/user/0/...`).
- Modificar `marcarComoSincronizados`:
    - Después de una sincronización exitosa, **eliminar físicamente** los archivos de la carpeta `fotos_pendientes/` para no llenar la memoria del teléfono.

## Plan de Verificación

### Pruebas Manuales
1.  **Registro Offline:** Seleccionar fotos, presionar "Guardar Offline" y **cerrar la app completamente**.
2.  **Reapertura:** Volver a abrir la app y presionar "Sincronizar".
3.  **Verificación Servidor:** Comprobar que el registro llega a Render con las fotos visibles.
4.  **Limpieza:** Usar un explorador de archivos o Logs para verificar que las fotos se eliminan del teléfono tras sincronizar.
