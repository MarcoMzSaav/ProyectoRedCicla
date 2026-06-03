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
    private static final String BASE_URL = "http://10.0.2.2:8000";
    private static final String API_URL = BASE_URL + "/api/sincronizar";
    private static final String LOGIN_URL = BASE_URL + "/api/login";

    private final Context context;
    private final ConexionSQLite dbHelper;
    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;

    public interface LoginCallback {
        void onSuccess(int id, String nombre, String rol);
        void onError(String mensaje);
    }

    public interface RouteCallback {
        void onSuccess(JsonObject data);
        void onError(String mensaje);
    }

    public SyncManager(Context context) {
        this.context = context;
        this.dbHelper = new ConexionSQLite(context);
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void login(String correo, String clave, LoginCallback callback) {
        executor.execute(() -> {
            JsonObject json = new JsonObject();
            json.addProperty("correo", correo);
            json.addProperty("clave", clave);

            RequestBody body = RequestBody.create(
                    gson.toJson(json),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(LOGIN_URL)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseData = response.body().string();
                JsonObject responseJson = gson.fromJson(responseData, JsonObject.class);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (response.isSuccessful()) {
                        int id = responseJson.get("id").getAsInt();
                        String nombre = responseJson.get("nombre").getAsString();
                        String rol = responseJson.get("rol").getAsString();
                        callback.onSuccess(id, nombre, rol);
                    } else {
                        String error = responseJson.has("message") ? 
                                responseJson.get("message").getAsString() : "Error en login";
                        callback.onError(error);
                    }
                });
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> 
                    callback.onError("Error de conexión con el servidor")
                );
            }
        });
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
                // Usamos los nombres de columna actualizados
                obj.addProperty("ruta_activa_id", cursor.getInt(cursor.getColumnIndexOrThrow("ruta_activa_id")));
                obj.addProperty("punto_id", cursor.getInt(cursor.getColumnIndexOrThrow("punto_id")));
                obj.addProperty("cantidad_retirada", cursor.getFloat(cursor.getColumnIndexOrThrow("cantidad_retirada")));
                obj.addProperty("fecha_hora", cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora")));
                obj.addProperty("ruta_img_antes", cursor.getString(cursor.getColumnIndexOrThrow("ruta_img_antes")));
                obj.addProperty("ruta_img_despues", cursor.getString(cursor.getColumnIndexOrThrow("ruta_img_despues")));
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

    public void obtenerRutaActiva(int usuarioId, RouteCallback callback) {
        executor.execute(() -> {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/api/ruta_activa/" + usuarioId)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseData = response.body().string();
                JsonObject json = gson.fromJson(responseData, JsonObject.class);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (response.isSuccessful()) {
                        callback.onSuccess(json);
                    } else {
                        callback.onError(json.get("message").getAsString());
                    }
                });
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Error de conexión"));
            }
        });
    }

    private void marcarComoSincronizados() {
        dbHelper.getWritableDatabase().execSQL("UPDATE registros_retiro SET sincronizado = 1 WHERE sincronizado = 0");
    }

    private void mostrarToast(String mensaje) {
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        );
    }
}
