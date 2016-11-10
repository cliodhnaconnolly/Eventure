package com.example.spitegirls.eventme;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

import static android.R.attr.id;
import static com.facebook.FacebookSdk.getApplicationContext;
import static com.facebook.GraphRequest.TAG;

/**
 * Created by Clíodhna on 09/11/2016.
 */

// Used help from on how to parse
// http://www.androidhive.info/2012/01/android-json-parsing-tutorial/

public class GetEvents extends AsyncTask<String, Void, String> {

    public HashSet<HashMap<String, String>> eventList = new HashSet<HashMap<String, String>>();
    @Override
    protected String doInBackground(String... params){
        if(params.toString() != null) {
            try {
                JSONObject jsonObj = new JSONObject(params.toString());

                Log.d("INITIAL PARAMS", params.toString());

                JSONArray events = jsonObj.getJSONArray("events");

                for (int i = 0; i < events.length(); i++) {
                    JSONObject event = events.optJSONObject(i);

                    String description = event.getString("description");
                    String name = event.getString("name");
                    String startTime = event.getString("start_time");
                    String id = event.getString("id");

                    JSONObject location = event.getJSONObject("location");
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
                Log.e(TAG, "Json parsing error: " + e.getMessage());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),
//                                "Json parsing error: " + e.getMessage(),
//                                Toast.LENGTH_LONG)
//                                .show();
//                    }
//                });
            }
        }
        else {
            Log.e(TAG, "Couldn't get json from server.");
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getApplicationContext(),
//                            "Couldn't get json from server. Check LogCat for possible errors!",
//                            Toast.LENGTH_LONG)
//                            .show();
//                }
//            });

        }

        return null;
    }
}
