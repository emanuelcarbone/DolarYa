package com.example.dolarya;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Objects;

public class MisSensoresActivity extends AppCompatActivity {

    Context context;
    TextView textAcelerometro;
    TextView textGiroscopo;
    TextView textCampoMagnetico;
    TextView textLuz;
    DecimalFormat floatFormateado = new DecimalFormat("###.###");

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_sensores);

        context = getApplicationContext();
        textGiroscopo = findViewById(R.id.textGiroscopio);
        textCampoMagnetico = findViewById(R.id.textCampoMagnetico);
        textLuz = findViewById(R.id.textLuz);
        textAcelerometro = findViewById(R.id.textAcelerometro);
        //Cargo los datos de SharedPreferences
        cargarPreferencias();
    }

    private void cargarPreferencias() {
        SharedPreferences preferences = context.getSharedPreferences("datos", Context.MODE_PRIVATE);
        float sensorAcelerometro = preferences.getFloat("sensorAcelerometro", 0);
        float sensorGiroscopioX = preferences.getFloat("sensorGiroscopioX", 0);
        float sensorGiroscopioY = preferences.getFloat("sensorGiroscopioY", 0);
        float sensorGiroscopioZ = preferences.getFloat("sensorGiroscopioZ", 0);
        float sensorCampoMagnetico = preferences.getFloat("sensorCampoMagnetico", 0);
        float sensorLuz = preferences.getFloat("sensorLuz", 0);

        textGiroscopo.setText("X: " + floatFormateado.format(sensorGiroscopioX) + " deg/s\n" +
                "Y: " + floatFormateado.format(sensorGiroscopioY) + " deg/s\n" +
                "Z: " + floatFormateado.format(sensorGiroscopioZ) + " deg/s");
        textCampoMagnetico.setText(floatFormateado.format(sensorCampoMagnetico) + " uT");
        textLuz.setText(sensorLuz + " lux");
        textAcelerometro.setText(sensorAcelerometro + " g");
    }
}
