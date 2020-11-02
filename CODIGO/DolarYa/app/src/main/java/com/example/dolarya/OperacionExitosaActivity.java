package com.example.dolarya;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class OperacionExitosaActivity extends AppCompatActivity {

    private Button btnHome;
    private TextView textResultado;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operacion_exitosa);

        btnHome = findViewById(R.id.btnHome);
        textResultado = findViewById(R.id.textResultado);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        textResultado.setText(extras.getString("textResultado"));
        btnHome.setOnClickListener(botonesListener);
    }

    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty
    private View.OnClickListener botonesListener = new View.OnClickListener() {

        public void onClick(View v) {
            Intent intent;

            //Se determina qué botón generó un evento
            switch (v.getId()) {
                case R.id.btnHome:
                    finish();
                    break;
                default:
                    Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_LONG).show();
            }
        }
    };
}
