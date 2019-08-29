package com.example.sqlliteproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.ybq.android.spinkit.SpinKitView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Comments extends DialogFragment {

    public static final String TAG = "example_dialog";

    RecyclerView comments;
    String pid, uid;
    EditText commenttext;
    ImageView comment;
    ShimmerFrameLayout shimmer;
    RequestQueue requestQueue;
    TextView nocomm;
    NestedScrollView nestedScrollView;
    SpinKitView spinKitView;
    String postid;
    CommentsAdapter adapter;
    private static final String COMMENTS_URL = PhpScripts.COMMENTS_URL;
    private static final String COMMENT_URL = PhpScripts.COMMENT_URL;

    public static Comments display(FragmentManager fragmentManager,String postid,String uid) {

        Bundle args = new Bundle();
        args.putString("uid", uid);
        args.putString("postid",postid);
        Comments exampleDialog = new Comments();
        exampleDialog.setArguments(args);
        exampleDialog.show(fragmentManager, TAG);
        return exampleDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);

    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Bundle args=getArguments();

        pid = args.getString("postid","");
        uid = args.getString("uid","");
        Log.i("pid",pid);
        Log.i("uid",uid);

        View view = inflater.inflate(R.layout.comments_dialog, container, false);
        try {
            final Dialog dialog = getDialog();

            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }


            });

            requestQueue = Volley.newRequestQueue(getActivity());
            ServiceManager serviceManager = new ServiceManager(getActivity());
            if (!serviceManager.isNetworkAvailable()) {
                startActivity(new Intent(getActivity(), NoInternet.class));
            }
            comments = view.findViewById(R.id.commentsrecyclerview);
            comments.setLayoutManager(new LinearLayoutManager(getActivity()));
            comment =  view.findViewById(R.id.postcomment1);
            commenttext =  view.findViewById(R.id.commenttext1);
            shimmer =  view.findViewById(R.id.commentspbar);
            nocomm =  view.findViewById(R.id.nocomments);
            shimmer.startShimmer();
            nestedScrollView=view.findViewById(R.id.nestedcommentsview);
            spinKitView=view.findViewById(R.id.commentspin2);

            nestedScrollView.setSmoothScrollingEnabled(true);
            nestedScrollView.smoothScrollTo(4,4);
            comments.setFocusable(false);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, COMMENTS_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("comments");

                        Log.i("arr", String.valueOf(jsonArray));

                        adapter = new CommentsAdapter(jsonArray, getActivity());
                        comments.setAdapter(adapter);


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
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<>();
                    params.put("pid", pid);

                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(stringRequest);


            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (commenttext.getText().toString().equals("")) {

                        Toast toast = Toast.makeText(getActivity(), "Empty Comment", Toast.LENGTH_SHORT);
                        toast.show();
                        commenttext.clearFocus();

                    } else {

                        comment.setVisibility(View.INVISIBLE);
                        spinKitView.setVisibility(View.VISIBLE);


                        StringRequest request = new StringRequest(Request.Method.POST, COMMENT_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            JSONObject jsonObject1 = null;
                            try {
                                jsonObject1 = new JSONObject(response);
                                String success = jsonObject1.getString("success");

                                if (success.equals("1")) {
                                    commenttext.clearFocus();
                                    Toast.makeText(getActivity(), "Comment Posted", Toast.LENGTH_SHORT).show();
                                    comment.setVisibility(View.VISIBLE);
                                    spinKitView.setVisibility(View.INVISIBLE);
                                    commenttext.setText("");
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, COMMENTS_URL, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            JSONObject jsonObject = null;
                                            try {
                                                jsonObject = new JSONObject(response);
                                                JSONArray jsonArray = jsonObject.getJSONArray("comments");

                                                adapter = new CommentsAdapter(jsonArray, getActivity());
                                                comments.setAdapter(adapter);


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
                                        protected Map<String, String> getParams() {

                                            Map<String, String> params = new HashMap<>();
                                            params.put("pid", pid);

                                            return params;
                                        }
                                    };
                                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                            30000,
                                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                    requestQueue.add(stringRequest);
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
                                Toast.makeText(getActivity(), "Comment is empty", Toast.LENGTH_SHORT).show();
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
            }
            });



        } catch (Exception e) {
            Log.i("exception", e.toString());
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

        JSONArray array;
        Context context;
        private static final String PROFILE_URL = PhpScripts.PROFILE_URL;


        CommentsAdapter(JSONArray jsonArray, Context c) {
            context = c;
            array = jsonArray;
            if (array.length() == 0) {
                shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);
                nocomm.setVisibility(View.VISIBLE);
            }
        }

        @NonNull
        @Override
        public CommentsAdapter.CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment, null);
            return new CommentsAdapter.CommentsViewHolder(v);
        }

        @Override
        public void onViewAttachedToWindow(@NonNull CommentsAdapter.CommentsViewHolder holder) {
            super.onViewAttachedToWindow(holder);

        }

        @Override
        public void onBindViewHolder(@NonNull final CommentsAdapter.CommentsViewHolder commentsViewHolder, int i) {

            Log.i("len", String.valueOf(array.length()));

            try {
                final JSONObject o = array.getJSONObject(i);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, PROFILE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject1 = null;
                        try {
                            jsonObject1 = new JSONObject(response);
                            JSONArray jsonArray = jsonObject1.getJSONArray("details");
                            JSONObject jsonObject2 = jsonArray.getJSONObject(0);

                            commentsViewHolder.username.setText(jsonObject2.getString("username"));
                            Log.i("img", jsonObject2.getString("image"));
                            //Picasso.get().load(jsonObject2.getString("image")).into(commentsViewHolder.propic);

                            Glide.with(context).load(jsonObject2.getString("image")).diskCacheStrategy(DiskCacheStrategy.ALL).override(100, 100).into(commentsViewHolder.propic);


                            commentsViewHolder.comment.setText(o.getString("comment"));
                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);
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
                        try {
                            params.put("userid", o.getString("user_id"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return params;
                    }

                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                requestQueue.add(stringRequest);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return array.length();
        }

        public class CommentsViewHolder extends RecyclerView.ViewHolder {

            CircleImageView propic;
            TextView username, comment;

            public CommentsViewHolder(@NonNull View itemView) {
                super(itemView);

                propic = itemView.findViewById(R.id.commentpic);
                username = itemView.findViewById(R.id.usernamecomment);
                comment = itemView.findViewById(R.id.commenttext);
            }

        }

    }
}
