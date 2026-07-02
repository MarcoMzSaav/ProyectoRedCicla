# Módulo de Gestión de Rutas y Sincronización - RedCicla

Se ha implementado con éxito el sistema de gestión de rutas y su sincronización en tiempo real con la aplicación móvil.

## Cambios Implementados

### 1. Gestión de Rutas y Personal (Web)
- **Asignación de Conductores**: Los administradores pueden asignar personal a las rutas y reasignarlos dinámicamente.
- **Gestión de Puntos**: Capacidad para añadir o quitar puntos de reciclaje de forma individual en cada ruta.
- **Sincronización Automática**: El sistema actualiza la tabla `rutas_activas` en tiempo real al realizar cambios en la web.

### 2. Evidencia Visual y Reportes
- **Galería de Terreno**: Visualización de fotos "Antes" y "Después" con soporte para descarga y modales de alta resolución.
- **Tolerancia a Datos**: El sistema maneja de forma segura los registros que no contienen evidencia fotográfica.

### 3. Sincronización Web-Móvil (App)
- **Botón de Actualizar**: Se añadió un botón flotante en el mapa del celular para que el conductor pueda forzar la descarga de la última ruta sin reiniciar la app.
- **Actualización Dinámica**: El mapa se limpia y redibuja automáticamente al recibir nuevos datos del servidor.

## Verificación Realizada

1. **Prueba de Flujo Completo**:
    - Se creó una ruta en la web y se asignó un conductor.
    - Se verificó que la app móvil recibía los datos mediante el botón de actualización.
    - Se cambió el conductor y se confirmó que el nuevo usuario recibió la ruta instantáneamente.
2. **Prueba de Interfaz**: Se validó el diseño del botón flotante en Android y la visualización de imágenes en el panel administrativo.

## Archivos Clave
- **[main.py](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/main.py)** (Servidor Central)
- **[rutas.html](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/templates/rutas.html)** (Gestión Web)
- **[MapaActivity.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/MapaActivity.java)** (Lógica Móvil)
