package com.example.assistcontrol;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.instacart.library.truetime.TrueTime;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static android.widget.Toast.LENGTH_SHORT;

public class justificacion extends AppCompatActivity {

    ImageButton regresa, lista_justificacion;
    EditText et_justificacion;
    Button btn_justificacion;
    String userID, text_justificacion;
    String s_hora, s_ano, s_mes, s_dia, s_fecha, nombreCompleto = "";
    FirebaseAuth fAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String TAG = "Justificacion.java";
    Date trueTime;
    Date hora_dispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_justificacion);

        regresa = findViewById(R.id.btn_regre);
        et_justificacion = findViewById(R.id.texto_justificacion);
        btn_justificacion = findViewById(R.id.enviar_justificacion);
        lista_justificacion = findViewById(R.id.list_justificacion);

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        DocumentReference documentUsuario = db.collection("UNAN, León").document(userID);
        documentUsuario.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String p_nombre = document.getString("PrimerNombre");
                        String s_nombre = document.getString("SegundoNombre");
                        String p_apellido = document.getString("PrimerApellido");
                        String s_apellido = document.getString("SegundoApellido");
                        nombreCompleto = p_nombre + " " + s_nombre + " " + p_apellido + " " + s_apellido;
                    }
                }
            }
        });

        regresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        btn_justificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciandoTruetime();

                s_hora = getString(R.string.tt_time_gmt, _formatDate(trueTime, "HH:mm:ss", TimeZone.getTimeZone("GMT-06:00")));
                s_ano = getString(R.string.tt_time_gmt, _formatDate(trueTime, "yyyy", TimeZone.getTimeZone("GMT-06:00")));
                s_mes = getString(R.string.tt_time_gmt, _formatDate(trueTime, "M", TimeZone.getTimeZone("GMT-06:00")));
                s_dia = getString(R.string.tt_time_gmt, _formatDate(trueTime, "d", TimeZone.getTimeZone("GMT-06:00")));

                s_fecha = s_dia + s_mes + s_ano;

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
                            text_justificacion = et_justificacion.getText().toString();

                            if (text_justificacion.isEmpty()) {
                                Toast.makeText(justificacion.this, "Escribe tu justificación.", Toast.LENGTH_SHORT).show();
                            } else {
                                DocumentReference doc_justificaciones = db.collection("UNAN, León").document(userID).collection("Justificaciones")
                                        .document(s_fecha);
                                doc_justificaciones.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot justificaciones_ds = task.getResult();
                                            if (justificaciones_ds.exists()) {
                                                StyleableToast.makeText(justificacion.this, "¡Ya realizó su Justificación!", R.style.toasttyle_salida).show();
                                            } else {
                                                text_justificacion = et_justificacion.getText().toString();
                                                Map<String, Object> justificacion = new HashMap<>();
                                                justificacion.put("Año", s_ano);
                                                justificacion.put("Mes", s_mes);
                                                justificacion.put("Dia", s_dia);
                                                justificacion.put("Hora", s_hora);
                                                justificacion.put("Justificacion", text_justificacion);
                                                justificacion.put("NombreCompleto", nombreCompleto);

                                                db.collection("UNAN, León").document(userID)
                                                        .collection("Justificaciones").document(s_fecha)
                                                        .set(justificacion)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                StyleableToast.makeText(justificacion.this, "Se envió su Justificación", R.style.toasttyle_entrada).show();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("Justificación", "Error al añadir Justificación", e);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        Toast.makeText(justificacion.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(justificacion.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        lista_justificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean online = internetIsConnected();
                //Variables Comprobación de internet
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
                            // Código si hay conexión
                            Intent intent = new Intent(justificacion.this, listaJustificaciones.class);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(justificacion.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(justificacion.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }//FIN ONCREATE

    public void iniciandoTruetime() {
        if (!TrueTime.isInitialized()) {
            Toast.makeText(getApplicationContext(), "Sorry TrueTime not yet initialized. Trying again.", LENGTH_SHORT).show();

            return;
        }

        trueTime = TrueTime.now();
        hora_dispositivo = new Date();
    }

    private String _formatDate(Date date, String pattern, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(timeZone);

        return format.format(date);
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