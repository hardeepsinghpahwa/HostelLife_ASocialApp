package com.example.sqlliteproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jgabrielfreitas.core.BlurImageView;

import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class tempp extends AppCompatActivity {

    CircleImageView pro;
    BlurImageView pro2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tempp);


        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever


       // pro2.setImageDrawable(dd);

    }

}
