package com.example.appredcicla.network;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.appredcicla.database.ConexionSQLite;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SyncManager {

    private static final String TAG = "SyncManager";
    // IP 10.0.2.2 es para el emulador Android apuntando al localhost de la PC
    private static final String API_URL = "http://10.0.2.2:8000/api/sincronizar"; 
    
    private final Context context;
    private final ConexionSQLite dbHelper;
    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;

    public SyncManager(Context context) {
        this.context = context;
        this.dbHelper = new ConexionSQLite(context);
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void sincronizarDatos() {
        executor.execute(() -> {
            Cursor cursor = dbHelper.obtenerRegistrosPendientesSincronizar();
            if (cursor == null || cursor.getCount() == 0) {
                mostrarToast("No hay datos pendientes para sincronizar");
                if (cursor != null) cursor.close();
                return;
            }

            JsonArray jsonArray = new JsonArray();
            while (cursor.moveToNext()) {
                JsonObject obj = new JsonObject();
                // Ojo: los nombres de llaves deben coincidir con lo que espera Flask en main.py
                obj.addProperty("punto_reciclaje_id", cursor.getInt(cursor.getColumnIndexOrThrow("punto_reciclaje_id")));
                obj.addProperty("cantidad_conductor", cursor.getFloat(cursor.getColumnIndexOrThrow("cantidad_retirada")));
                obj.addProperty("fecha_hora", cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora")));
                jsonArray.add(obj);
            }
            cursor.close();

            String jsonPayload = gson.toJson(jsonArray);
            RequestBody body = RequestBody.create(
                    jsonPayload,
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    // Si el servidor respondió OK, marcamos como sincronizados en la BD local
                    marcarComoSincronizados();
                    mostrarToast("Sincronización exitosa con Talca");
                } else {
                    mostrarToast("Error de servidor: " + response.code());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error de red", e);
                mostrarToast("Error de conexión. Revisa si el servidor Flask está encendido.");
            }
        });
    }

    private void marcarComoSincronizados() {
        // Actualizamos todos los registros que estaban en 0 a 1
        dbHelper.getWritableDatabase().execSQL("UPDATE registros_retiro SET sincronizado = 1 WHERE sincronizado = 0");
    }

    private void mostrarToast(String mensaje) {
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        );
    }
}
