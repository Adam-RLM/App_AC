package com.example.assistcontrol;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.Value;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class listaJustificaciones extends AppCompatActivity {

    RecyclerView recycler;
    ArrayList<Justificaciones> justificacionesArrayList;
    AdaptadorJustificaciones myAdapter;
    FirebaseAuth fAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageButton regresa;
    String userID;
    private AdaptadorJustificaciones.RecyclerViewClickListener listener;

    String s_hora, s_ano, s_mes, s_dia, s_fecha;
    String TAG = "listaJustificaciones";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_justificaciones);
        regresa = findViewById(R.id.btn_regre);
        recycler = findViewById(R.id.recyclerid);
        setOnClickListener();
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        justificacionesArrayList = new ArrayList<Justificaciones>();
        myAdapter = new AdaptadorJustificaciones(listaJustificaciones.this, justificacionesArrayList, listener);

        recycler.setAdapter(myAdapter);

        EventChangeListener();

        regresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
    }//Fin Oncreate

    private void setOnClickListener() {
        listener = new AdaptadorJustificaciones.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), Mostrando_Justificacion.class);
                intent.putExtra("justificacion", justificacionesArrayList.get(position).getJustificacion());
                startActivity(intent);
            }
        };
    }

    public void EventChangeListener(){
        db.collection("UNAN, León").document(userID)
                .collection("Justificaciones").orderBy("Mes", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e!= null){
                            Log.e("Firestore Error", e.getMessage());
                            return;
                        }

                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                            if (dc.getType() == DocumentChange.Type.ADDED){
                                justificacionesArrayList.add(dc.getDocument().toObject(Justificaciones.class));
                            }
                            myAdapter.notifyDataSetChanged();
                        }
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