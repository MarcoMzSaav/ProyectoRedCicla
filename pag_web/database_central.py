import sqlite3

def inicializar_bd_central():
    conexion = sqlite3.connect('redcicla_central.db')
    cursor = conexion.cursor()
    
    # 1. Creamos las tablas de forma secuencial e independiente
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS usuarios (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT,
            correo TEXT UNIQUE NOT NULL,
            clave TEXT NOT NULL,
            rol TEXT NOT NULL
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
    
    # Aseguramos que las tablas existan físicamente guardando los cambios primero
    conexion.commit()
    
    # 2. Inyecciones de prueba protegidas con un bloque Try/Except
    try:
        cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje VALUES (101, 'Talca Centro', '1 Oriente 4 Norte', 500.0)")
        cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje VALUES (105, 'San Clemente', 'Huamachuco 230', 800.0)")
        cursor.execute("INSERT OR IGNORE INTO usuarios (correo, clave, rol) VALUES ('admin@redcicla.cl', 'admin123', 'Administrador')")
        conexion.commit()
    except sqlite3.OperationalError as e:
        print(f"⚠️ Nota de inicialización: {e}")
        
    conexion.close()
    print("📋 Base de datos central relacional armada y sincronizada con éxito.")

if __name__ == '__main__':
    inicializar_bd_central()