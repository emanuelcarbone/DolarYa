package com.example.dolarya;

import android.content.Intent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.widget.EditText;
import android.widget.TextView;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.widget.Toast;
import com.example.dolarya.API_SOA.ApiClient;
import com.example.dolarya.API_SOA.SessionManager;
import com.example.dolarya.API_SOA.SoaEventoRequest;
import com.example.dolarya.API_SOA.SoaEventoResponse;
import com.example.dolarya.API_SOA.SoaService;
import com.example.dolarya.Firebase.Dolar;
import com.example.dolarya.Firebase.MyFirebase;
import com.example.dolarya.Firebase.User;
import com.google.android.gms.common.api.Api;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class CompraActivity extends AppCompatActivity implements SensorEventListener {
    private TextView textPrecioDolar, textSaldoPesos, textPesosAInvertir;
    private EditText txtMontoDolares;
    private Button btnCalcularMonto, btnComprar;
    private static Context context;
    private SessionManager sessionManager;
    private SensorManager mSensorManager;
    private ShakeDetector mShakeDetector;
    private ApiClient apiClient;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference referenceUsuarios, referenceDolar;
    private User user;
    private Dolar dolar;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compra);

        context = getApplicationContext();
        //Manejador de sesión
        sessionManager = new SessionManager(context);

        //Instancia y referencia de la BD en Firebase
        firebaseDatabase = MyFirebase.getInstance();
        referenceUsuarios = firebaseDatabase.getReference("/users");
        referenceDolar = firebaseDatabase.getReference("/dolar");
        referenceDolar.keepSynced(true);

        //Obtenemos el usuario
        user = (User) getIntent().getSerializableExtra("user");
        //Listener que obtiene y detecta cambios del valor del dólar de Firebase
        referenceDolar.addValueEventListener(postListener);

        //Iniciamos servicio que detecta el shake
        Intent intent = new Intent(this, ShakeService.class);
        startService(intent);
        //Configuramos el detector de shake
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(shakeListener);

        //Obtenemos el manejador de sensores e inicializamos sensores
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        inicializarSensores();

        //Componentes de la vista
        textPrecioDolar = findViewById(R.id.textPrecioDolar);
        textSaldoPesos = findViewById(R.id.textSaldoPesos);
        textPesosAInvertir = findViewById(R.id.textPesosAInvertir);
        txtMontoDolares = findViewById(R.id.txtMontoDolares);
        btnCalcularMonto = findViewById(R.id.btnCalcularMonto);
        btnComprar = findViewById(R.id.btnComprar);

        //Mostramos el valor del dólar y el saldo en pesos del usuario
        textSaldoPesos.setText("$ " + user.getCuentaPesos());

        //Listeners
        btnCalcularMonto.setOnClickListener(botonesListener);
        btnComprar.setOnClickListener(botonesListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inicializarSensores();
    }

    @Override
    public void onPause() {
        pararSensores();
        super.onPause();
    }

    @Override
    protected void onStop() {
        pararSensores();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        inicializarSensores();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        pararSensores();
        super.onDestroy();
    }

    protected void inicializarSensores() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mShakeDetector, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    private void pararSensores() {
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
        mSensorManager.unregisterListener(mShakeDetector);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String texto;
        //Sincronizamos los sensores y guardamos los datos según corresponda
        synchronized(this) {
            switch(event.sensor.getType()) {
                case Sensor.TYPE_GYROSCOPE:
                    texto = "\nX: " + event.values[0] + " deg/s\n";
                    texto += "Y: " + event.values[1] + " deg/s\n";
                    texto += "Z: " + event.values[2] + " deg/s\n";
                    Log.i("sensorGiroscopio", texto);
                    guardarPreferenciasGiroscopio(event.values[0], event.values[1], event.values[2]);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD :
                    texto = event.values[0] + " uT" + "\n";
                    Log.i("sensorCampoMagnetico", texto);
                    guardarPreferenciasCampoMagnetico(event.values[0]);
                    break;
                case Sensor.TYPE_LIGHT :
                    texto = event.values[0] + " Lux \n";
                    Log.i("sensorLuz", texto);
                    guardarPreferenciasLuz(event.values[0]);
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private ShakeDetector.OnShakeListener shakeListener = new ShakeDetector.OnShakeListener() {
        @Override
        public void onShake(int count) {
            //Cuando detectamos el shake, guardamos el resultado de la fuerza g asociada y registramos el evento
            guardarPreferenciasAcelerometro(count);
            registrarEventoAcelerometro();
        }
    };

    public void guardarPreferenciasAcelerometro(float gForce) {
        SharedPreferences preferences = context.getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("sensorAcelerometro", gForce);
        editor.apply();
    }

    private void guardarPreferenciasGiroscopio(float x, float y, float z) {
        SharedPreferences preferences = context.getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("sensorGiroscopioX", x);
        editor.putFloat("sensorGiroscopioY", y);
        editor.putFloat("sensorGiroscopioZ", z);
        editor.apply();
    }

    private void guardarPreferenciasCampoMagnetico(float uT) {
        SharedPreferences preferences = context.getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("sensorCampoMagnetico", uT);
        editor.apply();
    }

    private void guardarPreferenciasLuz(float lx) {
        SharedPreferences preferences = context.getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("sensorLuz", lx);
        editor.apply();
    }

    private View.OnClickListener botonesListener = new View.OnClickListener() {
        public void onClick(View v) {
            //Realizamos las acciones del botón correspondiente, previamente validando los datos
            switch (v.getId()) {
                case R.id.btnCalcularMonto:
                    if(!validarImporteDolares())
                        return;

                    Double montoPesos = calcularMontoPesos(Double.parseDouble(txtMontoDolares.getText().toString()));
                    textPesosAInvertir.setText("$ " + Double.toString(montoPesos));
                    break;
                case R.id.btnComprar:
                    if(!validarPesosAInvertir())
                        return;

                    Double importePesos = Double.parseDouble(textPesosAInvertir.getText().toString().substring(1));
                    Double importeDolares = Double.parseDouble(txtMontoDolares.getText().toString());
                    //Verificamos que el usuario tenga los pesos suficientes para la compra
                    if(user.hasPesosSuficientes(importePesos)){
                        user.debitarPesos(importePesos);
                        user.acreditarDolares(importeDolares);
                        //Actualizamos el usuario en Firebase
                        actualizarDatosUsuario(user);
                        registrarCompraDolares();
                        //Vamos a la pantalla de operación exitosa
                        Intent intent = new Intent(CompraActivity.this, OperacionExitosaActivity.class);
                        intent.putExtra("textResultado", "Se acreditaron correctamente U$S" + txtMontoDolares.getText().toString());
                        startActivity(intent);
                        finish();
                    }
                    else
                        Toast.makeText(CompraActivity.this, "No tenés pesos suficientes para la compra", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private Double calcularMontoPesos(Double montoDolares){
        return montoDolares * dolar.getPrecio();
    }

    public void registrarEventoAcelerometro() {
        apiClient = ApiClient.getInstance();
        //Regeneramos el token si corresponde
        try {
            if(!sessionManager.isTokenVigente()){
                sessionManager.regenerarToken();
            }
        } catch(Exception e) {
            sessionManager.regenerarToken();
        }
        SoaEventoRequest request = new SoaEventoRequest();
        request.setEnv("PROD");
        request.setType_events("acelerometro");
        request.setDescription("shake");

        apiClient.registrarEvento(sessionManager.getToken(), request, new Callback<SoaEventoResponse>() {
            @Override
            public void onResponse(Call<SoaEventoResponse> call, Response<SoaEventoResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CompraActivity.this, "Se registró un shake", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(CompraActivity.this, "Error en los datos del shake", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<SoaEventoResponse> call, Throwable t) {
                Toast.makeText(CompraActivity.this, "Falló el registro del shake", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void registrarCompraDolares() {
        apiClient = ApiClient.getInstance();
        //Regeneramos el token si corresponde
        try {
            if(!sessionManager.isTokenVigente()){
                sessionManager.regenerarToken();
            }
        } catch(Exception e) {
            sessionManager.regenerarToken();
        }
        SoaEventoRequest request = new SoaEventoRequest();
        request.setEnv("PROD");
        request.setType_events("intercambio de divisas");
        request.setDescription("compra de dolares");

        apiClient.registrarEvento(sessionManager.getToken(), request, new Callback<SoaEventoResponse>() {
            @Override
            public void onResponse(Call<SoaEventoResponse> call, Response<SoaEventoResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(CompraActivity.this, "Error en los datos de la compra de dólares", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<SoaEventoResponse> call, Throwable t) {
                Toast.makeText(CompraActivity.this, "Falló el registro de la compra de dólares", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            //Obtenemos el valor del dólar desde Firebase
            dolar = dataSnapshot.getValue(Dolar.class);
            textPrecioDolar.setText("$" + dolar.getPrecio());
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {}
    };

    private void actualizarDatosUsuario(User user) {
        referenceUsuarios.child(user.getId()).setValue(user);
    }

    private boolean validarImporteDolares() {
        if (txtMontoDolares.getText().toString().equals("") || txtMontoDolares.getText().toString().equals(".")) {
            Toast.makeText(this, "Debés ingresar un monto de dólares", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Double.parseDouble(txtMontoDolares.getText().toString()) == 0) {
            Toast.makeText(this, "Debés ingresar un monto de dólares mayor a cero", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validarPesosAInvertir() {
        if (textPesosAInvertir.getText().toString().equals("$ ...")) {
            Toast.makeText(this, "Debés verificar la cantidad de pesos a invertir", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
