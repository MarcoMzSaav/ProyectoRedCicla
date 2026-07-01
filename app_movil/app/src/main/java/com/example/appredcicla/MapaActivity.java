package com.example.appredcicla;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.appredcicla.network.SyncManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SyncManager syncManager;
    private List<LatLng> listaPuntosRuta = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        syncManager = new SyncManager(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.btnIrRetiro).setOnClickListener(v -> finish());
        
        Button btnOptimizar = findViewById(R.id.btnOptimizarRuta);
        if (btnOptimizar != null) {
            btnOptimizar.setOnClickListener(v -> optimizarRuta());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        cargarPuntosDeServidor();
    }

    private void cargarPuntosDeServidor() {
        int usuarioId = getSharedPreferences("Sesion", MODE_PRIVATE).getInt("usuario_id", -1);
        if (usuarioId == -1) return;

        syncManager.obtenerRutaActiva(usuarioId, new SyncManager.RouteCallback() {
            @Override
            public void onSuccess(JsonObject data) {
                if (data.has("puntos")) {
                    JsonArray puntos = data.getAsJsonArray("puntos");
                    listaPuntosRuta.clear();
                    mMap.clear();

                    if (puntos.size() == 0) {
                        Toast.makeText(MapaActivity.this, "No hay puntos en esta ruta", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    boolean hayPuntosValidos = false;

                    for (JsonElement p : puntos) {
                        JsonObject obj = p.getAsJsonObject();
                        if (obj.has("latitud") && !obj.get("latitud").isJsonNull() && 
                            obj.has("longitud") && !obj.get("longitud").isJsonNull()) {
                            
                            double lat = obj.get("latitud").getAsDouble();
                            double lng = obj.get("longitud").getAsDouble();
                            LatLng pos = new LatLng(lat, lng);
                            listaPuntosRuta.add(pos);

                            mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(obj.get("direccion").getAsString())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            
                            builder.include(pos);
                            hayPuntosValidos = true;
                        }
                    }

                    if (hayPuntosValidos) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                    }
                }
            }

            @Override
            public void onError(String mensaje) {
                Toast.makeText(MapaActivity.this, "Error: " + mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void optimizarRuta() {
        if (listaPuntosRuta.size() < 2) {
            Toast.makeText(this, "Se necesitan al menos 2 puntos", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        url.append("origin=").append(listaPuntosRuta.get(0).latitude).append(",").append(listaPuntosRuta.get(0).longitude);
        url.append("&destination=").append(listaPuntosRuta.get(listaPuntosRuta.size()-1).latitude).append(",").append(listaPuntosRuta.get(listaPuntosRuta.size()-1).longitude);
        
        if (listaPuntosRuta.size() > 2) {
            url.append("&waypoints=optimize:true");
            for (int i = 1; i < listaPuntosRuta.size() - 1; i++) {
                url.append("|").append(listaPuntosRuta.get(i).latitude).append(",").append(listaPuntosRuta.get(i).longitude);
            }
        }
        
        url.append("&key=").append(getString(R.string.google_maps_key));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url.toString()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MapaActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
                    
                    if (jsonObject.get("status").getAsString().equals("OK")) {
                        JsonArray routes = jsonObject.getAsJsonArray("routes");
                        String polyline = routes.get(0).getAsJsonObject()
                                .get("overview_polyline").getAsJsonObject()
                                .get("points").getAsString();
                        
                        List<LatLng> decodedPath = decodePolyline(polyline);
                        
                        runOnUiThread(() -> {
                            mMap.addPolyline(new PolylineOptions()
                                    .addAll(decodedPath)
                                    .width(12)
                                    .color(Color.BLUE));
                            Toast.makeText(MapaActivity.this, "Ruta optimizada trazada", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
