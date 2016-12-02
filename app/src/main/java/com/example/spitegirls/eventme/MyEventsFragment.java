package com.example.spitegirls.eventme;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class MyEventsFragment extends Fragment {

    SharedPreferences preference;
    SharedPreferences.Editor editor;

    private ListView listView;
    public ProgressBar spinner;

    private ArrayList<Event> pastEvents;
    private ArrayList<Event> futureEvents;

    public static MyEventsFragment newInstance(Bundle bundle) {
        MyEventsFragment fragment = new MyEventsFragment();
        if(bundle != null){
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    public MyEventsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        editor = preference.edit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);
        listView = (ListView) view.findViewById(R.id.list);
        spinner = (ProgressBar) view.findViewById(R.id.spinner);

        final SwipeRefreshLayout swipeView = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);

        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                ((MainActivity) getActivity()).refreshData();
            }
                       });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();

            if(bundle.getSerializable("arraylist") != null){
                ArrayList<Event> eventList = (ArrayList<Event>) bundle.getSerializable("arraylist");
                futureEvents = new ArrayList<Event>();
                pastEvents = new ArrayList<Event>();
                // Sort list and populate pastEvents and FutureEvents
                Calendar today = Calendar.getInstance();

                Log.d("CALENDAR DATE IS", today.toString());

                for(int i = 0; i < eventList.size(); i++){
                    if(today.after(eventList.get(i).getCalendarDate())) {
                        pastEvents.add(eventList.get(i));
                    }
                    else{
                        futureEvents.add(eventList.get(i));
                    }
                }

                //Sorting lists by date
                Collections.sort(futureEvents, new Comparator<Event>() {
                    public int compare(Event ev1, Event ev2) {
                        if (ev2.startTime == null || ev1.startTime == null) return 0; //Just to be safe
                        return ev1.getCalendarDate().compareTo(ev2.getCalendarDate());
                    }
                });

                Collections.sort(pastEvents, new Comparator<Event>() {
                    public int compare(Event ev1, Event ev2) {
                        if (ev1.startTime == null || ev2.startTime == null) return 0; //Just to be safe
                        return ev2.getCalendarDate().compareTo(ev1.getCalendarDate());
                    }
                });

                // First check existing state
                boolean futureTrue = preference.getBoolean("futureEvents", true);

                // Depending on saved preference we set up list
                if(futureTrue){
                    setUpList(futureEvents);
                } else {
                    setUpList(pastEvents);
                }

            }

        } else {
            spinner.setVisibility(View.VISIBLE);
        }

    }

    private void setUpList(final ArrayList<Event> givenList) {
        CustomListAdapter adapter = new CustomListAdapter(getActivity(), R.layout.list_layout, givenList);
        listView.setAdapter(adapter);
        spinner.setVisibility(View.INVISIBLE);

        // Adding functionality for user clicks
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle args = new Bundle();
                args.putSerializable("event", givenList.get(i));

                EventDetailsFragment eventFrag = EventDetailsFragment.newInstance(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.my_frame, eventFrag)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.time_frame, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
        MenuItem futureOption = menu.findItem(R.id.menu_item_time_future);
        MenuItem pastOption = menu.findItem(R.id.menu_item_time_past);

        if(preference.getBoolean("futureEvents", true)){
            futureOption.setChecked(true);
        } else {
            pastOption.setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_time_past:
                item.setChecked(true);

                // Preference stored for future use
                editor.putBoolean("futureEvents", false);
                editor.commit();

                if(pastEvents != null){
                    setUpList(pastEvents);
                    spinner.setVisibility(View.INVISIBLE);
                }
                return true;
            case R.id.menu_item_time_future:
                item.setChecked(true);

                // Preference stored for future use
                editor.putBoolean("futureEvents", true);
                editor.commit();

                if(futureEvents != null){
                    setUpList(futureEvents);
                    spinner.setVisibility(View.INVISIBLE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int myEventsFragment = 0;

        BottomNavigationView bottomNavigationView;

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(myEventsFragment).setChecked(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(myEventsFragment).setChecked(true);
        }
    }

}
