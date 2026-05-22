import os
import sqlite3
import webbrowser
from threading import Timer
from flask import Flask, render_template
# Importamos el creador de base de datos
from database_central import inicializar_bd_central

app = Flask(__name__)

@app.route('/')
def dashboard():
    # Conectamos a la BD para extraer los retiros reales
    conexion = sqlite3.connect('redcicla_central.db')
    cursor = conexion.cursor()
    
    # Hacemos un JOIN para saber la comuna del punto limpio asignado al retiro
    cursor.execute('''
        SELECT r.punto_reciclaje_id, p.comuna, r.fecha_hora, r.cantidad_conductor, r.cantidad_balanza, r.estado
        FROM retiros_central r
        JOIN puntos_reciclaje p ON r.punto_reciclaje_id = p.id
    ''')
    retiros_db = cursor.fetchall()
    conexion.close()
    
    # Pasamos los registros de la BD directamente al HTML
    return render_template('dashboard.html', retiros=retiros_db)

def abrir_navegador():
    webbrowser.open_new("http://127.0.0.1:8000/")

if __name__ == '__main__':
    os.system('cls' if os.name == 'nt' else 'clear')
    
    # ⚙️ Inicializamos el archivo SQL central antes de prender la web
    inicializar_bd_central()
    
    print("=" * 60)
    print("♻️  PLATAFORMA WEB ADMINISTRATIVA - REDCICLA (TALCA)  ♻️")
    print("=" * 60)
    print("🚀 Servidor iniciado con éxito.")
    print("🌐 Abriendo automáticamente el panel en tu navegador...")
    print("=" * 60)
    
    Timer(1.5, abrir_navegador).start()
    app.run(debug=True, port=8000, use_reloader=False)