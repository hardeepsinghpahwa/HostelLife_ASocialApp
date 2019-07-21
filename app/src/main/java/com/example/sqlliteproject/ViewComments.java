package com.example.sqlliteproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewComments extends AppCompatActivity {


    RecyclerView comments;
    String pid,uid;
    EditText commenttext;
    Button comment;
    RequestQueue requestQueue;
    CommentsAdapter adapter;
    private static final String COMMENTS_URL="https://172.20.8.98/phpmyadmin/login/getcomments.php";
    private static final String COMMENT_URL = "https://172.20.8.98/phpmyadmin/login/comment.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comments);

        requestQueue= Volley.newRequestQueue(ViewComments.this);

        comments=findViewById(R.id.commentsrecyclerview);
        comments.setLayoutManager(new LinearLayoutManager(ViewComments.this));

        comment=findViewById(R.id.postcomment1);
        commenttext=findViewById(R.id.commenttext1);

        pid=getIntent().getStringExtra("postid");
        uid=getIntent().getStringExtra("userid");

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment.setText("Posting");

                StringRequest request = new StringRequest(Request.Method.POST, COMMENT_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        adapter.notifyDataSetChanged();

                        JSONObject jsonObject1 = null;
                        try {
                            jsonObject1 = new JSONObject(response);
                        String success = jsonObject1.getString("success");

                        if(success.equals("1"))
                        {
                            Toast.makeText(ViewComments.this, "Comment Posted", Toast.LENGTH_SHORT).show();
                            comment.setText("Posted");
                            Timer t = new Timer();
                            t.schedule(new TimerTask() {

                                           @Override
                                           public void run() {
                                               comment.setText("Post");
                                           }

                                       },
                                    0,
                                    1000);
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

                        params.put("user_id", uid);
                        params.put("post_id", pid);
                        if (!commenttext.getText().toString().equals("")) {
                            params.put("comment", commenttext.getText().toString());
                        } else {
                            Toast.makeText(ViewComments.this, "Comment is empty", Toast.LENGTH_SHORT).show();
                        }
                        return params;
                    }
                };

                requestQueue.add(request);

            }
        });



        StringRequest stringRequest=new StringRequest(Request.Method.POST, COMMENTS_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("comments");

                    adapter =new CommentsAdapter(jsonArray,ViewComments.this);
                    comments.setAdapter(adapter);


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
            protected Map<String, String> getParams() {

                Map<String,String> params=new HashMap<>();
                params.put("pid",pid);

                return params;
            }
        };

        requestQueue.add(stringRequest);


    }

   public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>
    {

        JSONArray array;
        Context context;
        private static final String PROFILE_URL="https://172.20.8.103/phpmyadmin/login/profile.php";


        CommentsAdapter(JSONArray jsonArray,Context c)
        {
            context=c;
            array=jsonArray;
        }

        @NonNull
        @Override
        public CommentsAdapter.CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment,null);
            return new CommentsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final CommentsAdapter.CommentsViewHolder commentsViewHolder, int i) {

            try {
                final JSONObject o= array.getJSONObject(i);

                StringRequest stringRequest=new StringRequest(Request.Method.POST, PROFILE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject1 = null;
                        try {
                            jsonObject1 = new JSONObject(response);
                            JSONArray jsonArray = jsonObject1.getJSONArray("details");
                            JSONObject jsonObject2 = jsonArray.getJSONObject(0);

                            commentsViewHolder.username.setText(jsonObject2.getString("username"));
                            Picasso.get().load(jsonObject2.getString("image")).into(commentsViewHolder.propic);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {

                        Map<String, String> params = new HashMap<>();
                        try {
                            params.put("userid", o.getString("user_id"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return params;
                    }

                    };
                requestQueue.add(stringRequest);
                commentsViewHolder.comment.setText(o.getString("comment"));





            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return array.length();
        }

        public class CommentsViewHolder extends RecyclerView.ViewHolder{

            CircleImageView propic;
            TextView username,comment;
            public CommentsViewHolder(@NonNull View itemView) {
                super(itemView);

                propic=itemView.findViewById(R.id.commentpic);
                username=itemView.findViewById(R.id.usernamecomment);
                comment=itemView.findViewById(R.id.commenttext);
            }

        }

    }


}
