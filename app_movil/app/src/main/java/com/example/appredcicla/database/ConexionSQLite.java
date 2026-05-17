package com.example.appredcicla.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class ConexionSQLite extends SQLiteOpenHelper {

    // 1. Definimos el nombre y la versión de la base de datos local (.db)
    private static final String DATABASE_NAME = "redcicla_local.db";
    private static final int DATABASE_VERSION = 1;

    // 2. Constructor obligatorio para enlazar la BD con el sistema Android
    public ConexionSQLite(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 3. Aquí adentro se ejecutan los comandos SQL nativos al instalar la app por primera vez
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Tabla de Camiones (Para control de flotas en terreno)
        String tableCamion = "CREATE TABLE camiones (" +
                "patente TEXT PRIMARY KEY, " +
                "capacidad_carga REAL, " +
                "estado INTEGER, " + // 1 para disponible, 0 para mantenimiento
                "alerta INTEGER" +
                ");";

        // Tabla de Registros de Retiro (¡La más importante para el pesaje offline en rutas rurales!)
        String tableRegistroRetiro = "CREATE TABLE registros_retiro (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "punto_reciclaje_id INTEGER, " +
                "cantidad_retirada REAL, " + // Aquí el operador guarda los kg de vidrio del visor
                "fecha_hora TEXT, " +
                "ruta_img_antes TEXT, " +
                "ruta_img_despues TEXT, " +
                "sincronizado INTEGER DEFAULT 0" + // 0 = Solo en celular, 1 = Ya se subió a la web en Talca
                ");";

        // Ejecutamos las sentencias SQL en el teléfono celular
        db.execSQL(tableCamion);
        db.execSQL(tableRegistroRetiro);
    }

    // Se ejecuta de manera automática si en el futuro cambian la estructura (versión) de las tablas
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS camiones");
        db.execSQL("DROP TABLE IF EXISTS registros_retiro");
        onCreate(db);
    }

    // =========================================================================
    // MÉTODOS AYUDANTES (Muestra de cómo usar la BD local desde Java)
    // =========================================================================

    /**
     * Guarda un pesaje de vidrio de forma 100% offline en el almacenamiento interno del smartphone.
     */
    public boolean guardarPesajeOffline(int puntoId, float kgVidrio, String fecha, String imgAntes, String imgDespues) {
        SQLiteDatabase db = this.getWritableDatabase(); // Abrimos la BD en modo escritura
        ContentValues valores = new ContentValues();

        // Mapeamos los datos a sus respectivas columnas SQL
        valores.put("punto_reciclaje_id", puntoId);
        valores.put("cantidad_retirada", kgVidrio);
        valores.put("fecha_hora", fecha);
        valores.put("ruta_img_antes", imgAntes);
        valores.put("ruta_img_despues", imgDespues);
        valores.put("sincronizado", 0); // Al guardarse en ruta rural, viaja por defecto como NO sincronizado

        long resultado = db.insert("registros_retiro", null, valores);
        db.close(); // Cerramos la conexión para no saturar memoria

        return resultado != -1; // Retorna true si se guardó correctamente en la memoria
    }

    /**
     * Consulta y extrae todos los registros acumulados que aún NO se han subido al servidor web.
     */
    public Cursor obtenerRegistrosPendientesSincronizar() {
        SQLiteDatabase db = this.getReadableDatabase(); // Modo lectura
        // SQL Nativo puro para buscar las que tienen estado 0
        return db.rawQuery("SELECT * FROM registros_retiro WHERE sincronizado = 0", null);
    }
}