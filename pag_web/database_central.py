import os
import sqlite3
from werkzeug.security import generate_password_hash

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'redcicla_central.db')

def inicializar_bd_central():
    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    
    # ==========================================
    # 1. CREAR TODAS LAS TABLAS DEL SISTEMA
    # ==========================================
    cursor.execute('CREATE TABLE IF NOT EXISTS zonas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL)')
    cursor.execute('''CREATE TABLE IF NOT EXISTS rutas (
        id INTEGER PRIMARY KEY AUTOINCREMENT, 
        zona_id INTEGER, 
        nombre TEXT NOT NULL, 
        conductor_id INTEGER,
        FOREIGN KEY(zona_id) REFERENCES zonas(id),
        FOREIGN KEY(conductor_id) REFERENCES empleados(id))''')
    
    cursor.execute('''CREATE TABLE IF NOT EXISTS empleados (
        id INTEGER PRIMARY KEY AUTOINCREMENT, rut TEXT UNIQUE NOT NULL, nombre_completo TEXT NOT NULL,
        correo TEXT UNIQUE, telefono TEXT, clave_acceso TEXT NOT NULL, rol TEXT NOT NULL, licencia_conducir TEXT, estado INTEGER DEFAULT 1)''')

    cursor.execute('''CREATE TABLE IF NOT EXISTS camiones (
        id INTEGER PRIMARY KEY AUTOINCREMENT, patente TEXT UNIQUE NOT NULL, capacidad_carga REAL,
        ultima_revision TEXT, estado INTEGER DEFAULT 1, alerta INTEGER DEFAULT 0)''')
        
    cursor.execute('''CREATE TABLE IF NOT EXISTS revisiones_tecnicas (
        id INTEGER PRIMARY KEY AUTOINCREMENT, camion_id INTEGER, fecha_revision TEXT NOT NULL,
        aprobada INTEGER NOT NULL, observaciones TEXT, fecha_vencimiento TEXT NOT NULL,
        FOREIGN KEY(camion_id) REFERENCES camiones(id))''')

    cursor.execute('''CREATE TABLE IF NOT EXISTS puntos_reciclaje (
        id INTEGER PRIMARY KEY AUTOINCREMENT, ruta_id INTEGER, zona_id INTEGER, direccion TEXT,
        latitud REAL, longitud REAL, capacidad REAL DEFAULT 0.0, intervalo_recoleccion INTEGER, fecha_ultima_recoleccion TEXT, estado INTEGER DEFAULT 1,
        FOREIGN KEY(ruta_id) REFERENCES rutas(id), FOREIGN KEY(zona_id) REFERENCES zonas(id))''')

    # 🛡️ MIGRACIÓN AUTOMÁTICA: Forzamos la existencia de columnas críticas una por una
    columnas_a_verificar = [
        ("capacidad", "REAL DEFAULT 0.0"),
        ("latitud", "REAL"),
        ("longitud", "REAL")
    ]
    
    for col_nombre, col_tipo in columnas_a_verificar:
        try:
            cursor.execute(f"ALTER TABLE puntos_reciclaje ADD COLUMN {col_nombre} {col_tipo}")
            conexion.commit() 
            print(f"✅ Columna '{col_nombre}' agregada con éxito a puntos_reciclaje.")
        except sqlite3.OperationalError:
            pass

    # Nueva migración para la tabla rutas
    try:
        cursor.execute("ALTER TABLE rutas ADD COLUMN conductor_id INTEGER")
        conexion.commit()
        print("✅ Columna 'conductor_id' agregada con éxito a rutas.")
    except sqlite3.OperationalError:
        pass

    cursor.execute('CREATE TABLE IF NOT EXISTS contenedores (id INTEGER PRIMARY KEY AUTOINCREMENT, punto_id INTEGER, capacidad REAL NOT NULL, estado INTEGER NOT NULL, FOREIGN KEY(punto_id) REFERENCES puntos_reciclaje(id))')
    cursor.execute('CREATE TABLE IF NOT EXISTS historial_contenedor (id INTEGER PRIMARY KEY AUTOINCREMENT, contenedor_id INTEGER, valor REAL, fecha TEXT, FOREIGN KEY(contenedor_id) REFERENCES contenedores(id))')
    
    cursor.execute('''CREATE TABLE IF NOT EXISTS rutas_activas (
        id INTEGER PRIMARY KEY AUTOINCREMENT, ruta_id INTEGER, camion_id INTEGER, conductor_id INTEGER, ayudante_id INTEGER,
        fecha_inicio TEXT NOT NULL, fecha_fin TEXT, pesaje_calculado REAL, pesaje_final REAL,
        FOREIGN KEY(ruta_id) REFERENCES rutas(id), FOREIGN KEY(camion_id) REFERENCES camiones(id),
        FOREIGN KEY(conductor_id) REFERENCES empleados(id), FOREIGN KEY(ayudante_id) REFERENCES empleados(id))''')

    cursor.execute('''CREATE TABLE IF NOT EXISTS registros_retiro (
        id INTEGER PRIMARY KEY AUTOINCREMENT, ruta_activa_id INTEGER, punto_id INTEGER,
        fecha_hora TEXT NOT NULL, cantidad_retirada REAL, ruta_img_antes TEXT, ruta_img_despues TEXT,
        estado TEXT DEFAULT 'Pendiente',
        FOREIGN KEY(ruta_activa_id) REFERENCES rutas_activas(id), FOREIGN KEY(punto_id) REFERENCES puntos_reciclaje(id))''')

    # ==========================================
    # 2. SEMBRADO DE DATOS (DATA SEEDING)
    # ==========================================
    
    # A) Empleados (Todos activos)
    empleados_data = [
        ('11.111.111-1', 'Jefe General', 'jefe@redcicla.cl', '+56911111111', generate_password_hash('jefe123'), 'Jefe', 'N/A', 1),
        ('22.222.222-2', 'Admin Talca', 'admin@redcicla.cl', '+56922222222', generate_password_hash('admin123'), 'Administrador', 'N/A', 1),
        ('33.333.333-3', 'Conductor Juan', 'juan@redcicla.cl', '+56933333333', generate_password_hash('pass123'), 'Conductor', 'A4', 1),
        ('44.444.444-4', 'Conductor Pedro', 'pedro@redcicla.cl', '+56944444444', generate_password_hash('pass123'), 'Conductor', 'A4', 1),
        ('55.555.555-5', 'Ayudante Luis', 'luis@redcicla.cl', '+56955555555', generate_password_hash('pass123'), 'Ayudante', 'N/A', 1),
        ('66.666.666-6', 'Ayudante Ana', 'ana@redcicla.cl', '+56966666666', generate_password_hash('pass123'), 'Ayudante', 'N/A', 1)
    ]
    for emp in empleados_data:
        cursor.execute("INSERT OR IGNORE INTO empleados (rut, nombre_completo, correo, telefono, clave_acceso, rol, licencia_conducir, estado) VALUES (?,?,?,?,?,?,?,?)", emp)

    # B) Camiones
    camiones_data = [
        ('AB-CD-12', 5000.0, '2026-01-10', 1, 0),
        ('EF-GH-34', 7500.0, '2025-06-20', 1, 0),
        ('IJ-KL-56', 4000.0, '2026-05-01', 1, 0),
        ('MN-OP-78', 9000.0, '2024-12-15', 0, 0), # En taller
        ('QR-ST-90', 6000.0, '2025-08-30', 1, 0)
    ]
    for cam in camiones_data:
        cursor.execute("INSERT OR IGNORE INTO camiones (patente, capacidad_carga, ultima_revision, estado, alerta) VALUES (?,?,?,?,?)", cam)

    # C) Puntos de Reciclaje en Talca
    cursor.execute("SELECT COUNT(*) FROM puntos_reciclaje")
    if cursor.fetchone()[0] == 0:
        puntos_talca = [
            ("1 Oriente 4 Norte", -35.4264, -71.6554), ("Av. Lircay 1230", -35.4120, -71.6620),
            ("Dos Sur 1540", -35.4280, -71.6500), ("Av. Colin 890", -35.4450, -71.6680),
            ("Calle 11 Oriente 234", -35.4300, -71.6400), ("Av. San Miguel 3450", -35.4520, -71.6800),
            ("Cinco Oriente 560", -35.4200, -71.6500), ("Alameda 1200", -35.4270, -71.6580),
            ("Calle 30 Oriente 112", -35.4350, -71.6350), ("Av. Isidoro del Solar 90", -35.4220, -71.6650),
            ("Calle 8 Sur 1400", -35.4400, -71.6450), ("Av. Cancha Rayada 550", -35.4180, -71.6700),
            ("Calle 4 Norte 320", -35.4230, -71.6520), ("Av. circunvalación 2200", -35.4550, -71.6600),
            ("Calle 1 Sur 500", -35.4260, -71.6500), ("Parque Piduco 100", -35.4320, -71.6580)
        ]
        cursor.executemany("INSERT INTO puntos_reciclaje (direccion, latitud, longitud, estado) VALUES (?, ?, ?, 1)", puntos_talca)

    # D) Retiros Falsos para el Dashboard
    cursor.execute("SELECT COUNT(*) FROM registros_retiro")
    if cursor.fetchone()[0] == 0:
        retiros = [
            (None, 1, '2026-06-02 10:30', 125.5, None, None, 'Completado'),
            (None, 2, '2026-06-02 11:15', 85.0, None, None, 'Completado'),
            (None, 3, '2026-06-02 12:00', 210.3, None, None, 'Pendiente')
        ]
        cursor.executemany("INSERT INTO registros_retiro (ruta_activa_id, punto_id, fecha_hora, cantidad_retirada, ruta_img_antes, ruta_img_despues, estado) VALUES (?, ?, ?, ?, ?, ?, ?)", retiros)

    conexion.commit()
    conexion.close()
    print("✅ MEGA Base de Datos inicializada y poblada con todas las tablas correctas.")

