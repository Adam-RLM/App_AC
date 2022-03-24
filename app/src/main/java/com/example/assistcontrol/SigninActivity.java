package com.example.assistcontrol;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SigninActivity extends AppCompatActivity {

    private RadioButton hombre, mujer;
    private Button registrarse;
    private EditText fecha_nacimiento;
    private EditText pnombre, snombre, papellido, sapellido, email, pass_personal;
    private boolean administrador = false;
    private String s_pnombre, s_snombre = "Sin información", s_papellido, s_sapellido = "Sin información", s_email,
            s_pass_personal, s_hombre, s_mujer, s_fecha_nacimiento = "vacio", UserID, estado = "1", estado2 = "1", estado3 = "0";
    private int dia, mes, ano;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth;
    private static final String TAG = "SigninActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        fAuth = FirebaseAuth.getInstance();
        pnombre = findViewById(R.id.primernombre_signin);
        snombre = findViewById(R.id.segundonombre_signin);
        papellido = findViewById(R.id.primerapellido_signin);
        sapellido = findViewById(R.id.segundoapellido_signin);
        email = findViewById(R.id.identificador_signin);
        pass_personal = findViewById(R.id.pass_identificador_signin);
        hombre = findViewById(R.id.hombre_signin);
        mujer = findViewById(R.id.mujer_signin);
        fecha_nacimiento = findViewById(R.id.fechanacimiento_signin);
        registrarse = findViewById(R.id.registrarse_signin);
        final Intent intent = new Intent(SigninActivity.this, successActivity.class);

        /*Ocultar el teclado al inicio de la app*/
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        /*--------------------------------------------------------------------------------------------*/

        /*Obtener fecha de nacimiento*/
        fecha_nacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendario = Calendar.getInstance();
                ano = calendario.get(Calendar.YEAR);
                mes = calendario.get(Calendar.MONTH);
                dia = calendario.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog fecha = new DatePickerDialog(SigninActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fecha_nacimiento.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        s_fecha_nacimiento = fecha_nacimiento.getText().toString();
                    }
                }, ano, mes, dia);

                calendario.add(Calendar.YEAR, -75);
                fecha.getDatePicker().setMinDate(calendario.getTimeInMillis());
                final Calendar calendario2 = Calendar.getInstance();
                fecha.getDatePicker().setMaxDate(calendario2.getTimeInMillis());
                fecha.show();
            }
        });
        /*---------------------------------------------------------------------------------------------------------------------------*/

        /*----------------MÉTODO REGISTRARSE-----------------*/

        registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean online = internetIsConnected();
                /*Variables Comprobación de internet*/
                ConnectivityManager cm;
                NetworkInfo ni;
                cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                ni = cm.getActiveNetworkInfo();
                boolean tipoConexionWIFI = false;
                /*--------------------------------------------------*/
                if (ni != null) {
                    ConnectivityManager connManager1 = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mWifi = connManager1.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mWifi.isConnected() && online) {
                        tipoConexionWIFI = true;
                        if (tipoConexionWIFI == true) {

                            s_pnombre = pnombre.getText().toString();
                            s_snombre = snombre.getText().toString();
                            s_papellido = papellido.getText().toString();
                            s_sapellido = sapellido.getText().toString();
                            s_email = email.getText().toString();
                            s_pass_personal = pass_personal.getText().toString();
                            s_hombre = hombre.getText().toString();
                            s_mujer = mujer.getText().toString();

                            /*----------Si noCheked hombre----------*/
                            if (s_pnombre.isEmpty()) {
                                Toast.makeText(SigninActivity.this, "Ingrese su Primer Nombre", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (s_papellido.isEmpty()) {
                                Toast.makeText(SigninActivity.this, "Ingrese su Primer Apellido", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (s_fecha_nacimiento == "vacio") {
                                Toast.makeText(SigninActivity.this, "Ingrese su Fecha de Nacimiento", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (s_email.isEmpty()) {
                                Toast.makeText(SigninActivity.this, "Ingrese el Correo Electrónico", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (s_pass_personal.isEmpty()) {
                                Toast.makeText(SigninActivity.this, "Ingrese su Contraseña", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (s_pass_personal.length() < 6) {
                                Toast.makeText(SigninActivity.this, "Ingrese contraseña con al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                            } else if (hombre.isChecked() == false && mujer.isChecked() == false) {
                                Toast.makeText(SigninActivity.this, "Seleccione su Sexo", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (hombre.isChecked()) {

                                final Map<String, Object> trabajador = new HashMap<>();
                                trabajador.put("PrimerNombre", s_pnombre);
                                trabajador.put("SegundoNombre", s_snombre);
                                trabajador.put("PrimerApellido", s_papellido);
                                trabajador.put("SegundoApellido", s_sapellido);
                                trabajador.put("FechaNacimiento", s_fecha_nacimiento);
                                trabajador.put("Sexo", s_hombre);
                                trabajador.put("Email", s_email);
                                trabajador.put("Estado", estado);
                                trabajador.put("Estado2", estado2);
                                trabajador.put("Estado3", estado3);
                                trabajador.put("Administrador", administrador);
                                //trabajador.put("Password", s_pass_personal);

                                //Añadiendo nuevo trabajador a Institución
                                fAuth.createUserWithEmailAndPassword(s_email, s_pass_personal).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            UserID = fAuth.getCurrentUser().getUid();
                                            final DocumentReference d_referencia_usuarios = db.collection("UNAN, León").document(UserID);
                                            d_referencia_usuarios.set(trabajador).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "¡Completo! Perfil creado para: " + UserID);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "Fallo: " + e.toString());
                                                }
                                            });
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(SigninActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else if (mujer.isChecked()) {

                                final Map<String, Object> trabajador = new HashMap<>();
                                trabajador.put("PrimerNombre", s_pnombre);
                                trabajador.put("SegundoNombre", s_snombre);
                                trabajador.put("PrimerApellido", s_papellido);
                                trabajador.put("SegundoApellido", s_sapellido);
                                trabajador.put("FechaNacimiento", s_fecha_nacimiento);
                                trabajador.put("Sexo", s_mujer);
                                trabajador.put("Email", s_email);
                                trabajador.put("Estado", estado);
                                trabajador.put("Estado2", estado2);
                                trabajador.put("Estado3", estado3);
                                trabajador.put("Administrador", administrador);
                                //trabajador.put("Password", s_pass_personal);

                                //Añadiendo nuevo trabajador a Institución
                                fAuth.createUserWithEmailAndPassword(s_email, s_pass_personal).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            UserID = fAuth.getCurrentUser().getUid();
                                            final DocumentReference d_referencia_usuarios = db.collection("UNAN, León").document(UserID);
                                            d_referencia_usuarios.set(trabajador).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "¡Completo! Perfil creado para: " + UserID);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "Fallo: " + e.toString());
                                                }
                                            });
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(SigninActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        Toast.makeText(SigninActivity.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(SigninActivity.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });
    }/*Fin onCreate*/

    /*Función para ocultar la barra de navegación*/
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    /*Comprobar que la red no está Limitada*/
    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        //| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        //| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        /*| View.SYSTEM_UI_FLAG_FULLSCREEN);*/
    }
    /*----------------------------------------------------------------------*/
}