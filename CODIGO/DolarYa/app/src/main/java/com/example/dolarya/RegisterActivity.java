package com.example.dolarya;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dolarya.API_SOA.ApiClient;
import com.example.dolarya.API_SOA.SessionManager;
import com.example.dolarya.API_SOA.SoaRequest;
import com.example.dolarya.API_SOA.SoaResponse;
import com.example.dolarya.API_SOA.SoaService;
import com.example.dolarya.Firebase.MyFirebase;
import com.example.dolarya.Firebase.User;
import com.google.android.gms.common.api.Api;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import retrofit2.Callback;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {
    private EditText txtNombre, txtApellido, txtDni, txtEmail, txtPassword, txtComision;
    private Button btnRegistrarse;
    private SessionManager sessionManager;
    private ApiClient apiClient;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private User user;

@SuppressLint("SourceLockedOrientationActivity")
@Override
protected void onCreate(Bundle savedInstanceState){
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    getSupportActionBar().hide();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    //Manejador de sesión
    sessionManager = new SessionManager(getApplicationContext());

    //Instancia y referencia de la BD en Firebase
    firebaseDatabase = MyFirebase.getInstance();
    reference = firebaseDatabase.getReference("/users/");

    //Componentes de la vista
    txtNombre = findViewById(R.id.txtNombre);
    txtApellido = findViewById(R.id.txtApellido);
    txtDni = findViewById(R.id.txtDni);
    txtEmail = findViewById(R.id.txtEmail);
    txtPassword = findViewById(R.id.txtPassword);
    txtComision = findViewById(R.id.txtComision);
    btnRegistrarse = findViewById(R.id.btnRegistrarse);

    //Listener
    btnRegistrarse.setOnClickListener(botonesListener);
}

    private View.OnClickListener botonesListener = new View.OnClickListener() {
        public void onClick(View v) {
            //Verificamos conexión a internet y validamos campos del login
            if (!hayConexion()) {
                Toast.makeText(RegisterActivity.this, String.format("No hay conexión a Internet."), Toast.LENGTH_LONG).show();
                return;
            }
            if (!validarCampos())
                return;

            apiClient = ApiClient.getInstance();
            SoaRequest request = new SoaRequest();
            request.setEnv("PROD");
            request.setName(txtNombre.getText().toString());
            request.setLastname(txtApellido.getText().toString());
            request.setDni(Long.parseLong(txtDni.getText().toString()));
            request.setEmail(txtEmail.getText().toString());
            request.setPassword(txtPassword.getText().toString());
            request.setCommission(Integer.parseInt(txtComision.getText().toString()));

            apiClient.registrarUsuario(request, new Callback<SoaResponse>() {
                @Override
                public void onResponse(Call<SoaResponse> call, Response<SoaResponse> response) {
                    if (response.isSuccessful()) {
                        //Registramos al usuario en Firebase
                        user = new User();
                        user.setEmail(request.getEmail());
                        user.setCuentaPesos(0);
                        user.setCuentaDolares(0);
                        agregarUsuarioFirebase(user);

                        sessionManager.guardarEmail(request.getEmail());
                        //Se guardan tokens
                        sessionManager.guardarToken(response.body().getToken());
                        sessionManager.guardarTokenRefresh(response.body().getToken_refresh());
                        //Vamos al main de la app
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Datos incorrectos en la registración", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SoaResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Falló la registración", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private boolean hayConexion() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected())
            return true;

        return false;
    }

    private boolean validarCampos() {
        if(txtNombre.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Debe ingresar un nombre", Toast.LENGTH_LONG).show();
            return false;
        }
        if(txtApellido.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Debe ingresar un apellido", Toast.LENGTH_LONG).show();
            return false;
        }
        if(txtDni.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Debe ingresar un DNI", Toast.LENGTH_LONG).show();
            return false;
        }
        if(txtEmail.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Debe ingresar un email", Toast.LENGTH_LONG).show();
            return false;
        }
        if(txtPassword.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Debe ingresar una contraseña", Toast.LENGTH_LONG).show();
            return false;
        }
        if(txtPassword.getText().toString().length() < 8) {
            Toast.makeText(RegisterActivity.this, "La contraseña debe contener al menos 8 caracteres", Toast.LENGTH_LONG).show();
            return false;
        }
        if(txtComision.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Debe ingresar un numero de comisión", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void agregarUsuarioFirebase(User user) {
        reference.push().setValue(user);
    }
}
