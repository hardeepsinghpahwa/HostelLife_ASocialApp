package com.example.sqlliteproject;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPost extends AppCompatActivity {


    RequestQueue requestQueue;
    String postid;
    private static final String LIKE_URL = PhpScripts.LIKE_URL;
    private static final String CHECK_URL = PhpScripts.CHECK_URL;
    private static final String COMMENT_URL = PhpScripts.COMMENT_URL;
    private static final String PROFILE_URL = PhpScripts.PROFILE_URL;
    private static final String GETPOST_URL = PhpScripts.GETPOST_URL;

    TextView username, time, description, location, likes, comments;
    ImageView postpic;
    EditText commenttext;
    CircleImageView propic;
    Button postcomment;
    ImageButton comment;
    SparkButton like;
    String loginuser;
    String time1, img, desc, loc, likes1, userid1, comments1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        postid = getIntent().getStringExtra("postid");
        loginuser = getIntent().getStringExtra("loginuser");

        ServiceManager serviceManager = new ServiceManager(getApplicationContext());
        if (!serviceManager.isNetworkAvailable()) {
            startActivity(new Intent(ViewPost.this, NoInternet.class));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Log.i("loginuser", loginuser);

        username = findViewById(R.id.postusername1);
        time = findViewById(R.id.posttime1);
        description = findViewById(R.id.postdesc1);
        location = findViewById(R.id.postlocation1);
        postpic = findViewById(R.id.postpic1);
        propic = findViewById(R.id.propic1);
        like = findViewById(R.id.like1);
        comment = findViewById(R.id.comment1);
        commenttext = findViewById(R.id.commenttext1);
        likes = findViewById(R.id.likesno1);
        comments = findViewById(R.id.commentsno1);
        postcomment = findViewById(R.id.postcomment1);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, GETPOST_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    JSONArray jsonArray = jsonObject1.getJSONArray("details");
                    JSONObject jsonObject2 = jsonArray.getJSONObject(0);

                    time1 = jsonObject2.getString("date");
                    img = jsonObject2.getString("image");
                    desc = jsonObject2.getString("description");
                    loc = jsonObject2.getString("location");
                    likes1 = jsonObject2.getString("likes");
                    comments1 = jsonObject2.getString("comments");
                    userid1 = jsonObject2.getString("userid");

                    StringRequest stringRequest2 = new StringRequest(Request.Method.POST, PROFILE_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject1 = new JSONObject(response);
                                JSONArray jsonArray = jsonObject1.getJSONArray("details");
                                JSONObject jsonObject2 = jsonArray.getJSONObject(0);

                                username.setText(jsonObject2.getString("username"));
                                getSupportActionBar().setTitle(jsonObject2.getString("username"));
                                Picasso.get().load(jsonObject2.getString("image")).into(propic);


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

                            Map<String, String> params = new HashMap<>();
                            params.put("userid", userid1);

                            return params;
                        }
                    };
                    stringRequest2.setRetryPolicy(new DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    requestQueue.add(stringRequest2);
                    String dateStart = time1;

                    //HH converts hour in 24 hours format (0-23), day calculation
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date();

                    Date d1 = null;
                    Date d2 = null;

                    try {
                        d1 = format.parse(dateStart);
                        d2 = format.parse(formatter.format(date));

                        //in milliseconds
                        long diff = d2.getTime() - d1.getTime();

                        long diffSeconds = diff / 1000 % 60;
                        long diffMinutes = diff / (60 * 1000) % 60;
                        long diffHours = diff / (60 * 60 * 1000) % 24;
                        long diffDays = diff / (24 * 60 * 60 * 1000);

                        if (diffDays > 1) {
                            time.setText(diffDays + " days");
                        } else if (diffDays == 1) {
                            time.setText(diffDays + " day");
                        } else if (diffHours > 1) {
                            time.setText(diffHours + " hours ago");
                        } else if (diffHours == 1) {
                            time.setText(diffHours + " hour ago");
                        } else if (diffMinutes > 1) {
                            time.setText(diffMinutes + " minutes ago");
                        } else if (diffMinutes == 1) {
                            time.setText(diffMinutes + " minute ago");
                        } else if (diffSeconds > 1) {
                            time.setText(diffSeconds + " seconds ago");
                        } else if (diffDays == 1) {
                            time.setText(diffSeconds + " second ago");
                        }


                        Log.i("u", desc);

                        if (desc.equals("")) {
                            description.setVisibility(View.GONE);
                        }
                        description.setText(desc);
                        location.setText(loc);


                        if ((Integer.parseInt(likes1) > 1)) {
                            likes.setText(likes1 + " likes");
                        } else {
                            likes.setText(likes1 + " like");
                        }

                        if ((Integer.parseInt(comments1) > 1)) {
                            comments.setText(comments1 + " comments");
                        } else {
                            comments.setText(comments1 + " comment");
                        }

                        Picasso.get().load(img).into(postpic);

                    } catch (ParseException e) {
                        e.printStackTrace();
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
                Map<String, String> params = new HashMap<>();
                params.put("postid", postid);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);


        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, CHECK_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;

                try {
                    jsonObject = new JSONObject(response);
                    final String success = jsonObject.getString("success");

                    if (success.equals("1")) {
                        like.setChecked(true);
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
                Map<String, String> params = new HashMap<>();
                params.put("pid", postid);
                params.put("uid", loginuser);


                return params;
            }
        };
        stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest1);


        postcomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postcomment.setText("Posting...");


                StringRequest request = new StringRequest(Request.Method.POST, COMMENT_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject1 = null;
                        try {
                            jsonObject1 = new JSONObject(response);
                            String commentz = jsonObject1.getString("comments");
                            String success = jsonObject1.getString("success");

                            if (success.equals("1")) {
                                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                        InputMethodManager.HIDE_NOT_ALWAYS);

                                Toast.makeText(ViewPost.this, "Comment Posted", Toast.LENGTH_SHORT).show();
                                postcomment.setText("Post");
                            }

                            if ((Integer.parseInt(commentz) > 1)) {
                                commenttext.setText("");
                                commenttext.setFocusable(false);
                                comments.setText(commentz + " comments");
                            } else {
                                comments.setText(commentz + " comment");
                                commenttext.setText("");
                                commenttext.setFocusable(false);
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
                        Map<String, String> params = new HashMap<>();

                        params.put("user_id", loginuser);
                        params.put("post_id", postid);
                        if (!commenttext.getText().toString().equals("")) {
                            params.put("comment", commenttext.getText().toString());
                        } else {
                            Toast.makeText(ViewPost.this, "Comment is empty", Toast.LENGTH_SHORT).show();
                        }
                        return params;
                    }
                };
                request.setRetryPolicy(new DefaultRetryPolicy(
                        30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                requestQueue.add(request);
            }
        });


        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in = new Intent(ViewPost.this, ViewComments.class);

                in.putExtra("userid", loginuser);
                in.putExtra("postid", postid);
                startActivity(in);

            }
        });


        like.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                if (buttonState) {

                    like.setEnabled(false);
                    like.setClickable(false);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, LIKE_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject1 = null;
                            try {
                                jsonObject1 = new JSONObject(response);
                                String likess = jsonObject1.getString("likes");
                                if ((Integer.parseInt(likess) > 1)) {
                                    likes.setText(likess + " likes");
                                    like.setClickable(true);
                                    like.setEnabled(true);

                                } else {
                                    like.setEnabled(true);
                                    like.setClickable(true);
                                    likes.setText(likess + " like");
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

                            Map<String, String> params = new HashMap<>();
                            params.put("success", "0");
                            params.put("user_id", loginuser);
                            params.put("post_id", postid);

                            return params;
                        }

                    };
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    requestQueue.add(stringRequest);
                }

                else {

                    like.setEnabled(false);
                    like.setClickable(false);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, LIKE_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject1 = null;
                            try {
                                jsonObject1 = new JSONObject(response);
                                String likess = jsonObject1.getString("likes");
                                if ((Integer.parseInt(likess) > 1)) {
                                    likes.setText(likess + " likes");
                                    like.setEnabled(true);
                                    like.setClickable(true);
                                } else {
                                    like.setEnabled(true);
                                    like.setClickable(true);
                                    likes.setText(likess + " like");
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

                            Map<String, String> params = new HashMap<>();
                            params.put("success", "1");
                            params.put("user_id", loginuser);
                            params.put("post_id", postid);

                            return params;
                        }

                    };
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    requestQueue.add(stringRequest);

                }
            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState) {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState) {

            }
        });

        /*like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                like.setClickable(false);

                StringRequest stringRequest1 = new StringRequest(Request.Method.POST, CHECK_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                            final String success = jsonObject.getString("success");

                            if (success.equals("1")) {


                            } else if (success.equals("0")) {


                            }


                            Log.i("su", success);
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

                        Map<String, String> params = new HashMap<>();
                        params.put("pid", postid);
                        params.put("uid", loginuser);


                        return params;
                    }
                };
                stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                        30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                requestQueue.add(stringRequest1);

            }
        });*/


        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment.requestFocus();
            }
        });

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
