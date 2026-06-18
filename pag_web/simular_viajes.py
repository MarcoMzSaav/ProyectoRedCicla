import sqlite3
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'redcicla_central.db')

conexion = sqlite3.connect(DB_PATH)
cursor = conexion.cursor()

# Simulamos 4 retiros de vidrio masivos realizados el día de hoy
viajes_falsos = [
    (1, 1, '2026-06-17 09:30', 450.5, 'Completado'),
    (1, 2, '2026-06-17 10:15', 320.0, 'Completado'),
    (1, 3, '2026-06-17 11:45', 510.3, 'Completado'),
    (1, 4, '2026-06-17 12:30', 180.2, 'Pendiente')
]

cursor.executemany('''
    INSERT INTO registros_retiro (ruta_activa_id, punto_id, fecha_hora, cantidad_retirada, estado) 
    VALUES (?, ?, ?, ?, ?)
''', viajes_falsos)

conexion.commit()
conexion.close()

print("✅ Viajes simulados inyectados en la base de datos.")