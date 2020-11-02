package com.example.dolarya;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dolarya.API_SOA.ApiClient;
import com.example.dolarya.API_SOA.SessionManager;
import com.example.dolarya.API_SOA.SoaEventoRequest;
import com.example.dolarya.API_SOA.SoaEventoResponse;
import com.example.dolarya.API_SOA.SoaRequest;
import com.example.dolarya.API_SOA.SoaResponse;
import com.example.dolarya.API_SOA.SoaService;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private EditText txtEmail, txtPasswordLogin;
    private Button btnIngresar, btnRegistrarse;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Manejador de sesión
        sessionManager = new SessionManager(getApplicationContext());

        //Componentes de la vista
        txtEmail = findViewById(R.id.txtEmail);
        txtPasswordLogin  = findViewById(R.id.txtPasswordLogin);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnIngresar = findViewById(R.id.btnIngresar);

        //Listeners
        btnIngresar.setOnClickListener(botonIngresarListener);
        btnRegistrarse.setOnClickListener(botonRegistrarseListener);
    }

    private View.OnClickListener botonIngresarListener = new View.OnClickListener() {
        public void onClick(View v) {
            //Verificamos conexión a internet y validamos campos del login
            if (!hayConexion()) {
                Toast.makeText(LoginActivity.this, "No hay conexión a internet", Toast.LENGTH_LONG).show();
                return;
            }
            if (!validarCampos())
                return;

            apiClient = ApiClient.getInstance();
            SoaRequest request = new SoaRequest();
            request.setEmail(txtEmail.getText().toString());
            request.setPassword(txtPasswordLogin.getText().toString());

            apiClient.loginUsuario(request, new Callback<SoaResponse>() {
                @Override
                public void onResponse(Call<SoaResponse> call, Response<SoaResponse> response) {
                    if (response.isSuccessful()) {
                        sessionManager.guardarEmail(request.getEmail());
                        //Se guardan tokens
                        sessionManager.guardarToken(response.body().getToken());
                        sessionManager.guardarTokenRefresh(response.body().getToken_refresh());
                        //Se registra el login
                        registrarEventoLogin();
                        //Vamos al main de la app
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "No se encontró al usuario", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<SoaResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Falló el login", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private View.OnClickListener botonRegistrarseListener = new View.OnClickListener() {
        public void onClick(View v) {
            //Vamos a la pantalla de registración
            Intent registroActivityIntent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(registroActivityIntent);
            finish();
        }
    };

    private void registrarEventoLogin() {
        apiClient = ApiClient.getInstance();
        SoaEventoRequest request = new SoaEventoRequest();
        request.setEnv("PROD");
        request.setType_events("login");
        request.setDescription("Se realizó un login correctamente");

        apiClient.registrarEvento(sessionManager.getToken(), request, new Callback<SoaEventoResponse>() {
            @Override
            public void onResponse(Call<SoaEventoResponse> call, Response<SoaEventoResponse> response) {
                if(!response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Error en los datos del login", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<SoaEventoResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Falló el registro del login", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hayConexion() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean validarCampos() {
        if (txtEmail.getText().toString().equals("")) {
            Toast.makeText(this, "Tenés que ingresar un email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtPasswordLogin.getText().toString().equals("")) {
            Toast.makeText(this, "Tenés que ingresar una contraseña", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
