# ESTO ES PARA SIMULAR PROBLEMAS EN EL SISTEMA DE REPORTES EN TERRENO, COMO UN VIAJE FALLIDO O UN PROBLEMA CON EL CAMIÓN
import sqlite3
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'redcicla_central.db')

conexion = sqlite3.connect(DB_PATH)
cursor = conexion.cursor()

# Simulamos que un camión no pudo hacer el retiro por un problema real
viaje_fallido = (1, 5, '2026-06-17 14:10', 0.0, 'No Recolectado - Auto tapando el contenedor')

cursor.execute('''
    INSERT INTO registros_retiro (ruta_activa_id, punto_id, fecha_hora, cantidad_retirada, estado) 
    VALUES (?, ?, ?, ?, ?)
''', viaje_fallido)

conexion.commit()
conexion.close()
print("✅ Reporte de incidencia simulado con éxito.")