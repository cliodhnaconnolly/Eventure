package com.example.spitegirls.eventme;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import static android.view.View.GONE;

public class EventDetailsFragment extends Fragment{

    private HashMap<String, String> details;

    public static EventDetailsFragment newInstance(Bundle details) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        if(details != null){
            fragment.setArguments(details);
            Log.d("INPUT TO ", details.toString());
            Log.d("SET ARGS", fragment.getArguments().toString());

        }
        return fragment;
    }

    public EventDetailsFragment() {

    }


    // http://stackoverflow.com/questions/32201453/hashmap-as-param-for-a-fragment
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();
            if(bundle.getSerializable("hashmap") != null){
                details = (HashMap<String, String>) bundle.getSerializable("hashmap");

                // Set up info
                TextView title = (TextView) view.findViewById(R.id.title);
                title.setText(details.get("name"));
                TextView description = (TextView) view.findViewById(R.id.description);
                description.setText(details.get("description"));

                TextView startTime = (TextView) view.findViewById(R.id.start_time_details);
                if(!details.get("startTime").isEmpty()){
                    startTime.setText(details.get("startTime"));
                } else { startTime.setVisibility(View.GONE); }

                TextView city = (TextView) view.findViewById(R.id.city_details);
                if(!details.get("city").isEmpty()){
                    city.setText(details.get("city"));
                } else { city.setVisibility(View.GONE); }

                TextView country = (TextView) view.findViewById(R.id.country_details);
                if(!details.get("country").isEmpty()){
                    country.setText(details.get("country"));
                } else { country.setVisibility(View.GONE); }

                TextView latitude = (TextView) view.findViewById(R.id.latitude_details);
                if(!details.get("latitude").isEmpty()){
                    latitude.setText(details.get("latitude"));
                } else { latitude.setVisibility(View.GONE); }

                TextView longitude = (TextView) view.findViewById(R.id.longitude_details);
                if(!details.get("longitude").isEmpty()){
                    longitude.setText(details.get("longitude"));
                } else { longitude.setVisibility(View.GONE); }

                TextView source = (TextView) view.findViewById(R.id.source_details);
                if(!details.get("source").isEmpty()){
                    source.setText(details.get("source"));
                } else { source.setVisibility(View.GONE); }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }
}
