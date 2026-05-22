import os
import sqlite3
import webbrowser
from threading import Timer
from flask import Flask, render_template, request, jsonify
from database_central import inicializar_bd_central

app = Flask(__name__)

@app.route('/')
def dashboard():
    # Usamos un bloque "with" para asegurar que la BD se cierre pase lo que pase
    try:
        conexion = sqlite3.connect('redcicla_central.db')
        cursor = conexion.cursor()
        cursor.execute('''
            SELECT r.punto_reciclaje_id, p.comuna, r.fecha_hora, r.cantidad_conductor, r.cantidad_balanza, r.estado
            FROM retiros_central r
            JOIN puntos_reciclaje p ON r.punto_reciclaje_id = p.id
            ORDER BY r.id DESC
        ''')
        retiros_db = cursor.fetchall()
    except Exception as e:
        print(f"⚠️ Error al leer la BD: {e}")
        retiros_db = []
    finally:
        conexion.close() # Cerramos explícitamente para liberar el archivo .db
        
    return render_template('dashboard.html', retiros=retiros_db)

@app.route('/api/sincronizar', methods=['POST'])
def sincronizar_datos():
    conexion = None
    try:
        datos_recibidos = request.get_json()
        if not datos_recibidos:
            return jsonify({"status": "error", "message": "No se enviaron datos"}), 400

        # Abrimos la conexión en modo exclusivo para evitar colisiones
        conexion = sqlite3.connect('redcicla_central.db', timeout=10)
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
            conexion.close() # 🔒 LIBERAMOS EL ARCHIVO AL INSTANTE

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