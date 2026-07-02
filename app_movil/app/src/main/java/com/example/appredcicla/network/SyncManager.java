package com.example.appredcicla.network;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.appredcicla.database.ConexionSQLite;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private static final String BASE_URL = "https://redcicla.onrender.com";
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
            try {
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
                    ResponseBody responseBody = response.body();
                    String responseData = (responseBody != null) ? responseBody.string() : "{}";
                    JsonObject responseJson = gson.fromJson(responseData, JsonObject.class);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.isSuccessful() && responseJson != null) {
                            int id = responseJson.get("id").getAsInt();
                            String nombre = responseJson.get("nombre").getAsString();
                            String rol = responseJson.get("rol").getAsString();
                            callback.onSuccess(id, nombre, rol);
                        } else {
                            String error = (responseJson != null && responseJson.has("message")) ? 
                                    responseJson.get("message").getAsString() : "Error en login (" + response.code() + ")";
                            callback.onError(error);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Login error", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Error de conexión"));
            }
        });
    }

    public void sincronizarDatos() {
        executor.execute(() -> {
            try {
                Cursor cursor = dbHelper.obtenerRegistrosPendientesSincronizar();
                if (cursor == null || cursor.getCount() == 0) {
                    mostrarToast("No hay datos pendientes para sincronizar");
                    if (cursor != null) cursor.close();
                    return;
                }

                JsonArray jsonArray = new JsonArray();
                while (cursor.moveToNext()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("ruta_activa_id", cursor.getInt(cursor.getColumnIndexOrThrow("ruta_activa_id")));
                    obj.addProperty("punto_id", cursor.getInt(cursor.getColumnIndexOrThrow("punto_id")));
                    obj.addProperty("cantidad_retirada", cursor.getFloat(cursor.getColumnIndexOrThrow("cantidad_retirada")));
                    obj.addProperty("fecha_hora", cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora")));

                    String uriAntes = cursor.getString(cursor.getColumnIndexOrThrow("ruta_img_antes"));
                    String uriDespues = cursor.getString(cursor.getColumnIndexOrThrow("ruta_img_despues"));

                    obj.addProperty("ruta_img_antes", convertImageToBase64(uriAntes));
                    obj.addProperty("ruta_img_despues", convertImageToBase64(uriDespues));

                    jsonArray.add(obj);
                }
                cursor.close();

                RequestBody body = RequestBody.create(
                        gson.toJson(jsonArray),
                        MediaType.parse("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        marcarComoSincronizados();
                        mostrarToast("Sincronización exitosa");
                    } else {
                        mostrarToast("Error de servidor: " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Sync error", e);
                mostrarToast("Error de conexión durante sincronización");
            }
        });
    }

    public void obtenerRutaActiva(int usuarioId, RouteCallback callback) {
        executor.execute(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL + "/api/ruta_activa/" + usuarioId)
                        .get()
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    ResponseBody responseBody = response.body();
                    String responseData = (responseBody != null) ? responseBody.string() : "{}";
                    JsonObject json = gson.fromJson(responseData, JsonObject.class);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.isSuccessful() && json != null) {
                            callback.onSuccess(json);
                        } else {
                            String error = (json != null && json.has("message")) ? 
                                    json.get("message").getAsString() : "Error al obtener ruta";
                            callback.onError(error);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Route fetch error", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Error de conexión"));
            }
        });
    }

    public void reportarProblema(int puntoId, RouteCallback callback) {
        executor.execute(() -> {
            try {
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("punto_id", puntoId);

                RequestBody body = RequestBody.create(
                        gson.toJson(jsonBody),
                        MediaType.parse("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url(BASE_URL + "/api/puntos/reportar_problema")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    ResponseBody responseBody = response.body();
                    String responseData = (responseBody != null) ? responseBody.string() : "{}";
                    JsonObject responseJson = gson.fromJson(responseData, JsonObject.class);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.isSuccessful() && responseJson != null) {
                            callback.onSuccess(responseJson);
                        } else {
                            String error = (responseJson != null && responseJson.has("message")) ? 
                                    responseJson.get("message").getAsString() : "Error al reportar";
                            callback.onError(error);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Report error", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Error de conexión"));
            }
        });
    }

    private void marcarComoSincronizados() {
        // 1. Obtener las rutas de los archivos antes de marcarlos como sincronizados
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT ruta_img_antes, ruta_img_despues FROM registros_retiro WHERE sincronizado = 0", null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String pathAntes = cursor.getString(0);
                String pathDespues = cursor.getString(1);

                // 2. Eliminar físicamente los archivos internos
                eliminarArchivoFisico(pathAntes);
                eliminarArchivoFisico(pathDespues);
            }
            cursor.close();
        }

        // 3. Marcar en la base de datos
        dbHelper.getWritableDatabase().execSQL("UPDATE registros_retiro SET sincronizado = 1 WHERE sincronizado = 0");
    }

    private void eliminarArchivoFisico(String path) {
        if (path != null && !path.equals("sin_foto") && !path.startsWith("content://")) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    if (file.delete()) {
                        Log.d(TAG, "Archivo eliminado: " + path);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error eliminando archivo: " + path, e);
            }
        }
    }

    private String convertImageToBase64(String uriString) {
        if (uriString == null || uriString.isEmpty() || uriString.equals("sin_foto")) {
            return "";
        }
        try {
            InputStream inputStream;
            if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                inputStream = context.getContentResolver().openInputStream(Uri.parse(uriString));
            } else {
                // Es una ruta física interna (/data/user/0/...)
                File file = new File(uriString);
                if (!file.exists()) {
                    Log.e(TAG, "Archivo no encontrado en ruta: " + uriString);
                    return "";
                }
                inputStream = new FileInputStream(file);
            }

            if (inputStream == null) return "";

            // 1. Decodificar a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) return "";

            // 2. Comprimir la imagen (Calidad 60% para que sea ligera en Render)
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, output);
            byte[] bytes = output.toByteArray();

            Log.d(TAG, "Imagen convertida. Tamaño Base64: " + bytes.length + " bytes");

            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Error converting image to Base64", e);
            return "";
        }
    }

    private void mostrarToast(String mensaje) {
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        );
    }
}
