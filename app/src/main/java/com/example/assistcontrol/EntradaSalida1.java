package com.example.assistcontrol;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

public class EntradaSalida1 extends AppCompatActivity {
    ImageButton regresa;
    TextView m_fecha;
    Button b_entrada, b_salida;
    FirebaseAuth fAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String s_hora, s_ano, s_mes, s_dia, s_fecha, userID, estado, obt_MAC, nombreCompleto = "", hora_Entrada2 = "", hora_Salida = "", hora_Salida2 = "";
    private WifiManager wifi;
    int REQUESTCODE = 200;
    private static final String TAG = "EntradaSalida1";
    Date trueTime;
    Date hora_dispositivo;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada_salida1);
        b_entrada = findViewById(R.id.boton_marcar_entrada1);
        b_salida = findViewById(R.id.boton_marcar_salida1);
        regresa = findViewById(R.id.btn_regre);

        /*Obteniendo datos de firebase*/
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        regresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

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

        /*Verificar si está activo el GPS*/
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertNoGPS();
            }
        }

        iniciandoTruetime();
        permisos();

        String day = getString(R.string.tt_time_gmt, _formatDate(trueTime, "d", TimeZone.getTimeZone("GMT-06:00")));
        String month = getString(R.string.tt_time_gmt, _formatDate(trueTime, "M", TimeZone.getTimeZone("GMT-06:00")));
        String year = getString(R.string.tt_time_gmt, _formatDate(trueTime, "yyyy", TimeZone.getTimeZone("GMT-06:00")));
        Log.d(TAG, day);
        Log.d(TAG, month);
        Log.d(TAG, year);

        int aux = Integer.parseInt(day);
        int aux2 = aux - 1;
        String day2 = aux2 + "";
        String date = day2 + "" + month + "" + year;

        Log.d(TAG, date);

        marcaje();

        //verificando si marcó ayer o no.
        final DocumentReference document2 = db.collection("UNAN, León").document(userID).collection("Salida").document(date);
        document2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!(document.exists())) {
                        final DocumentReference document3 = db.collection("UNAN, León").document(userID);
                        document3.update("Estado", "1").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                b_entrada = findViewById(R.id.boton_marcar_entrada1);
                                b_salida = findViewById(R.id.boton_marcar_salida1);

                                b_entrada.setVisibility(View.VISIBLE);
                                b_salida.setVisibility(View.GONE);
                                verificacion_entrada_salida();
                                Log.d(TAG, "Se actualizó el estado por no marcar la salida ayer. :)");
                            }
                        });
                    } else {
                        final DocumentReference document9 = db.collection("UNAN, León").document(userID);
                        document9.update("Estado", "1").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                b_entrada = findViewById(R.id.boton_marcar_entrada1);
                                b_salida = findViewById(R.id.boton_marcar_salida1);

                                b_entrada.setVisibility(View.VISIBLE);
                                b_salida.setVisibility(View.GONE);
                                verificacion_entrada_salida();
                                Log.d(TAG, "El documento de salida de ayer existe.");
                            }
                        });
                    }
                }
            }
        });

        m_fecha = findViewById(R.id.mostrarfecha);

        Date d = new Date();
        //SACAMOS LA FECHA COMPLETA
        SimpleDateFormat fecha_completa = new SimpleDateFormat("d ' de 'MMMM");
        SimpleDateFormat di = new SimpleDateFormat("EEEE");
        String dia_actual = di.format(d);
        String fechacComplString = fecha_completa.format(d);
        m_fecha.setText(dia_actual + ", " + fechacComplString);

        Log.d(TAG, "Usuario:  " + userID);

    } //Fin del OnCreate

    public void verificacion_entrada_salida() {
        iniciandoTruetime();
        String day = getString(R.string.tt_time_gmt, _formatDate(trueTime, "d", TimeZone.getTimeZone("GMT-06:00")));
        String month = s_mes = getString(R.string.tt_time_gmt, _formatDate(trueTime, "M", TimeZone.getTimeZone("GMT-06:00")));
        String year = s_ano = getString(R.string.tt_time_gmt, _formatDate(trueTime, "yyyy", TimeZone.getTimeZone("GMT-06:00")));

        int aux = Integer.parseInt(day);
        int aux2 = aux - 1;
        String day2 = aux2 + "";
        String date = day2 + "" + month + "" + year;
        String date_actual = day + "" + month + "" + year;

        final DocumentReference document4 = db.collection("UNAN, León").document(userID).collection("Entrada").document(date_actual);
        document4.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        final DocumentReference document5 = db.collection("UNAN, León").document(userID);
                        document5.update("Estado", "0").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                b_entrada = findViewById(R.id.boton_marcar_entrada1);
                                b_salida = findViewById(R.id.boton_marcar_salida1);

                                b_entrada.setVisibility(View.GONE);
                                b_salida.setVisibility(View.VISIBLE);

                                Log.d(TAG, "Se actualizó el estado porque si marcaste entrada1 hoy, solo verificar. :)");

                                return;
                            }
                        });

                    } else {
                        return;
                    }
                }
            }
        });
    }

    public void met_marca_entrada1(View view) {
        getMacId();
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
                    DocumentReference MAC_de_la_red = db.collection("Puntos de Acceso").document(obt_MAC);
                    MAC_de_la_red.get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot MAC = task.getResult();
                                        if (MAC.exists()) {
                                            iniciandoTruetime();

                                            s_hora = getString(R.string.tt_time_gmt,
                                                    _formatDate(trueTime, "HH:mm:ss", TimeZone.getTimeZone("GMT-06:00")));
                                            s_ano = getString(R.string.tt_time_gmt, _formatDate(trueTime, "yyyy", TimeZone.getTimeZone("GMT-06:00")));
                                            s_mes = getString(R.string.tt_time_gmt, _formatDate(trueTime, "M", TimeZone.getTimeZone("GMT-06:00")));
                                            s_dia = getString(R.string.tt_time_gmt, _formatDate(trueTime, "d", TimeZone.getTimeZone("GMT-06:00")));

                                            s_fecha = s_dia + s_mes + s_ano;

                                            Log.d(TAG, s_hora);
                                            if (estado.equals("1")) {
                                                DocumentReference verificar_entrada = db.collection("UNAN, León").document(userID).collection("Entrada")
                                                        .document(s_fecha);
                                                verificar_entrada.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot v_entrada = task.getResult();
                                                            if (v_entrada.exists()) {
                                                                StyleableToast.makeText(EntradaSalida1.this, "¡Ya marcó su Entrada!", R.style.toasttyle_entrada).show();
                                                            } else {
                                                                DocumentReference document = db.collection("UNAN, León").document(userID);
                                                                document.update("Estado", "0").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Map<String, Object> entrada = new HashMap<>();
                                                                        entrada.put("Año", s_ano);
                                                                        entrada.put("Mes", s_mes);
                                                                        entrada.put("Dia", s_dia);
                                                                        entrada.put("Hora", s_hora);
                                                                        entrada.put("HoraEntrada2", hora_Entrada2);
                                                                        entrada.put("HoraSalida", hora_Salida);
                                                                        entrada.put("HoraSalida2", hora_Salida2);
                                                                        entrada.put("Id", s_fecha);
                                                                        entrada.put("NombreCompleto", nombreCompleto);

                                                                        db.collection("UNAN, León").document(userID)
                                                                                .collection("Entrada").document(s_fecha)
                                                                                .set(entrada)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        b_entrada = findViewById(R.id.boton_marcar_entrada1);
                                                                                        b_salida = findViewById(R.id.boton_marcar_salida1);

                                                                                        DocumentReference marcaste = db.collection("UNAN, León").document(userID);
                                                                                        marcaste.update("Estado3", "1").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                Log.d("MARCASTE", "Ya marcaste aunque sea");
                                                                                            }
                                                                                        });

                                                                                        b_entrada.setVisibility(View.GONE);
                                                                                        b_salida.setVisibility(View.VISIBLE);
                                                                                        StyleableToast.makeText(EntradaSalida1.this, "Marca Entrada exitosa\n\n" + "      " + s_hora + "\n" + "      " + s_dia + " / " + s_mes + " / " + s_ano, R.style.toasttyle_entrada).show();
                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Log.d(TAG, "Error al añadir Entrada", e);
                                                                            }
                                                                        });
                                                                        Log.d(TAG, "Se actualizó el estado. :)");

                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "No se actualizó el estado. :(");
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        } else {
                                            StyleableToast.makeText(EntradaSalida1.this, "Punto de Acceso Denegado.", R.style.toasttyle_salida).show();
                                            return;
                                        }
                                    }
                                }
                            });
                }//fin comprobación internet
            } else {
                Toast activo = Toast.makeText(EntradaSalida1.this, "Error al Marcar Entrada.\n\nRevise su conexión a Internet.", Toast.LENGTH_SHORT);
                activo.show();
            }
        } else {
            Toast activo = Toast.makeText(EntradaSalida1.this, "Error al Marcar Entrada.\n\nRevise su conexión a Internet.", Toast.LENGTH_SHORT);
            activo.show();
        }
    }

    public void met_marca_salida1(View view) {
        getMacId();
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
                    DocumentReference MAC_de_la_red = db.collection("Puntos de Acceso").document(obt_MAC);
                    MAC_de_la_red.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot MAC = task.getResult();
                                if (MAC.exists()) {
                                    iniciandoTruetime();

                                    s_hora = getString(R.string.tt_time_gmt,
                                            _formatDate(trueTime, "HH:mm:ss", TimeZone.getTimeZone("GMT-06:00")));
                                    s_ano = getString(R.string.tt_time_gmt, _formatDate(trueTime, "yyyy", TimeZone.getTimeZone("GMT-06:00")));
                                    s_mes = getString(R.string.tt_time_gmt, _formatDate(trueTime, "M", TimeZone.getTimeZone("GMT-06:00")));
                                    s_dia = getString(R.string.tt_time_gmt, _formatDate(trueTime, "d", TimeZone.getTimeZone("GMT-06:00")));

                                    s_fecha = s_dia + s_mes + s_ano;

                                    if (estado.equals("0")) {
                                        DocumentReference verificar_salida = db.collection("UNAN, León").document(userID).collection("Salida")
                                                .document(s_fecha);

                                        verificar_salida.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot v_salida = task.getResult();
                                                    if (v_salida.exists()) {
                                                        StyleableToast.makeText(EntradaSalida1.this, "¡Ya marcó su Salida!", R.style.toasttyle_salida).show();
                                                    } else {
                                                        DocumentReference document = db.collection("UNAN, León").document(userID);
                                                        document.update("Estado", "1").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Map<String, Object> salida = new HashMap<>();
                                                                salida.put("Año", s_ano);
                                                                salida.put("Mes", s_mes);
                                                                salida.put("Dia", s_dia);
                                                                salida.put("Hora", s_hora);
                                                                salida.put("HoraSalida2", hora_Salida2);
                                                                salida.put("Id", s_fecha);

                                                                Log.d(TAG, "Se actualizó el estado. :)");

                                                                db.collection("UNAN, León")
                                                                        .document(userID).collection("Salida").document(s_fecha)
                                                                        .set(salida)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                DocumentReference documenthsal = db.collection("UNAN, León").document(userID).collection("Entrada").document(s_fecha);
                                                                                documenthsal.update("HoraSalida", s_hora).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        b_entrada = findViewById(R.id.boton_marcar_entrada1);
                                                                                        b_salida = findViewById(R.id.boton_marcar_salida1);

                                                                                        b_entrada.setVisibility(View.VISIBLE);
                                                                                        b_salida.setVisibility(View.GONE);

                                                                                        StyleableToast.makeText(EntradaSalida1.this, "Marca Salida exitosa\n\n" + "      " + s_hora + "\n" + "      " + s_dia + " / " + s_mes + " / " + s_ano, R.style.toasttyle_salida).show();
                                                                                    }
                                                                                });
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                        Log.d(TAG, "Error al Marcar Salida", e);
                                                                    }
                                                                });
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "No se actualizó el estado. :(");
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    StyleableToast.makeText(EntradaSalida1.this, "Punto de Acceso Denegado.", R.style.toasttyle_salida).show();
                                }
                            }
                        }
                    });
                }
            } else {
                Toast activo = Toast.makeText(EntradaSalida1.this, "Error al Marcar Salida.\n\nRevise su conexión a Internet.", Toast.LENGTH_SHORT);
                activo.show();
            }
        } else {
            Toast activo = Toast.makeText(EntradaSalida1.this, "Error al Marcar Salida.\n\nRevise su conexión a Internet.", Toast.LENGTH_SHORT);
            activo.show();
        }
    }

    //Verificar permisos
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void permisos() {
        int permiso_ubicacion = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permiso_ubicacion == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUESTCODE);
        }
    }

    public void marcaje() {
        //Obteniendo estado del marcaje
        final DocumentReference document = db.collection("UNAN, León").document(userID);
        document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        estado = document.getString("Estado");
                        Log.d("Estado", estado);
                    }
                }
            }
        });
    }

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

    public void AlertNoGPS() {
        AlertDialog alert;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS no está activado.\n\nPor razones de Seguridad en API 26 o mayor, es necesario activar el Hardware GPS aparte del Permiso de Ubicación para:\n\n1) Mostrar el SSID de la red a la que está conectado.\n2) Reconocer la dirección MAC para poder Marcar Entrada/Salida.\n\n¿Deseas Activarlo?")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert = builder.create();
        alert.show();
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

    /*Comprobar que la red no está Limitada*/
    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    /*Obtener MAC del enrutador*/
    public void getMacId() {
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

            if (mWifi.isConnected()) {
                tipoConexionWIFI = true;
            }
            if (tipoConexionWIFI == true) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                Log.d(TAG, wifiInfo.getBSSID());
                obt_MAC = wifiInfo.getBSSID();
            }//fin comprobación internet
        } else {
            Log.d(TAG, "No hay conexión para obtener la MAC.");
        }
    }
}
