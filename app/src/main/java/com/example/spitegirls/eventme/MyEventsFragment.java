package com.example.spitegirls.eventme;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
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
            String id = "";
            String country = "";
            String city = "";
            String longitude = "";
            String latitude = "";
            //String source = "";

            if(eventDetails != null) {
                try {
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

                        // Has to be included
                        id = event.getString("id");
//                        Log.d("id is", id);

                        // Kinda works but all outta order man due to Async vs Async
//                        Bundle coverBundle = new Bundle();
//                        coverBundle.putString("fields", "cover");
//                        // Getting cover photo from event_id
//                        new GraphRequest(
//                                AccessToken.getCurrentAccessToken(),
//                                "/" + id,
//                                coverBundle,
//                                HttpMethod.GET,
//                                new GraphRequest.Callback() {
//                                    public void onCompleted(GraphResponse response) {
//                                        /* handle the result */
//                                        //Log.d("RESPONSE", "<" + response.getRawResponse() + ">");
//                                       // try {
//                                        JSONObject responseJSONObject = response.getJSONObject();
////                                            JSONObject test = response.getJSONObject();
//                                            if (responseJSONObject != null && responseJSONObject.has("cover")) {
//                                                //Log.d("THERE IS", "A COVER");
//                                                try {
//                                                    JSONObject cover = responseJSONObject.getJSONObject("cover");
//                                                    sourceMap.put(id, cover.getString("source"));
//                                                    Log.d("ID IS", id);
////                                                    Log.d("SOURCE IS", sourceList.get(sourceList.size()-1));
//                                                } catch ( JSONException e ) { e.printStackTrace(); }
//                                            } else {
//                                                //Log.d("FALSE", "ALARM");
//                                            }
//                                        //} catch (JSONException e) { e.printStackTrace(); }
//                                    }
//                                }
//                        ).executeAsync();
                        //Log.d("TEST", "damn things not working");
//                        Log.d("SOURCE IS", "<" + source  + ">");

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

                        HashMap<String, String> eventMap = new HashMap<>();
                        eventMap.put("description", description);
                        eventMap.put("name", name);
                        eventMap.put("startTime", startTime);
                        eventMap.put("id", id);
                        eventMap.put("country", country);
                        eventMap.put("city", city);
                        eventMap.put("latitude", latitude);
                        eventMap.put("longitude", longitude);
//                        eventMap.put("source", source);
//                        Log.d("SOURCE IS NOW", source);
//                        Log.d("EVENTMAP SRC", eventMap.get("source"));

                        eventList.add(eventMap);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.toString());
                }
            }
            else {
                Log.e(TAG, "Couldn't get json from server.");
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
            // Try change to GONE instead
            test.setVisibility(TextView.INVISIBLE);
            spinner.setVisibility(View.INVISIBLE);

            // Adding functionality for user clicks
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d("INT I IS", Integer.toString(i));
                    Log.d("LONG L IS", Long.toString(l));

                    Bundle args = new Bundle();
                    args.putSerializable("hashmap", eventList.get(i));
//                    Log.d("TEST", "<" + sourceMap.get(eventList.get(i).get("id")) + ">");
//                    args.putSerializable("source", sourceMap.get(eventList.get(i).get("id")));

                    EventDetailsFragment eventFrag = EventDetailsFragment.newInstance(args);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.my_frame, eventFrag)
                            .addToBackStack(null)
                            .commit();
                }
            });


            // Makes list smaller for testing inclusion of images
//            ArrayList<HashMap<String, String>> toSend = new ArrayList<HashMap<String, String>>();
//            toSend.add(eventList.get(0));

            // Currently nothing displays so that's fun
//            CustomList adapter = new CustomList(getActivity(), toSend);
//            listView.setAdapter(adapter);
        }

    }

}
