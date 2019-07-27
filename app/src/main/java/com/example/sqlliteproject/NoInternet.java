package com.example.sqlliteproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class NoInternet extends AppCompatActivity {

    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
        button=findViewById(R.id.checkinternet);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ServiceManager serviceManager = new ServiceManager(getApplicationContext());
                if (serviceManager.isNetworkAvailable()) {
                    finish();
                } else {
                    Toast.makeText(NoInternet.this, "Turn on you data connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
