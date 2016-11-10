package com.example.spitegirls.eventme;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.facebook.GraphRequest.TAG;

public class MyEventsFragment extends Fragment {

    private ListView listView;
    public ArrayList<HashMap<String, String>> eventList;
    public JSONObject eventDetails;
    private TextView test;

    public static MyEventsFragment newInstance(Bundle eventsInfo) {
        MyEventsFragment fragment = new MyEventsFragment();
        if(eventsInfo != null){
            fragment.setArguments(eventsInfo);
            Log.d("INPUT TO ", eventsInfo.toString());
            Log.d("SET ARGS", fragment.getArguments().toString());

        }

        return fragment;
    }

    public MyEventsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventList = new ArrayList<HashMap<String, String>>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);
        listView = (ListView) view.findViewById(R.id.list);
        test = (TextView) view.findViewById(R.id.test_my_events);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(this.getArguments() != null){
            try {
                String eventsDetailsString = getArguments().getString("allEvents");
                Log.d("INPUT IS" , "<" + eventsDetailsString + ">");
                eventDetails = new JSONObject(eventsDetailsString);
                new GetEvents().execute(eventDetails.toString());
            } catch (JSONException e) { e.printStackTrace(); }
        } else {
            Log.d("INSTEAD", "IS NULL");
        }

    }

    // Used help from on how to parse
    // http://www.androidhive.info/2012/01/android-json-parsing-tutorial/

    private class GetEvents extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params){
            if(params.toString() != null) {
                try {
//                    JSONObject jsonObj = new JSONObject(params.toString());
//                    Log.d("PARAMS IS ", params.toString());

                    JSONObject jsonObject = eventDetails;

                    Log.d("INITIAL PARAMS", params.toString());

                    JSONArray events = jsonObject.getJSONArray("data");

                    for (int i = 0; i < events.length(); i++) {
                        JSONObject event = events.optJSONObject(i);

                        String description = event.getString("description");
                        Log.d("Description is", description);
                        String name = event.getString("name");
                        Log.d("NAme is", name);
                        String startTime = event.getString("start_time");
                        Log.d("Start time is", startTime);
                        String id = event.getString("id");
                        Log.d("id is", id);

                        JSONObject place = event.getJSONObject("place");
                        JSONObject location = place.getJSONObject("location");
                        String country = location.getString("country");
                        String city = location.getString("city");
                        String latitude = location.getString("latitude");
                        String longitude = location.getString("longitude");

                        HashMap<String, String> eventMap = new HashMap<>();
                        eventMap.put("description", description);
                        eventMap.put("name", name);
                        eventMap.put("startTime", startTime);
                        eventMap.put("id", id);
                        eventMap.put("country", country);
                        eventMap.put("city", city);
                        eventMap.put("latitude", latitude);
                        eventMap.put("longitude", longitude);

                        eventList.add(eventMap);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.toString());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(),
//                                    "Json parsing error: " + e.getMessage(),
//                                    Toast.LENGTH_LONG)
//                                    .show();
//                        }
//                    });
                }
            }
            else {
                Log.e(TAG, "Couldn't get json from server.");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),
//                                "Couldn't get json from server. Check LogCat for possible errors!",
//                                Toast.LENGTH_LONG)
//                                .show();
//                    }
//                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ListAdapter adapter = new SimpleAdapter(
                    getActivity(), eventList, R.layout.list_layout,
                    new String[]{"name", "start_time", "id", "city"},
                    new int[]{R.id.name, R.id.start_time, R.id.id, R.id.city});
            listView.setAdapter(adapter);
            test.setVisibility(TextView.INVISIBLE);
        }

    }

}
