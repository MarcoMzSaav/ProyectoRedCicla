# Visualización de Imágenes en Reportes de Terreno

Este plan detalla la integración de las fotografías "Antes" y "Después" en el listado de reportes de terreno para auditoría y verificación.

## Cambios Propuestos

### Backend (Flask)

#### [main.py](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/main.py)

- **`reportes_terreno`**: Actualizar la consulta SQL para incluir las columnas `ruta_img_antes` y `ruta_img_despues` de la tabla `registros_retiro`.
- Asegurar que las rutas de las imágenes sean accesibles a través de la carpeta `static`.

### Frontend (Templates)

#### [reportes_terreno.html](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/templates/reportes_terreno.html)

- Añadir una nueva columna "Evidencia (Fotos)" a la tabla.
- **Lógica de Visualización**:
    - Si el reporte tiene imágenes, mostrar íconos o botones miniatura.
    - Implementar un Modal de Bootstrap para ampliar las fotos al hacer clic.
    - Si no hay imágenes, mostrar un mensaje discreto como "Sin registro visual".
- Añadir botones de descarga directa para cada imagen si están presentes.

## Plan de Verificación

### Pruebas Manuales
1.  **Carga de Datos:** Verificar que el listado sigue funcionando correctamente con reportes antiguos que no tienen imágenes.
2.  **Visualización:** Comprobar que si existen rutas de imagen válidas en la base de datos, los íconos/botones aparecen.
3.  **Modal:** Abrir el modal y verificar que las imágenes se cargan y se ven correctamente en tamaño grande.
4.  **Descarga:** Probar que los enlaces de descarga funcionan (si se implementan).
