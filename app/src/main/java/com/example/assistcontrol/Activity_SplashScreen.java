package com.example.assistcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Activity_SplashScreen extends Activity {

   // private ProgressBar progressBar;
    Animation bottom1, top1;
    private TextView asco;
    //private ImageView ascoimagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__splash_screen);

        asco = findViewById(R.id.textView);
       // ascoimagen = findViewById(R.id.letraa);
       // progressBar = findViewById(R.id.progressbar);
       // progressBar.setVisibility(progressBar.VISIBLE);

        bottom1 = AnimationUtils.loadAnimation(this, R.anim.bottom);
       // top1 = AnimationUtils.loadAnimation(this, R.anim.top);

        asco.setAnimation(bottom1);
        //ascoimagen.setAnimation(top1);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity_SplashScreen.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1500);
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