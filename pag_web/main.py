import os
import sqlite3
import webbrowser
from datetime import datetime
from threading import Timer
from werkzeug.security import generate_password_hash, check_password_hash
from flask import Flask, render_template, request, jsonify, session, redirect, url_for
from database_central import inicializar_bd_central

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'redcicla_central.db')

app = Flask(__name__)
app.secret_key = 'redcicla_clave_super_secreta_2026'

# ==========================================
# 1. RUTAS DE SESIÓN (LOGIN / LOGOUT)
# ==========================================
@app.route('/login', methods=['GET', 'POST'])
def login():
    error = None
    if request.method == 'POST':
        correo = request.form['correo']
        clave_ingresada = request.form['clave']

        conexion = sqlite3.connect(DB_PATH)
        cursor = conexion.cursor()
        # Ahora traemos la clave_acceso guardada en la base de datos para compararla
        cursor.execute("SELECT nombre_completo, rol, estado, clave_acceso FROM empleados WHERE correo = ?", (correo,))
        usuario = cursor.fetchone()
        conexion.close()

        # check_password_hash hace la magia de comprobar si "jefe123" coincide con el hash guardado
        if usuario and check_password_hash(usuario[3], clave_ingresada):
            nombre, rol, estado, _ = usuario
            if estado == 1:
                session['usuario_nombre'] = nombre
                session['usuario_rol'] = rol
                return redirect(url_for('dashboard'))
            else:
                error = "Su cuenta ha sido desactivada. Contacte al Jefe de Operaciones."
        else:
            error = "Correo o contraseña incorrectos."

    return render_template('login.html', error=error)

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))

# ==========================================
# 2. DASHBOARD
# ==========================================
@app.route('/')
def dashboard():
    if 'usuario_nombre' not in session:
        return redirect(url_for('login'))

    try:
        conexion = sqlite3.connect(DB_PATH)
        cursor = conexion.cursor()
        cursor.execute('''
            SELECT r.punto_id, p.direccion, r.fecha_hora, r.cantidad_retirada, r.estado
            FROM registros_retiro r
            JOIN puntos_reciclaje p ON r.punto_id = p.id
            ORDER BY r.id DESC
        ''')
        retiros_db = cursor.fetchall()
    except Exception as e:
        retiros_db = []
    finally:
        if 'conexion' in locals():
            conexion.close() 
        
    return render_template('dashboard.html', retiros=retiros_db, nombre=session.get('usuario_nombre'), rol=session.get('usuario_rol'))

# ==========================================
# 3. MÓDULO CRUD DE EMPLEADOS (PERSONAL)
# ==========================================
@app.route('/usuarios')
def gestionar_usuarios():
    if session.get('usuario_rol') not in ['Jefe', 'Administrador']:
        return redirect(url_for('dashboard'))

    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    cursor.execute("SELECT id, rut, nombre_completo, correo, telefono, rol, estado FROM empleados ORDER BY rol, nombre_completo")
    lista_usuarios = cursor.fetchall()
    conexion.close()

    return render_template('usuarios.html', usuarios=lista_usuarios)
@app.route('/usuarios/crear', methods=['POST'])
def crear_usuario():
    rol_actual = session.get('usuario_rol')
    if rol_actual not in ['Jefe', 'Administrador']:
        return redirect(url_for('dashboard'))
    
    rut = request.form['rut']
    nombre = request.form['nombre']
    correo = request.form['correo']
    telefono = request.form['telefono']
    clave_plana = request.form['clave']
    rol_nuevo = request.form['rol']

    if rol_actual == 'Administrador' and rol_nuevo in ['Jefe', 'Administrador']:
        return redirect(url_for('gestionar_usuarios'))

    # Transformamos la clave plana a un Hash antes de guardarla
    clave_hash = generate_password_hash(clave_plana)

    try:
        conexion = sqlite3.connect(DB_PATH)
        cursor = conexion.cursor()
        cursor.execute("INSERT INTO empleados (rut, nombre_completo, correo, telefono, clave_acceso, rol, estado) VALUES (?, ?, ?, ?, ?, ?, 1)", 
                       (rut, nombre, correo, telefono, clave_hash, rol_nuevo))
        conexion.commit()
    except sqlite3.IntegrityError:
        print("Error: El correo o RUT ya existe.") 
    finally:
        if 'conexion' in locals():
            conexion.close()
    
    return redirect(url_for('gestionar_usuarios'))
