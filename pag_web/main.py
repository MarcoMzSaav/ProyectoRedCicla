import os
import sqlite3
import webbrowser
from threading import Timer
from flask import Flask, render_template, request, jsonify, session, redirect, url_for
from database_central import inicializar_bd_central

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'redcicla_central.db')

app = Flask(__name__)
app.secret_key = 'redcicla_clave_super_secreta_2026'

@app.route('/login', methods=['GET', 'POST'])
def login():
    error = None
    if request.method == 'POST':
        correo = request.form['correo']
        clave = request.form['clave']

        conexion = sqlite3.connect(DB_PATH)
        cursor = conexion.cursor()
        # 🚨 Adaptado a 'empleados' y 'nombre_completo' y 'clave_acceso'
        cursor.execute("SELECT nombre_completo, rol, estado FROM empleados WHERE correo = ? AND clave_acceso = ?", (correo, clave))
        usuario = cursor.fetchone()
        conexion.close()

        if usuario:
            nombre, rol, estado = usuario
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

@app.route('/')
def dashboard():
    if 'usuario_nombre' not in session:
        return redirect(url_for('login'))

    try:
        conexion = sqlite3.connect(DB_PATH)
        cursor = conexion.cursor()
        # 🚨 Adaptado a 'registros_retiro'
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
# 5. MÓDULO CRUD DE EMPLEADOS
# ==========================================
@app.route('/usuarios')
def gestionar_usuarios():
    if session.get('usuario_rol') not in ['Jefe', 'Administrador']:
        return redirect(url_for('dashboard'))

    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    cursor.execute("SELECT id, nombre_completo, correo, rol, estado FROM empleados ORDER BY rol, nombre_completo")
    lista_usuarios = cursor.fetchall()
    conexion.close()

    return render_template('usuarios.html', usuarios=lista_usuarios)

@app.route('/usuarios/crear', methods=['POST'])
def crear_usuario():
    if session.get('usuario_rol') not in ['Jefe', 'Administrador']:
        return redirect(url_for('dashboard'))
    
    rut = request.form['rut'] # Nuevo campo requerido por la BD
    nombre = request.form['nombre']
    correo = request.form['correo']
    telefono = request.form['telefono'] # Nuevo campo
    clave = request.form['clave']
    rol = request.form['rol']

    try:
        conexion = sqlite3.connect(DB_PATH)
        cursor = conexion.cursor()
        cursor.execute("INSERT INTO empleados (rut, nombre_completo, correo, telefono, clave_acceso, rol, estado) VALUES (?, ?, ?, ?, ?, ?, 1)", 
                       (rut, nombre, correo, telefono, clave, rol))
        conexion.commit()
    except sqlite3.IntegrityError:
        print("Error: El correo o RUT ya existe.") 
    finally:
        if 'conexion' in locals():
            conexion.close()
    
    return redirect(url_for('gestionar_usuarios'))

@app.route('/usuarios/estado/<int:id_usuario>', methods=['POST'])
def cambiar_estado_usuario(id_usuario):
    if session.get('usuario_rol') not in ['Jefe', 'Administrador']:
        return redirect(url_for('dashboard'))

    conexion = sqlite3.connect(DB_PATH)
    cursor = conexion.cursor()
    cursor.execute("SELECT estado FROM empleados WHERE id = ?", (id_usuario,))
    estado_actual = cursor.fetchone()[0]
    nuevo_estado = 0 if estado_actual == 1 else 1
    cursor.execute("UPDATE empleados SET estado = ? WHERE id = ?", (nuevo_estado, id_usuario))
    conexion.commit()
    conexion.close()

    return redirect(url_for('gestionar_usuarios'))


# --- 4. API DE SINCRONIZACIÓN MÓVIL ---
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

# ==========================================
# 6. PÁGINAS EN CONSTRUCCIÓN (Navegación)
# ==========================================

@app.route('/flota')
def gestion_flota():
    if 'usuario_nombre' not in session:
        return redirect(url_for('login'))
    return render_template('construccion.html', titulo="Gestión de Flota")

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