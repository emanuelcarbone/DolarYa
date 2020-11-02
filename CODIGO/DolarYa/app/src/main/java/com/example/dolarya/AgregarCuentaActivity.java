package com.example.dolarya;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dolarya.Firebase.MyFirebase;
import com.example.dolarya.Firebase.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class AgregarCuentaActivity extends AppCompatActivity {
    private RadioGroup radGrpTipoCuenta;
    private EditText txtAlias;
    private Button btnAgregarCuenta;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private User user;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_cuenta);

        //Instancia y referencia de la BD en Firebase
        firebaseDatabase = MyFirebase.getInstance();
        reference = firebaseDatabase.getReference("/users");
        reference.keepSynced(true);
        //Obtenemos el usuario
        user = (User) getIntent().getSerializableExtra("user");

        //Componentes de la vista
        radGrpTipoCuenta = findViewById(R.id.radGrpTipoCuenta);
        txtAlias = findViewById(R.id.txtAlias);
        btnAgregarCuenta = findViewById(R.id.btnAgregarCuenta);

        //Listener
        btnAgregarCuenta.setOnClickListener(botonesListener);
    }

    private View.OnClickListener botonesListener = new View.OnClickListener() {
        public void onClick(View v) {
            //Validamos campos del activity
            if(!validarCampos())
                return;

            String alias = txtAlias.getText().toString();

            //Verificamos que el alias que se intenta agregar no exista
            if(!user.existeAlias(alias)) {
                String moneda = null;
                //Agregamos el alias en la lista que corresponda
                switch(radGrpTipoCuenta.getCheckedRadioButtonId()){
                    case R.id.radPesos:
                        moneda = "pesos";
                        break;
                    case R.id.radDolares:
                        moneda = "dolares";
                        break;
                }
                user.agregarAlias(alias, moneda);

                //Actualizamos el usuario en Firebase
                actualizarDatosUsuario(user);
                //Vamos a la pantalla de operación exitosa
                Intent intent = new Intent(AgregarCuentaActivity.this, OperacionExitosaActivity.class);
                intent.putExtra("textResultado", "Se agregó correctamente la cuenta en " + moneda + ": " + alias);
                startActivity(intent);
                finish();
            }
            else{
                Toast.makeText(getApplicationContext(),"Ups! Parece que esa cuenta ya está agregada", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private boolean validarCampos() {
        if (txtAlias.getText().toString().equals("")) {
            Toast.makeText(AgregarCuentaActivity.this, "Debe ingresar un alias", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void actualizarDatosUsuario(User user) {
        reference.child(user.getId()).setValue(user);
    }
}
