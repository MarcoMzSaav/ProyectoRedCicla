package com.example.appredcicla;

import android.content.Intent; // 🚀 Importación necesaria para cambiar de pantalla
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appredcicla.network.SyncManager;

public class MainActivity extends AppCompatActivity {

    private EditText campoCorreo;
    private EditText campoClave;
    private Button botonIngresar;
    private SyncManager syncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        syncManager = new SyncManager(this);

        // Conectamos los componentes del XML con Java
        campoCorreo = findViewById(R.id.inputCorreo);
        campoClave = findViewById(R.id.inputClave);
        botonIngresar = findViewById(R.id.btnIniciarSesion);

        // Acción del botón
        botonIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = campoCorreo.getText().toString().trim();
                String clave = campoClave.getText().toString().trim();

                if (correo.isEmpty() || clave.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, completa los campos", Toast.LENGTH_SHORT).show();
                } else {
                    botonIngresar.setEnabled(false);
                    botonIngresar.setText("Verificando...");

                    syncManager.login(correo, clave, new SyncManager.LoginCallback() {
                        @Override
                        public void onSuccess(int id, String nombre, String rol) {
                            // Guardamos el ID, nombre y rol para usarlo en la siguiente actividad
                            getSharedPreferences("Sesion", MODE_PRIVATE).edit()
                                    .putInt("usuario_id", id)
                                    .putString("usuario_nombre", nombre)
                                    .putString("usuario_rol", rol)
                                    .apply();
                            
                            Toast.makeText(MainActivity.this, "Bienvenido " + nombre, Toast.LENGTH_SHORT).show();
                            
                            Intent intento = new Intent(MainActivity.this, RegistrarRetiroActivity.class);
                            startActivity(intento);
                            finish();
                        }

                        @Override
                        public void onError(String mensaje) {
                            botonIngresar.setEnabled(true);
                            botonIngresar.setText("INICIAR SESIÓN");
                            Toast.makeText(MainActivity.this, "Error: " + mensaje, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}