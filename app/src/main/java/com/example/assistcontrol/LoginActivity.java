package com.example.assistcontrol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextView registrarse_login, olvido;
    private Button boton_iniciar;
    private EditText email, pass_personal;
    private CheckBox b_mant_sesion;
    private String s_email = "", s_pass_personal = "";
    private boolean estado;
    private static final String STRING_PREFERENCES = "save.session.user";
    private static final String PREFERENCE_ESTADO_BUTTON_SESION = "estado.button.sesion";
    private static final String TAG = "Firebase_Login";
    FirebaseAuth fAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*Ocultar el teclado al inicio de la app*/
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        /*--------------------------------------------------------------------------------------------*/

        if (obtener_estado_button()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        fAuth = FirebaseAuth.getInstance();
        boton_iniciar = findViewById(R.id.iniciar_sesion);
        registrarse_login = findViewById(R.id.registrarse_login);
        email = findViewById(R.id.identificador_login);
        pass_personal = findViewById(R.id.pass_identificador_login);
        b_mant_sesion = findViewById(R.id.mantener_sesion);
        olvido = findViewById(R.id.olvidocontraseña);

        boton_iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s_email = email.getText().toString();
                s_pass_personal = pass_personal.getText().toString();

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
                        if (tipoConexionWIFI) {
                            /*Código si hay conexión*/
                            if (s_email.isEmpty() && s_pass_personal.isEmpty()) {
                                Toast.makeText(LoginActivity.this, "Ingrese los Datos.", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (s_email.isEmpty()) {
                                Toast.makeText(LoginActivity.this, "Ingrese el Correo Electrónico.", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (s_pass_personal.isEmpty()) {
                                Toast.makeText(LoginActivity.this, "Ingrese la Contraseña.", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (!s_email.isEmpty() && !s_pass_personal.isEmpty()) {

                                /*Verificando el Email y la Contraseña*/
                                fAuth.signInWithEmailAndPassword(s_email, s_pass_personal).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            guardarestadoCheckBox();

                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                            Toast.makeText(LoginActivity.this, "¡Inicio de sesión correctamente!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        olvido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RecuperarContrasena.class);
                startActivity(i);
            }
        });

        /*Método para el TextView Registrarse*/
        registrarse_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
                startActivity(intent);
            }
        });
        /*----------------------------------------------------------------------------------------*/
    }/*Fin onCreate*/
    /*-----------------------------------------------------------------------------------------------*/

    /*Cambiando estado del botón*/
    public static void change_estado_button(Context c, boolean b) {
        SharedPreferences preferences = c.getSharedPreferences(STRING_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(PREFERENCE_ESTADO_BUTTON_SESION, b).apply();
    }
    /*--------------------------------------------------------------------------------------------*/

    /*Guardar sesión usando SharedPreference*/
    public void guardarestadoCheckBox() {
        SharedPreferences preferences = getSharedPreferences(STRING_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(PREFERENCE_ESTADO_BUTTON_SESION, b_mant_sesion.isChecked()).apply();
    }

    public boolean obtener_estado_button() {
        SharedPreferences preferences = getSharedPreferences(STRING_PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(PREFERENCE_ESTADO_BUTTON_SESION, false);
    }
    /*---------------------------------------------------------------------------------------------------*/

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