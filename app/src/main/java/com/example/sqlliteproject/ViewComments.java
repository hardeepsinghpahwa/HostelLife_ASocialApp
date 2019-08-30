package com.example.sqlliteproject;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;


public class ViewComments extends AppCompatActivity {


    RecyclerView comments;
    String pid,uid;
    EditText commenttext;
    Button comment;
    ProgressBar progressBar;
    RequestQueue requestQueue;
    TextView nocomm;
    private static final String COMMENTS_URL=PhpScripts.COMMENTS_URL;
    private static final String COMMENT_URL = PhpScripts.COMMENT_URL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comments);


    }


}
