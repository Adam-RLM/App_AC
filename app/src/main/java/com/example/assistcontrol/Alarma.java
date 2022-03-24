package com.example.assistcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class Alarma extends AppCompatActivity {

    Spinner sp_horas, sp_minutos;
    Button aceptar_alarma, regresar, selefecha, selehora, guardar_noti, eliminar_noti;
    TextView tvfecha, tvhora;
    ImageButton regresa;
    private int minuto, hora, dia, mes, anio;
    private Context thisContext = this;
    private TextView notificationsTime;
    private int alarmID = 1;
    private SharedPreferences settings;

    Calendar actual = Calendar.getInstance();
    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);

        sp_horas = findViewById(R.id.spinner_hora);
        sp_minutos = findViewById(R.id.spinner2_minutos);
        aceptar_alarma = findViewById(R.id.config_alarma);
        regresar = findViewById(R.id.regresar);
        regresa = findViewById(R.id.btn_regre);

        settings = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        String hour, minute;

        hour = settings.getString("hour","");
        minute = settings.getString("minute","");

        notificationsTime = (TextView) findViewById(R.id.notifications_time);

        if(hour.length() > 0)
        {
            notificationsTime.setText(hour + ":" + minute);
        }

        findViewById(R.id.change_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Alarma.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String finalHour, finalMinute;

                        finalHour = "" + selectedHour;
                        finalMinute = "" + selectedMinute;
                        if (selectedHour < 10) finalHour = "0" + selectedHour;
                        if (selectedMinute < 10) finalMinute = "0" + selectedMinute;
                        notificationsTime.setText(finalHour + ":" + finalMinute);

                        Calendar today = Calendar.getInstance();

                        today.set(Calendar.HOUR_OF_DAY, selectedHour);
                        today.set(Calendar.MINUTE, selectedMinute);
                        today.set(Calendar.SECOND, 0);

                        SharedPreferences.Editor edit = settings.edit();
                        edit.putString("hour", finalHour);
                        edit.putString("minute", finalMinute);

                        //SAVE ALARM TIME TO USE IT IN CASE OF REBOOT
                        edit.putInt("alarmID", alarmID);
                        edit.putLong("alarmTime", today.getTimeInMillis());

                        edit.commit();

                        Toast.makeText(Alarma.this, getString(R.string.changed_to, finalHour + ":" + finalMinute), Toast.LENGTH_LONG).show();

                        Utils.setAlarm(alarmID, today.getTimeInMillis(), Alarma.this);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle(getString(R.string.select_time));
                mTimePicker.show();

            }
        });
        //guardar_noti = findViewById(R.id.btn_guardar_notificacion);
        //eliminar_noti = findViewById(R.id.btn_eliminar_noti);

        /*guardar_noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListenerBtn_Activar_Alarma(v);
                Toast.makeText(Alarma.this, "Notificación Guardada", Toast.LENGTH_SHORT).show();
            }
        });
        eliminar_noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListenerBtn_Desactivar_Alarma(v);
                Toast.makeText(Alarma.this, "Notificación eliminada", Toast.LENGTH_SHORT).show();
            }
        });*/

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.horas, R.layout.spinner_item_signin);
        sp_horas.setAdapter(adapter1);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.minutos, R.layout.spinner_item_signin);
        sp_minutos.setAdapter(adapter2);

        aceptar_alarma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regresar = findViewById(R.id.regresar);
                String hora_sel = sp_horas.getSelectedItem().toString();
                String minuto_sel = sp_minutos.getSelectedItem().toString();
                int hora_seleccionada = Integer.parseInt(hora_sel);
                int minuto_seleccionado = Integer.parseInt(minuto_sel);

                establecer_Alarma("Assist Control", hora_seleccionada, minuto_seleccionado);
                aceptar_alarma.setVisibility(View.GONE);
                regresar.setVisibility(View.VISIBLE);
            }
        });

        regresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
    }

    /*private void onClickListenerBtn_Activar_Alarma(View view){
        startService(new Intent(thisContext, Servicio_notificaciones.class));
    }

    private void onClickListenerBtn_Desactivar_Alarma(View view){
        stopService(new Intent(thisContext, Servicio_notificaciones.class));
    }*/

    public void establecer_Alarma(String mensaje, int hora, int minuto){
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_MESSAGE, mensaje)
                .putExtra(AlarmClock.EXTRA_HOUR, hora)
                .putExtra(AlarmClock.EXTRA_MINUTES, minuto);

        if (intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
    }

    /*Función para ocultar la barra de navegación*/
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
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
            /*selefecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anio = calendar.get(Calendar.YEAR);
                mes = calendar.get(Calendar.MONTH);
                dia = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int y, int m, int d) {
                        calendar.set(Calendar.DAY_OF_MONTH, d);
                        calendar.set(Calendar.MONTH, m);
                        calendar.set(Calendar.YEAR, y);

                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        String strDate = format.format(calendar.getTime());
                        tvfecha.setText(strDate);
                    }
                }, anio, mes, dia);
                datePickerDialog.show();
            }
        });

        selehora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hora = actual.get(Calendar.HOUR_OF_DAY);
                minuto = actual.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int h, int m) {
                        calendar.set(Calendar.HOUR_OF_DAY, h);
                        calendar.set(Calendar.MINUTE, m);

                        tvhora.setText(String.format("%02d:%02d", h, m));
                    }
                }, hora, minuto, true);
                timePickerDialog.show();
            }
        });*/
}