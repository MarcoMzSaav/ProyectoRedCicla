# Sincronización de Imágenes (Base64)

Este plan corrige el problema de que las imágenes enviadas desde la app móvil no son visibles en la web, implementando una transferencia real de datos en formato Base64.

## Cambios Propuestos

### App Móvil (Android Java)

#### [SyncManager.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/network/SyncManager.java)

- Modificar la función `sincronizarDatos()` para que, en lugar de enviar el URI local del teléfono (que el servidor no puede leer), lea el archivo de imagen y lo convierta a una cadena **Base64**.
- Añadir una función auxiliar `convertImageToBase64(Uri uri)` para procesar las fotos antes del envío.

### Plataforma Web (Python/Flask)

#### [main.py](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/main.py)

- Actualizar el endpoint `/api/sincronizar`:
    - Detectar si las cadenas recibidas en `ruta_img_antes` y `ruta_img_despues` son Base64.
    - Decodificar los datos y guardar el archivo físico (`.jpg`) en la carpeta `static/images/fotos/`.
    - Guardar en la base de datos el **nombre del archivo** generado (ej: `retiro_105_antes.jpg`) en lugar de la cadena Base64 completa.

## Plan de Verificación

### Pruebas Manuales
1.  **Registro en App:** Tomar o seleccionar una foto en el celular y presionar "Guardar".
2.  **Sincronización:** Presionar "Sincronizar con la nube" en la app.
3.  **Verificación de Archivos:** Comprobar que en el servidor (`pag_web/static/images/fotos/`) aparecen archivos nuevos con extensión `.jpg`.
4.  **Visualización Web:** Ir a "Reportes de Terreno" en la web y verificar que al hacer clic en "Antes" o "Después", la imagen se abre correctamente y se puede descargar.
