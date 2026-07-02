# Implementación del Módulo de Rutas

Este plan detalla la creación de un nuevo apartado en la plataforma web para gestionar las rutas de recolección de vidrio.

## Cambios Propuestos

### Backend (Flask)

#### [main.py](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/main.py)

- Agregar la ruta `/rutas` (GET) para listar todas las rutas.
- Agregar la ruta `/rutas/crear` (POST) para registrar nuevas rutas asignando un nombre y múltiples puntos de reciclaje.
- Modificar el procesamiento de datos para enviar la lista de rutas y los puntos disponibles al template.

### Frontend (Templates)

#### [rutas.html](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/templates/rutas.html) [NUEVO]

- Crear un listado de rutas en formato de tabla o tarjetas.
- Implementar un desplegable (Bootstrap Collapse) para ver los puntos asociados a cada ruta.
- Crear un modal para "Nueva Ruta" con:
    - Campo para el nombre.
    - Listado de selección múltiple (checkboxes) de puntos de reciclaje que **no** tienen ruta asignada o selección libre.

#### [base.html](file:///C:/Users/maxxi/OneDrive/Escritorio/RCicla/ProyectoRedCicla/pag_web/templates/base.html)

- Agregar el enlace "Rutas" a la barra de navegación lateral o superior.

## Plan de Verificación

### Pruebas Manuales
1.  **Navegación:** Verificar que el enlace "Rutas" aparece y lleva a la página correcta.
2.  **Visualización:** Comprobar que las rutas existentes se listan y el botón de desplegar puntos funciona.
3.  **Creación:** Intentar crear una ruta nueva con un nombre y 2-3 puntos seleccionados. Verificar que los puntos se actualizan en la base de datos con el nuevo `ruta_id`.
4.  **Validación:** Asegurarse de que no se puedan crear rutas sin nombre o sin puntos.