@app.route('/usuarios/estado/<int:id_usuario>', methods=['POST'])
def cambiar_estado_usuario(id_usuario):
    rol_actual = session.get('usuario_rol')
    if rol_actual not in ['Jefe', 'Administrador']:
        return redirect(url_for('dashboard'))

    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    
    # Obtener datos del usuario a modificar
    cursor.execute("SELECT rol, estado FROM empleados WHERE id = ?", (id_usuario,))
    usuario_db = cursor.fetchone()
    
    if usuario_db:
        rol_objetivo, estado_actual = usuario_db
        
        # 🛡️ SEGURIDAD: Un administrador no puede bloquear a un Jefe ni a otro Admin
        if rol_actual == 'Administrador' and rol_objetivo in ['Jefe', 'Administrador']:
            conexion.close()
            return redirect(url_for('gestionar_usuarios'))

        nuevo_estado = 0 if estado_actual == 1 else 1
        cursor.execute("UPDATE empleados SET estado = ? WHERE id = ?", (nuevo_estado, id_usuario))
        conexion.commit()
        
    conexion.close()
    return redirect(url_for('gestionar_usuarios'))

@app.route('/usuarios/editar/<int:id_usuario>', methods=['POST'])
def editar_usuario(id_usuario):
    rol_actual = session.get('usuario_rol')
    if rol_actual not in ['Jefe', 'Administrador']:
        return redirect(url_for('dashboard'))
    
    rut = request.form['rut']
    nombre = request.form['nombre']
    correo = request.form['correo']
    telefono = request.form['telefono']
    rol_nuevo = request.form['rol']

    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    
    # 🛡️ SEGURIDAD: Comprobamos a quién intentan editar
    cursor.execute("SELECT rol FROM empleados WHERE id = ?", (id_usuario,))
    rol_objetivo = cursor.fetchone()[0]

    # Reglas estrictas:
    # 1. El Admin no puede editar a Jefes ni a otros Admins.
    # 2. El Admin no puede ascender a un Conductor para que sea Admin.
    if rol_actual == 'Administrador':
        if rol_objetivo in ['Jefe', 'Administrador'] or rol_nuevo in ['Jefe', 'Administrador']:
            conexion.close()
            return redirect(url_for('gestionar_usuarios'))

    cursor.execute('''
        UPDATE empleados 
        SET rut = ?, nombre_completo = ?, correo = ?, telefono = ?, rol = ? 
        WHERE id = ?
    ''', (rut, nombre, correo, telefono, rol_nuevo, id_usuario))
    
    conexion.commit()
    conexion.close()

    return redirect(url_for('gestionar_usuarios'))

# ==========================================
# 4. MÓDULO DE GESTIÓN DE FLOTA
# ==========================================
@app.route('/flota')
def gestion_flota():
    if 'usuario_nombre' not in session:
        return redirect(url_for('login'))

    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    
    # --- CEREBRO AUTOMÁTICO DE ALERTAS (HU-13) ---
    # 1. Traemos las fechas de revisión de todos los camiones
    cursor.execute("SELECT id, ultima_revision FROM camiones")
    camiones_revision = cursor.fetchall()
    
    hoy = datetime.now()
    
    for camion in camiones_revision:
        id_camion = camion[0]
        fecha_rev_str = camion[1]
        
        try:
            # Convertimos el texto de la base de datos a una Fecha real de Python
            fecha_rev = datetime.strptime(fecha_rev_str, "%Y-%m-%d")
            # Calculamos cuántos días han pasado
            dias_pasados = (hoy - fecha_rev).days
            
            # Si pasaron más de 365 días (1 año), se prende la alerta (1), si no, se apaga (0)
            nueva_alerta = 1 if dias_pasados >= 365 else 0
            
            # Actualizamos silenciosamente el camión en la base de datos
            cursor.execute("UPDATE camiones SET alerta = ? WHERE id = ?", (nueva_alerta, id_camion))
        except Exception as e:
            pass # Por si un camión tiene la fecha en blanco o mal escrita
            
    conexion.commit()
    # --------------------------------------------

    # 2. Ahora sí, le mandamos los camiones actualizados a la página web
    cursor.execute("SELECT id, patente, capacidad_carga, ultima_revision, estado, alerta FROM camiones ORDER BY patente")
    lista_camiones = cursor.fetchall()
    conexion.close()

    return render_template('flota.html', camiones=lista_camiones)

