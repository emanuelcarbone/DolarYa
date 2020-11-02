package com.example.dolarya;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dolarya.API_SOA.SessionManager;
import com.example.dolarya.API_SOA.SoaEventoRequest;
import com.example.dolarya.API_SOA.SoaEventoResponse;
import com.example.dolarya.API_SOA.SoaService;
import com.example.dolarya.Firebase.MyFirebase;
import com.example.dolarya.Firebase.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DepositarRetirarActivity extends AppCompatActivity {
    private TextView textSaldoPesos, textSaldoDolares;
    private RadioGroup radGrpTipoMovimiento;
    private Spinner listaCuentas;
    private ArrayAdapter<String> adapterPesos, adapterDolares;
    private EditText txtImporte;
    private Button btnTransferir;
    private SessionManager sessionManager;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private User user;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_depositar_retirar);

        //Manejador de sesion
        sessionManager = new SessionManager(getApplicationContext());

        //Instancia y referencia de la BD en Firebase
        firebaseDatabase = MyFirebase.getInstance();
        reference = firebaseDatabase.getReference("/users");
        //Obtenemos el usuario
        user = (User) getIntent().getSerializableExtra("user");

        //Componentes de la vista
        textSaldoPesos = findViewById(R.id.textSaldoPesos);
        textSaldoDolares = findViewById(R.id.textSaldoDolares);
        radGrpTipoMovimiento = findViewById(R.id.radGrpTipoMovimiento);
        listaCuentas = findViewById(R.id.listaCuentas);
        txtImporte = findViewById(R.id.txtImporte);
        btnTransferir = findViewById(R.id.btnTransferir);

        //Mostramos los saldos en pesos y en dólares del usuario
        mostrarSaldos();
        //Cargamos los alias del usuario en los adapter del spinner (por default, se muestran primeros los alias en pesos)
        configurarAdaptersListaCuentas();

        //Listeners
        radGrpTipoMovimiento.setOnCheckedChangeListener(radioGroupListener);
        btnTransferir.setOnClickListener(botonListener);
    }

    public void mostrarSaldos(){
        textSaldoPesos.setText("$ " + user.getCuentaPesos());
        textSaldoDolares.setText("U$S " + user.getCuentaDolares());
    }

    public void configurarAdaptersListaCuentas(){
        adapterPesos = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                user.getAliasPesos());
        adapterPesos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterDolares = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                user.getAliasDolares());
        adapterDolares.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cargarAdapterPesos();
    }

    public void cargarAdapterPesos(){
        listaCuentas.setAdapter(adapterPesos);
    }

    public void cargarAdapterDolares(){
        listaCuentas.setAdapter(adapterDolares);
    }

    private RadioGroup.OnCheckedChangeListener radioGroupListener = new RadioGroup.OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup radGrp, int radId) {
            //Cargamos la lista de alias que correspondan
            switch(radId) {
                case R.id.radDepositar:
                    cargarAdapterPesos();
                    break;
                case R.id.radRetirar:
                    cargarAdapterDolares();
                    break;
            }
        }
    };

    private View.OnClickListener botonListener = new View.OnClickListener() {
        public void onClick(View v) {
            //Validamos campos del activity
            if(!validarCampos())
                return;

            Intent intent = new Intent(DepositarRetirarActivity.this, OperacionExitosaActivity.class);
            //Obtenemos el importe que ingresó el usuario
            Double importe = Double.parseDouble(txtImporte.getText().toString());

            //Realizamos el movimiento que corresponda
            switch (radGrpTipoMovimiento.getCheckedRadioButtonId()) {
                case R.id.radDepositar:
                    user.acreditarPesos(importe);
                    //Actualizamos el usuario en Firebase
                    actualizarDatosUsuario(user);
                    //Vamos a la pantalla de operación exitosa
                    intent.putExtra("textResultado", "Se recibieron correctamente $" + importe.toString() + " desde la cuenta indicada");
                    startActivity(intent);
                    finish();
                    break;
                case R.id.radRetirar:
                    //Verificamos que el usuario tenga los dólares suficientes para la transferencia
                    if(user.hasDolaresSuficientes(importe)){
                        user.debitarDolares(importe);
                        //Actualizamos el usuario en Firebase
                        actualizarDatosUsuario(user);
                        //Vamos a la pantalla de operación exitosa
                        intent.putExtra("textResultado", "Se retiraron correctamente U$S" + importe.toString() + " a la cuenta indicada");
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Dólares insuficientes para la transacción", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private boolean validarCampos() {
        if (txtImporte.getText().toString().equals("") || txtImporte.getText().toString().equals(".")) {
            Toast.makeText(DepositarRetirarActivity.this, "Debe ingresar un importe", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Double.parseDouble(txtImporte.getText().toString()) == 0) {
            Toast.makeText(DepositarRetirarActivity.this, "Debe ingresar un importe mayor a cero", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void actualizarDatosUsuario(User user) {
        reference.child(user.getId()).setValue(user);
    }
}
