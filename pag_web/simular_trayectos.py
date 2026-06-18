import sqlite3
import os
from datetime import datetime

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'redcicla_central.db')

def simular_operacion_completa():
    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()

    print("🧹 Limpiando tablas operativas antiguas para evitar duplicados...")
    cursor.execute("DELETE FROM registros_retiro")
    cursor.execute("DELETE FROM rutas_activas")
    cursor.execute("DELETE FROM puntos_reciclaje")
    cursor.execute("DELETE FROM rutas")
    cursor.execute("DELETE FROM zonas")
    
    # 1. Aseguramos una Zona base
    cursor.execute("INSERT INTO zonas (id, nombre) VALUES (1, 'Región del Maule - Talca')")

    # 2. Inyectamos los 5 Conductores (Juan + 4 más)
    # Nota: Usamos INSERT OR IGNORE por si ya existen en la tabla empleados
    conductores = [
        (3, '11.111.111-1', 'Juan Conductor', 'juan@redcicla.cl', '911111111', 'hash_falso', 'Conductor', 1),
        (10, '22.222.222-2', 'Carlos Mendoza', 'carlos@redcicla.cl', '922222222', 'hash_falso', 'Conductor', 1),
        (11, '33.333.333-3', 'Pedro Gómez', 'pedro@redcicla.cl', '933333333', 'hash_falso', 'Conductor', 1),
        (12, '44.444.444-4', 'Miguel Silva', 'miguel@redcicla.cl', '944444444', 'hash_falso', 'Conductor', 1),
        (13, '55.555.555-5', 'Diego Soto', 'diego@redcicla.cl', '955555555', 'hash_falso', 'Conductor', 1)
    ]
    cursor.executemany('''
        INSERT OR IGNORE INTO empleados (id, rut, nombre_completo, correo, telefono, clave_acceso, rol, estado)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ''', conductores)

    # 3. Inyectamos los 5 Camiones con patentes y capacidades distintas
    camiones = [
        (1, 'ABCD-12', 5000.0, '2025-01-10', 1, 0),
        (2, 'XYZW-34', 4500.0, '2025-03-15', 1, 0),
        (3, 'KLMN-56', 6000.0, '2025-02-20', 1, 0),
        (4, 'QRST-78', 4000.0, '2025-05-05', 1, 0),
        (5, 'UVWX-90', 5500.0, '2025-04-12', 1, 0)
    ]
    cursor.executemany('''
        INSERT OR IGNORE INTO camiones (id, patente, capacidad_carga, ultima_revision, estado, alerta)
        VALUES (?, ?, ?, ?, ?, ?)
    ''', camiones)

    # 4. Creamos las 5 Rutas de recolección para Talca
    rutas = [
        (1, 1, 'Ruta Norte-Centro (Juan)'),
        (2, 1, 'Ruta Sur-Oriente (Carlos)'),
        (3, 1, 'Ruta Colin-Poniente (Pedro)'),
        (4, 1, 'Ruta Casco Histórico (Miguel)'),
        (5, 1, 'Ruta Piduco-Periferia (Diego)')
    ]
    cursor.executemany("INSERT INTO rutas (id, zona_id, nombre) VALUES (?, ?, ?)", rutas)

    # 5. Registramos tus 16 PUNTOS EXACTOS distribuidos estratégicamente en las 5 rutas
    # Estructura: (id, ruta_id, direccion, latitud, longitud, estado)
    puntos = [
        # Ruta 1 (Asignada a Juan) - 4 Puntos
        (1, 1, '1 Oriente 4 Norte', -35.4264, -71.6554, 1),
        (2, 1, 'Av. Lircay 1230', -35.412, -71.662, 1),
        (7, 1, 'Cinco Oriente 560', -35.42, -71.65, 1),
        (12, 1, 'Av. Cancha Rayada 550', -35.418, -71.67, 1),
        
        # Ruta 2 (Asignada a Carlos) - 3 Puntos
        (3, 2, 'Dos Sur 1540', -35.428, -71.65, 1),
        (5, 2, 'Calle 11 Oriente 234', -35.43, -71.64, 1),
        (9, 2, 'Calle 30 Oriente 112', -35.435, -71.635, 1),
        
        # Ruta 3 (Asignada a Pedro) - 3 Puntos
        (4, 3, 'Av. Colin 890', -35.445, -71.668, 1),
        (6, 3, 'Av. San Miguel 3450', -35.452, -71.68, 1),
        (14, 3, 'Av. circunvalación 2200', -35.455, -71.66, 1),
        
        # Ruta 4 (Asignada a Miguel) - 3 Puntos
        (8, 4, 'Alameda 1200', -35.427, -71.658, 1),
        (10, 4, 'Av. Isidoro del Solar 90', -35.422, -71.665, 1),
        (13, 4, 'Calle 4 Norte 320', -35.423, -71.652, 1),
        
        # Ruta 5 (Asignada a Diego) - 3 Puntos
        (11, 5, 'Calle 8 Sur 1400', -35.44, -71.645, 1),
        (15, 5, 'Calle 1 Sur 500', -35.426, -71.65, 1),
        (16, 5, 'Parque Piduco 100', -35.432, -71.658, 1)
    ]
    cursor.executemany('''
        INSERT INTO puntos_reciclaje (id, ruta_id, direccion, latitud, longitud, estado)
        VALUES (?, ?, ?, ?, ?, ?)
    ''', puntos)

    # 6. Activamos los 5 viajes de hoy (Rutas Activas)
    # Estructura: (id, ruta_id, camion_id, conductor_id, ayudante_id, fecha_inicio, fecha_fin)
    viajes_activos = [
        (1, 1, 1, 3, 5, '2026-06-17 08:00', None),                # Juan (En curso)
        (2, 2, 2, 10, 5, '2026-06-17 07:30', '2026-06-17 13:00'), # Carlos (Terminado)
        (3, 3, 3, 11, 5, '2026-06-17 08:15', None),               # Pedro (En curso)
        (4, 4, 4, 12, 5, '2026-06-17 09:00', None),               # Miguel (Acaba de salir)
        (5, 5, 5, 13, 5, '2026-06-17 08:45', None)                # Diego (En curso)
    ]
    cursor.executemany('''
        INSERT INTO rutas_activas (id, ruta_id, camion_id, conductor_id, ayudante_id, fecha_inicio, fecha_fin)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    ''', viajes_activos)

    # 7. Simulamos los reportes que los camioneros enviaron desde terreno
    # Estructura: (ruta_activa_id, punto_id, fecha_hora, cantidad_retirada, estado)
    reportes_terreno = [
        # --- VIAJE 1: Juan (Completó 2 puntos, le quedan 2 pendientes) -> 50% de avance
        (1, 1, '2026-06-17 09:10', 320.5, 'Completado'),
        (1, 2, '2026-06-17 10:05', 410.0, 'Completado'),
        
        # --- VIAJE 2: Carlos (Completó sus 3 puntos y se fue para el centro de acopio) -> 100% de avance
        (2, 3, '2026-06-17 08:10', 280.0, 'Completado'),
        (2, 5, '2026-06-17 09:25', 310.5, 'Completado'),
        (2, 9, '2026-06-17 11:40', 500.0, 'Completado'),
        
        # --- VIAJE 3: Pedro (Pasó por 2 puntos, pero 1 falló) -> 66% de avance operativo
        (3, 4, '2026-06-17 09:30', 440.0, 'Completado'),
        (3, 6, '2026-06-17 11:00', 0.0, 'No Recolectado - Contenedor quemado/vandalizado'),
        
        # --- VIAJE 4: Miguel (No ha reportado nada aún, va recién en camino) -> 0% de avance
        # (No agregamos registros para simular que todo está en espera)
        
        # --- VIAJE 5: Diego (Pasó por 2 puntos, 1 exitoso, 1 con un auto tapando) -> 66% de avance
        (5, 11, '2026-06-17 10:15', 190.4, 'Completado'),
        (5, 15, '2026-06-17 11:50', 0.0, 'No Recolectado - Auto particular obstruyendo el paso')
    ]
    cursor.executemany('''
        INSERT INTO registros_retiro (ruta_activa_id, punto_id, fecha_hora, cantidad_retirada, estado)
        VALUES (?, ?, ?, ?, ?)
    ''', reportes_terreno)

    conexion.commit()
    conexion.close()
    print("🚀 ¡MEGA SIMULACIÓN INYECTADA!")
    print("• 5 Conductores y 5 Camiones listos.")
    print("• 16 Puntos de reciclaje georreferenciados y distribuidos.")
    print("• Datos de avance, incidentes y pesos cargados con éxito.")

if __name__ == '__main__':
    simular_operacion_completa()