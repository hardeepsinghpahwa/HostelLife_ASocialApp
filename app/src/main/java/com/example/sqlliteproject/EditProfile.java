package com.example.sqlliteproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class EditProfile extends AppCompatActivity {


    EditText name,username,description,email,phone,bdate;
    CircleImageView profilepic;
    RadioButton male,female,other;
    Button save;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    Uri uri=null,resultUri=null;
    DatePickerDialog.OnDateSetListener pDateSetListener;
    StorageReference storageReference;
    TextView inuse;
    private static int PICK_IMAGE = 1;
    ProgressBar progressBar;
    RelativeLayout relativeLayout;
    String downloadlink,img;
    String un;
    private int pYear;
    private int pMonth;
    static final int DATE_DIALOG_ID = 0;
    private int pDay;
    ProgressBar progressBar1;
    private static final String PROFILE_URL=PhpScripts.PROFILE_URL;
    private static final String USERNAME_CHECK_URL=PhpScripts.USERNAME_CHECK_URL;
    private static final String PROFILE_UPDATE_URL=PhpScripts.PROFILE_UPDATE_URL;

    RequestQueue requestQueue;
    String uid,gen;
    RadioGroup gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        name=findViewById(R.id.editname);
        username=findViewById(R.id.editusername);
        description=findViewById(R.id.editdesc);
        email=findViewById(R.id.editemail);
        phone=findViewById(R.id.editphone);
        bdate=findViewById(R.id.editdateofb);
        profilepic=findViewById(R.id.pp);
        male=findViewById(R.id.editmale);
        female=findViewById(R.id.editfemale);
        other=findViewById(R.id.editother);
        save=findViewById(R.id.save);
        profilepic=findViewById(R.id.pp);
        relativeLayout=findViewById(R.id.editprofilelayout);
        gender=findViewById(R.id.gen);
        inuse=findViewById(R.id.inuse);
        storageReference = FirebaseStorage.getInstance().getReference();
        progressBar=findViewById(R.id.usernameprogress);
        progressBar1=findViewById(R.id.editprofileprogressbar);

        final Calendar cal = Calendar.getInstance();
        pYear = cal.get(Calendar.YEAR);
        pMonth = cal.get(Calendar.MONTH);
        pDay = cal.get(Calendar.DAY_OF_MONTH);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");
        pDateSetListener =
                new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        pYear = year;
                        pMonth = monthOfYear;
                        pDay = dayOfMonth;
                        updateDisplay();
                    }
                };

        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;

            }
        });

        uid=getIntent().getStringExtra("uid");

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

            }
        });

        final StringRequest stringRequest=new StringRequest(Request.Method.POST, PROFILE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject1=new JSONObject(response);
                    JSONArray jsonArray=jsonObject1.getJSONArray("details");
                    JSONObject jsonObject2=jsonArray.getJSONObject(0);

                    name.setText(jsonObject2.getString("name"));
                    username.setText(jsonObject2.getString("username"));
                    email.setText(jsonObject2.getString("email"));
                    gen=(jsonObject2.getString("gender"));
                    un=jsonObject2.getString("username");

                    if(jsonObject2.getString("phone").equals("0"))
                    {
                        phone.setText("");
                    }
                    else {
                        phone.setText(jsonObject2.getString("phone"));

                    }

                   if(gen.equals("Male"))
                   {
                       male.setChecked(true);
                   }else  if(gen.equals("Female"))
                   {
                       female.setChecked(true);
                   }else  if(gen.equals("Other"))
                   {
                       other.setChecked(true);
                   }

                   img=jsonObject2.getString("image");

                    bdate.setText(jsonObject2.getString("birthday"));
                    Picasso.get().load(jsonObject2.getString("image")).into(profilepic);
                    description.setText(jsonObject2.getString("description"));

                    progressBar1.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> params=new HashMap<>();
                params.put("userid",uid);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue= Volley.newRequestQueue(EditProfile.this);
        requestQueue.add(stringRequest);


        bdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                progressBar.setVisibility(View.VISIBLE);
                inuse.setVisibility(View.GONE);
                StringRequest stringRequest1=new StringRequest(Request.Method.POST, USERNAME_CHECK_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        JSONObject jsonObject= null;
                        try {
                            jsonObject = new JSONObject(response);
                            String success=jsonObject.getString("success");
                            Log.i("suc",success);

                            if(success.equals("0") || s.toString().equals(un))
                            {
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                            else if(success.equals("1") ){
                                progressBar.setVisibility(View.INVISIBLE);
                                inuse.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {

                        Map<String, String> params = new HashMap<>();
                        Log.i("u",s.toString());
                        params.put("uname",s.toString());

                        return params;
                    }
                };
                stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                        30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(stringRequest1);

            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int selid=gender.getCheckedRadioButtonId();

                switch (selid)
                {
                    case R.id.editmale:
                        gen="Male";
                        break;

                    case R.id.editfemale:
                        gen="Female";
                        break;

                    case R.id.editother:
                        gen="Others";
                        break;

                    default:
                        gen="";
                        break;
                }

                if (name.getText().toString().equals("")) {
                    name.setError("This can't be empty");
                    name.requestFocus();
                } else if (email.getText().toString().equals("")) {
                    email.setError("This can't be empty");
                    email.requestFocus();
                } else if (!validate(email.getText().toString())) {
                    email.setError("Incorrect Email");
                    email.requestFocus();
                } else if (username.getText().toString().equals("")) {
                    username.setError("This can't be empty");
                    username.requestFocus();
                }
                else if(inuse.getVisibility()==View.VISIBLE || progressBar.getVisibility()==(View.VISIBLE))
                {
                    username.requestFocus();
                }
                else {
                if (resultUri != null) {
                    final AlertDialog alertDialog = new SpotsDialog.Builder()
                            .setContext(EditProfile.this)
                            .setMessage("Logging you in")
                            .setCancelable(false)
                            .setTheme(R.style.Custom)
                            .build();
                    alertDialog.show();
                    storageReference = storageReference.child("Profile Pictures/" + username.getText().toString());
                    UploadTask uploadTask = storageReference.putFile(resultUri);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Log.i("", task.getResult().toString());
                                downloadlink = task.getResult().toString();

                                StringRequest stringRequest1 = new StringRequest(Request.Method.POST, PROFILE_UPDATE_URL, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        JSONObject jsonObject = null;
                                        try {
                                            jsonObject = new JSONObject(response);
                                            String success = jsonObject.getString("success");
                                            Log.i("suc", success);

                                            if (success.equals("0")) {
                                                alertDialog.dismiss();
                                                Log.i("update", "failed");
                                                Toast.makeText(EditProfile.this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                                            } else if (success.equals("1")) {
                                                alertDialog.dismiss();
                                                Log.i("update", "success");
                                                Toast.makeText(EditProfile.this, "Changes Saved", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                    }
                                }) {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        final Map<String, String> params = new HashMap<>();

                                        params.put("name", name.getText().toString());
                                        params.put("email", email.getText().toString());
                                        params.put("username", username.getText().toString().toLowerCase());
                                        params.put("image", downloadlink);
                                        params.put("description", description.getText().toString());
                                        params.put("birthday", bdate.getText().toString());
                                        params.put("gender", gen);
                                        params.put("uid", uid);

                                        if (phone.getText().toString().equals("")) {
                                            params.put("phone", "0");

                                        } else {
                                            params.put("phone", phone.getText().toString());
                                        }

                                        return params;

                                    }
                                };
                                stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                                        30000,
                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                requestQueue.add(stringRequest1);

                            }
                        }

                    });
                }
                else {
                    final AlertDialog alertDialog = new SpotsDialog.Builder()
                            .setContext(EditProfile.this)
                            .setMessage("Saving details")
                            .setCancelable(false)
                            .setTheme(R.style.Custom)
                            .build();
                    alertDialog.show();
                StringRequest stringRequest1=new StringRequest(Request.Method.POST, PROFILE_UPDATE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject= null;
                        try {
                            jsonObject = new JSONObject(response);
                            String success=jsonObject.getString("success");
                            Log.i("suc",success);

                            if (success.equals("0")) {
                                Log.i("update", "failed");
                                alertDialog.dismiss();
                                Toast.makeText(EditProfile.this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                            } else if (success.equals("1")) {
                                Log.i("update", "success");
                                alertDialog.dismiss();
                                Toast.makeText(EditProfile.this, "Changes Saved", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        final Map<String, String> params = new HashMap<>();


                            params.put("name", name.getText().toString());
                            params.put("email", email.getText().toString());
                            params.put("username", username.getText().toString());
                            params.put("image",img);
                            params.put("description", description.getText().toString());
                            params.put("birthday",bdate.getText().toString());
                            params.put("gender",gen);
                            params.put("uid",uid);
                            if(phone.getText().toString().equals(""))
                            {
                                params.put("phone","0");

                            }
                            else {
                                params.put("phone",phone.getText().toString());
                            }


                        return params;
                    }
                };
                    stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(stringRequest1);
            }
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
                profilepic.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }

        }
    }

    /**
     * Updates the date in the TextView
     */
    private void updateDisplay() {
        bdate.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(pMonth + 1).append("/")
                        .append(pDay).append("/")
                        .append(pYear).append(" "));
    }

    /**
     * Displays a notification when the date is updated
     */


    /**
     * Create a new dialog for date picker
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        pDateSetListener,
                        pYear, pMonth, pDay);
        }
        return null;
    }
    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
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
