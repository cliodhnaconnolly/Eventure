package com.example.spitegirls.eventme;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.support.v4.app.FragmentTabHost;
import android.widget.Toast;

import java.lang.annotation.Annotation;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import android.app.ActionBar.Tab;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class MyEventsFragment extends Fragment{

    private ListView listView;
    private TextView test;
    public ProgressBar spinner;
    private ArrayList<Event> eventList;
    private ArrayList<Event> futureEventList;
    private ArrayList<Event> pastEventList;
    public Calendar date;

    public static MyEventsFragment newInstance(Bundle bundle) {
        MyEventsFragment fragment = new MyEventsFragment();
        if(bundle != null){
            fragment.setArguments(bundle);
//            Log.d("INPUT TO ", bundle.toString());
//            Log.d("SET ARGS", fragment.getArguments().toString());

        }
        return fragment;
    }

    public MyEventsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Calendar date = Calendar.getInstance();
        String stringDate = df.format(date.getTime());
        eventList = new ArrayList<Event>();
        futureEventList = new ArrayList<Event>();
        pastEventList = new ArrayList<Event>();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);
        listView = (ListView) view.findViewById(R.id.list);
        test = (TextView) view.findViewById(R.id.test_my_events);
        spinner = (ProgressBar) view.findViewById(R.id.spinner);
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();

            if(bundle.getSerializable("arraylist") != null){
                eventList = (ArrayList<Event>) bundle.getSerializable("arraylist");
                for (Event event: eventList){
                   String time = getEventTime(event);
                   String date = getEventdate(event);
                   if(isInFuture(date, time)){
                       futureEventList.add(event);
                   }
                   else{
                       pastEventList.add(event);
                   }
                }
                CustomListAdapter adapter = new CustomListAdapter(getActivity(), R.layout.list_layout, eventList);
                listView.setAdapter(adapter);
                test.setVisibility(TextView.INVISIBLE);
                spinner.setVisibility(View.INVISIBLE);

                // Adding functionality for user clicks
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Log.d("INT I IS", Integer.toString(i));
                        Log.d("LONG L IS", Long.toString(l));

                        Bundle args = new Bundle();
                        args.putSerializable("event", eventList.get(i));

                        EventDetailsFragment eventFrag = EventDetailsFragment.newInstance(args);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.my_frame, eventFrag)
                                .addToBackStack(null)
                                .commit();
                    }
                });

            }

        } else {
            spinner.setVisibility(View.VISIBLE);
            Log.d("INSTEAD", "IS NULL");
        }

    }

    // When orientation changes we want to maintain the item in bottom nav
    // Don't really need this cause 0 is default anyways
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        BottomNavigationView bottomNavigationView;

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
        }
    }

    private boolean isInFuture(String date, String time){
        // Today's date
        Calendar today = Calendar.getInstance();
        // Inputted date
        Calendar eventDate = parseDateTime(date, time);
        //Toast.makeText(getContext(), eventDate.toString(), Toast.LENGTH_LONG).show();


        if(today.after(eventDate)){
            return false;
        } else {
            return true;
        }
    }


    private Calendar parseDateTime(String date, String time){
        // Inputted date
        Calendar input = Calendar.getInstance();
        String[] splitDate = date.split("-");
        String[] splitTime = time.split(":");
//
//        // Parsing inputted Date
        int day = Integer.parseInt(splitDate[2]);
        int month = Integer.parseInt(splitDate[1])-1;
        int year = Integer.parseInt(splitDate[0]);

        int hours = Integer.parseInt(splitTime[0]);
        int minutes = Integer.parseInt(splitTime[1]);
        //Toast.makeText(getContext(), year,month,day,hours,minutes, Toast.LENGTH_SHORT).show();
        // Setting input Calendar with parsed data
        //input.set(year, month, day, hours, minutes);
        input.set(year, month, day, hours, minutes);
        return input;
    }

    private String getEventdate(Event event){
        String date = event.startTime;
        String[] parts = date.split("T");
        String splitdate = parts[0];
        //Toast.makeText(getContext(), date, Toast.LENGTH_SHORT).show();
       return splitdate;
    }

    private String getEventTime(Event event){
        String time = event.startTime;
        String[] parts = time.split("T");
        String splitTime = parts[1];
        //Toast.makeText(getContext(), date, Toast.LENGTH_SHORT).show();
        return splitTime;
    }
}
