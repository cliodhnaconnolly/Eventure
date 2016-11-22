package com.example.spitegirls.eventme;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class MyEventsFragment extends Fragment {

    private ListView listView;
    public ProgressBar spinner;
    private ArrayList<Event> eventList;

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
        eventList = new ArrayList<Event>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);
        listView = (ListView) view.findViewById(R.id.list);
        spinner = (ProgressBar) view.findViewById(R.id.spinner);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();

            if(bundle.getSerializable("arraylist") != null){
                eventList = (ArrayList<Event>) bundle.getSerializable("arraylist");

                CustomListAdapter adapter = new CustomListAdapter(getActivity(), R.layout.list_layout, eventList);
                listView.setAdapter(adapter);
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

}
