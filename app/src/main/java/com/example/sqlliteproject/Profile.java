package com.example.sqlliteproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView username,desc,email,phn,name,gender,birthday;
    CircleImageView propic;
    ImageView edit;
    String uid;
    RequestQueue requestQueue;
    private static final String PROFILE_URL="https://172.20.8.47/phpmyadmin/login/profile.php";

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
        View v= inflater.inflate(R.layout.fragment_profile, container, false);

        username=v.findViewById(R.id.user_name);
        email=v.findViewById(R.id.emailtext);
        name=v.findViewById(R.id.nametext);
        phn=v.findViewById(R.id.phone);
        desc=v.findViewById(R.id.description);
        propic=v.findViewById(R.id.ppic);
        edit=v.findViewById(R.id.editprofile);
        gender=v.findViewById(R.id.gender);
        birthday=v.findViewById(R.id.birthday);

        try {

            JSONObject jsonObject=new JSONObject((getActivity().getIntent().getStringExtra("json")));

            uid=jsonObject.getString("uid");


            StringRequest stringRequest=new StringRequest(Request.Method.POST, PROFILE_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject1=new JSONObject(response);
                        JSONArray jsonArray=jsonObject1.getJSONArray("details");
                        JSONObject jsonObject2=jsonArray.getJSONObject(0);

                        name.setText(jsonObject2.getString("name"));
                        username.setText(jsonObject2.getString("username"));
                        if(jsonObject2.getString("email").equals(""))
                        {
                            email.setVisibility(View.GONE);
                        }
                        email.setText(jsonObject2.getString("email"));
                        if(jsonObject2.getString("phone").equals("0"))
                        {
                            phn.setVisibility(View.GONE);
                        }
                        gender.setText(jsonObject2.getString("gender"));
                        Date date1=new SimpleDateFormat("MM/dd/yyyy").parse(jsonObject2.getString("birthday"));
                        SimpleDateFormat format=new SimpleDateFormat("dd MMMM yyyy");
                        String d=format.format(date1);
                        birthday.setText(d);
                        Picasso.get().load(jsonObject2.getString("image")).into(propic);
                        if(jsonObject2.getString("description").equals(""))
                        {
                            desc.setHint("Edit to add a description");
                            desc.setTextSize(15);
                        }
                        desc.setText(jsonObject2.getString("description"));


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
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String,String> params=new HashMap<>();
                    params.put("userid",uid);

                    return params;
                }
            };

            requestQueue= Volley.newRequestQueue(getActivity());
            requestQueue.add(stringRequest);



        } catch (JSONException e) {
            e.printStackTrace();
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(),EditProfile.class);
                i.putExtra("uid",uid);
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
}