@app.route('/flota/crear', methods=['POST'])
def crear_camion():
    if 'usuario_nombre' not in session:
        return redirect(url_for('login'))
    
    patente = request.form['patente'].upper()
    capacidad = request.form['capacidad']
    revision = request.form['revision']

    try:
        conexion = sqlite3.connect(DB_PATH)
        cursor = conexion.cursor()
        cursor.execute("INSERT INTO camiones (patente, capacidad_carga, ultima_revision, estado, alerta) VALUES (?, ?, ?, 1, 0)", 
                       (patente, float(capacidad), revision))
        conexion.commit()
    except sqlite3.IntegrityError:
        print("Error: La patente ya está registrada.") 
    finally:
        if 'conexion' in locals():
            conexion.close()
    
    return redirect(url_for('gestion_flota'))

@app.route('/flota/estado/<int:id_camion>', methods=['POST'])
def cambiar_estado_camion(id_camion):
    if 'usuario_nombre' not in session:
        return redirect(url_for('login'))

    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    
    cursor.execute("SELECT estado FROM camiones WHERE id = ?", (id_camion,))
    estado_actual = cursor.fetchone()[0]
    nuevo_estado = 0 if estado_actual == 1 else 1
    
    cursor.execute("UPDATE camiones SET estado = ? WHERE id = ?", (nuevo_estado, id_camion))
    conexion.commit()
    conexion.close()

    return redirect(url_for('gestion_flota'))

@app.route('/flota/editar/<int:id_camion>', methods=['POST'])
def editar_camion(id_camion):
    if 'usuario_nombre' not in session:
        return redirect(url_for('login'))
    
    patente = request.form['patente'].upper()
    capacidad = request.form['capacidad']
    revision = request.form['revision']

    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    cursor.execute('''
        UPDATE camiones 
        SET patente = ?, capacidad_carga = ?, ultima_revision = ? 
        WHERE id = ?
    ''', (patente, float(capacidad), revision, id_camion))
    conexion.commit()
    conexion.close()

    return redirect(url_for('gestion_flota'))

# ==========================================
# 5. PÁGINAS EN CONSTRUCCIÓN (Navegación)
# ==========================================
@app.route('/puntos')
def puntos_limpios():
    if 'usuario_nombre' not in session:
        return redirect(url_for('login'))
    return render_template('construccion.html', titulo="Puntos Limpios (Talca)")

@app.route('/reporte-co2')
def reporte_co2():
    if 'usuario_nombre' not in session:
        return redirect(url_for('login'))
    return render_template('construccion.html', titulo="Reporte de Huella de CO2")

# ==========================================
# 6. API DE SINCRONIZACIÓN MÓVIL
# ==========================================
@app.route('/api/sincronizar', methods=['POST'])
def sincronizar_datos():
    conexion = None
    try:
        datos_recibidos = request.get_json()
        if not datos_recibidos:
            return jsonify({"status": "error", "message": "No se enviaron datos"}), 400

        conexion = sqlite3.connect(DB_PATH, timeout=10)
        cursor = conexion.cursor()

        for registro in datos_recibidos:
            cursor.execute('''
                INSERT INTO registros_retiro (punto_id, cantidad_retirada, fecha_hora, estado)
                VALUES (?, ?, ?, 'Pendiente')
            ''', (
                int(registro['punto_reciclaje_id']),
                float(registro['cantidad_conductor']),
                registro['fecha_hora']
            ))

        conexion.commit()
        return jsonify({"status": "success", "message": "Sincronizado"}), 200

    except Exception as e:
        if conexion:
            conexion.rollback()
        return jsonify({"status": "error", "message": str(e)}), 500
    finally:
        if conexion:
            conexion.close()

def abrir_navegador():
    webbrowser.open_new("http://127.0.0.1:8000/")

if __name__ == '__main__':
    os.system('cls' if os.name == 'nt' else 'clear')
    inicializar_bd_central()
    
    print("=" * 60)
    print("♻️  PLATAFORMA WEB ADMINISTRATIVA - REDCICLA (TALCA)  ♻️")
    print("=" * 60)
    
    Timer(1.5, abrir_navegador).start()
    app.run(debug=True, port=8000, use_reloader=False)