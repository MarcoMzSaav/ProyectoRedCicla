# Botón de Actualización de Ruta en App Móvil

Este plan detalla la adición de un botón de actualización en la pantalla del mapa de la aplicación móvil para asegurar que el conductor tenga siempre la última ruta asignada desde la web.

## Cambios Propuestos

### Frontend Móvil (Android Layout)

#### [activity_mapa.xml](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/res/layout/activity_mapa.xml)

- Añadir un botón flotante (FloatingActionButton) o un botón en la barra superior/inferior para refrescar.
- Se propone añadir un botón con un ícono de actualización en la esquina superior del mapa o integrarlo en el menú inferior.

### Lógica Móvil (Android Java)

#### [MapaActivity.java](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/app_movil/app/src/main/java/com/example/appredcicla/MapaActivity.java)

- Configurar el Listener para el nuevo botón.
- Al presionar, ejecutar nuevamente `cargarPuntosDeServidor()`.
- Mostrar un mensaje de "Actualizando ruta..." mediante un `Toast`.
- Limpiar el mapa y la polilínea actual antes de cargar los nuevos datos.

## Plan de Verificación

### Pruebas Manuales
1.  **Cambio en Web:** Cambiar la ruta o el conductor en la plataforma administrativa.
2.  **Presionar Actualizar:** En la app móvil, presionar el nuevo botón de actualizar.
3.  **Confirmación Visual:** Verificar que los marcadores en el mapa cambian a los nuevos puntos asignados.
4.  **Estado Vacío:** Si se elimina la ruta en la web y se actualiza en la app, verificar que el mapa se limpie correctamente.
