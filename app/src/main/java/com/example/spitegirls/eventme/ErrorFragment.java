package com.example.spitegirls.eventme;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class ErrorFragment extends Fragment {

    public final static int NO_INTERNET_APPEARANCE = 0;
    public final static int NO_LOCATION_APPEARANCE = 1;

    private TextView noInternetText;
    private ImageView noInternetImage;
    private TextView noLocationText;
    private ImageView noLocationImage;
    private Button tapToRetry;
    private Integer error = null;

    public static ErrorFragment newInstance(Bundle bundle) {
        ErrorFragment fragment =  new ErrorFragment();
        if(bundle != null){
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    public ErrorFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
          View view = inflater.inflate(R.layout.fragment_error, container, false);

        noInternetImage = (ImageView) view.findViewById(R.id.image_view_internet);
        noInternetText = (TextView) view.findViewById(R.id.text_view_no_internet);

        noLocationImage = (ImageView) view.findViewById(R.id.image_view_location);
        noLocationText = (TextView) view.findViewById(R.id.text_view_no_location);

        tapToRetry = (Button) view.findViewById(R.id.tap_to_retry);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();
            error = bundle.getInt("appearance");
            Log.d("WHAT YA PASSING", error.toString());
            if(error == NO_INTERNET_APPEARANCE){
                Log.d("GETTIN IN THE IF", error.toString());
                noInternetText.setVisibility(View.VISIBLE);
                noInternetImage.setVisibility(View.VISIBLE);
            } else if(error == NO_LOCATION_APPEARANCE){
                noLocationImage.setVisibility(View.VISIBLE);
                noLocationText.setVisibility(View.VISIBLE);
            }

            // Set up button listener here
            tapToRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(error != null){
                        LocationManager loc = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));
                        ConnectivityManager con =
                                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

                        if(error == NO_LOCATION_APPEARANCE && loc.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            getActivity().finish();
                            final Intent intent = getActivity().getIntent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                            getActivity().startActivity(intent);
                        }

                        else if(error == NO_INTERNET_APPEARANCE && (con.getActiveNetworkInfo() != null &&
                                con.getActiveNetworkInfo().isConnectedOrConnecting())){
                            Log.d(" AM I FUCKING HERE", error.toString());
                            getActivity().finish();
                            final Intent intent = getActivity().getIntent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                            getActivity().startActivity(intent);
                        }
                    }
                }
            });

        } else {
            tapToRetry.setVisibility(View.GONE);
            noLocationImage.setVisibility(View.VISIBLE);
            noLocationText.setVisibility(View.INVISIBLE);
        }

        // ELSE
        // (NO ARGUMENTS MEAN NO TAP TO RETRY BUT YES LOCATION
        // {
        // tapToRetry.setVisibility(View.GONE)
        // noLocationImage.setVisibility(View.VISIBLE)
        // noLocationText.setVisibility(View.VISIBLE)
        // return; if possibly otherwise doesnt matter, we can have onClickListener OR Move onCLickListener to inside if(this.getArgs != null)
        // }

//        // Set up button listener here
//        tapToRetry.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(error != null){
//                    LocationManager loc = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));
//                    ConnectivityManager con =
//                            (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
//
//                    if(error == NO_LOCATION_APPEARANCE && loc.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//                        getActivity().finish();
//                        final Intent intent = getActivity().getIntent();
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
//                        getActivity().startActivity(intent);
//                    }
//
//                    else if(error == NO_INTERNET_APPEARANCE && (con.getActiveNetworkInfo() != null &&
//                            con.getActiveNetworkInfo().isConnectedOrConnecting())){
//                        Log.d(" AM I FUCKING HERE", error.toString());
//                        getActivity().finish();
//                        final Intent intent = getActivity().getIntent();
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
//                        getActivity().startActivity(intent);
//                    }
//                }
//            }
//        });
    }

}
