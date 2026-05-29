import os
import sqlite3

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'redcicla_central.db')

def inicializar_bd_central():
    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    
    # 1. ZONAS Y RUTAS
    cursor.execute('CREATE TABLE IF NOT EXISTS zonas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL)')
    cursor.execute('CREATE TABLE IF NOT EXISTS rutas (id INTEGER PRIMARY KEY AUTOINCREMENT, zona_id INTEGER, nombre TEXT NOT NULL, FOREIGN KEY(zona_id) REFERENCES zonas(id))')

    # 2. EMPLEADOS (Antes Usuarios - Mantenemos el estado para el Borrado Lógico)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS empleados (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            rut TEXT UNIQUE NOT NULL,
            nombre_completo TEXT NOT NULL,
            correo TEXT UNIQUE,
            telefono TEXT,
            clave_acceso TEXT NOT NULL,
            rol TEXT NOT NULL,
            licencia_conducir TEXT,
            estado INTEGER DEFAULT 1
        )
    ''')

    # 3. CAMIONES Y REVISIONES
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS camiones (
            id INTEGER PRIMARY KEY AUTOINCREMENT, patente TEXT UNIQUE NOT NULL, capacidad_carga REAL,
            ultima_revision TEXT, estado INTEGER, alerta INTEGER
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS revisiones_tecnicas (
            id INTEGER PRIMARY KEY AUTOINCREMENT, camion_id INTEGER, fecha_revision TEXT NOT NULL,
            aprobada INTEGER NOT NULL, observaciones TEXT, fecha_vencimiento TEXT NOT NULL,
            FOREIGN KEY(camion_id) REFERENCES camiones(id)
        )
    ''')

    # 4. PUNTOS, CONTENEDORES Y RUTAS ACTIVAS
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS puntos_reciclaje (
            id INTEGER PRIMARY KEY AUTOINCREMENT, ruta_id INTEGER, zona_id INTEGER, direccion TEXT,
            latitud REAL, longitud REAL, intervalo_recoleccion INTEGER, fecha_ultima_recoleccion TEXT, estado INTEGER,
            FOREIGN KEY(ruta_id) REFERENCES rutas(id), FOREIGN KEY(zona_id) REFERENCES zonas(id)
        )
    ''')
    cursor.execute('CREATE TABLE IF NOT EXISTS contenedores (id INTEGER PRIMARY KEY AUTOINCREMENT, punto_id INTEGER, capacidad REAL NOT NULL, estado INTEGER NOT NULL, FOREIGN KEY(punto_id) REFERENCES puntos_reciclaje(id))')
    cursor.execute('CREATE TABLE IF NOT EXISTS historial_contenedor (id INTEGER PRIMARY KEY AUTOINCREMENT, contenedor_id INTEGER, valor REAL, fecha TEXT, FOREIGN KEY(contenedor_id) REFERENCES contenedores(id))')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS rutas_activas (
            id INTEGER PRIMARY KEY AUTOINCREMENT, ruta_id INTEGER, camion_id INTEGER, conductor_id INTEGER, ayudante_id INTEGER,
            fecha_inicio TEXT NOT NULL, fecha_fin TEXT, pesaje_calculado REAL, pesaje_final REAL,
            FOREIGN KEY(ruta_id) REFERENCES rutas(id), FOREIGN KEY(camion_id) REFERENCES camiones(id),
            FOREIGN KEY(conductor_id) REFERENCES empleados(id), FOREIGN KEY(ayudante_id) REFERENCES empleados(id)
        )
    ''')

    # 5. REGISTROS DE RETIRO (Añadimos 'estado' para que la tabla del dashboard funcione)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS registros_retiro (
            id INTEGER PRIMARY KEY AUTOINCREMENT, ruta_activa_id INTEGER, punto_id INTEGER,
            fecha_hora TEXT NOT NULL, cantidad_retirada REAL, ruta_img_antes TEXT, ruta_img_despues TEXT,
            estado TEXT DEFAULT 'Pendiente',
            FOREIGN KEY(ruta_activa_id) REFERENCES rutas_activas(id), FOREIGN KEY(punto_id) REFERENCES puntos_reciclaje(id)
        )
    ''')

    conexion.commit()

    # --- INYECCIÓN DE DATOS MOCK ---
    try:
        # Puntos de reciclaje
        cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje (id, direccion, estado) VALUES (101, '1 Oriente 4 Norte, Talca', 1)")
        cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje (id, direccion, estado) VALUES (105, 'Huamachuco 230, San Clemente', 1)")
        
        # Empleados (Con los nuevos atributos del amigo)
        cursor.execute("INSERT OR IGNORE INTO empleados (rut, nombre_completo, correo, telefono, clave_acceso, rol, licencia_conducir, estado) VALUES ('11.111.111-1', 'Jefe General', 'jefe@redcicla.cl', '+56911111111', 'jefe123', 'Jefe', 'N/A', 1)")
        cursor.execute("INSERT OR IGNORE INTO empleados (rut, nombre_completo, correo, telefono, clave_acceso, rol, licencia_conducir, estado) VALUES ('22.222.222-2', 'Admin Talca', 'admin@redcicla.cl', '+56922222222', 'admin123', 'Administrador', 'N/A', 1)")
        cursor.execute("INSERT OR IGNORE INTO empleados (rut, nombre_completo, correo, telefono, clave_acceso, rol, licencia_conducir, estado) VALUES ('33.333.333-3', 'Conductor Despedido', 'juan@redcicla.cl', '+56933333333', 'juan123', 'Conductor', 'A4', 0)")
        
        conexion.commit()
    except sqlite3.OperationalError as e:
        print(f"⚠️ Nota: {e}")
        
    conexion.close()
    print("📋 MEGA Base de datos RedCicla armada e integrada con éxito.")

if __name__ == '__main__':
    inicializar_bd_central()