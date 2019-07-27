package com.example.sqlliteproject;

import android.app.Activity;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
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

import org.json.JSONException;
import org.json.JSONObject;

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
    LinearLayout linearLayout;
    EditText posttext, location;
    private static final String NEW_URL=PhpScripts.NEW_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        pickimg = findViewById(R.id.img);
        post = findViewById(R.id.post);
        posttext = findViewById(R.id.postt);
        linearLayout=findViewById(R.id.newpostlayout);
        location = findViewById(R.id.location);

        userid=getIntent().getStringExtra("userid");
        pp=getIntent().getStringExtra("pp");
        ServiceManager serviceManager = new ServiceManager(getApplicationContext());
        if (!serviceManager.isNetworkAvailable()) {
            startActivity(new Intent(NewPost.this,NoInternet.class));
        }

        linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;

            }
        });


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
                        public void onComplete(@NonNull final Task<Uri> task) {
                            if (task.isSuccessful()) {
                                downloadlink = task.getResult().toString();
                                StringRequest stringRequest=new StringRequest(Request.Method.POST, NEW_URL, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        JSONObject jsonObject= null;
                                        try {
                                            jsonObject = new JSONObject(response);
                                            String success=jsonObject.getString("success");

                                            if(success.equals("1"))
                                            {
                                                Toast.makeText(NewPost.this, "Posted", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                            else{
                                                Log.i("error","success=0");
                                                Toast.makeText(NewPost.this, "Error Posting Image, success=0", Toast.LENGTH_SHORT).show();

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.i("error",error.toString());
                                        Toast.makeText(NewPost.this, "Error Posting Image", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        String u=UUID.randomUUID().toString();

                                        Log.i("details",u+userid+posttext.getText().toString()+location.getText().toString());
                                        Log.i("link",downloadlink);

                                        Map<String,String> params=new HashMap<>();
                                        params.put("image",downloadlink);
                                        params.put("uid",u);
                                        params.put("userid",userid);
                                        params.put("description",posttext.getText().toString());
                                        params.put("location",location.getText().toString());

                                        return params;
                                    }
                                };

                                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                        30000,
                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


                                requestqueue= Volley.newRequestQueue(getApplicationContext());
                                requestqueue.add(stringRequest);
                                alertDialog.dismiss();


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
