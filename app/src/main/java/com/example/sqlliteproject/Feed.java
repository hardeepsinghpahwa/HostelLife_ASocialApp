
package com.example.sqlliteproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Feed extends Fragment {


    ImageView add;
    private static final String SHOW_URL = PhpScripts.SHOW_URL;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final int RESULT_CROP = 400;
    String username;
    boolean alreadyexecuted=false;

    NestedScrollView scrollView;
    private String mParam1;
    private String mParam2;

    RecyclerView recyclerView;

    String profilepic;
    TextView noposts;
    String json;
    Toolbar toolbar;
    public String userid;
    SpinKitView progressBar;

    private OnFragmentInteractionListener mListener;

    public Feed() {
        // Required empty public constructor
    }

    public static Feed newInstance(String param1, String param2) {
        Feed fragment = new Feed();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void performCrop(String picUri) {
        try {
            //Start Crop Activity

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            File f = new File(picUri);
            Uri contentUri = Uri.fromFile(f);

            cropIntent.setDataAndType(contentUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 280);
            cropIntent.putExtra("outputY", 280);

            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, RESULT_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "your device doesn't support the crop action!";
            Toast toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        alreadyexecuted=false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_feed, container, false);


        recyclerView = v.findViewById(R.id.postsrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar=v.findViewById(R.id.feedprogressbar);
        scrollView=v.findViewById(R.id.scrollview);
        noposts=v.findViewById(R.id.feednoposts);
        toolbar=v.findViewById(R.id.toolbar);
        scrollView.setSmoothScrollingEnabled(true);
        scrollView.smoothScrollTo(6,6);


        ((AppCompatActivity)(getActivity())).setSupportActionBar(toolbar);
        ServiceManager serviceManager = new ServiceManager(getActivity());
        if (!serviceManager.isNetworkAvailable()) {
            startActivity(new Intent(getActivity(),NoInternet.class));
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        try {

            if(getActivity().getIntent().getStringExtra("json")==null)
            {
                SharedPreferences shared = getActivity().getSharedPreferences("Mypref", Context.MODE_PRIVATE);

                json=shared.getString("json","");

            }
            else {
                json=getActivity().getIntent().getStringExtra("json");
            }

            JSONObject jsonObject = new JSONObject(json);

            username = jsonObject.getString("username");
            profilepic = jsonObject.getString("image");
            userid = jsonObject.getString("uid");


            Log.i("uname", userid);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SHOW_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.i("res", response);

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("posts");

                    recyclerView.setAdapter(new AdapterClass(jsonArray, userid, getActivity()));

                    if(jsonArray.length()==0)
                    {
                        progressBar.setVisibility(View.GONE);
                        noposts.setVisibility(View.VISIBLE);
                    }

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
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);


        add = v.findViewById(R.id.addpost);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), NewPost.class);
                i.putExtra("userid", userid);
                i.putExtra("pp", profilepic);
                startActivity(i);


            }
        });


        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class AdapterClass extends RecyclerView.Adapter<AdapterClass.ViewHolderClass> {
        JSONArray jsonArray;
        String uid;
        RequestQueue requestQueue;
        Context context;
        String pid,likepid;
        int pos=0;


        private static final String LIKE_URL = PhpScripts.LIKE_URL;
        private static final String CHECK_URL = PhpScripts.CHECK_URL;
        private static final String COMMENT_URL = PhpScripts.COMMENT_URL;
        private static final String PROFILE_URL = PhpScripts.PROFILE_URL;


        AdapterClass(JSONArray array, String u, Context c) {
            jsonArray = array;
            uid = u;
            context = c;
        }

        @Override
        public void onViewAttachedToWindow(@NonNull ViewHolderClass holder) {
            super.onViewAttachedToWindow(holder);
            progressBar.setVisibility(View.GONE);
            if(!alreadyexecuted) {
                recyclerView.smoothScrollToPosition(pos);
                alreadyexecuted = true;
            }
        }

        @NonNull
        @Override
        public AdapterClass.ViewHolderClass onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_item, null);

            return new ViewHolderClass(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final AdapterClass.ViewHolderClass viewHolderClass, final int i) {

            try {

                final JSONObject object = jsonArray.getJSONObject(i);

                Log.i("posts", object.toString());

                String time = object.getString("date");
                String img = object.getString("image");
                String desc = object.getString("description");
                String loc = object.getString("location");
                String likes = object.getString("likes");
                String comments = object.getString("comments");
                final String userid1 = object.getString("userid");
                String num = String.valueOf(object.getInt("number"));

                StringRequest stringRequest2 = new StringRequest(Request.Method.POST, PROFILE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject1 = new JSONObject(response);
                            JSONArray jsonArray = jsonObject1.getJSONArray("details");
                            JSONObject jsonObject2 = jsonArray.getJSONObject(0);

                            viewHolderClass.username.setText(jsonObject2.getString("username"));

                            Picasso.get().load(jsonObject2.getString("image")).resize(200,200).into(viewHolderClass.propic);


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
                requestQueue = Volley.newRequestQueue(context);
                requestQueue.add(stringRequest2);


                String dateStart = time;

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
                        viewHolderClass.time.setText(diffDays + " days");
                    } else if (diffDays == 1) {
                        viewHolderClass.time.setText(diffDays + " day");
                    } else if (diffHours > 1) {
                        viewHolderClass.time.setText(diffHours + " hours ago");
                    } else if (diffHours == 1) {
                        viewHolderClass.time.setText(diffHours + " hour ago");
                    } else if (diffMinutes > 1) {
                        viewHolderClass.time.setText(diffMinutes + " minutes ago");
                    } else if (diffMinutes == 1) {
                        viewHolderClass.time.setText(diffMinutes + " minute ago");
                    } else if (diffSeconds > 1) {
                        viewHolderClass.time.setText(diffSeconds + " seconds ago");
                    } else if (diffDays == 1) {
                        viewHolderClass.time.setText(diffSeconds + " second ago");
                    }


                    Log.i("u", desc);

                    if (desc.equals("")) {
                        viewHolderClass.description.setVisibility(View.GONE);
                    }
                    viewHolderClass.description.setText(desc);
                    viewHolderClass.location.setText(loc);
                    viewHolderClass.no = num;

                    if ((Integer.parseInt(likes) > 1)) {
                        viewHolderClass.likes.setText(likes + " likes");
                    } else {
                        viewHolderClass.likes.setText(likes + " like");
                    }

                    if ((Integer.parseInt(comments) > 1)) {
                        viewHolderClass.comments.setText(comments + " comments");
                    } else {
                        viewHolderClass.comments.setText(comments + " comment");
                    }

                    Picasso.get().load(img).resize(500,500).into(viewHolderClass.postpic);



                    StringRequest stringRequest1 = new StringRequest(Request.Method.POST, CHECK_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject = null;

                            try {
                                jsonObject = new JSONObject(response);
                                final String success = jsonObject.getString("success");

                                if (success.equals("1")) {
                                    viewHolderClass.like.setChecked(true);
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
                            try {
                                JSONObject o = jsonArray.getJSONObject(i);
                                likepid = o.getString("uid");


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Map<String, String> params = new HashMap<>();
                            params.put("pid",likepid);
                            params.put("uid", uid);


                            return params;
                        }
                    };
                    stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest1);


                } catch (ParseException e) {
                    e.printStackTrace();
                }


                requestQueue = Volley.newRequestQueue(context);












                viewHolderClass.username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Intent intent=new Intent(getActivity(),ProfileDetails.class);
                        try {
                            intent.putExtra("loginuser",userid);
                            intent.putExtra("userid",object.getString("userid"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(intent);
                    }
                });






                viewHolderClass.postcomment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewHolderClass.postcomment.setText("Posting...");

                        try {
                            JSONObject object = jsonArray.getJSONObject(i);
                            pid = object.getString("uid");
                            Log.i("pid", pid);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        StringRequest request = new StringRequest(Request.Method.POST, COMMENT_URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                JSONObject jsonObject1 = null;
                                try {
                                    jsonObject1 = new JSONObject(response);
                                    String commentz = jsonObject1.getString("comments");
                                    String success = jsonObject1.getString("success");

                                    if (success.equals("1")) {
                                        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                                        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                                                InputMethodManager.HIDE_NOT_ALWAYS);

                                        Toast.makeText(getActivity(), "Comment Posted", Toast.LENGTH_SHORT).show();
                                        viewHolderClass.postcomment.setText("Post");
                                    }

                                    if ((Integer.parseInt(commentz) > 1)) {
                                        viewHolderClass.commenttext.setText("");
                                        viewHolderClass.commenttext.setFocusable(false);
                                        viewHolderClass.comments.setText(commentz + " comments");
                                    } else {
                                        viewHolderClass.comments.setText(commentz + " comment");
                                        viewHolderClass.commenttext.setText("");
                                        viewHolderClass.commenttext.setFocusable(false);
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
                                if (!viewHolderClass.commenttext.getText().toString().equals("")) {
                                    params.put("comment", viewHolderClass.commenttext.getText().toString());
                                } else {
                                    Toast.makeText(context, "Comment is empty", Toast.LENGTH_SHORT).show();
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


                viewHolderClass.comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent in = new Intent(context, ViewComments.class);
                        try {
                            JSONObject o = jsonArray.getJSONObject(i);
                            pid = o.getString("uid");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pos=i;
                        in.putExtra("userid", uid);
                        in.putExtra("postid", pid);
                        context.startActivity(in);
                        getActivity().overridePendingTransition(R.anim.slidein,R.anim.slideout);

                    }
                });






                viewHolderClass.like.setEventListener(new SparkEventListener() {
                    @Override
                    public void onEvent(ImageView button, boolean buttonState) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(i);
                            pid = object.getString("uid");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (buttonState) {
                            viewHolderClass.like.setClickable(false);
                            viewHolderClass.like.setEnabled(false);


                            StringRequest stringRequest = new StringRequest(Request.Method.POST, LIKE_URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    JSONObject jsonObject1 = null;
                                    try {
                                        jsonObject1 = new JSONObject(response);
                                        String likes = jsonObject1.getString("likes");
                                        if ((Integer.parseInt(likes) > 1)) {
                                            viewHolderClass.likes.setText(likes + " likes");
                                            viewHolderClass.like.setClickable(true);
                                            viewHolderClass.like.setEnabled(true);

                                        } else {
                                            viewHolderClass.likes.setText(likes + " like");
                                            viewHolderClass.like.setClickable(true);
                                            viewHolderClass.like.setEnabled(true);

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
                                    params.put("user_id", uid);
                                    params.put("post_id", pid);

                                    return params;
                                }

                            };
                            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                    30000,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            requestQueue.add(stringRequest);

                        }
                        else{
                            viewHolderClass.like.setClickable(false);
                            viewHolderClass.like.setEnabled(false);

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, LIKE_URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    JSONObject jsonObject1 = null;
                                    try {
                                        jsonObject1 = new JSONObject(response);
                                        String likes = jsonObject1.getString("likes");
                                        if ((Integer.parseInt(likes) > 1)) {
                                            viewHolderClass.likes.setText(likes + " likes");
                                            viewHolderClass.like.setClickable(true);
                                            viewHolderClass.like.setEnabled(true);

                                        } else {
                                            viewHolderClass.likes.setText(likes + " like");
                                            viewHolderClass.like.setClickable(true);
                                            viewHolderClass.like.setEnabled(true);

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
                                    params.put("user_id", uid);
                                    params.put("post_id", pid);

                                    return params;
                                }

                            };  stringRequest.setRetryPolicy(new DefaultRetryPolicy(
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





                /*
                viewHolderClass.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(i);
                            pid = object.getString("uid");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, CHECK_URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response);
                                    final String success = jsonObject.getString("success");

                                    if (success.equals("1")) {
                                        StringRequest stringRequest = new StringRequest(Request.Method.POST, LIKE_URL, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                JSONObject jsonObject1 = null;
                                                try {
                                                    jsonObject1 = new JSONObject(response);
                                                    String likes = jsonObject1.getString("likes");
                                                    if ((Integer.parseInt(likes) > 1)) {
                                                        viewHolderClass.likes.setText(likes + " likes");
                                                        viewHolderClass.like.pressOnTouch(true);
                                                    } else {
                                                        viewHolderClass.likes.setText(likes + " like");
                                                        viewHolderClass.like.pressOnTouch(true);
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
                                                params.put("success", success);
                                                params.put("user_id", uid);
                                                params.put("post_id", pid);

                                                return params;
                                            }

                                        };  stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                30000,
                                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                        requestQueue.add(stringRequest);

                                    } else if (success.equals("0")) {
                                        StringRequest stringRequest = new StringRequest(Request.Method.POST, LIKE_URL, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                JSONObject jsonObject1 = null;
                                                try {
                                                    jsonObject1 = new JSONObject(response);
                                                    String likes = jsonObject1.getString("likes");
                                                    if ((Integer.parseInt(likes) > 1)) {
                                                        viewHolderClass.likes.setText(likes + " likes");
                                                        viewHolderClass.like.pressOnTouch(true);
                                                    } else {
                                                        viewHolderClass.likes.setText(likes + " like");
                                                        viewHolderClass.like.pressOnTouch(true);
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
                                                params.put("success", success);
                                                params.put("user_id", uid);
                                                params.put("post_id", pid);

                                                return params;
                                            }

                                        };
                                        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                30000,
                                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                        requestQueue.add(stringRequest);

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
                                params.put("pid", pid);
                                params.put("uid", uid);


                                return params;
                            }
                        };  stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                                30000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        requestQueue.add(stringRequest1);

                    }
                });*/


                viewHolderClass.comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewHolderClass.comment.requestFocus();
                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return jsonArray.length();
        }

        class ViewHolderClass extends RecyclerView.ViewHolder {

            TextView username, time, description, location, likes, comments;
            ImageView postpic;
            EditText commenttext;
            CircleImageView propic;
            Button postcomment;
            ImageButton comment;
            SparkButton like;
            String no;

            public ViewHolderClass(@NonNull View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.postusername);
                time = itemView.findViewById(R.id.posttime);
                description = itemView.findViewById(R.id.postdesc);
                location = itemView.findViewById(R.id.postlocation);
                postpic = itemView.findViewById(R.id.postpic);
                propic = itemView.findViewById(R.id.propic);
                like = itemView.findViewById(R.id.like);
                comment = itemView.findViewById(R.id.comment);
                commenttext = itemView.findViewById(R.id.commenttext);
                likes = itemView.findViewById(R.id.likesno);
                comments = itemView.findViewById(R.id.commentsno);
                postcomment = itemView.findViewById(R.id.postcomment);
            }
        }

    }


}
