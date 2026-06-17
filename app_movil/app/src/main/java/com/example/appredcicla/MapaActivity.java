package com.example.appredcicla;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MapaActivity extends AppCompatActivity {

    private WebView webViewMapa;
    private Button btnIrRetiro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        webViewMapa = findViewById(R.id.webViewMapa);
        btnIrRetiro = findViewById(R.id.btnIrRetiro);

        WebSettings webSettings = webViewMapa.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webViewMapa.setWebViewClient(new WebViewClient());

        webViewMapa.loadUrl("https://redcicla.onrender.com");

        btnIrRetiro.setOnClickListener(v -> finish());
    }
}