package com.example.spitegirls.eventme;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

// Example used is
// https://www.learn2crack.com/2013/10/android-custom-listview-images-text-example.html

// Combined with
// http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view

public class CustomListAdapter extends ArrayAdapter<Event> {

    public CustomListAdapter(Context context, int textViewResourceId){
        super(context, textViewResourceId);
    }

    public CustomListAdapter(Context context, int resource, ArrayList<Event> events) {
        super(context, resource, events);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View v = view;

        if(v == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(R.layout.list_layout, null);
        }

        Event event = getItem(position);
        Log.d("IN ADAPTER", event.toString());

        if(event != null) {
            TextView textName = (TextView) v.findViewById(R.id.name);
            TextView textPlaceName = (TextView) v.findViewById(R.id.place_name);
            TextView textTime = (TextView) v.findViewById(R.id.time);

            textName.setText(event.name);
            if(event.placeName != null && !event.placeName.isEmpty()){
                textPlaceName.setText(event.placeName);
            } else if(event.city != null && !event.city.isEmpty()) {
                textPlaceName.setText(event.city);
            } else if(event.country != null && !event.country.isEmpty()){
                textPlaceName.setText(event.country);
            } else {
                textPlaceName.setText("No location given");
            }

            textTime.setText(event.getReadableDate());

        }

        return v;
    }

}
