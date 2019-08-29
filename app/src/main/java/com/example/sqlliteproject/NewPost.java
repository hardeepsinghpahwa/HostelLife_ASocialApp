package com.example.sqlliteproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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

import org.json.JSONException;
import org.json.JSONObject;

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
                    final Intent data = new Intent();

                    // Add the required data to be returned to the MainActivity
                    data.putExtra("uri", resultUri.toString());
                    data.putExtra("posttext", posttext.getText().toString());
                    data.putExtra("loc", location.getText().toString());

                    // Set the resultCode to Activity.RESULT_OK to
                    // indicate a success and attach the Intent
                    // which contains our result data
                    setResult(Activity.RESULT_OK, data);

                    finish();
                }
                else {
                    Toast.makeText(NewPost.this, "Image and post cant be empty", Toast.LENGTH_SHORT).show();
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
