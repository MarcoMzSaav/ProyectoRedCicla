package com.example.appredcicla;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Importamos tu conector de base de datos relacional
import com.example.appredcicla.database.ConexionSQLite;
import com.example.appredcicla.network.SyncManager;

public class RegistrarRetiroActivity extends AppCompatActivity {

    private TextView txtNombreRuta, txtPatenteCamion;
    private Spinner spinnerPuntos;
    private EditText campoKgVidrio;
    private Button botonGuardar, botonSincronizar;
    private ConexionSQLite dbHelper;
    private SyncManager syncManager;
    private int rutaActivaId = -1; // Guardamos el ID de la ruta activa
    private List<String> listaDirecciones = new ArrayList<>();
    private List<Integer> listaIdsPuntos = new ArrayList<>();
    private List<Double> listaCapacidades = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_retiro);

        dbHelper = new ConexionSQLite(this);
        syncManager = new SyncManager(this);

        txtNombreRuta = findViewById(R.id.txtNombreRuta);
        txtPatenteCamion = findViewById(R.id.txtPatenteCamion);
        spinnerPuntos = findViewById(R.id.spinnerPuntos);
        campoKgVidrio = findViewById(R.id.inputKgVidrio);
        botonGuardar = findViewById(R.id.btnGuardarRetiro);
        botonSincronizar = findViewById(R.id.btnSincronizar);

        // 1. Cargar Ruta Activa
        int usuarioId = getSharedPreferences("Sesion", MODE_PRIVATE).getInt("usuario_id", -1);
        if (usuarioId != -1) {
            cargarDatosRuta(usuarioId);
        } else {
            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
        }

        botonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerPuntos.getSelectedItem() == null || rutaActivaId == -1) {
                    Toast.makeText(RegistrarRetiroActivity.this, "Error: Datos de ruta no cargados", Toast.LENGTH_SHORT).show();
                    return;
                }

                String kgVidrioStr = campoKgVidrio.getText().toString().trim();
                if (kgVidrioStr.isEmpty()) {
                    Toast.makeText(RegistrarRetiroActivity.this, "Ingresa los kg", Toast.LENGTH_SHORT).show();
                } else {
                    int pos = spinnerPuntos.getSelectedItemPosition();
                    int puntoId = listaIdsPuntos.get(pos);
                    double capacidadMax = listaCapacidades.get(pos);
                    float kgVidrio = Float.parseFloat(kgVidrioStr);

                    // Validación simple de capacidad
                    if (kgVidrio > capacidadMax) {
                        Toast.makeText(RegistrarRetiroActivity.this, "¡Aviso! El pesaje supera la capacidad del punto (" + capacidadMax + " kg)", Toast.LENGTH_LONG).show();
                    }

                    String fechaActual = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                    // Guardamos con la nueva estructura que incluye rutaActivaId
                    boolean exito = dbHelper.guardarPesajeOffline(
                            rutaActivaId, puntoId, kgVidrio, fechaActual, "img_antes.jpg", "img_despues.jpg"
                    );

                    if (exito) {
                        Toast.makeText(RegistrarRetiroActivity.this, "¡Guardado Localmente!", Toast.LENGTH_LONG).show();
                        campoKgVidrio.setText("");
                    }
                }
            }
        });

        botonSincronizar.setOnClickListener(v -> syncManager.sincronizarDatos());
    }

    private void cargarDatosRuta(int usuarioId) {
        syncManager.obtenerRutaActiva(usuarioId, new SyncManager.RouteCallback() {
            @Override
            public void onSuccess(JsonObject data) {
                rutaActivaId = data.get("ruta_activa_id").getAsInt();
                txtNombreRuta.setText("Ruta: " + data.get("nombre_ruta").getAsString());
                txtPatenteCamion.setText("Camión: " + data.get("patente_camion").getAsString());

                JsonArray puntos = data.getAsJsonArray("puntos");
                listaDirecciones.clear();
                listaIdsPuntos.clear();
                listaCapacidades.clear();
                for (JsonElement p : puntos) {
                    JsonObject obj = p.getAsJsonObject();
                    listaDirecciones.add(obj.get("direccion").getAsString() + " (" + obj.get("capacidad").getAsFloat() + " kg)");
                    listaIdsPuntos.add(obj.get("id").getAsInt());
                    listaCapacidades.add(obj.get("capacidad").getAsDouble());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(RegistrarRetiroActivity.this,
                        android.R.layout.simple_spinner_item, listaDirecciones);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPuntos.setAdapter(adapter);
            }

            @Override
            public void onError(String mensaje) {
                txtNombreRuta.setText("Error: " + mensaje);
            }
        });
    }
}