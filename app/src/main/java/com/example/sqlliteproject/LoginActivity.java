package com.example.sqlliteproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.example.sqlliteproject.Register.validate;

public class LoginActivity extends AppCompatActivity {


    Button login;
    EditText email,password;
    TextView register;
    public static String URL_LOGIN="https://172.20.8.98/phpmyadmin/login/login.php";
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login=findViewById(R.id.loginbutton);
        email=findViewById(R.id.loginemail);
        password=findViewById(R.id.loginpassword);
        register=findViewById(R.id.reg);

        email.setText("hardeepsingh3485.in@gmail.com");
        password.setText("hello1234");

        handleSSLHandshake();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,Register.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail=email.getText().toString();
                String pass=password.getText().toString();

                if(!validate(mail))
                {
                    email.setError("Incorrect Email");
                    email.requestFocus();
                }
                else if(pass.equals(""))
                {
                    password.setError("This can't be empty");
                    password.requestFocus();
                }
                else {
                    Login(mail,pass);
                }
            }
        });

    }

    void Login(final String email, final String password){

        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.i("res",response);

                    JSONObject jsonObject=new JSONObject(response);
                    String success=jsonObject.getString("success");
                    JSONArray jsonArray=jsonObject.getJSONArray("login");

                    if(success.equals("1"))
                    {
                        Intent intent=new Intent(LoginActivity.this,Home.class);
                        intent.putExtra("json", (jsonArray.getJSONObject(0)).toString());
                        startActivity(intent);
                        Log.i("success",success);

                        Log.i("one",(jsonArray.getJSONObject(0)).toString());

                        for(int i=0;i<jsonArray.length();i++)
                        {
                            JSONObject object=jsonArray.getJSONObject(i);

                            String n= object.getString("name");
                            String em=object.getString("email");
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("success",e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("success",error.toString());

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> params=new HashMap<>();
                params.put("email",email);
                params.put("password",password);

                return params;
            }

        };

        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);

    }


    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
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



    class LoginTask extends AsyncTask<String,Void,Void>
    {
        @Override
        protected Void doInBackground(final String... strings) {

            StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_LOGIN, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject=new JSONObject(response);
                        String success=jsonObject.getString("success");
                        JSONArray jsonArray=jsonObject.getJSONArray("login");

                        if(success.equals("1"))
                        {
                            Log.i("success",success);
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                JSONObject object=jsonArray.getJSONObject(i);

                                String n= object.getString("name");
                                String em=object.getString("email");
                            }
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

                    Map<String,String> params=new HashMap<>();
                    params.put("email",strings[0]);
                    params.put("password",strings[1]);

                    return params;
                }

            };

            RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);

            return null;
        }
    }

}
