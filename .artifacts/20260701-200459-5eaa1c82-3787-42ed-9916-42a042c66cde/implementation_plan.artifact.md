# Corrección de Sincronización de Imágenes (Base64) - Fase 2

Este plan resuelve el problema de que las imágenes no aparecen en la web, atacando tres frentes: permisos/formato en la app, almacenamiento en el servidor y rutas de visualización.

## Cambios Realizados / Propuestos

### 1. Servidor (Infraestructura)
- [Hecho] Se detectó que existía un archivo llamado `fotos` que bloqueaba la creación de la carpeta. Se eliminó y se creó el directorio `pag_web/static/images/fotos/`.

### 2. App Móvil (Android Java)

#### [SyncManager.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/network/SyncManager.java)
- Optimizar `convertImageToBase64`:
    - Añadir **compresión** de imagen (JPEG al 70%) para evitar que la cadena Base64 sea demasiado pesada y cause errores de red o memoria (OOM).
    - Añadir Logs para verificar en consola si la conversión es exitosa o falla por permisos.
- Asegurar el envío de la cadena Base64 pura.

### 3. Plataforma Web (Python/Flask)

#### [main.py](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/main.py)
- Reforzar `/api/sincronizar`:
    - Manejar posibles prefijos de Base64 (ej: `data:image/jpeg;base64,`).
    - Mejorar el manejo de errores al guardar archivos para que no dejen la base de datos con rutas vacías silenciosamente.

## Plan de Verificación

### Pruebas Manuales
1.  **Verificar Carpeta:** Ejecutar `list_files` en `static/images/fotos` para asegurar que está vacía y lista.
2.  **Sincronizar:** Realizar un registro en la app y sincronizar.
3.  **Verificar Logs:** Revisar la consola de Android Studio para ver si `SyncManager` indica "Base64 Length: > 0".
4.  **Verificar Servidor:** Comprobar que aparecen archivos `.jpg` en la carpeta `fotos`.
5.  **Verificar Web:** Abrir el reporte y confirmar que el botón ahora abre la imagen real.

---
> [!IMPORTANT]
> Si estás probando desde un **celular físico**, asegúrate de que el `BASE_URL` en `SyncManager.java` apunte a la **IP de tu computadora** y no a `redcicla.onrender.com`.
