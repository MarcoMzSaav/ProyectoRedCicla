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

// Importamos tu conector de base de datos relacional
import com.example.appredcicla.database.ConexionSQLite;

public class RegistrarRetiroActivity extends AppCompatActivity {

    private EditText campoPuntoId;
    private EditText campoKgVidrio;
    private Button botonGuardar;
    private ConexionSQLite dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Enlazamos con el XML del formulario de retiro
        setContentView(R.layout.activity_registrar_retiro);

        // 2. Inicializamos el motor SQLite local
        dbHelper = new ConexionSQLite(this);

        // 3. Conectamos los inputs y el botón del formulario
        campoPuntoId = findViewById(R.id.inputPuntoId);
        campoKgVidrio = findViewById(R.id.inputKgVidrio);
        botonGuardar = findViewById(R.id.btnGuardarRetiro);

        // 4. Programamos la acción de guardar en terreno (Offline)
        botonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String puntoIdStr = campoPuntoId.getText().toString().trim();
                String kgVidrioStr = campoKgVidrio.getText().toString().trim();

                // Validamos que el conductor no envíe el formulario vacío
                if (puntoIdStr.isEmpty() || kgVidrioStr.isEmpty()) {
                    Toast.makeText(RegistrarRetiroActivity.this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Convertimos los textos a los tipos de datos correctos (int y float)
                    int puntoId = Integer.parseInt(puntoIdStr);
                    float kgVidrio = Float.parseFloat(kgVidrioStr);

                    // Capturamos la fecha y hora del celular de forma automática
                    String fechaActual = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                    // Guardamos el pesaje real en la base de datos interna del smartphone
                    boolean exito = dbHelper.guardarPesajeOffline(
                            puntoId,
                            kgVidrio,
                            fechaActual,
                            "foto_antes_id" + puntoId + ".jpg",   // Simulamos los nombres de las fotos requeridas
                            "foto_despues_id" + puntoId + ".jpg"
                    );

                    if (exito) {
                        Toast.makeText(RegistrarRetiroActivity.this, "¡Retiro Guardado Localmente (Offline)!", Toast.LENGTH_LONG).show();

                        // Limpiamos el formulario para que el chofer quede listo para el siguiente punto de Talca
                        campoPuntoId.setText("");
                        campoKgVidrio.setText("");
                    } else {
                        Toast.makeText(RegistrarRetiroActivity.this, "Error al escribir en la base de datos.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}