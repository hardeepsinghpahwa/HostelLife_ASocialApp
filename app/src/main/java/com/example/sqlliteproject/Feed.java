
package com.example.sqlliteproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import
        android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.franmontiel.fullscreendialog.FullScreenDialogFragment;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.transitionseverywhere.extra.Scale;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class Feed extends Fragment {


    ImageView add;
    private static final String SHOW_URL = PhpScripts.SHOW_URL;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final int RESULT_CROP = 400;
    FaceGraphic mFaceGraphic;
    String username;
    boolean alreadyexecuted = false;
    Float va;
    int pos=0;
    SparkButton sparkButton;
    NestedScrollView scrollView;
    private String mParam1;
    private String mParam2;

    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    //=
    RecyclerView recyclerView;
    ShimmerFrameLayout shimmerFrameLayout;
    String profilepic;
    TextView noposts;
    String json;
    LinearLayoutManager linearLayoutManager;
    Toolbar toolbar;
    TextView posi;
    public String userid;
    SpinKitView progressBar;

    private OnFragmentInteractionListener mListener;

    public Feed() {
        // Required empty public constructor
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getActivity().finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Face Tracker sample")
                .setMessage("permission")
                .setPositiveButton("ok", listener)
                .show();
    }
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
        alreadyexecuted = false;
        if (getActivity().getIntent().getStringExtra("json") == null) {
            SharedPreferences shared = getActivity().getSharedPreferences("Mypref", Context.MODE_PRIVATE);

            json = shared.getString("json", "");

        } else {
            json = getActivity().getIntent().getStringExtra("json");
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            username = jsonObject.getString("username");
            profilepic = jsonObject.getString("image");
            userid = jsonObject.getString("uid");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.i("uname", userid);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SHOW_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.i("res", response);

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("posts");

                    recyclerView.setAdapter(new AdapterClass(jsonArray, userid, getActivity()));

                    if (jsonArray.length() == 0) {
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

    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(getActivity(), permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, "permission",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("ok", listener)
                .show();
    }


    private void createCameraSource() {

        Context context = getContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_feed, container, false);


        recyclerView = v.findViewById(R.id.postsrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar = v.findViewById(R.id.feedprogressbar);
        scrollView = v.findViewById(R.id.scrollview);
        noposts = v.findViewById(R.id.feednoposts);
        toolbar = v.findViewById(R.id.toolbar);
        scrollView.setSmoothScrollingEnabled(true);
        scrollView.smoothScrollTo(6, 6);
        shimmerFrameLayout=v.findViewById(R.id.shimmer_view_container);
        posi=v.findViewById(R.id.posi);


        mPreview =  v.findViewById(R.id.preview);
        mGraphicOverlay =  v.findViewById(R.id.faceOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

        shimmerFrameLayout.startShimmerAnimation();
        ((AppCompatActivity) (getActivity())).setSupportActionBar(toolbar);
        ServiceManager serviceManager = new ServiceManager(getActivity());
        if (!serviceManager.isNetworkAvailable()) {
            startActivity(new Intent(getActivity(), NoInternet.class));
        }


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        try {

            if (getActivity().getIntent().getStringExtra("json") == null) {
                SharedPreferences shared = getActivity().getSharedPreferences("Mypref", Context.MODE_PRIVATE);

                json = shared.getString("json", "");

            } else {
                json = getActivity().getIntent().getStringExtra("json");
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

                    if (jsonArray.length() == 0) {
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
        linearLayoutManager = ((LinearLayoutManager)recyclerView.getLayoutManager());
        final int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();


        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                posi.setText(String.valueOf(position));
                pos=position;

                View v=linearLayoutManager.findViewByPosition(pos);
                Log.i("pos", String.valueOf(pos));

                SparkButton sparkButton=v.findViewById(R.id.like);

                Log.i("check", String.valueOf(sparkButton.isChecked()));

                posi.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        }));

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

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            View v=linearLayoutManager.findViewByPosition(pos);
            Log.i("pos", String.valueOf(pos));

            sparkButton=v.findViewById(R.id.like);

            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            va=mFaceGraphic.smileval();


            Log.i("check", String.valueOf(sparkButton.isChecked()));
            if(!sparkButton.isChecked() && va>0.5)
            {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        sparkButton.performClick();

                    }
                });
            }
            /*if(va>0.5 && pos==5)
            {
                Toast.makeText(getActivity(), "Pos "+pos+" Acc "+va, Toast.LENGTH_SHORT).show();
                View v=linearLayoutManager.findViewByPosition(pos);

                v.findViewById(R.id.like).performClick();
            }*/
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }


        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

    class AdapterClass extends RecyclerView.Adapter<AdapterClass.ViewHolderClass> {
        JSONArray jsonArray;
        String uid;
        RequestQueue requestQueue;
        Context context;
        String pid, likepid;
        int pos = 0;


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
            shimmerFrameLayout.stopShimmerAnimation();
            shimmerFrameLayout.setVisibility(View.GONE);
            if (!alreadyexecuted) {
                recyclerView.smoothScrollToPosition(pos);
                alreadyexecuted = true;
            }
        }

        @NonNull
        @Override
        public AdapterClass.ViewHolderClass onCreateViewHolder(@NonNull final ViewGroup viewGroup, int i) {
            final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_item, null);
            return new ViewHolderClass(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final AdapterClass.ViewHolderClass viewHolderClass, final int i) {


            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //Toast.makeText(context, ""+linearLayoutManager.findFirstVisibleItemPosition(), Toast.LENGTH_SHORT).show();
                }
            });
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

                            Glide.with(context).load(jsonObject2.getString("image")).diskCacheStrategy(DiskCacheStrategy.ALL).override(200, 200).into(viewHolderClass.propic);

                            Glide.with(context).load(jsonObject2.getString("image")).diskCacheStrategy(DiskCacheStrategy.ALL).override(100, 100).into(viewHolderClass.propic2);

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

                    Glide.with(getActivity()).load(img).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(viewHolderClass.postpic);


                    viewHolderClass.postcomment.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.RollIn)
                            .duration(700)
                            .playOn(viewHolderClass.postcomment);

                    viewHolderClass.like.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.RollIn)
                            .duration(700)
                            .playOn(viewHolderClass.like);
                    viewHolderClass.comment.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.RollIn)
                            .duration(700)
                            .playOn(viewHolderClass.comment);
                    viewHolderClass.commenttext.setVisibility(View.VISIBLE);

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

                    //Picasso.get().load(img).resize(500,500).into(viewHolderClass.postpic);



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
                            params.put("pid", likepid);
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

                viewHolderClass.username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(getActivity(), ProfileDetails.class);
                        try {
                            intent.putExtra("loginuser", userid);
                            intent.putExtra("userid", object.getString("userid"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(intent);
                    }
                });


                viewHolderClass.postcomment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (viewHolderClass.commenttext.getText().toString().equals("")) {

                            Toast toast = Toast.makeText(context, "Empty Comment", Toast.LENGTH_SHORT);
                            View view = toast.getView();
                            view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            TextView text = (TextView) view.findViewById(android.R.id.message);
                            text.setTextColor(getActivity().getColor(R.color.colorPrimary));
                            toast.show();

                        } else {

                            viewHolderClass.postcomment.setVisibility(View.INVISIBLE);
                            viewHolderClass.spin.setVisibility(View.VISIBLE);

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

                                            Toast toast = Toast.makeText(context, "Commemnt Posted", Toast.LENGTH_SHORT);
                                            View view = toast.getView();
                                            view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                            TextView text = (TextView) view.findViewById(android.R.id.message);
                                            text.setTextColor(getActivity().getColor(R.color.colorPrimary));
                                            toast.show();
                                            viewHolderClass.postcomment.setVisibility(View.VISIBLE);
                                            viewHolderClass.spin.setVisibility(View.GONE);
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
                                        Toast toast = Toast.makeText(context, "Empty Comment", Toast.LENGTH_SHORT);
                                        View view = toast.getView();
                                        view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                        TextView text = (TextView) view.findViewById(android.R.id.message);
                                        text.setTextColor(getActivity().getColor(R.color.colorPrimary));
                                        toast.show();                                    }
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


                viewHolderClass.comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        FullScreenDialogFragment dialogFragment;
                        /*Intent in = new Intent(context, ViewComments.class);
                        try {
                            JSONObject o = jsonArray.getJSONObject(i);
                            pid = o.getString("uid");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pos = i;
                        in.putExtra("userid", uid);
                        in.putExtra("postid", pid);
                        context.startActivity(in);
                        getActivity().overridePendingTransition(R.anim.slidein, R.anim.slideout);*/

                        Bundle bundle = new Bundle();
                        new FullScreenDialogFragment.Builder(getActivity())
                                .setTitle("Comments")
                                .setContent(CommentFrag.class, bundle)
                                .build();

                        dialogFragment = new FullScreenDialogFragment.Builder(getActivity())
                                .setTitle("hi")
                                .build();

                        dialogFragment.show(getFragmentManager(), "dialog");

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

                        } else {
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
            CircleImageView propic, propic2;
            ImageView postcomment;
            ImageButton comment;
            SparkButton like;
            String no;
            SpinKitView spin;

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
                propic2 = itemView.findViewById(R.id.propic2);
                spin=itemView.findViewById(R.id.commentspin);
            }
        }

    }


}
