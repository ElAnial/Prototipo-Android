package com.example.sqlite;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Pantalla de portada (Splash). Muestra una imagen unos instantes antes de
 * iniciar la aplicación y luego continúa hacia LoginActivity (que a su vez
 * redirige al Dashboard si ya existe una sesión activa).
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable irAlLogin = () -> {
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.postDelayed(irAlLogin, SPLASH_DELAY_MS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(irAlLogin);
    }
}