def crear_ruta_prueba():
    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    
    print("🛠️ Ejecutando limpieza y reasignación de ruta para Juan...")
    
    # 1. Aseguramos datos maestros básicos
    cursor.execute("INSERT OR IGNORE INTO zonas (id, nombre) VALUES (1, 'Talca Centro')")
    cursor.execute("INSERT OR IGNORE INTO rutas (id, zona_id, nombre) VALUES (1, 1, 'Ruta Norte-1')")
    cursor.execute("INSERT OR IGNORE INTO camiones (id, patente, capacidad_carga, estado, alerta) VALUES (1, 'AB-CD-12', 5000, 1, 0)")
    
    # 2. Aseguramos puntos de reciclaje vinculados a la ruta 1
    puntos_vinculados = [
        (201, 1, 1, 'Calle 1 Oriente #123, Talca', 1000.0, -35.4264, -71.6554, 1),
        (202, 1, 1, 'Av. San Miguel #456, Talca', 800.0, -35.4320, -71.6310, 1)
    ]
    for p in puntos_vinculados:
        cursor.execute('''
            INSERT OR REPLACE INTO puntos_reciclaje (id, ruta_id, zona_id, direccion, capacidad, latitud, longitud, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ''', p)
    
    # 3. Buscamos el ID real de Juan
    cursor.execute("SELECT id FROM empleados WHERE correo = 'juan@redcicla.cl'")
    juan = cursor.fetchone()
    
    if juan:
        juan_id = juan[0]
        # Borramos rutas activas previas de Juan para que la App siempre detecte una nueva hoy
        cursor.execute("DELETE FROM rutas_activas WHERE conductor_id = ?", (juan_id,))
        
        from datetime import datetime
        fecha_hoy = datetime.now().strftime("%Y-%m-%d %H:%M")
        
        cursor.execute('''
            INSERT INTO rutas_activas (ruta_id, camion_id, conductor_id, fecha_inicio)
            VALUES (1, 1, ?, ?)
        ''', (juan_id, fecha_hoy))
        
        print(f"✅ RUTA ASIGNADA: Juan (ID: {juan_id}) ahora tiene la 'Ruta Norte-1' activa.")
    else:
        print("❌ ERROR: No se encontró a Juan en la base de datos.")
    
    conexion.commit()
    conexion.close()

if __name__ == '__main__':
    inicializar_bd_central()
    crear_ruta_prueba()