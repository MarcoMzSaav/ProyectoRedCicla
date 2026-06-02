import os
import sqlite3
from werkzeug.security import generate_password_hash

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'redcicla_central.db')

def inicializar_bd_central():
    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    
    # 1. ZONAS Y RUTAS
    cursor.execute('CREATE TABLE IF NOT EXISTS zonas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL)')
    cursor.execute('CREATE TABLE IF NOT EXISTS rutas (id INTEGER PRIMARY KEY AUTOINCREMENT, zona_id INTEGER, nombre TEXT NOT NULL, FOREIGN KEY(zona_id) REFERENCES zonas(id))')

    # 2. EMPLEADOS (Antes Usuarios)
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

    # 3. CAMIONES Y REVISIONES (¡Aquí está la tabla que faltaba!)
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

    # 4. PUNTOS Y RUTAS ACTIVAS
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS puntos_reciclaje (
            id INTEGER PRIMARY KEY AUTOINCREMENT, ruta_id INTEGER, zona_id INTEGER, direccion TEXT,
            latitud REAL, longitud REAL, capacidad REAL DEFAULT 0.0, intervalo_recoleccion INTEGER, fecha_ultima_recoleccion TEXT, estado INTEGER,
            FOREIGN KEY(ruta_id) REFERENCES rutas(id), FOREIGN KEY(zona_id) REFERENCES zonas(id)
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS rutas_activas (
            id INTEGER PRIMARY KEY AUTOINCREMENT, ruta_id INTEGER, camion_id INTEGER, conductor_id INTEGER, ayudante_id INTEGER,
            fecha_inicio TEXT NOT NULL, fecha_fin TEXT, pesaje_calculado REAL, pesaje_final REAL,
            FOREIGN KEY(ruta_id) REFERENCES rutas(id), FOREIGN KEY(camion_id) REFERENCES camiones(id),
            FOREIGN KEY(conductor_id) REFERENCES empleados(id), FOREIGN KEY(ayudante_id) REFERENCES empleados(id)
        )
    ''')

    # 5. REGISTROS DE RETIRO
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
        cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje (id, direccion, estado) VALUES (101, '1 Oriente 4 Norte, Talca', 1)")
        cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje (id, direccion, estado) VALUES (105, 'Huamachuco 230, San Clemente', 1)")
        conexion.commit()
    except sqlite3.OperationalError as e:
        print(f"⚠️ Error en puntos: {e}")

# 👇 2. REEMPLAZAR ESTA PARTE 👇
    try:
        # Generamos los hashes de las contraseñas
        hash_jefe = generate_password_hash('jefe123')
        hash_admin = generate_password_hash('admin123')
        hash_juan = generate_password_hash('juan123')
        hash_pedro = generate_password_hash('pedro123')

        # Roles permitidos: Jefe, Administrador, Conductor, Ayudante
        cursor.execute("INSERT OR IGNORE INTO empleados (rut, nombre_completo, correo, telefono, clave_acceso, rol, licencia_conducir, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 
                       ('11.111.111-1', 'Jefe General', 'jefe@redcicla.cl', '+56911111111', hash_jefe, 'Jefe', 'N/A', 1))
        cursor.execute("INSERT OR IGNORE INTO empleados (rut, nombre_completo, correo, telefono, clave_acceso, rol, licencia_conducir, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 
                       ('22.222.222-2', 'Admin Talca', 'admin@redcicla.cl', '+56922222222', hash_admin, 'Administrador', 'N/A', 1))
        cursor.execute("INSERT OR IGNORE INTO empleados (rut, nombre_completo, correo, telefono, clave_acceso, rol, licencia_conducir, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 
                       ('33.333.333-3', 'Juan Conductor', 'juan@redcicla.cl', '+56933333333', hash_juan, 'Conductor', 'A4', 1))
        cursor.execute("INSERT OR IGNORE INTO empleados (rut, nombre_completo, correo, telefono, clave_acceso, rol, licencia_conducir, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 
                       ('44.444.444-4', 'Pedro Ayudante', 'pedro@redcicla.cl', '+56944444444', hash_pedro, 'Ayudante', 'N/A', 1))
        conexion.commit()
    except sqlite3.OperationalError as e:
        print(f"⚠️ Error en empleados: {e}")
        
    conexion.close()
    print("📋 MEGA Base de datos RedCicla armada e integrada con éxito.")

def crear_ruta_prueba():
    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    
    # 1. Aseguramos que existan datos maestros
    cursor.execute("INSERT OR IGNORE INTO zonas (id, nombre) VALUES (1, 'Talca Centro')")
    cursor.execute("INSERT OR IGNORE INTO rutas (id, zona_id, nombre) VALUES (1, 1, 'Ruta Norte-1')")
    cursor.execute("INSERT OR IGNORE INTO camiones (id, patente, capacidad_carga, estado, alerta) VALUES (1, 'ABCD-12', 5000, 1, 0)")
    
    # 2. Aseguramos puntos de reciclaje para esa ruta
    cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje (id, ruta_id, zona_id, direccion, capacidad, estado) VALUES (201, 1, 1, 'Calle 1 Oriente #123, Talca', 1000.0, 1)")
    cursor.execute("INSERT OR IGNORE INTO puntos_reciclaje (id, ruta_id, zona_id, direccion, capacidad, estado) VALUES (202, 1, 1, 'Av. San Miguel #456, Talca', 800.0, 1)")
    
    # 3. Buscamos el ID real de Juan en la tabla empleados
    cursor.execute("SELECT id FROM empleados WHERE correo = 'juan@redcicla.cl'")
    juan = cursor.fetchone()
    
    if juan:
        juan_id = juan[0]
        # 4. Verificamos si Juan ya tiene una ruta activa para no duplicar
        cursor.execute("SELECT id FROM rutas_activas WHERE conductor_id = ? AND fecha_fin IS NULL", (juan_id,))
        existe = cursor.fetchone()
        
        if not existe:
            from datetime import datetime
            fecha_hoy = datetime.now().strftime("%Y-%m-%d %H:%M")
            cursor.execute('''
                INSERT INTO rutas_activas (ruta_id, camion_id, conductor_id, fecha_inicio)
                VALUES (1, 1, ?, ?)
            ''', (juan_id, fecha_hoy))
            print(f"✅ Ruta activa asignada exitosamente a Juan (ID: {juan_id})")
        else:
            print(f"ℹ️ Juan (ID: {juan_id}) ya tiene una ruta activa asignada.")
    
    conexion.commit()
    conexion.close()

if __name__ == '__main__':
    inicializar_bd_central()
    crear_ruta_prueba()