package com.example.spitegirls.eventme;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
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

            // Get passed constant from arguments to see which Error display is required
            Bundle bundle = this.getArguments();
            error = bundle.getInt("appearance");

            if(error == NO_INTERNET_APPEARANCE){
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
                        LocationManager locationManager = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

                        // If issue has been rectified restart activity now with issue sorted
                        if(error == NO_LOCATION_APPEARANCE && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            getActivity().finish();
                            final Intent intent = getActivity().getIntent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                            getActivity().startActivity(intent);
                        }

                        else if(error == NO_INTERNET_APPEARANCE && (connectivityManager.getActiveNetworkInfo() != null &&
                                connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting())){
                            getActivity().finish();
                            final Intent intent = getActivity().getIntent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                            getActivity().startActivity(intent);
                        }
                    }
                }
            });

        } else {
            // If no arguments given it is a location permissions error
            tapToRetry.setVisibility(View.GONE);
            noLocationImage.setVisibility(View.VISIBLE);
            noLocationText.setVisibility(View.VISIBLE);
        }

    }

}
