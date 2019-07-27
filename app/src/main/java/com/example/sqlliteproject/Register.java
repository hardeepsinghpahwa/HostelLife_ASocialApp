package com.example.sqlliteproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dmax.dialog.SpotsDialog;

public class Register extends AppCompatActivity {

    private TextInputEditText name, pass, reenterpass, email, username;
    private Button register;
    private ProgressBar progressBar;
    private ProgressDialog reg;
    private static int PICK_IMAGE = 1;
    ImageView profilepic;
    String downloadlink;
    Uri uri, resultUri;
    String gen;
    AlertDialog alertDialog;
    DatePickerDialog.OnDateSetListener pDateSetListener;
    StorageReference storageReference;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private EditText pPickDate;
    private int pYear;
    private int pMonth;
    static final int DATE_DIALOG_ID = 0;
    private int pDay;
    RadioGroup gender;

    public static String URL_Reg = PhpScripts.URL_Reg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ServiceManager serviceManager = new ServiceManager(getApplicationContext());
        if (!serviceManager.isNetworkAvailable()) {
            startActivity(new Intent(Register.this,NoInternet.class));
        }

        name = findViewById(R.id.name);
        pass = findViewById(R.id.password);
        reenterpass = findViewById(R.id.reenterpassword);
        email = findViewById(R.id.email);
        register = findViewById(R.id.register);
        progressBar = findViewById(R.id.pbar);
        username = findViewById(R.id.username);
        profilepic = findViewById(R.id.profilepic);
        pPickDate = findViewById(R.id.selectdate);
        gender=findViewById(R.id.gender);
        storageReference = FirebaseStorage.getInstance().getReference();

        pPickDate.setText("");
        pPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        /** Get the current date */
        final Calendar cal = Calendar.getInstance();
        pYear = cal.get(Calendar.YEAR);
        pMonth = cal.get(Calendar.MONTH);
        pDay = cal.get(Calendar.DAY_OF_MONTH);

        /** Display the current date in the TextView */
        updateDisplay();

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

            }
        });



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


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int selid=gender.getCheckedRadioButtonId();

                switch (selid)
                {
                    case R.id.male:
                        gen="Male";
                        break;

                    case R.id.female:
                        gen="Female";
                        break;

                    case R.id.other:
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
                } else if (selid==-1) {
                    Toast.makeText(Register.this, "Select Gender", Toast.LENGTH_SHORT).show();
                } else if (pass.getText().toString().equals("")) {
                    pass.setError("This can't be empty");
                    pass.requestFocus();
                } else if (pass.getText().toString().length() < 7) {
                    pass.setError("Password should be greater than 6 characters");
                    pass.requestFocus();
                } else if (reenterpass.getText().toString().equals("")) {
                    reenterpass.setError("This can't be empty");
                    reenterpass.requestFocus();


                } else if (!pass.getText().toString().equals(reenterpass.getText().toString())) {
                    reenterpass.setError("Passwords Don't match");
                    reenterpass.requestFocus();
                } else {

                    alertDialog = new SpotsDialog.Builder()
                            .setContext(Register.this)
                            .setMessage("Processing")
                            .setCancelable(false)
                            .setTheme(R.style.Custom)
                            .build();
                    alertDialog.show();
                    if (resultUri != null) {


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
                                    new Reg().execute();
                                }
                                else {
                                    Toast.makeText(Register.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                }
                            }
                        });
                    } else {
                        downloadlink = "https://firebasestorage.googleapis.com/v0/b/sqlliteproject.appspot.com/o/pp.png?alt=media&token=0504a41b-1d7f-482e-9031-2f971e36bc37";
                        new Reg().execute();
                    }
                }
            }
        });


    }

    /**
     * Updates the date in the TextView
     */
    private void updateDisplay() {
        pPickDate.setText(
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

    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }


    class Reg extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


            final String namet = name.getText().toString();
            final String password = pass.getText().toString();
            final String image = downloadlink;
            final String mail = email.getText().toString();
            final String uname = username.getText().toString().toLowerCase();
            final String bdate= pPickDate.getText().toString();

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_Reg, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Log.i("e", response);

                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        Log.i("success", success);

                        if (success.equals("1")) {

                            alertDialog.dismiss();
                            finish();
                            Toast.makeText(Register.this, "Registration Success", Toast.LENGTH_SHORT).show();
                        }
                        else if(success.equals("0"))
                        {
                            alertDialog.dismiss();
                            Toast.makeText(Register.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        alertDialog.dismiss();
                        Toast.makeText(Register.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        Log.i("e", e.toString());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    alertDialog.dismiss();
                    Toast.makeText(Register.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    Log.i("error", error.toString());
                }
            }) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    String u = UUID.randomUUID().toString();
                    Log.i("uid", u);
                    Map<String, String> params = new HashMap<>();
                    params.put("uid", u);
                    params.put("name", namet);
                    params.put("email", mail);
                    params.put("password", password);
                    params.put("username", uname);
                    params.put("image", image);
                    params.put("birthday",bdate);
                    params.put("gender",gen);

                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            int x=2;// retry count
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                    x, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(stringRequest);


            return null;
        }
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


}
