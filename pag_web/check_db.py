import sqlite3
import os

DB_PATH = os.path.join(os.path.dirname(__file__), 'redcicla_central.db')

def check():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute("SELECT id, ruta_img_antes, ruta_img_despues FROM registros_retiro WHERE ruta_img_antes IS NOT NULL AND ruta_img_antes != ''")
    rows = cursor.fetchall()
    print(f"Found {len(rows)} records with images")
    for row in rows:
        print(row)
    conn.close()

if __name__ == "__main__":
    check()
