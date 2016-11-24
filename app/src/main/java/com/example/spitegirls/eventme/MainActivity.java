package com.example.spitegirls.eventme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.facebook.GraphRequest.TAG;

// This is the most useful thing I've found RE:fragments
// https://guides.codepath.com/android/Creating-and-Using-Fragments#fragment-lifecycle

// How to branch and merge
// https://git-scm.com/book/en/v2/Git-Branching-Basic-Branching-and-Merging

// Link to view database possibly you may need to login or something
// https://console.firebase.google.com/project/eventure-36efb/database/data/

// How to read and write from database
// https://firebase.google.com/docs/database/android/read-and-write

public class MainActivity extends AppCompatActivity implements MyAccountFragment.OnItemSelectedListener {

    MyAccountFragment accountFragment;
    MyEventsFragment eventsFragment;
    EventsNearMeFragment eventsNearFragment;

    public JSONObject unparsedEventsData;
    public ArrayList<Event> parsedEventsList;

    private ArrayList<Event> databaseEvents;
    private ArrayList<Event> combinedEvents;

    private DatabaseReference mEventReference;
    private DatabaseReference mIdReference;

    private int currentId;

    private static boolean MY_EVENTS_REQUESTED = false;
    private static boolean NEARBY_EVENTS_REQUESTED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user has previously selected an alternate theme
        if(checkThemePref()){
            setTheme(R.style.OriginalAppTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        setContentView(R.layout.activity_main);

        // Sets up database references
        mEventReference = FirebaseDatabase.getInstance().getReference();
        mIdReference = FirebaseDatabase.getInstance().getReference();

        FacebookSdk.sdkInitialize(getApplicationContext());

        final Bundle inBundle = getIntent().getExtras();

        // Behaviour should match official Google guidelines
        // https://material.google.com/components/bottom-navigation.html#bottom-navigation-behavior

        BottomNavigationView bottomBar = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        // Load My Events Screen from the get-go
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Problem with this is that it's not gonna undo once internet is on.
        if(isNetworkAvailable()) {
            if (combinedEvents == null) {
                transaction.replace(R.id.my_frame, new MyEventsFragment());
                transaction.commit();
                Log.d("Call from if main", "ture");
                getEventDetails();
                MY_EVENTS_REQUESTED = true;
                NEARBY_EVENTS_REQUESTED = false;
                // This won't be called with data due to Async tasks
                //setUpMyEventsFragmentWithData();
            } else {
                setUpMyEventsFragmentWithData();
            }
        } else {
            Bundle bundle = new Bundle();
            bundle.putInt("appearance", ErrorFragment.NO_INTERNET_APPEARANCE);
            ErrorFragment fragment = ErrorFragment.newInstance(bundle);
            transaction.replace(R.id.my_frame, fragment);
            transaction.commit();
        }

        bottomBar.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        if(isNetworkAvailable()) {
                            switch (item.getItemId()) {
                                case R.id.my_events:
                                    // Checks if data to display is ready
                                    if(combinedEvents == null) {
                                        transaction.replace(R.id.my_frame, new MyEventsFragment());
                                        transaction.commit();
                                        getEventDetails();
                                        MY_EVENTS_REQUESTED = true;
                                        NEARBY_EVENTS_REQUESTED = false;
//                                        setUpMyEventsFragmentWithData();
                                    } else {
                                        setUpMyEventsFragmentWithData();
                                    }

                                    break;
                                case R.id.events_near_me:
                                    //Makes sure data is pulled in case they go to map first. Sets up events for events near me fragment.
                                    if(combinedEvents == null) {
                                        transaction.replace(R.id.my_frame, new EventsNearMeFragment());
                                        transaction.commit();
                                        getEventDetails();
                                        NEARBY_EVENTS_REQUESTED = true;
                                        MY_EVENTS_REQUESTED = false;
//                                        setUpEventsNearMeFragmentWithData();
                                    } else {
                                        setUpEventsNearMeFragmentWithData();
                                    }
                                    break;
                                case R.id.create_event:
                                    transaction.replace(R.id.my_frame, new CreateEventFragment());
                                    transaction.commit();
                                    break;
                                case R.id.my_account:
                                    accountFragment = MyAccountFragment.newInstance(inBundle);
                                    transaction.replace(R.id.my_frame, accountFragment);
                                    transaction.commit();
                                    break;
                            }
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putInt("appearance", ErrorFragment.NO_INTERNET_APPEARANCE);
                            ErrorFragment fragment = ErrorFragment.newInstance(bundle);
                            transaction.replace(R.id.my_frame, fragment);
                            transaction.commit();
                        }
                        return false;
                    }
                }
        );
    }

    @Override
    public void onStart() {
        super.onStart();

        // Currently these event listeners are grabbing too much data, want to change this

        // Gets current id value from DB
        ValueEventListener idListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get id and update values
                // dataSnapshot returns a hashmap, eg. {id=0}
                Log.d("RESULT GOT IS", dataSnapshot.getValue().toString());
                HashMap idResult  = (HashMap) dataSnapshot.getValue();

                // Why it swaps I do not know
                Log.d("Result type", idResult.get("id").getClass().toString());

                // Someday I will fix why this fluctuates between Long and String, today is not that day
                if(idResult.get("id").getClass() == Long.class ){
                    Long id = (Long) idResult.get("id");
                    currentId = id.intValue();
                } else if (idResult.get("id").getClass() == String.class){
                    int id = Integer.parseInt((String) idResult.get("id"));
                    currentId = id;
                } else {
                    Log.d("ERROR", "failed to figure life out");
                }
                Log.d("Current id", "is " + currentId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting ID failed, log a message
                Log.w(TAG, "loadID:onCancelled", databaseError.toException());
            }
        };
        mIdReference.addValueEventListener(idListener);

        // Gets current events from DB
        mEventReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d("RESULT EVENT", dataSnapshot.getValue().toString());
