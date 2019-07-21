package com.example.sqlliteproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

public class NewPost extends AppCompatActivity {

    ImageView pickimg;
    Button post;
    Uri uri,resultUri;
    RequestQueue requestqueue;
    String downloadlink,userid,pp;
    StorageReference storageReference;
    private static int PICK_IMAGE = 1;
    EditText posttext, location;
    private static final String URL="https://172.20.8.98/phpmyadmin/login/savepost.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("New Post");
        pickimg = findViewById(R.id.img);
        post = findViewById(R.id.post);
        posttext = findViewById(R.id.postt);
        location = findViewById(R.id.location);

        userid=getIntent().getStringExtra("userid");
        pp=getIntent().getStringExtra("pp");

        storageReference = FirebaseStorage.getInstance().getReference();
        pickimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri != null && posttext.getText().toString() != null) {
                    final AlertDialog alertDialog = new SpotsDialog.Builder()
                            .setContext(NewPost.this)
                            .setMessage("Uploading")
                            .setCancelable(false)
                            .setTheme(R.style.Custom)
                            .build();
                    alertDialog.show();
                    storageReference = storageReference.child("Saved Images/" + UUID.randomUUID().toString() + ".jpg");
                    UploadTask uploadTask = storageReference.putFile(resultUri);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                alertDialog.dismiss();
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                downloadlink = task.getResult().toString();
                                StringRequest stringRequest=new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                    }
                                })
                                {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        String u=UUID.randomUUID().toString();
                                        Map<String,String> params=new HashMap<>();
                                        params.put("image",downloadlink);
                                        params.put("uid",u);
                                        params.put("userid",userid);
                                        params.put("description",posttext.getText().toString());
                                        params.put("location",location.getText().toString());

                                        return params;
                                    }
                                };
                                requestqueue= Volley.newRequestQueue(getApplicationContext());
                                requestqueue.add(stringRequest);
                                alertDialog.dismiss();
                                Log.i("link",downloadlink);
                                Toast.makeText(NewPost.this, "Posted", Toast.LENGTH_SHORT).show();
                                finish();

                            }
                        }
                    });
                }
                else {
                    Toast.makeText(NewPost.this, "Image cant be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == PICK_IMAGE && data!=null) {
                    uri = data.getData();

                        CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(200, 200).start(this);
                    }
                    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        if (resultCode == RESULT_OK) {
                            resultUri = result.getUri();
                            pickimg.setImageURI(resultUri);

                        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                            Exception error = result.getError();
                        }

                }
            }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
