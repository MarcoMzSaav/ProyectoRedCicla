# Módulo de Gestión de Rutas y Sincronización - RedCicla

Se ha implementado con éxito el sistema de gestión de rutas, su sincronización en tiempo real y la persistencia de imágenes en terreno.

## Cambios Implementados

### 1. Gestión de Rutas y Personal (Web)
- **Asignación de Conductores**: Los administradores pueden asignar personal a las rutas y reasignarlos dinámicamente.
- **Gestión de Puntos**: Capacidad para añadir o quitar puntos de reciclaje de forma individual en cada ruta.
- **Sincronización Automática**: El sistema actualiza la tabla `rutas_activas` en tiempo real al realizar cambios en la web.

### 2. Evidencia Visual y Reportes
- **Transferencia Real de Imágenes**: Implementación de codificación Base64 con compresión en la app y decodificación en el servidor.
- **Galería de Terreno**: Visualización de fotos reales con soporte para descarga y modales de alta resolución.
- **Tolerancia a Datos**: El sistema maneja de forma segura los registros que no contienen evidencia fotográfica.

### 3. Sincronización Web-Móvil y Persistencia Offline
- **Botón de Actualizar**: Se añadió un botón flotante en el mapa del celular para sincronización manual.
- **Actualización Dinámica**: El mapa y la pantalla principal se refrescan automáticamente al detectar cambios en la asignación.
- **Persistencia de Imágenes**: Copia física de fotos al almacenamiento interno del dispositivo al guardar offline, evitando su pérdida al cerrar la aplicación.
- **Limpieza Automática**: Borrado físico de archivos temporales tras una sincronización exitosa a la nube.

## Verificación Realizada

1. **Prueba de Persistencia Offline**:
    - Se guardó un registro offline con fotos y se cerró la aplicación.
    - Al reabrir y sincronizar, las fotos llegaron correctamente al servidor, confirmando que la copia física funciona.
2. **Prueba de Sincronización de Imágenes**:
    - Se verificó la creación de archivos `.jpg` en `static/images/fotos/` del servidor.
    - Se confirmó que las fotos son visibles y descargables desde el panel de Reportes de Terreno.
3. **Prueba de Flujo Completo**:
    - Se asignó un conductor a una ruta en la web y se verificó que los datos estaban disponibles instantáneamente tras presionar actualizar en la app.

## Archivos Clave
- **[main.py](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/main.py)** (Servidor Central)
- **[SyncManager.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/network/SyncManager.java)** (Red y Persistencia)
- **[RegistrarRetiroActivity.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/RegistrarRetiroActivity.java)** (Gestión Offline)
