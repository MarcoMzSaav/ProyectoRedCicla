package com.example.appredcicla.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConexionSQLite extends SQLiteOpenHelper {

    // 1. Definimos el nombre y la versión de la base de datos local (.db)
    // Versión 4: Añadimos latitud/longitud a puntos_reciclaje para caché de ruta
    private static final String DATABASE_NAME = "redcicla_local.db";
    private static final int DATABASE_VERSION = 4;

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

        // Tabla de Puntos de Reciclaje (Copia local de la central para caché offline)
        String tablePunto = "CREATE TABLE puntos_reciclaje (" +
                "id INTEGER PRIMARY KEY, " +
                "direccion TEXT, " +
                "capacidad REAL, " +
                "latitud REAL, " +
                "longitud REAL, " +
                "estado INTEGER" +
                ");";

        // Tabla de Registros de Retiro (Sincronizada con la estructura de la Web)
        String tableRegistroRetiro = "CREATE TABLE registros_retiro (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ruta_activa_id INTEGER, " +
                "punto_id INTEGER, " +
                "fecha_hora TEXT NOT NULL, " +
                "cantidad_retirada REAL, " + 
                "ruta_img_antes TEXT, " +
                "ruta_img_despues TEXT, " +
                "estado TEXT DEFAULT 'Pendiente', " +
                "sincronizado INTEGER DEFAULT 0" + // 0 = Pendiente, 1 = Sincronizado
                ");";

        // Ejecutamos las sentencias SQL en el teléfono celular
        db.execSQL(tableCamion);
        db.execSQL(tablePunto);
        db.execSQL(tableRegistroRetiro);
    }

    // Se ejecuta si subimos la versión de la base de datos
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS camiones");
        db.execSQL("DROP TABLE IF EXISTS puntos_reciclaje");
        db.execSQL("DROP TABLE IF EXISTS registros_retiro");
        onCreate(db);
    }

    /**
     * Guarda un pesaje de vidrio de forma offline. 
     * Si ya existe un registro no sincronizado para ese punto en esa ruta, suma la cantidad.
     */
    public boolean guardarPesajeOffline(int rutaActivaId, int puntoId, float kgVidrio, String fecha, String imgAntes, String imgDespues) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // 1. Buscamos si ya existe un registro pendiente para este punto en esta ruta
        Cursor cursor = db.rawQuery("SELECT id, cantidad_retirada FROM registros_retiro WHERE ruta_activa_id = ? AND punto_id = ? AND sincronizado = 0", 
                new String[]{String.valueOf(rutaActivaId), String.valueOf(puntoId)});

        boolean exito;
        ContentValues valores = new ContentValues();

        if (cursor.moveToFirst()) {
            // 2. Si existe, sumamos la cantidad y actualizamos las fotos si se enviaron nuevas
            int idExistente = cursor.getInt(0);
            float cantidadExistente = cursor.getFloat(1);
            float nuevaCantidad = cantidadExistente + kgVidrio;

            valores.put("cantidad_retirada", nuevaCantidad);
            valores.put("fecha_hora", fecha); // Actualizamos a la última hora de pesaje
            if (imgAntes != null) valores.put("ruta_img_antes", imgAntes);
            if (imgDespues != null) valores.put("ruta_img_despues", imgDespues);

            int filasAfectadas = db.update("registros_retiro", valores, "id = ?", new String[]{String.valueOf(idExistente)});
            exito = filasAfectadas > 0;
        } else {
            // 3. Si no existe, creamos un registro nuevo
            valores.put("ruta_activa_id", rutaActivaId);
            valores.put("punto_id", puntoId);
            valores.put("cantidad_retirada", kgVidrio);
            valores.put("fecha_hora", fecha);
            valores.put("ruta_img_antes", imgAntes);
            valores.put("ruta_img_despues", imgDespues);
            valores.put("estado", "Pendiente");
            valores.put("sincronizado", 0);

            long resultado = db.insert("registros_retiro", null, valores);
            exito = resultado != -1;
        }
        
        cursor.close();
        db.close();
        return exito;
    }

    /**
     * Guarda la ruta activa y sus puntos en la caché local para acceso offline.
     */
    public void guardarRutaLocal(int rutaActivaId, String nombreRuta, String patente, JsonArray puntos) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Limpiar caché anterior de puntos
        db.execSQL("DELETE FROM puntos_reciclaje");

        // 2. Insertar los nuevos puntos
        for (JsonElement element : puntos) {
            JsonObject obj = element.getAsJsonObject();
            ContentValues v = new ContentValues();
            v.put("id", obj.get("id").getAsInt());
            v.put("direccion", obj.get("direccion").getAsString());
            v.put("capacidad", obj.get("capacidad").getAsFloat());
            v.put("latitud", obj.has("latitud") && !obj.get("latitud").isJsonNull() ? obj.get("latitud").getAsDouble() : 0.0);
            v.put("longitud", obj.has("longitud") && !obj.get("longitud").isJsonNull() ? obj.get("longitud").getAsDouble() : 0.0);
            v.put("estado", 1);
            db.insert("puntos_reciclaje", null, v);
        }
        db.close();
    }

    /**
     * Obtiene los puntos guardados en la caché local.
     */
    public Cursor obtenerPuntosLocales() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM puntos_reciclaje", null);
    }

    /**
     * Consulta registros pendientes de subir a la nube.
     */
    public Cursor obtenerRegistrosPendientesSincronizar() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM registros_retiro WHERE sincronizado = 0", null);
    }
}
