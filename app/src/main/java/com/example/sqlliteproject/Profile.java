package com.example.sqlliteproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class Profile extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView username,username2, desc, email, phn, name, birthday, noposts;
    CircleImageView propic;
    ImageView edit,back;
    LinearLayout me,about,info;
    AlertDialog alertDialog;
    String uid, json;
    NestedScrollView nestedScrollView;
    TextView logout,followers,following,postsno;
    SpinKitView progressBar,recpbar;
    AppBarLayout appBar;
    SharedPreferences shared;
    RecyclerView posts;
    Toolbar toolbar;
    LinearLayout l1;
    RequestQueue requestQueue;
    private static final String PROFILE_URL = PhpScripts.PROFILE_URL;
    private static final String GETPOSTSURL = PhpScripts.GETPOSTSURL;

    private OnFragmentInteractionListener mListener;

    public Profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Profile.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_profile, container, false);

        ServiceManager serviceManager = new ServiceManager(getActivity());
        if (!serviceManager.isNetworkAvailable()) {
            startActivity(new Intent(getActivity(), NoInternet.class));
        }

        username = v.findViewById(R.id.user_name);
        email = v.findViewById(R.id.emailtext);
        name = v.findViewById(R.id.nametext);
        phn = v.findViewById(R.id.phone);
        desc = v.findViewById(R.id.description);
        propic = v.findViewById(R.id.ppic);
        edit = v.findViewById(R.id.editprofile);
        birthday = v.findViewById(R.id.birthday);
        noposts = v.findViewById(R.id.nopostsyet);
        posts = v.findViewById(R.id.profileposts);
        logout = v.findViewById(R.id.logout);
        progressBar = v.findViewById(R.id.profileprogressbar);
        appBar=v.findViewById(R.id.app_bar);
        recpbar=v.findViewById(R.id.spin_kit);
        toolbar=v.findViewById(R.id.toolbarprofile);
        l1=v.findViewById(R.id.l1);
        followers=v.findViewById(R.id.followers);
        following=v.findViewById(R.id.following);
        postsno=v.findViewById(R.id.postsno);
        username2=v.findViewById(R.id.user_name2);
        me=v.findViewById(R.id.l1);
        about=v.findViewById(R.id.about);
        info=v.findViewById(R.id.info);
        back=v.findViewById(R.id.back);
        nestedScrollView=v.findViewById(R.id.profilenestedscroll);

        loaddata();

        AnimationDrawable animationDrawable = (AnimationDrawable) back.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        nestedScrollView.setSmoothScrollingEnabled(true);
        nestedScrollView.smoothScrollTo(4,4);

        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    username.setVisibility(View.VISIBLE);
                    l1.setVisibility(View.INVISIBLE);
                    logout.setVisibility(View.VISIBLE);
                    toolbar.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn)
                            .duration(700)
                            .playOn(toolbar);
                } else if (isShow) {
                    isShow = false;
                    username.setVisibility(View.INVISIBLE);
                    l1.setVisibility(View.VISIBLE);
                    logout.setVisibility(View.INVISIBLE);
                    toolbar.setVisibility(View.GONE);

                }
            }
        });

        shared = getActivity().getSharedPreferences("Mypref", Context.MODE_PRIVATE);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog = new SpotsDialog.Builder()
                                .setContext(getActivity())
                                .setMessage("Logging out")
                                .setCancelable(false)
                                .setTheme(R.style.Custom)
                                .build();
                        alertDialog.show();
                        SharedPreferences.Editor editor = shared.edit();
                        editor.clear();
                        editor.commit();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                alertDialog.dismiss();
                                getActivity().finish();
                                Toast.makeText(getActivity(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();

                            }
                        }, 2000);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        builder.create().dismiss();
                    }
                }).setMessage("Are you sure to logout");

                builder.create().show();

            }
        });


        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditProfile.class);
                i.putExtra("uid", uid);
                startActivityForResult(i,12);
                getActivity().overridePendingTransition(R.anim.fadein,R.anim.fadeout);
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==12)
        {
            if (resultCode == Activity.RESULT_OK) {

                // Get the result from the returned Intent
                int ok = data.getIntExtra("ok",0);

                if(ok==1)
                {
                    loaddata();
                }

            }
        }
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

    private void loaddata(){
        if (getActivity().getIntent().getStringExtra("json") == null) {
            SharedPreferences shared = getActivity().getSharedPreferences("Mypref", Context.MODE_PRIVATE);

            json = shared.getString("json", "");

        } else {
            json = getActivity().getIntent().getStringExtra("json");
        }


        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);

            uid = jsonObject.getString("uid");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringRequest stringRequest1 = new StringRequest(Request.Method.POST, GETPOSTSURL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i("res", response);

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("posts");

                            if (jsonArray.length() == 0) {
                                progressBar.setVisibility(View.GONE);
                                noposts.setVisibility(View.VISIBLE);
                            }

                            posts.setAdapter(new PostAdapter1(jsonArray, uid, getActivity()));


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
                ) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {

                        Map<String, String> params = new HashMap<>();
                        params.put("userid", uid);

                        return params;
                    }
                };
                stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                        30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                StringRequest stringRequest = new StringRequest(Request.Method.POST, PROFILE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        recpbar.setVisibility(View.VISIBLE);
                        try {
                            JSONObject jsonObject1 = new JSONObject(response);
                            JSONArray jsonArray = jsonObject1.getJSONArray("details");
                            JSONObject jsonObject2 = jsonArray.getJSONObject(0);
                            requestQueue.add(stringRequest1);

                            back.setVisibility(View.VISIBLE);
                            name.setText(jsonObject2.getString("name"));
                            username.setText(jsonObject2.getString("username"));
                            username2.setText("@"+jsonObject2.getString("username"));

                            if (jsonObject2.getString("email").equals("")) {
                                email.setVisibility(View.GONE);
                            }
                            email.setText(jsonObject2.getString("email"));
                            if (jsonObject2.getString("phone").equals("0")) {
                                phn.setVisibility(View.GONE);
                            }
                            Date date1 = new SimpleDateFormat("MM/dd/yyyy").parse(jsonObject2.getString("birthday"));
                            SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
                            String d = format.format(date1);
                            birthday.setText(d);
                            Glide.with(getActivity()).load(jsonObject2.getString("image")).into(propic);

                            propic.setVisibility(View.VISIBLE);


                            YoYo.with(Techniques.FlipInX)
                                    .duration(700)
                                    .playOn(propic);
                            if (jsonObject2.getString("description").equals("")) {
                                desc.setHint("Edit to add a description");
                                desc.setTextSize(15);
                            }
                            desc.setText(jsonObject2.getString("description"));
                            me.setVisibility(View.VISIBLE);

                            YoYo.with(Techniques.FadeInUp)
                                    .duration(700)
                                    .playOn(about);
                            YoYo.with(Techniques.FadeInUp)
                                    .duration(700)
                                    .playOn(info);
                            back.setFocusable(false);
                            about.setVisibility(View.VISIBLE);
                            info.setVisibility(View.VISIBLE);
                            edit.setVisibility(View.VISIBLE);
                            edit.setFocusable(true);
                            edit.bringToFront();

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
                        params.put("userid", uid);

                        return params;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                requestQueue = Volley.newRequestQueue(getActivity());
                requestQueue.add(stringRequest);

            }
        }).start();

        posts.setLayoutManager(new GridLayoutManager(getActivity(),3));

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    class PostAdapter1 extends RecyclerView.Adapter<PostAdapter1.PostViewHolder1> {

        JSONArray jsonArray;
        String uid, postid;
        Context context;

        public PostAdapter1(JSONArray jsonArray, String uid, Context context) {
            this.jsonArray = jsonArray;
            this.uid = uid;
            this.context = context;
        }

        @Override
        public void onViewAttachedToWindow(@NonNull PostViewHolder1 holder) {
            super.onViewAttachedToWindow(holder);
            recpbar.setVisibility(View.GONE);
        }

        @NonNull
        @Override
        public PostAdapter1.PostViewHolder1 onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profilepost, null);
            return new PostAdapter1.PostViewHolder1(v);

        }

        @Override
        public void onBindViewHolder(@NonNull PostAdapter1.PostViewHolder1 postViewHolder, final int i) {

            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //Picasso.get().load(jsonObject.getString("image")).placeholder(R.drawable.greyround).into(postViewHolder.postimage);
                Glide.with(getActivity()).load(jsonObject.getString("image")).placeholder(R.drawable.greyround).into(postViewHolder.postimage);
                postid = jsonObject.getString("uid");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            postViewHolder.postimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        JSONObject object = jsonArray.getJSONObject(i);

                        Intent intent = new Intent(getActivity(), ViewPost.class);
                        intent.putExtra("loginuser", uid);
                        intent.putExtra("postid", object.getString("uid"));
                        startActivity(intent);
                        CustomIntent.customType(getActivity(),"left-to-right");

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

        public class PostViewHolder1 extends RecyclerView.ViewHolder {
            ImageView postimage;

            public PostViewHolder1(@NonNull View itemView) {
                super(itemView);

                postimage = itemView.findViewById(R.id.postimg);

            }
        }
    }


}
