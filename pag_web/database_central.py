import os
import sqlite3

# Blindaje de ruta absoluta FISO
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'redcicla_central.db')

def inicializar_bd_central():
    conexion = sqlite3.connect(DB_PATH) # <-- Blindado
    cursor = conexion.cursor()
    
    # 1. CREACIÓN DE TABLAS
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS usuarios (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT,
            correo TEXT UNIQUE NOT NULL,
            clave TEXT NOT NULL,
            rol TEXT NOT NULL,
            estado INTEGER DEFAULT 1
        )
    ''')
    
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS puntos_reciclaje (
            id INTEGER PRIMARY KEY,
            comuna TEXT NOT NULL,
            direccion TEXT NOT NULL,
            capacidad_maxima REAL NOT NULL
        )
    ''')
    
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS retiros_central (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            punto_reciclaje_id INTEGER,
            cantidad_conductor REAL,
            cantidad_balanza REAL DEFAULT 0,
            fecha_hora TEXT NOT NULL,
            estado TEXT DEFAULT 'Pendiente',
            FOREIGN KEY(punto_reciclaje_id) REFERENCES puntos_reciclaje(id)
        )
    ''')
    conexion.commit()
    
    # 2. INYECCIÓN DE DATOS
    try:
        cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje VALUES (101, 'Talca Centro', '1 Oriente 4 Norte', 500.0)")
        cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje VALUES (105, 'San Clemente', 'Huamachuco 230', 800.0)")
        
        cursor.execute("INSERT OR IGNORE INTO usuarios (nombre, correo, clave, rol, estado) VALUES ('Jefe General', 'jefe@redcicla.cl', 'jefe123', 'Jefe', 1)")
        cursor.execute("INSERT OR IGNORE INTO usuarios (nombre, correo, clave, rol, estado) VALUES ('Admin Talca', 'admin@redcicla.cl', 'admin123', 'Administrador', 1)")
        cursor.execute("INSERT OR IGNORE INTO usuarios (nombre, correo, clave, rol, estado) VALUES ('Conductor Despedido', 'juan@redcicla.cl', 'juan123', 'Conductor', 0)")
        conexion.commit()
    except sqlite3.OperationalError as e:
        print(f"⚠️ Nota de inicialización: {e}")
        
    conexion.close()
    print("📋 Base de datos central relacional armada y sincronizada con éxito.")

if __name__ == '__main__':
    inicializar_bd_central()