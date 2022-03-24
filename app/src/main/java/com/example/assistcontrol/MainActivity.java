package com.example.assistcontrol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firestore.v1.StructuredQuery;
import com.instacart.library.truetime.TrueTime;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static android.view.View.INVISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static com.example.assistcontrol.R.drawable.png_male;

public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    TextView tview_minombre, tview_sexo, tview_edad, tview_institucion, tview_identificador, tview_ip, wifi_ssid, m_fecha, textV_entrada, textV_salida;
    ImageView imsexo;
    CalendarView registro;
    Adaptor_de_los_Fragmentos adaptador_de_los_fragmentos;
    AppCompatImageButton item1, item2, item3;
    Button b_entrada, b_salida, btn_close_sesion;
    FirebaseAuth fAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userID, obt_MAC, nombreCompleto = "";
    private WifiManager wifi;
    int REQUESTCODE = 200;
    private static final String TAG = "MainActivity";
    Date trueTime;
    Date hora_dispositivo;
    private String s_hora;
    private String s_ano;
    private String s_mes;
    private String s_dia;
    private String s_fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        item1 = findViewById(R.id.registros);
        item2 = findViewById(R.id.marcar);
        item3 = findViewById(R.id.perfil);
        viewPager = findViewById(R.id.contenedor_de_fragmentos);
        btn_close_sesion = findViewById(R.id.btn_cerrar_sesion);

        iniciandoTruetime();

        /*Obteniendo datos de firebase*/
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

        siMarcaste();

        adaptador_de_los_fragmentos = new Adaptor_de_los_Fragmentos(getSupportFragmentManager());
        viewPager.setAdapter(adaptador_de_los_fragmentos);

        item1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        item2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
        item3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onPageSelected(int posicion) {
                onChangeTab(posicion);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    } /*-------fin OnCreate--------*/

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onChangeTab(int posicion) {
        if (posicion == 0) {
            item1.setImageDrawable(getDrawable(R.drawable.ic_ico_registro_selec));
            item1.setBackground(getDrawable(R.drawable.color_item_seleccionado));

            item2.setImageDrawable(getDrawable(R.drawable.ic_ico_marcar));
            item2.setBackground(getDrawable(R.drawable.estilo_item_bar));

            item3.setImageDrawable(getDrawable(R.drawable.ic_ico_perfil));
            item3.setBackground(getDrawable(R.drawable.estilo_item_bar));

            registro = findViewById(R.id.calendario_registro);

            registro.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                    textV_entrada = findViewById(R.id.tv_entrada);
                    textV_salida = findViewById(R.id.tv_salida);
                    textV_entrada.setText("No hay Registros.");
                    textV_salida.setText("No hay Registros");
                    String fecha_calendario;

                    fecha_calendario = dayOfMonth + "" + (month + 1) + "" + year;

                    DocumentReference registros_entrada = db.collection("UNAN, León").document(userID).collection("Entrada")
                            .document(fecha_calendario);
                    DocumentReference registros_salida = db.collection("UNAN, León").document(userID).collection("Salida")
                            .document(fecha_calendario);

                    registros_entrada.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    textV_entrada = findViewById(R.id.tv_entrada);
                                    String dia, mes, anio, hora, hora2;

                                    dia = document.getString("Dia");
                                    mes = document.getString("Mes");
                                    anio = document.getString("Año");
                                    hora = document.getString("Hora");
                                    hora2 = document.getString("HoraEntrada2");

                                    textV_entrada.setText(dia + "/" + mes + "/" + anio + " - " + hora + " - " + hora2);
                                }
                            }
                        }
                    });

                    registros_salida.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    textV_salida = findViewById(R.id.tv_salida);
                                    String dia, mes, anio, hora, hora2;

                                    dia = document.getString("Dia");
                                    mes = document.getString("Mes");
                                    anio = document.getString("Año");
                                    hora = document.getString("Hora");
                                    hora2 = document.getString("HoraSalida2");

                                    textV_salida.setText(dia + "/" + mes + "/" + anio + " - " + hora + " - " + hora2);
                                }
                            }
                        }
                    });
                }
            });
        }
        if (posicion == 1) {
            item1.setImageDrawable(getDrawable(R.drawable.ic_ico_registro));
            item1.setBackground(getDrawable(R.drawable.estilo_item_bar));

            item2.setImageDrawable(getDrawable(R.drawable.ic_ico_marcar_selec));
            item2.setBackground(getDrawable(R.drawable.color_item_seleccionado));

            item3.setImageDrawable(getDrawable(R.drawable.ic_ico_perfil));
            item3.setBackground(getDrawable(R.drawable.estilo_item_bar));

            /*Verificar si está activo el GPS*/
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    AlertNoGPS();
                }
            }

            b_entrada = findViewById(R.id.boton_marcar_entrada);
            b_salida = findViewById(R.id.boton_marcar_salida);

            verificar_salida1();
            siMarcaste();

            b_entrada.setVisibility(View.VISIBLE);

            m_fecha = findViewById(R.id.mostrarfecha);

            Date d = new Date();
            //SACAMOS LA FECHA COMPLETA
            SimpleDateFormat fecha_completa = new SimpleDateFormat("d ' de 'MMMM");
            SimpleDateFormat di = new SimpleDateFormat("EEEE");
            String dia_actual = di.format(d);
            String fechacComplString = fecha_completa.format(d);
            m_fecha.setText(dia_actual + ", " + fechacComplString);

            Log.d(TAG, "Usuario:  " + userID);
        }
        if (posicion == 2) {
            item1.setImageDrawable(getDrawable(R.drawable.ic_ico_registro));
            item1.setBackground(getDrawable(R.drawable.estilo_item_bar));

            item2.setImageDrawable(getDrawable(R.drawable.ic_ico_marcar));
            item2.setBackground(getDrawable(R.drawable.estilo_item_bar));

            item3.setImageDrawable(getDrawable(R.drawable.ic_ico_perfil_selec));
            item3.setBackground(getDrawable(R.drawable.color_item_seleccionado));

            /*Verificar si está activo el GPS*/
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    AlertNoGPS();
                }
            }

            permisos();

            DocumentReference document = db.collection("UNAN, León").document(userID);
            document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            tview_minombre = findViewById(R.id.nombre_completo);
                            tview_sexo = findViewById(R.id.tv_sexo);
                            tview_edad = findViewById(R.id.tv_edad);
                            tview_institucion = findViewById(R.id.tv_institucion);
                            tview_identificador = findViewById(R.id.tv_identificador);
                            tview_ip = findViewById(R.id.red_ip);
                            imsexo = findViewById(R.id.iv_sexo);
                            tview_ip = findViewById(R.id.red_ip);
                            wifi_ssid = findViewById(R.id.red_conectado);

                            String p_nombre = document.getString("PrimerNombre");
                            String s_nombre = document.getString("SegundoNombre");
                            String p_apellido = document.getString("PrimerApellido");
                            String s_apellido = document.getString("SegundoApellido");
                            String s_sexo = document.getString("Sexo");
                            String s_edad = document.getString("FechaNacimiento");
                            String s_institucion = "UNAN, León";
                            String s_id = document.getString("Email");
                            /*----------------------------------------------------------------------------------------------------------------------------------------*/

                            /*Calcular edad*/
                            String[] fecha_nacimiento = s_edad.split("/");
                            int dia = Integer.parseInt(fecha_nacimiento[0]);
                            int mes = Integer.parseInt(fecha_nacimiento[1]);
                            int anio = Integer.parseInt(fecha_nacimiento[2]);
                            String s_edaduser;

                            Calendar c = Calendar.getInstance();
                            int diaA = c.get(Calendar.DAY_OF_MONTH);
                            int mesA = c.get(Calendar.MONTH);
                            int anioA = c.get(Calendar.YEAR);

                            int edade = anioA - anio;
                            if (mes > mesA) {
                                edade--;
                            } else if (mesA == mes) {
                                if (dia > diaA) {
                                    edade--;
                                }
                            }
                            s_edaduser = String.valueOf(edade);
                            /*------------------------------------------------------------------------*/

                            /*Obtener IP y SSID de la red WIFI*/

                            wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                            try {
                                //Método que devuelve el identificador de servicio de la red Wifi 802.11
                                String red_wifi = wifi.getConnectionInfo().getSSID();
                                int my_ip = wifi.getConnectionInfo().getIpAddress();
                                String my_ipAddress = Formatter.formatIpAddress(my_ip);

                                if (red_wifi != null) {
                                    wifi_ssid.setText(red_wifi);
                                    tview_ip.setText(my_ipAddress);
                                    Log.d("MainActivity", "Nombre: " + red_wifi);
                                } else {
                                    Log.d("MainActivity", "No hay nada");
                                }
                            } catch (Exception ex) {
                                ex.getMessage();
                            }
                            /*------------------------------------------------------------------------*/

                            /*Mostrando datos en el perfil*/
                            if (s_sexo.equals("Hombre")) {
                                tview_sexo.setText("Hombre");
                                imsexo.setImageResource(R.drawable.png_male);
                            } else if (s_sexo.equals("Mujer")) {
                                tview_sexo.setText("Mujer");
                                imsexo.setImageResource(R.drawable.png_woman);
                            }

                            tview_identificador.setText(s_id);
                            tview_institucion.setText(s_institucion);
                            tview_edad.setText(s_edaduser + " años");
                            tview_minombre.setText(p_nombre + " " + s_nombre + " " + p_apellido + " " + s_apellido);
                            /*----------------------------------------------------------------------------------------*/
                        } else {
                            Log.d(TAG, "No se encontró el documento");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    public void met_marca_entrada(View view) {
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
                    Intent intent = new Intent(MainActivity.this, EntradaSalida1.class);
                    startActivity(intent);
                }
            }else {
                Toast activo = Toast.makeText(MainActivity.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT);
                activo.show();
            }
        } else {
            Toast activo = Toast.makeText(MainActivity.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT);
            activo.show();
        }
    }

    public void met_marca_salida(View view){
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
                    Intent intent = new Intent(MainActivity.this, EntradaSalida2.class);
                    startActivity(intent);
                }
            }else {
                Toast activo = Toast.makeText(MainActivity.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT);
                activo.show();
            }
        } else {
            Toast activo = Toast.makeText(MainActivity.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT);
            activo.show();
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void verificar_salida1(){
        /*Verificar si está activo el GPS*/
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertNoGPS();
            }
        }

        iniciandoTruetime();
        permisos();

        s_hora = getString(R.string.tt_time_gmt,
                _formatDate(trueTime, "HH:mm:ss", TimeZone.getTimeZone("GMT-06:00")));
        s_ano = getString(R.string.tt_time_gmt, _formatDate(trueTime, "yyyy", TimeZone.getTimeZone("GMT-06:00")));
        s_mes = getString(R.string.tt_time_gmt, _formatDate(trueTime, "M", TimeZone.getTimeZone("GMT-06:00")));
        s_dia = getString(R.string.tt_time_gmt, _formatDate(trueTime, "d", TimeZone.getTimeZone("GMT-06:00")));

        s_fecha = s_dia + s_mes + s_ano;

        DocumentReference salida1 = db.collection("UNAN, León").document(userID).collection("Salida").document(s_fecha);
        salida1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot dc_v_salida = task.getResult();
                    if (dc_v_salida.exists()){
                        b_entrada = findViewById(R.id.boton_marcar_entrada);
                        b_salida = findViewById(R.id.boton_marcar_salida);

                        b_entrada.setVisibility(View.GONE);
                        b_salida.setVisibility(View.VISIBLE);
                    }else {
                        return;
                    }
                }
            }
        });
    }

    public void siMarcaste(){
        iniciandoTruetime();

        s_ano = getString(R.string.tt_time_gmt, _formatDate(trueTime, "yyyy", TimeZone.getTimeZone("GMT-06:00")));
        s_mes = getString(R.string.tt_time_gmt, _formatDate(trueTime, "M", TimeZone.getTimeZone("GMT-06:00")));
        s_dia = getString(R.string.tt_time_gmt, _formatDate(trueTime, "d", TimeZone.getTimeZone("GMT-06:00")));

        s_fecha = s_dia + s_mes + s_ano;

        DocumentReference refEntrada = db.collection("UNAN, León").document(userID).collection("Entrada").document(s_fecha);
        refEntrada.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot dS = task.getResult();
                    if (dS.exists()){
                        DocumentReference marcaste = db.collection("UNAN, León").document(userID);
                        marcaste.update("Estado3", "1").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("MARCASTE", "Ya marcaste aunque sea: " + s_fecha);
                            }
                        });
                    }else {
                        DocumentReference marcaste = db.collection("UNAN, León").document(userID);
                        marcaste.update("Estado3", "0").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("NOMARCASTE", "No has marcado nada");
                            }
                        });
                    }
                }
            }
        });
    }

    public void sistema_alarma(View view) {
        Intent intent = new Intent(MainActivity.this, Alarma.class);
        startActivity(intent);
        //finish();
    }

    public void justificaciones(View v) {
        Intent intent = new Intent(MainActivity.this, justificacion.class);
        startActivity(intent);
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

    public void cerrar(View v) {
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
                    new AlertDialog.Builder(this)
                            .setTitle("Cerrar Sesión")
                            .setMessage("¿Deseas Cerrar Sesión?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    LoginActivity.change_estado_button(MainActivity.this, false);
                                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "No pasa nada");
                                }
                            }).show();
                }
            }else {
                Toast activo = Toast.makeText(MainActivity.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT);
                activo.show();
            }
        } else {
            Toast activo = Toast.makeText(MainActivity.this, "Revise su conexión a Internet.", Toast.LENGTH_SHORT);
            activo.show();
        }
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
