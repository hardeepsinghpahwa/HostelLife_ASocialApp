package com.example.sqlliteproject;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class ProfileDetails extends AppCompatActivity {

    String userid,loginuser;
    TextView desc, name, birthday,noposts,posttext;
    CircleImageView propic;
    RequestQueue requestQueue;
    RecyclerView posts;
    RelativeLayout l;
    SpinKitView spin;
    LinearLayout pro,foll;
    private static final String PROFILE_URL = PhpScripts.PROFILE_URL;
    private static final String GETPOSTSURL = PhpScripts.GETPOSTSURL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ServiceManager serviceManager = new ServiceManager(getApplicationContext());
        if (!serviceManager.isNetworkAvailable()) {
            startActivity(new Intent(ProfileDetails.this,NoInternet.class));
        }

        userid = getIntent().getStringExtra("userid");
        loginuser = getIntent().getStringExtra("loginuser");

        Log.i("uid",userid);

        name = findViewById(R.id.nametext1);
        desc = findViewById(R.id.description1);
        propic = findViewById(R.id.profilepic1);
        birthday = findViewById(R.id.birthday1);
        posts=findViewById(R.id.postsrecyclerview1);
        noposts=findViewById(R.id.nopostsyet1);
        pro=findViewById(R.id.profil);
        foll=findViewById(R.id.folllayout);
        posttext=findViewById(R.id.posttext);
        l=findViewById(R.id.ll);
        spin=findViewById(R.id.spinn);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, PROFILE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    JSONArray jsonArray = jsonObject1.getJSONArray("details");
                    JSONObject jsonObject2 = jsonArray.getJSONObject(0);

                    name.setText(jsonObject2.getString("name"));
                    getSupportActionBar().setTitle(jsonObject2.getString("username"));
                    Date date1 = new SimpleDateFormat("MM/dd/yyyy").parse(jsonObject2.getString("birthday"));
                    SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
                    String d = format.format(date1);
                    birthday.setText(d);
                    Glide.with(ProfileDetails.this).load(jsonObject2.getString("image")).placeholder(R.drawable.propicback).into(propic);

                    if (jsonObject2.getString("description").equals("")) {
                        desc.setVisibility(View.GONE);
                        l.setVisibility(View.GONE);
                    } else {
                        desc.setText(jsonObject2.getString("description"));
                    }

                    spin.setVisibility(View.INVISIBLE);
                    pro.setVisibility(View.VISIBLE);
                    foll.setVisibility(View.VISIBLE);
                    posttext.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeInUp)
                            .duration(700)
                            .playOn(pro);
                    YoYo.with(Techniques.FadeInUp)
                            .duration(700)
                            .playOn(posttext);
                    YoYo.with(Techniques.FadeInUp)
                            .duration(700)
                            .playOn(foll);


                    StringRequest stringRequest1 = new StringRequest(Request.Method.POST, GETPOSTSURL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.i("res", response);

                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonArray = jsonObject.getJSONArray("posts");

                                if(jsonArray.length()==0)
                                {
                                    noposts.setVisibility(View.VISIBLE);
                                }

                                posts.setAdapter(new PostAdapter(jsonArray, userid, ProfileDetails.this));


                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.i("success", e.toString());
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("success", error.toString());

                        }
                    }
                    ){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {

                            Map<String, String> params = new HashMap<>();
                            params.put("userid", userid);

                            return params;
                        }
                    };
                    stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    requestQueue.add(stringRequest1);


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
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
                params.put("userid", userid);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);


        posts.setLayoutManager(new GridLayoutManager(ProfileDetails.this,3));



    }

    class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>
    {

        JSONArray jsonArray;
        String uid;
        Context context;
        String postid;

        public PostAdapter(JSONArray jsonArray, String uid, Context context) {
            this.jsonArray = jsonArray;
            this.uid = uid;
            this.context = context;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profilepost,null);
            return new PostViewHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder postViewHolder, final int i) {

            try {
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                Glide.with(ProfileDetails.this).load(jsonObject.getString("image")).placeholder(R.drawable.greyround).into(postViewHolder.postimage);

                postid=jsonObject.getString("uid");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            postViewHolder.postimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        JSONObject object=jsonArray.getJSONObject(i);

                        Intent in=new Intent(context,ViewPost.class);
                        in.putExtra("postid",object.getString("uid"));
                        in.putExtra("loginuser",loginuser);
                        startActivity(in);
                        CustomIntent.customType(ProfileDetails.this,"left-to-right");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return jsonArray.length();
        }

        public class PostViewHolder extends RecyclerView.ViewHolder
        {
            ImageView postimage;
            public PostViewHolder(@NonNull View itemView) {
                super(itemView);

                postimage=itemView.findViewById(R.id.postimg);

            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        CustomIntent.customType(ProfileDetails.this,"right-to-left");
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
