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
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.facebook.GraphRequest.TAG;

public class MyEventsFragment extends Fragment {

    private ListView listView;
    public ArrayList<HashMap<String, String>> eventList;
    public JSONObject eventDetails;
    private TextView test;
    public ProgressBar spinner;

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
        spinner = (ProgressBar) view.findViewById(R.id.spinner);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(this.getArguments() != null){
            try {
                String eventsDetailsString = getArguments().getString("allEvents");
                Log.d("INPUT IS" , "<" + eventsDetailsString + ">");
                eventDetails = new JSONObject(eventsDetailsString);
                new GetEvents().execute();
            } catch (JSONException e) { e.printStackTrace(); }
        } else {
            Log.d("INSTEAD", "IS NULL");
        }

    }

    // Used help from on how to parse
    // http://www.androidhive.info/2012/01/android-json-parsing-tutorial/

    private class GetEvents extends AsyncTask<Void, Void, Void> {

        @Override
        protected  void onPreExecute() {
            super.onPreExecute();
            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params){
            String description;
            String name;
            String startTime;
            String id;
            String country = "";
            String city = "";
            String longitude = "";
            String latitude = "";
            String source = "";

            if(eventDetails != null) {
                try {
//                    JSONObject jsonObj = new JSONObject(params.toString());
//                    Log.d("PARAMS IS ", params.toString());

                    JSONObject jsonObject = eventDetails;

                    Log.d("INITIAL PARAMS", params.toString());

                    JSONArray events = jsonObject.getJSONArray("data");

                    for (int i = 0; i < events.length(); i++) {
                        JSONObject event = events.optJSONObject(i);

                        if(event.has("description")){
                            description = event.getString("description");
//                            Log.d("Description is", description);
                        } else {
                            description = "No description given";
                        }

                        if(event.has("name")){
                            name = event.getString("name");
//                            Log.d("Name is", name);
                        } else {
                            name = "No name given";
                        }

                        if(event.has("start_time")){
                            startTime = event.getString("start_time");
//                            Log.d("Start time is", startTime);
                        } else {
                            startTime = "No time given";
                        }

                        if(event.has("cover")){
                            JSONObject cover = event.getJSONObject("cover");
                            if(cover.has("source")){
                                source = cover.getString("source");
                            }
                        }

                        // Has to be included
                        id = event.getString("id");
//                        Log.d("id is", id);

                        if(event.has("place")){
                            JSONObject place = event.getJSONObject("place");
                            if(place.has("location")){
                                JSONObject location = place.getJSONObject("location");
                                if(location.has("country")){
                                    country = location.getString("country");
                                } else { country = ""; }
                                if(location.has("city")){
                                    city = location.getString("city");
                                } else { city = ""; }
                                if(location.has("latitude")){
                                    latitude = location.getString("latitude");
                                } else { latitude = ""; }
                                if(location.has("longitude")){
                                    longitude = location.getString("longitude");
                                } else { longitude = ""; }
                            }
                        }
//                        JSONObject place = event.getJSONObject("place");
//                        JSONObject location = place.getJSONObject("location");
//                        String country = location.getString("country");
//                        String city = location.getString("city");
//                        String latitude = location.getString("latitude");
//                        String longitude = location.getString("longitude");

                        HashMap<String, String> eventMap = new HashMap<>();
                        eventMap.put("description", description);
                        eventMap.put("name", name);
                        eventMap.put("startTime", startTime);
                        eventMap.put("id", id);
                        eventMap.put("country", country);
                        eventMap.put("city", city);
                        eventMap.put("latitude", latitude);
                        eventMap.put("longitude", longitude);
                        eventMap.put("source", source);

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

            Log.d("FINISHED", "DoInBackground is finished");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ListAdapter adapter = new SimpleAdapter(
                    getActivity(), eventList, R.layout.list_layout,
                    new String[]{"name", "startTime", "id", "city"},
                    new int[]{R.id.name, R.id.start_time, R.id.id, R.id.city});
            listView.setAdapter(adapter);
            test.setVisibility(TextView.INVISIBLE);
            spinner.setVisibility(View.INVISIBLE);

            // Makes list smaller for testing inclusion of images
//            ArrayList<HashMap<String, String>> toSend = new ArrayList<HashMap<String, String>>();
//            toSend.add(eventList.get(0));

            // Currently nothing displays so that's fun
//            CustomList adapter = new CustomList(getActivity(), toSend);
//            listView.setAdapter(adapter);
        }

    }

}
