package com.example.spitegirls.eventme;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static android.R.attr.data;

// Example used is
// https://www.learn2crack.com/2013/10/android-custom-listview-images-text-example.html

// Doesn't currently work

public class CustomList extends ArrayAdapter<String> {

    private final Activity context;
    private ArrayList<HashMap<String, String>> eventList;

    public CustomList(Activity context, ArrayList<HashMap<String, String>> eventList) {
        super(context, R.layout.list_layout);
        this.context = context;
        this.eventList = eventList;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_layout, null, true);

        TextView textName = (TextView) rowView.findViewById(R.id.name);
        TextView textStartTime = (TextView) rowView.findViewById(R.id.start_time);
        TextView textId = (TextView) rowView.findViewById(R.id.id);
        TextView textCity = (TextView) rowView.findViewById(R.id.city);

//        ImageView eventIconView = (ImageView) rowView.findViewById(R.id.event_icon);

        textName.setText(eventList.get(position).get("name"));
        textStartTime.setText(eventList.get(position).get("startTime"));
        textId.setText(eventList.get(position).get("id"));
        textCity.setText(eventList.get(position).get("city"));

//        new DownloadImage((ImageView) view.findViewById(R.id.event_icon)).execute(eventList.get(position).get("source"));

        return rowView;
    }

}
