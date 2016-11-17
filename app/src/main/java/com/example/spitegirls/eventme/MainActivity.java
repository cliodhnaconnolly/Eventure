package com.example.spitegirls.eventme;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.facebook.GraphRequest.TAG;

// This is the most useful thing I've found RE:fragments
// https://guides.codepath.com/android/Creating-and-Using-Fragments#fragment-lifecycle

// How to branch and merge
// https://git-scm.com/book/en/v2/Git-Branching-Basic-Branching-and-Merging

public class MainActivity extends AppCompatActivity implements MyAccountFragment.OnItemSelectedListener {

    MyAccountFragment accountFragment;
    MyEventsFragment eventsFragment;

    public JSONObject unparsedEventsData;
    public ArrayList<Event> parsedEventsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.d("IN ONCREAT", "WORK CHrIST");

        FacebookSdk.sdkInitialize(getApplicationContext());

        Bundle inBundle = getIntent().getExtras();
        final String name = inBundle.get("name").toString();
        final String surname = inBundle.get("surname").toString();
        final String imageUrl = inBundle.get("imageUrl").toString();

        // Behaviour should match official Google guidelines
        // https://material.google.com/components/bottom-navigation.html#bottom-navigation-behavior

        BottomNavigationView bottomBar = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomBar.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        if(isNetworkAvailable()) {
                            switch (item.getItemId()) {
                                case R.id.my_events:
                                    // Checks if data to display is ready
                                    if(parsedEventsList == null) {
                                        transaction.replace(R.id.my_frame, new MyEventsFragment());
                                        transaction.commit();
                                        getEventDetails();
                                    } else {
                                        setUpMyEventsFragmentWithData();
                                    }

                                    break;
                                case R.id.events_near_me:
                                    transaction.replace(R.id.my_frame, new EventsNearMeFragment());
                                    transaction.commit();
                                    break;
                                case R.id.create_event:
                                    transaction.replace(R.id.my_frame, new CreateEventFragment());
                                    transaction.commit();
                                    break;
                                case R.id.my_account:
                                    accountFragment = MyAccountFragment.newInstance(name, surname, imageUrl);
                                    transaction.replace(R.id.my_frame, accountFragment);
                                    transaction.commit();
                                    break;
                            }
                        } else {
                            transaction.replace(R.id.my_frame, new NoInternetFragment());
                            transaction.commit();
                        }
                        return false;
                    }
                }
        );
    }

    public void getEventDetails() {
        // May need to make this wait for a bit
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted( JSONObject object, GraphResponse response) {
                        try {
                            setUnparsedEventData(object.getJSONObject("events"));
                            Log.d("FINISHED", "getEventDetails()");
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,events");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void getExtraEventDetails() {
        Bundle bundle = new Bundle();
        // Add extra fields to this bundle of shit you want to receive
        bundle.putString("fields", "cover");
        Log.d("IN", "getExtraEventsDetails");
        // Use public variable parsedEventsList
        for(final Event event : parsedEventsList){
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + event.id,
                    bundle,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            JSONObject responseJSONObject = response.getJSONObject();
                            if (responseJSONObject != null && responseJSONObject.has("cover")) {
                                try {
                                    event.coverURL = responseJSONObject.getString("source");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("FINISHED", "getExtraEventDetails");
                            }
                            // Call method to set up new fragment
                            setUpMyEventsFragmentWithData();
                        }
                    }
            ).executeAsync();
        }

    }

    private void setUpMyEventsFragmentWithData() {
        Bundle args = new Bundle();
        args.putSerializable("arraylist", parsedEventsList);
        eventsFragment = MyEventsFragment.newInstance(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.my_frame, eventsFragment);
        transaction.commit();
        Log.d("FINISHED", "setUpMyEventsFragmetnWithData");
    }

    private void setUnparsedEventData(JSONObject obj){
        unparsedEventsData = obj;

        Log.d("UNPARSED", unparsedEventsData.toString());
        new GetEvents().execute();
    }

    public void setParsedEventsList(ArrayList<Event> eventsList) {
        parsedEventsList = eventsList;
        Log.d("IN", "SETPARSEDEVENTS LIST");
        setUpMyEventsFragmentWithData();

        // Currently doesn't work so we're going to stop going down the rabbit hole at this stage
        // Make call to getExtraDetails
        // getExtraEventDetails();
    }

    @Override
    public void onLogoutItemSelected(String info){
        logout();
    }

    public void logout(){
        LoginManager.getInstance().logOut();
        Intent login = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(login);
        finish();
    }

    // Checks if a connection is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Used help from on how to parse
    // http://www.androidhive.info/2012/01/android-json-parsing-tutorial/

    public class GetEvents extends AsyncTask<Void, Void, Void> {

        // Received data
        private JSONObject eventData;
        // Generated data
        public ArrayList<Event> eventsList;

        @Override
        protected void onPreExecute() {

            eventData = unparsedEventsData;
            eventsList = new ArrayList<Event>();
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

            if(eventData.toString() != null) {
                try {
                    JSONArray events = eventData.getJSONArray("data");
                    //Log.d("events is ", events.toString());

                    for (int i = 0; i < events.length(); i++) {
                        JSONObject event = events.optJSONObject(i);
                        //Log.d("Event is", event.toString());

                        if (event.has("description")) {
                            description = event.getString("description");
//                            Log.d("Description is", description);
                        } else {
                            description = "No description given";
                        }

                        if (event.has("name")) {
                            name = event.getString("name");
//                            Log.d("Name is", name);
                        } else {
                            name = "No title given";
                        }

                        if (event.has("start_time")) {
                            startTime = event.getString("start_time");
//                            Log.d("Start time is", startTime);
                        } else {
                            startTime = "No time given";
                        }

                        // Can't be null
                        id = event.getString("id");
//                        Log.d("id is", id);

                        if (event.has("place")) {
                            JSONObject place = event.getJSONObject("place");
                            if (place.has("location")) {
                                JSONObject location = place.getJSONObject("location");
                                if (location.has("country")) {
                                    country = location.getString("country");
                                } else {
                                    country = "";
                                }
                                if (location.has("city")) {
                                    city = location.getString("city");
                                } else {
                                    city = "";
                                }
                                if (location.has("latitude")) {
                                    latitude = location.getString("latitude");
                                } else {
                                    latitude = "";
                                }
                                if (location.has("longitude")) {
                                    longitude = location.getString("longitude");
                                } else {
                                    longitude = "";
                                }
                            }
                        }

                        eventsList.add(new Event(description, name, id, country, city, startTime, latitude, longitude));
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            }
            else {
                Log.e(TAG, "Couldn't get json from server.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.d("FINISHED", "getEvents.execute");
            setParsedEventsList(eventsList);

        }
    }


}
