package com.example.spitegirls.eventme;

import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
            if(bundle.getInt("appearance") != NO_INTERNET_APPEARANCE){
                noInternetText.setVisibility(View.INVISIBLE);
                noInternetImage.setVisibility(View.INVISIBLE);
            } else {    // Implies No Internet
                noLocationImage.setVisibility(View.INVISIBLE);
                noLocationText.setVisibility(View.INVISIBLE);
            }
        }


        // Set up button listener here
        tapToRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do Stuff

                // Check internet / location again
                    // If there is thing then restart activity
                    // (or maybe something else but this would solve our opening problem)

                    // if there is no change maintain screen
                    // They can tap again
            }
        });
    }

}
