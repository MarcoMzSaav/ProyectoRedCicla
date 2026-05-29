import os
import sqlite3
import webbrowser
from threading import Timer
from flask import Flask, render_template, request, jsonify, session, redirect, url_for
from database_central import inicializar_bd_central

# Blindaje de ruta absoluta FISO
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, 'redcicla_central.db')

app = Flask(__name__)
app.secret_key = 'redcicla_clave_super_secreta_2026'

# --- 1. RUTA DE LOGIN ---
@app.route('/login', methods=['GET', 'POST'])
def login():
    error = None
    if request.method == 'POST':
        correo = request.form['correo']
        clave = request.form['clave']

        conexion = sqlite3.connect(DB_PATH) # <-- Blindado
        cursor = conexion.cursor()
        cursor.execute("SELECT nombre, rol, estado FROM usuarios WHERE correo = ? AND clave = ?", (correo, clave))
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

# --- 2. RUTA DE CERRAR SESIÓN ---
@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))

# --- 3. RUTA DEL DASHBOARD PROTEGIDA ---
@app.route('/')
def dashboard():
    if 'usuario_nombre' not in session:
        return redirect(url_for('login'))

    try:
        conexion = sqlite3.connect(DB_PATH) # <-- ¡Aquí solía estar el fantasma!
        cursor = conexion.cursor()
        cursor.execute('''
            SELECT r.punto_reciclaje_id, p.comuna, r.fecha_hora, r.cantidad_conductor, r.cantidad_balanza, r.estado
            FROM retiros_central r
            JOIN puntos_reciclaje p ON r.punto_reciclaje_id = p.id
            ORDER BY r.id DESC
        ''')
        retiros_db = cursor.fetchall()
    except Exception as e:
        retiros_db = []
    finally:
        if 'conexion' in locals():
            conexion.close() 
        
    return render_template('dashboard.html', retiros=retiros_db, nombre=session.get('usuario_nombre'), rol=session.get('usuario_rol'))

# --- 4. API DE SINCRONIZACIÓN MÓVIL ---
@app.route('/api/sincronizar', methods=['POST'])
def sincronizar_datos():
    conexion = None
    try:
        datos_recibidos = request.get_json()
        if not datos_recibidos:
            return jsonify({"status": "error", "message": "No se enviaron datos"}), 400

        conexion = sqlite3.connect(DB_PATH, timeout=10) # <-- Y aquí también estaba el fantasma
        cursor = conexion.cursor()

        for registro in datos_recibidos:
            cursor.execute('''
                INSERT INTO retiros_central (punto_reciclaje_id, cantidad_conductor, fecha_hora, estado)
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