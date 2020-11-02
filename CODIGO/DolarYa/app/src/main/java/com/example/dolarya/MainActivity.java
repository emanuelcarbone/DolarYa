package com.example.dolarya;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import com.example.dolarya.API_SOA.SessionManager;
import com.example.dolarya.Firebase.MyFirebase;
import com.example.dolarya.Firebase.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Button btnComprar, btnDepositarRetirar, btnAgregarCuenta, btnMisSensores;
    private Context context;
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
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        //Manejador de sesión
        sessionManager = new SessionManager(context);
        //Comprobamos el estado de la batería
        verificarEstadoBateria();
        //Instancia y referencia de la BD en Firebase
        firebaseDatabase = MyFirebase.getInstance();
        reference = firebaseDatabase.getReference("/users");
        reference.keepSynced(true);
        //Listener que obtiene y detecta cambios en el usuario de Firebase
        reference.addValueEventListener(postListener);

        //Componentes de la vista
        btnComprar = findViewById(R.id.btnComprar);
        btnDepositarRetirar = findViewById(R.id.btnDepositarRetirar);
        btnAgregarCuenta = findViewById(R.id.btnAgregarCuenta);
        btnMisSensores = findViewById(R.id.btnIngresar);

        //Listeners
        btnComprar.setOnClickListener(botonesListener);
        btnDepositarRetirar.setOnClickListener(botonesListener);
        btnAgregarCuenta.setOnClickListener(botonesListener);
        btnMisSensores.setOnClickListener(botonesListener);
    }

    private void verificarEstadoBateria(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent estadoBateria = context.registerReceiver(null, intentFilter);
        int nivel = estadoBateria.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int escala = estadoBateria.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int porcentaje = nivel * 100 / escala;
        String leyendaBateria;
        if(porcentaje >= 80){
            leyendaBateria = "% - Batería alta";
        }
        else if(porcentaje <= 20){
            leyendaBateria = "% - Bateria baja";
        }
        else{
            leyendaBateria = "% - Batería normal";
        }
        Toast.makeText(this, porcentaje + leyendaBateria, Toast.LENGTH_SHORT).show();
    }

    private View.OnClickListener botonesListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.btnComprar:
                    intent = new Intent(MainActivity.this, CompraActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
                case R.id.btnDepositarRetirar:
                    intent = new Intent(MainActivity.this, DepositarRetirarActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
                case R.id.btnAgregarCuenta:
                    intent = new Intent(MainActivity.this, AgregarCuentaActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    break;
                case R.id.btnIngresar:
                    intent = new Intent(MainActivity.this, MisSensoresActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            //Obtenemos el email con el que ingresó el usuario
            String emailLogin = sessionManager.getEmail();
            String emailFirebase;

            //Buscamos al usuario en Firebase
            for(DataSnapshot item: dataSnapshot.getChildren()) {
                emailFirebase = item.child("email").getValue().toString();
                if(emailFirebase.equals(emailLogin)){
                   user = item.getValue(User.class);
                   user.setId(item.getKey());
                   return;
                }
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {}
    };
}
