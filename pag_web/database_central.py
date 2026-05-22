import sqlite3

def inicializar_bd_central():
    # Creamos o conectamos el archivo de base de datos del servidor
    conexion = sqlite3.connect('redcicla_central.db')
    cursor = conexion.cursor()
    
    # 1. Tabla de Usuarios (HU-01 y HU-02: Control de accesos)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS usuarios (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT EXISTS,
            correo TEXT UNIQUE NOT NULL,
            clave TEXT NOT NULL,
            rol TEXT NOT NULL -- 'Administrador', 'Conductor', 'Ayudante'
        )
    ''')
    
    # 2. Tabla de Puntos de Reciclaje (HU-04: Gestión de puntos limpios en Talca)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS puntos_reciclaje (
            id INTEGER PRIMARY KEY,
            comuna TEXT NOT NULL,
            direccion TEXT NOT NULL,
            capacidad_maxima REAL NOT NULL
        )
    ''')
    
    # 3. Tabla Central de Retiros (Aquí caerán los datos que envíe la App Móvil)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS retiros_central (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            punto_reciclaje_id INTEGER,
            cantidad_conductor REAL, -- Lo que anotó el chofer con la pluma offline
            cantidad_balanza REAL DEFAULT 0, -- Lo que pesó la romana al volver a acopio
            fecha_hora TEXT NOT NULL,
            estado TEXT DEFAULT 'Pendiente', -- 'Validado' o 'Auditoría' si hay descuadre
            FOREIGN KEY(punto_reciclaje_id) REFERENCES puntos_reciclaje(id)
        )
    ''')
    
    # Insertamos algunos datos falsos (Mock) de puntos limpios para que la web tenga qué leer
    cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje VALUES (101, 'Talca Centro', '1 Oriente 4 Norte', 500.0)")
    cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje VALUES (105, 'San Clemente', 'Huamachuco 230', 800.0)")
    
    # Insertamos usuarios de prueba por defecto
    cursor.execute("INSERT OR IGNORE INTO usuarios (correo, clave, rol) VALUES ('admin@redcicla.cl', 'admin123', 'Administrador')")
    
    conexion.commit()
    conexion.close()
    print("📋 Base de datos central sincronizada e inicializada con éxito.")

if __name__ == '__main__':
    inicializar_bd_central()