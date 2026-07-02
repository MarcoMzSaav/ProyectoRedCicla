# Módulo de Gestión de Rutas y Sincronización - RedCicla

Se ha implementado con éxito el sistema de gestión de rutas, su sincronización en tiempo real y la robustez offline.

## Cambios Implementados

### 1. Gestión de Rutas y Personal (Web)
- **Asignación de Conductores**: Los administradores pueden asignar personal a las rutas y reasignarlos dinámicamente.
- **Gestión de Puntos**: Capacidad para añadir o quitar puntos de reciclaje de forma individual en cada ruta.
- **Sincronización Automática**: El sistema actualiza la tabla `rutas_activas` en tiempo real al realizar cambios en la web.

### 2. Evidencia Visual y Reportes
- **Transferencia Real de Imágenes**: Implementación de codificación Base64 con compresión en la app y decodificación en el servidor.
- **Galería de Terreno**: Visualización de fotos reales con soporte para descarga y modales de alta resolución.

### 3. Sincronización Web-Móvil y Robustez Offline
- **Botón de Actualizar**: Se añadió un botón flotante en el mapa del celular para sincronización manual.
- **Actualización Dinámica**: Refresco automático de pantallas al detectar cambios de asignación.
- **Persistencia de Imágenes**: Copia física de fotos al almacenamiento interno del dispositivo al guardar offline.
- **Caché de Ruta Offline (NUEVO)**: Implementación de base de datos local para guardar la ruta completa (puntos, coordenadas, metadatos). Si falla la conexión, la app carga automáticamente la última ruta conocida.

## Verificación Realizada

1. **Prueba de Modo Offline**:
    - Se desconectó la red y se verificó que la app mantiene el nombre de ruta, camión y listado de puntos desde la base de datos local.
    - El mapa muestra los marcadores guardados (en color naranja) sin necesidad de internet.
2. **Prueba de Persistencia Offline**:
    - Se confirmó que las fotos no se pierden al cerrar la aplicación forzosamente antes de sincronizar.
3. **Prueba de Sincronización de Imágenes**:
    - Se verificó la creación de archivos `.jpg` en el servidor y su correcta visualización en el panel de reportes.

## Archivos Clave
- **[main.py](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/main.py)** (Servidor Central)
- **[ConexionSQLite.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/database/ConexionSQLite.java)** (Motor de persistencia local)
- **[MapaActivity.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/MapaActivity.java)** (Lógica de mapa offline)