//                Log.d("RESULT TYPE", dataSnapshot.getValue().getClass().toString());
                HashMap results = (HashMap) dataSnapshot.getValue();
//                Log.d("EVENTS TYPE", results.get("events").getClass().toString());

                // Why there is some null things I have no idea
                ArrayList events = (ArrayList) results.get("events");
//                Log.d("Events arraylist", events.get(3).getClass().toString());
//                Log.d("Events size", "number" + events.size());
                for(int i=0; i<events.size(); i++){
                    if(events.get(i) != null){
                        HashMap map = (HashMap) events.get(i);
//                        Log.d("map", map.entrySet().toString());
                        Event event = new Event((String) map.get("description"), (String) map.get("name"),
                                (String) map.get("id"), (String) map.get("placeName"), (String) map.get("country"), (String) map.get("city"),
                                (String) map.get("startTime"), (String) map.get("latitude"), (String) map.get("longitude"));
//                        Log.d("NAME IS", event.toString());

                        // Check to try reduce duplications
                        if(databaseEvents == null || databaseEvents.size() == events.size()){
                            databaseEvents = new ArrayList<Event>();
                        }
                        databaseEvents.add(event);

                    }
                }
                Log.d("FINISHED", "retrieving db events");
                Log.d("DatabaseEvents is", "size" + databaseEvents.size());

                // Because it usually takes the db longer they now get the power
                setCombinedEvents();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Event failed, log a message
                Log.w(TAG, "loadEvent:onCancelled", databaseError.toException());
            }
        });

    }

    public Long getFreshId(){
        Long newFreshId = Long.valueOf(currentId + 1);
        // Current ID should update once the following call is made
        mEventReference.child("id").setValue(newFreshId);
        return newFreshId;
    }

    public void writeNewEvent(Event event) {
        mEventReference.child("events").child(getFreshId().toString()).setValue(event);
    }

    private void writeNewEvent(String description, String name, String id, String placeName, String country, String city, String startTime, String latitude, String longitude) {
        Event event = new Event(description, name, id, placeName, country, city, startTime, latitude, longitude);
        mEventReference.child("events").child(getFreshId().toString()).setValue(event);
    }

    // Checks locally stored preferences for decision about themes
    private boolean checkThemePref(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean pref = sharedPreferences.getBoolean("alternateTheme", false);
        return pref;
    }

    public void showDatePickerDialog(View view) {
        DialogFragment dateFrag = new CreateEventFragment.DatePickerFragment();
        dateFrag.show(this.getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View view) {
        DialogFragment timeFrag = new CreateEventFragment.TimePickerFragment();
        timeFrag.show(this.getSupportFragmentManager(), "timePicker");
    }

    // Gets generic events details from Facebook
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

    // Gets details like Cover Photo from Events from Facebook
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
                            //setUpMyEventsFragmentWithData();
                        }
                    }
            ).executeAsync();
        }

    }

    private void setUpMyEventsFragmentWithData() {
        Log.d("SETUPMYEVENTS", "Started");
        Bundle args = new Bundle();
        args.putSerializable("arraylist", combinedEvents);
        eventsFragment = MyEventsFragment.newInstance(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.my_frame, eventsFragment);
        transaction.commit();
        Log.d("FINISHED", "setUpMyEventsFragmentWithData");
    }

    private void setUpEventsNearMeFragmentWithData(){
        Bundle args = new Bundle();
        args.putSerializable("arraylist", combinedEvents);
        eventsNearFragment = EventsNearMeFragment.newInstance(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.my_frame, eventsNearFragment);
        transaction.commit();
        Log.d("FINISHED", "SetUpEventsNearMeFragmentWithData");
    }

    private void setUnparsedEventData(JSONObject obj){
        unparsedEventsData = obj;

        Log.d("UNPARSED", unparsedEventsData.toString());
        new GetEvents().execute();
    }

    public void setParsedEventsList(ArrayList<Event> eventsList) {
        //parsedEventsList = eventsList;
        if(parsedEventsList != null){
            parsedEventsList.addAll(eventsList);
        } else {
            parsedEventsList = eventsList;
        }
        Log.d("IN", "SETPARSEDEVENTS LIST");

//        setUpMyEventsFragmentWithData();

        //Pretty Sure the following isnt needed. Map populates without it. Dont want to fully Delete yet, Brian
        //Log.d("BEFORE", "setUpEventsNearMeFragmentWithData In SetParsedEventsList");
        //setUpEventsNearMeFragmentWithData();

        // Currently doesn't work so we're going to stop going down the rabbit hole at this stage
        // Make call to getExtraDetails
        // getExtraEventDetails();
    }

    private void setCombinedEvents(){
        Log.d("IN COMBINED", "start");
        // Replacing entirity of this each time this is called
        // Updating events with current knowledge
        // Would be nicer to append probably but not right now
        combinedEvents = new ArrayList<Event>();

        if(databaseEvents != null) {
            combinedEvents.addAll(databaseEvents);
        }

        if(parsedEventsList != null) {
            combinedEvents.addAll(parsedEventsList);
        }

        // Sort events here by time or something

        // Because we're only sure we have events now I get to use my icky global variables
        // Possibly set up listners or something?
        if(MY_EVENTS_REQUESTED){
            setUpMyEventsFragmentWithData();
            MY_EVENTS_REQUESTED = false;
        } else if(NEARBY_EVENTS_REQUESTED){
            setUpEventsNearMeFragmentWithData();
            NEARBY_EVENTS_REQUESTED = false;
        }

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
            String placeName ="";
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
                            if (place.has("name")){
                                placeName = place.getString(("name"));
                            }
                            else{
                                placeName = "";
                            }
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
                            else {
                                country = "";
                                city = "";
                                latitude = "";
                                longitude = "";
                            }
                        }

                        eventsList.add(new Event(description, name, id, placeName, country, city, startTime, latitude, longitude));
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
