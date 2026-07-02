package com.example.appredcicla;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.appredcicla.network.SyncManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
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
import okhttp3.ResponseBody;

public class MapaActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final int PERMISO_UBICACION = 1;

    private GoogleMap mMap;
    private SyncManager syncManager;
    private FusedLocationProviderClient fusedLocationClient;

    private final List<LatLng> listaPuntosRuta = new ArrayList<>();

    private LatLng ubicacionActual;
    private Polyline rutaDibujada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        syncManager = new SyncManager(this);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.btnIrRetiro)
                .setOnClickListener(v -> finish());

        Button btnOptimizar = findViewById(R.id.btnOptimizarRuta);

        if (btnOptimizar != null) {
            btnOptimizar.setOnClickListener(v -> optimizarRuta());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Posición inicial mientras cargan los datos.
        LatLng talca = new LatLng(-35.4264, -71.6554);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(talca, 13f)
        );

        cargarPuntosDeServidor();

        if (tienePermisoUbicacion()) {
            obtenerUbicacionActual();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISO_UBICACION
            );
        }
    }

    private boolean tienePermisoUbicacion() {
        boolean permisoPreciso =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean permisoAproximado =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        return permisoPreciso || permisoAproximado;
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacionActual() {
        if (!tienePermisoUbicacion() || mMap == null) {
            return;
        }

        // Muestra el punto azul de la ubicación del dispositivo.
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        CancellationTokenSource tokenSource =
                new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                tokenSource.getToken()
        ).addOnSuccessListener(location -> {

            if (location != null) {
                ubicacionActual = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );

                ajustarCamara();
            } else {
                Toast.makeText(
                        MapaActivity.this,
                        "No se pudo obtener la ubicación. Revisa que el GPS esté activado.",
                        Toast.LENGTH_LONG
                ).show();
            }

        }).addOnFailureListener(error ->
                Toast.makeText(
                        MapaActivity.this,
                        "Error al obtener la ubicación actual",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void cargarPuntosDeServidor() {
        int usuarioId = getSharedPreferences(
                "Sesion",
                MODE_PRIVATE
        ).getInt("usuario_id", -1);

        if (usuarioId == -1) {
            Toast.makeText(
                    this,
                    "No se encontró la sesión del usuario",
                    Toast.LENGTH_SHORT
            ).show();

            ajustarCamara();
            return;
        }

        syncManager.obtenerRutaActiva(
                usuarioId,
                new SyncManager.RouteCallback() {

                    @Override
                    public void onSuccess(JsonObject data) {
                        if (mMap == null) {
                            return;
                        }

                        listaPuntosRuta.clear();
                        mMap.clear();

                        if (!data.has("puntos")
                                || !data.get("puntos").isJsonArray()) {

                            Toast.makeText(
                                    MapaActivity.this,
                                    "La ruta no contiene puntos",
                                    Toast.LENGTH_SHORT
                            ).show();

                            ajustarCamara();
                            return;
                        }

                        JsonArray puntos =
                                data.getAsJsonArray("puntos");

                        if (puntos.size() == 0) {
                            Toast.makeText(
                                    MapaActivity.this,
                                    "No hay puntos en esta ruta",
                                    Toast.LENGTH_SHORT
                            ).show();

                            ajustarCamara();
                            return;
                        }

                        boolean hayPuntosValidos = false;

                        for (JsonElement elemento : puntos) {
                            if (!elemento.isJsonObject()) {
                                continue;
                            }

                            JsonObject punto =
                                    elemento.getAsJsonObject();

                            boolean tieneLatitud =
                                    punto.has("latitud")
                                            && !punto.get("latitud")
                                            .isJsonNull();

                            boolean tieneLongitud =
                                    punto.has("longitud")
                                            && !punto.get("longitud")
                                            .isJsonNull();

                            if (!tieneLatitud || !tieneLongitud) {
                                continue;
                            }

                            double latitud =
                                    punto.get("latitud").getAsDouble();

                            double longitud =
                                    punto.get("longitud").getAsDouble();

                            String direccion = punto.has("direccion")
                                    && !punto.get("direccion").isJsonNull()
                                    ? punto.get("direccion").getAsString()
                                    : "Punto de reciclaje";

                            LatLng posicion =
                                    new LatLng(latitud, longitud);

                            listaPuntosRuta.add(posicion);

                            mMap.addMarker(
                                    new MarkerOptions()
                                            .position(posicion)
                                            .title(direccion)
                                            .icon(
                                                    BitmapDescriptorFactory
                                                            .defaultMarker(
                                                                    BitmapDescriptorFactory.HUE_GREEN
                                                            )
                                            )
                            );

                            hayPuntosValidos = true;
                        }

                        if (!hayPuntosValidos) {
                            Toast.makeText(
                                    MapaActivity.this,
                                    "Los puntos no tienen coordenadas válidas",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                        ajustarCamara();
                    }

                    @Override
                    public void onError(String mensaje) {
                        Toast.makeText(
                                MapaActivity.this,
                                "Error: " + mensaje,
                                Toast.LENGTH_SHORT
                        ).show();

                        ajustarCamara();
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == PERMISO_UBICACION) {
            if (tienePermisoUbicacion()) {
                obtenerUbicacionActual();
            } else {
                Toast.makeText(
                        this,
                        "Debes permitir la ubicación para utilizar tu posición real",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void ajustarCamara() {
        if (mMap == null) {
            return;
        }

        LatLngBounds.Builder builder =
                new LatLngBounds.Builder();

        int cantidadUbicaciones = 0;

        if (ubicacionActual != null) {
            builder.include(ubicacionActual);
            cantidadUbicaciones++;
        }

        for (LatLng punto : listaPuntosRuta) {
            builder.include(punto);
            cantidadUbicaciones++;
        }

        if (cantidadUbicaciones == 0) {
            LatLng talca =
                    new LatLng(-35.4264, -71.6554);

            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            talca,
                            13f
                    )
            );

            return;
        }

        if (cantidadUbicaciones == 1) {
            LatLng unicaUbicacion;

            if (ubicacionActual != null) {
                unicaUbicacion = ubicacionActual;
            } else {
                unicaUbicacion = listaPuntosRuta.get(0);
            }

            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            unicaUbicacion,
                            15f
                    )
            );

            return;
        }

        LatLngBounds limites = builder.build();

        findViewById(R.id.map).post(() -> {
            if (mMap != null) {
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                                limites,
                                120
                        )
                );
            }
        });
    }

    private void optimizarRuta() {
        if (ubicacionActual == null) {
            Toast.makeText(
                    this,
                    "Esperando la ubicación actual del celular",
                    Toast.LENGTH_SHORT
            ).show();

            obtenerUbicacionActual();
            return;
        }

        if (listaPuntosRuta.isEmpty()) {
            Toast.makeText(
                    this,
                    "No hay puntos disponibles para crear la ruta",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        LatLng destino =
                listaPuntosRuta.get(listaPuntosRuta.size() - 1);

        StringBuilder url = new StringBuilder(
                "https://maps.googleapis.com/maps/api/directions/json?"
        );

        // La ruta comienza desde la ubicación real del celular.
        url.append("origin=")
                .append(ubicacionActual.latitude)
                .append(",")
                .append(ubicacionActual.longitude);

        url.append("&destination=")
                .append(destino.latitude)
                .append(",")
                .append(destino.longitude);

        // Los demás puntos se utilizan como paradas intermedias.
        if (listaPuntosRuta.size() > 1) {
            url.append("&waypoints=optimize:true");

            for (int i = 0;
                 i < listaPuntosRuta.size() - 1;
                 i++) {

                LatLng punto = listaPuntosRuta.get(i);

                url.append("|")
                        .append(punto.latitude)
                        .append(",")
                        .append(punto.longitude);
            }
        }

        url.append("&mode=driving");

        url.append("&key=")
                .append(getString(R.string.google_maps_key));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url.toString())
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(
                    @NonNull Call call,
                    @NonNull IOException e
            ) {
                runOnUiThread(() ->
                        Toast.makeText(
                                MapaActivity.this,
                                "Error de red al calcular la ruta",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }

            @Override
            public void onResponse(
                    @NonNull Call call,
                    @NonNull Response response
            ) throws IOException {

                try (Response respuesta = response) {
                    ResponseBody body = respuesta.body();

                    if (!respuesta.isSuccessful() || body == null) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        MapaActivity.this,
                                        "Error del servidor de Google: "
                                                + respuesta.code(),
                                        Toast.LENGTH_SHORT
                                ).show()
                        );

                        return;
                    }

                    String jsonData = body.string();

                    JsonObject jsonObject =
                            JsonParser.parseString(jsonData)
                                    .getAsJsonObject();

                    String estado = jsonObject.has("status")
                            ? jsonObject.get("status").getAsString()
                            : "ERROR";

                    if (!estado.equals("OK")) {
                        String mensajeError =
                                jsonObject.has("error_message")
                                        ? jsonObject
                                        .get("error_message")
                                        .getAsString()
                                        : "Google Maps respondió: "
                                        + estado;

                        runOnUiThread(() ->
                                Toast.makeText(
                                        MapaActivity.this,
                                        mensajeError,
                                        Toast.LENGTH_LONG
                                ).show()
                        );

                        return;
                    }

                    JsonArray rutas =
                            jsonObject.getAsJsonArray("routes");

                    if (rutas == null || rutas.size() == 0) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        MapaActivity.this,
                                        "No se encontró una ruta disponible",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );

                        return;
                    }

                    String polylineCodificada =
                            rutas.get(0)
                                    .getAsJsonObject()
                                    .getAsJsonObject(
                                            "overview_polyline"
                                    )
                                    .get("points")
                                    .getAsString();

                    List<LatLng> camino =
                            decodePolyline(polylineCodificada);

                    runOnUiThread(() -> {
                        if (rutaDibujada != null) {
                            rutaDibujada.remove();
                        }

                        rutaDibujada = mMap.addPolyline(
                                new PolylineOptions()
                                        .addAll(camino)
                                        .width(12)
                                        .color(Color.BLUE)
                        );

                        Toast.makeText(
                                MapaActivity.this,
                                "Ruta optimizada trazada",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
                }
            }
        });
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> puntos = new ArrayList<>();

        int index = 0;
        int longitudTexto = encoded.length();

        int latitud = 0;
        int longitud = 0;

        while (index < longitudTexto) {
            int b;
            int shift = 0;
            int resultado = 0;

            do {
                b = encoded.charAt(index++) - 63;
                resultado |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20 && index < longitudTexto);

            int diferenciaLatitud =
                    ((resultado & 1) != 0)
                            ? ~(resultado >> 1)
                            : resultado >> 1;

            latitud += diferenciaLatitud;

            shift = 0;
            resultado = 0;

            do {
                b = encoded.charAt(index++) - 63;
                resultado |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20 && index < longitudTexto);

            int diferenciaLongitud =
                    ((resultado & 1) != 0)
                            ? ~(resultado >> 1)
                            : resultado >> 1;

            longitud += diferenciaLongitud;

            LatLng punto = new LatLng(
                    latitud / 1E5,
                    longitud / 1E5
            );

            puntos.add(punto);
        }

        return puntos;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Actualiza la ubicación cuando el usuario vuelve al mapa.
        if (mMap != null && tienePermisoUbicacion()) {
            obtenerUbicacionActual();
        }
    }
}