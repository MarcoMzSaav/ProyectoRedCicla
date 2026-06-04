package com.example.appredcicla;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appredcicla.database.ConexionSQLite;
import com.example.appredcicla.network.SyncManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistrarRetiroActivity extends AppCompatActivity {

    private TextView txtNombreRuta, txtPatenteCamion, txtStatusAntes, txtStatusDespues;
    private Spinner spinnerPuntos;
    private EditText campoKgVidrio;
    private Button botonGuardar, botonSincronizar, btnFotoAntes, btnFotoDespues;
    private ImageButton btnReportarProblema;
    
    private ConexionSQLite dbHelper;
    private SyncManager syncManager;
    private int rutaActivaId = -1;
    private String pathAntes = "sin_foto", pathDespues = "sin_foto";

    private final List<String> listaDirecciones = new ArrayList<>();
    private final List<Integer> listaIdsPuntos = new ArrayList<>();
    private final List<Double> listaCapacidades = new ArrayList<>();

    private final ActivityResultLauncher<Intent> pickAntesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        pathAntes = uri.toString();
                        txtStatusAntes.setText("¡Foto Cargada!");
                        txtStatusAntes.setTextColor(0xFF346739);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickDespuesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        pathDespues = uri.toString();
                        txtStatusDespues.setText("¡Foto Cargada!");
                        txtStatusDespues.setTextColor(0xFF346739);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_retiro);

        dbHelper = new ConexionSQLite(this);
        syncManager = new SyncManager(this);

        // Vincular vistas
        txtNombreRuta = findViewById(R.id.txtNombreRuta);
        txtPatenteCamion = findViewById(R.id.txtPatenteCamion);
        spinnerPuntos = findViewById(R.id.spinnerPuntos);
        campoKgVidrio = findViewById(R.id.inputKgVidrio);
        botonGuardar = findViewById(R.id.btnGuardarRetiro);
        botonSincronizar = findViewById(R.id.btnSincronizar);
        btnReportarProblema = findViewById(R.id.btnReportarProblema);
        btnFotoAntes = findViewById(R.id.btnFotoAntes);
        btnFotoDespues = findViewById(R.id.btnFotoDespues);
        txtStatusAntes = findViewById(R.id.txtStatusAntes);
        txtStatusDespues = findViewById(R.id.txtStatusDespues);

        // Cargar datos iniciales
        int usuarioId = getSharedPreferences("Sesion", MODE_PRIVATE).getInt("usuario_id", -1);
        if (usuarioId != -1) {
            cargarDatosRuta(usuarioId);
        }

        // Listeners
        btnFotoAntes.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickAntesLauncher.launch(intent);
        });

        btnFotoDespues.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickDespuesLauncher.launch(intent);
        });

        btnReportarProblema.setOnClickListener(v -> {
            if (spinnerPuntos.getSelectedItem() == null || listaIdsPuntos.isEmpty()) {
                Toast.makeText(this, "Selecciona un punto primero", Toast.LENGTH_SHORT).show();
                return;
            }
            int pos = spinnerPuntos.getSelectedItemPosition();
            if (pos >= 0 && pos < listaIdsPuntos.size()) {
                int puntoId = listaIdsPuntos.get(pos);
                syncManager.reportarProblema(puntoId, new SyncManager.RouteCallback() {
                    @Override
                    public void onSuccess(JsonObject data) {
                        Toast.makeText(RegistrarRetiroActivity.this, "¡Problema Reportado!", Toast.LENGTH_SHORT).show();
                        cargarDatosRuta(usuarioId);
                    }
                    @Override
                    public void onError(String mensaje) {
                        Toast.makeText(RegistrarRetiroActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        botonGuardar.setOnClickListener(v -> {
            if (spinnerPuntos.getSelectedItem() == null || rutaActivaId == -1) {
                Toast.makeText(this, "Error: Datos de ruta no cargados", Toast.LENGTH_SHORT).show();
                return;
            }

            String kgStr = campoKgVidrio.getText().toString().trim();
            if (kgStr.isEmpty()) {
                Toast.makeText(this, "Ingresa los kg", Toast.LENGTH_SHORT).show();
                return;
            }

            int pos = spinnerPuntos.getSelectedItemPosition();
            int puntoId = listaIdsPuntos.get(pos);
            float kgVidrio = Float.parseFloat(kgStr);
            String fecha = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

            if (dbHelper.guardarPesajeOffline(rutaActivaId, puntoId, kgVidrio, fecha, pathAntes, pathDespues)) {
                Toast.makeText(this, "¡Guardado!", Toast.LENGTH_SHORT).show();
                resetForm();
            }
        });

        botonSincronizar.setOnClickListener(v -> syncManager.sincronizarDatos());
        
        Button btnIrMapa = findViewById(R.id.btnIrMapa);
        if (btnIrMapa != null) {
            btnIrMapa.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, Class.forName("com.example.appredcicla.MapaActivity"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Mapa no disponible", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                txtNombreRuta.setText("Sin ruta activa asignada");
                listaDirecciones.clear();
                spinnerPuntos.setAdapter(null);
            }
        });
    }

    private void resetForm() {
        campoKgVidrio.setText("");
        pathAntes = "sin_foto";
        pathDespues = "sin_foto";
        txtStatusAntes.setText("Sin foto");
        txtStatusAntes.setTextColor(0xFF999999);
        txtStatusDespues.setText("Sin foto");
        txtStatusDespues.setTextColor(0xFF999999);
    }
}
