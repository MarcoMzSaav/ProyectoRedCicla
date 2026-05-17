package com.example.appredcicla;

import android.content.Intent; // 🚀 Importación necesaria para cambiar de pantalla
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText campoCorreo;
    private EditText campoClave;
    private Button botonIngresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Conectamos los componentes del XML con Java
        campoCorreo = findViewById(R.id.inputCorreo);
        campoClave = findViewById(R.id.inputClave);
        botonIngresar = findViewById(R.id.btnIniciarSesion);

        // Acción del botón
        botonIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = campoCorreo.getText().toString();
                String clave = campoClave.getText().toString();

                if (correo.isEmpty() || clave.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, completa los campos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Ingresando a RedCicla...", Toast.LENGTH_SHORT).show();

                    // 🚀 Código para saltar de la pantalla de Login a la de Registro de Retiro
                    Intent intento = new Intent(MainActivity.this, RegistrarRetiroActivity.class);
                    startActivity(intento);
                }
            }
        });
    }
}