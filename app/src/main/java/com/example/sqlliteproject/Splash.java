package com.example.sqlliteproject;

import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {

                // This method will be executed once the timer is over
                // Start your app main activity

                ServiceManager serviceManager = new ServiceManager(getApplicationContext());
                if (serviceManager.isNetworkAvailable()) {

                    Intent i = new Intent(Splash.this, LoginActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(Splash.this, NoInternet.class);
                    startActivity(i);
                }


                // close this activity
                finish();
            }
        }, 3000);
    }

}

