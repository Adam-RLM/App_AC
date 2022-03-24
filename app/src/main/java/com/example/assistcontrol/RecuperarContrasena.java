package com.example.assistcontrol;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarContrasena extends AppCompatActivity {

    ImageButton regresa;
    Button btn_reestablecer;
    EditText email;
    private String correo = "";
    private FirebaseAuth mAuth;
    private ProgressDialog mdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasena);

        regresa = findViewById(R.id.btn_regre);
        btn_reestablecer = findViewById(R.id.reestablecercontrase);

        mAuth = FirebaseAuth.getInstance();
        mdialog = new ProgressDialog(this);

        regresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        btn_reestablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = findViewById(R.id.emailrecuperar);
                correo = email.getText().toString();

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
                            if (!correo.isEmpty()) {
                                mdialog.setMessage("Espere un momento...");
                                mdialog.setCanceledOnTouchOutside(false);
                                mdialog.show();
                                resetpass();
                            } else {
                                Toast.makeText(RecuperarContrasena.this, "Debe ingresar el Correo Electrónico", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(RecuperarContrasena.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(RecuperarContrasena.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }//fin OnCreate

    public void resetpass() {
        mAuth.setLanguageCode("es");
        mAuth.sendPasswordResetEmail(correo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RecuperarContrasena.this, "Se ha enviado un Correo Electrónico para reestablecer tu contraseña", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RecuperarContrasena.this, "No se pudo enviar el Correo para reestablecer tu contraseña", Toast.LENGTH_SHORT).show();
                }
                mdialog.dismiss();
            }
        });
    }

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