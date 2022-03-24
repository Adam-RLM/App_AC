package com.example.assistcontrol;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*public class MyPeriodicWorkManager extends Worker {
    private static final String TAG = "MyPeriodicWork";

    public MyPeriodicWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void guardarnotificacion(Data data, String TAG){
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                MyPeriodicWorkManager.class, 15, TimeUnit.MINUTES)
                .setInputData(data)
                .addTag(TAG)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance().enqueue(periodicWorkRequest);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.e(TAG, "doWork: Work is funcionando");*/

        /*String titulo = getInputData().getString("titulo");
        String detalle = getInputData().getString("detalle");
        int id = (int) getInputData().getLong("idnoti", 0);*/

        //return Result.success();
   // }
//}
