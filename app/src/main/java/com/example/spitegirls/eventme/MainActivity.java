package com.example.spitegirls.eventme;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.facebook.GraphRequest.TAG;

public class MainActivity extends AppCompatActivity implements MyAccountFragment.OnItemSelectedListener {

    MyAccountFragment accountFragment;
    MyEventsFragment eventsFragment;
    EventsNearMeFragment eventsNearFragment;

    private ArrayList<Event> databaseEvents;
    private ArrayList<Event> combinedEvents;
    private ArrayList<Event> facebookEvents;

    private DatabaseReference mEventReference;
    private DatabaseReference mIdReference;

    private StorageReference mStorageRef;

    private int currentId;
    private boolean permissionDenied;

    // Change to camel case
    private boolean MY_EVENTS_REQUESTED = false;
    private boolean NEARBY_EVENTS_REQUESTED = false;
    private static final int NUMBER_OF_TASKS = 2;

    private AtomicInteger workCounter;

    private byte[] photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Check if user has previously selected an alternate theme
        if(checkThemePref()){
            setTheme(R.style.OriginalAppTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);

        workCounter = new AtomicInteger(NUMBER_OF_TASKS);

        setContentView(R.layout.activity_main);

        // Sets up database references
        mEventReference = FirebaseDatabase.getInstance().getReference();
        mIdReference = FirebaseDatabase.getInstance().getReference();

        // Sets up storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();

        FacebookSdk.sdkInitialize(getApplicationContext());

        // Retrieves profile information from the LoginActivity
        final Bundle inBundle = getIntent().getExtras();

        BottomNavigationView bottomBar = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        // Load My Events Screen from the get-go
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(isNetworkAvailable()) {
            if (combinedEvents == null) {
                transaction.replace(R.id.my_frame, new MyEventsFragment());
                transaction.commit();
                getData();
                MY_EVENTS_REQUESTED = true;
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
                        // Try replace later call to getSupportFragmentManager() with this
                        // Clears stack between bottom navigation tabs according to Google Material Design Guidelines
                        FragmentManager fm = getSupportFragmentManager();
                        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                            fm.popBackStack(); //clear stack as android does not like backing through navigation tabs
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        if(isNetworkAvailable()) {
                            switch (item.getItemId()) {
                                case R.id.my_events:
                                    // Checks if data to display is ready
                                    // Data not ready, user has selected same tab again
                                    if(combinedEvents == null && MY_EVENTS_REQUESTED) {
                                        Log.d("IN IF", "A");
                                        transaction.replace(R.id.my_frame, new MyEventsFragment());
                                        transaction.commit();
                                    }
                                    // Data not ready, user previously requested it in NearbyEvents tab
                                    else if(combinedEvents == null && MY_EVENTS_REQUESTED) {
                                        Log.d("IN IF", "B");
                                        // Update user requested tab
                                        MY_EVENTS_REQUESTED = true;
                                        NEARBY_EVENTS_REQUESTED = false;

                                        // Continue to load blank fragment until data is ready
                                        transaction.replace(R.id.my_frame, new MyEventsFragment());
                                        transaction.commit();
                                    }
                                    // Data not ready, not previously requested
                                    else if(combinedEvents == null && !MY_EVENTS_REQUESTED && !NEARBY_EVENTS_REQUESTED) {
                                        Log.d("IN IF", "C");

                                        transaction.replace(R.id.my_frame, new MyEventsFragment());
                                        transaction.commit();
                                        getData();
                                        MY_EVENTS_REQUESTED = true;
                                    }
                                    else {
                                        Log.d("IN IF", "D");
                                        setUpMyEventsFragmentWithData();
                                    }

                                    break;
                                case R.id.events_near_me:
//                                     Makes sure data is pulled in case they go to map first. Sets up events for events near me fragment.
                                    // Data not ready, user changed tab was previously requested from My Events
                                    if (combinedEvents == null && MY_EVENTS_REQUESTED) {
                                        Log.d("IN IF", "E");
                                        // Update user requested tab
                                        NEARBY_EVENTS_REQUESTED = true;
                                        MY_EVENTS_REQUESTED = false;

                                        // Data is being requested so put up blank fragment and wait
                                        transaction.replace(R.id.my_frame, new EventsNearMeFragment());
                                        transaction.commit();

                                    }
                                    // Data not ready, user repeated request of current tab
                                    else if(combinedEvents == null && NEARBY_EVENTS_REQUESTED) {
                                        Log.d("IN IF", "F");
                                        // Refresh blank fragment purely for them to feel better about waiting all the milliseconds
                                        transaction.replace(R.id.my_frame, new EventsNearMeFragment());
                                        transaction.commit();
                                    }
                                    // Data not ready, data not previously requested
                                    else if(combinedEvents == null && !MY_EVENTS_REQUESTED && !NEARBY_EVENTS_REQUESTED) {
                                        Log.d("IN IF", "G");
                                        transaction.replace(R.id.my_frame, new EventsNearMeFragment());
                                        transaction.commit();
                                        getData();
                                        NEARBY_EVENTS_REQUESTED = true;
                                    }
                                    // Data is ready, load fragment with data
                                    else {
                                        Log.d("IN IF", "H");
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

    private void getData(){
        // Database does its own thing
        // Listener set up in onStart which gets initial data
        // Updates itself anytime after that data has been changed

        // Facebook however...
        // May need to make this wait for a bit

        // Get initial info from FB
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted( JSONObject object, GraphResponse response) {
                        try {
                            if(object != null && object.getJSONObject("events") != null) {
                                new GetEvents(workCounter, object.getJSONObject("events")).execute();
                            }
                            // Else no FB events were retrieved move on

                            Log.d("FINISHED", "getEventDetails()");
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,events");
        request.setParameters(parameters);
        request.executeAsync();
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

                // Reset db events
                databaseEvents = new ArrayList<Event>();

                // Since DB will update its data anytime there is change let it increment a task to be done
                if(workCounter.get() == 0) {
                    Log.d("Incrementing", "onStart");
                    workCounter.incrementAndGet();
                }

                // Events retrieved from database
                HashMap events = (HashMap) results.get("events");
                if(events != null) {
                    for (Object event : events.values()){
                        HashMap map = (HashMap) event;
                        Event submittedEvent = new Event((String) map.get("description"), (String) map.get("name"),
                                (String) map.get("id"), (String) map.get("placeName"), (String) map.get("country"), (String) map.get("city"),
                                (String) map.get("startTime"), (String) map.get("latitude"), (String) map.get("longitude"));

                            databaseEvents.add(submittedEvent);

//                        }
                    }
                    Log.d("FINISHED", "retrieving db events");
                    Log.d("DatabaseEvents is", "size" + databaseEvents.size());
                }

                // Task of retrieving events from Database is complete
                int remainingTasks = workCounter.decrementAndGet();
                Log.d("Decrementing", "onStart");

                // If no more tasks remain
                if(remainingTasks == 0) {
                    Log.d("NO more tasks", "onStart");
                    setCombinedEvents();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Event failed, log a message
                Log.w(TAG, "loadEvent:onCancelled", databaseError.toException());
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CreateEventFragment.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                ContentResolver contentResolver = getContentResolver();
                InputStream inputStream = contentResolver.openInputStream(uri);

                // Compress image so can download from storage promptly
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);

                photo = byteArrayOutputStream.toByteArray();

            } catch (FileNotFoundException e) { e.printStackTrace(); }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("I HAVE BEEN SUMMONED", "MainActivity");
        Log.d("GRaNT RESULT SIS NULL", "" + (grantResults == null));
        Log.d("GRANT RESULTS LENGTH IS", "" + grantResults.length);
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();

            // If permission denied disable EventsNearMeFragment
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.my_frame, new ErrorFragment());
//            transaction.commit();

            permissionDenied = true;
        }

        return;
    }

    @Override
    public void onBackPressed() {
        // Gives us functionality similar to other Google Apps using Bottom Navigation Bars such as Youtube
        FragmentManager fm = getSupportFragmentManager();
        // If nothing present in backstack, exit app on back
        if(fm.getBackStackEntryCount() == 0){
            super.onBackPressed();
            finishAffinity();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Preferred method of negotiating IllegalStateException according to Internet
        // Used when permission is denied in pop-up and application resumes
        if(permissionDenied){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.my_frame, new ErrorFragment());
            transaction.commit();

            // Reset boolean
            permissionDenied = false;
        }
    }

    // Generates new ID number for community-generated events
    public Long getFreshId(){
        Long newFreshId = Long.valueOf(currentId + 1);
        // Current ID should update once the following call is made
        mEventReference.child("id").setValue(newFreshId);
        return newFreshId;
    }

    public void writeNewEvent(Event event, boolean isPhotoSubmitted) {
        String eventId = getFreshId().toString();
        mEventReference.child("events").child(eventId).setValue(event);

        if(isPhotoSubmitted) {
            // Going to rewrite what is here each time
            StorageReference eventStorage = mStorageRef.child("photos/" + (eventId));
            eventStorage.putBytes(photo)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.d("GOOD JOB", "url is " + downloadUrl.toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Log.d("BAD JOB", "sad sad");
                        }
                    });
        }
    }

//    private void writeNewEvent(String description, String name, String id, String placeName, String country, String city, String startTime, String latitude, String longitude) {
//        Event event = new Event(description, name, id, placeName, country, city, startTime, latitude, longitude);
//        mEventReference.child("events").child(getFreshId().toString()).setValue(event);
//    }

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

    // Gets details like Cover Photo from Events from Facebook
    private void getExtraEventDetails() {
        Bundle bundle = new Bundle();
        // Add extra fields to this bundle of shit you want to receive
        bundle.putString("fields", "cover");
        Log.d("IN", "getExtraEventsDetails");
        // Use public variable parsedEventsList
        for(final Event event : facebookEvents){
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + event.id,
                    bundle,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            JSONObject responseJSONObject = response.getJSONObject();
                            Log.d("RESPONSE " + event.name, responseJSONObject.toString());
                            if (responseJSONObject != null && responseJSONObject.has("cover")) {
                                try {
                                    Log.d("Has", "Cover");
                                    JSONObject cover = responseJSONObject.getJSONObject("cover");
                                    event.coverURL = cover.getString("source");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("FINISHED", "getExtraEventDetails");

//                                // Task is finished, decrement the counter
//                                int remainingTasks = workCounter.decrementAndGet();
//                                Log.d("Decrementing counter", "GetExtraEventDetails");
//                                // If all tasks are completed
//                                if(remainingTasks == 0) {
//                                    Log.d("No more tasks", "GetExtraEventDetails");
//                                    setCombinedEvents();
//                                }
                            }
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

    private void setCombinedEvents(){
        Log.d("IN COMBINED", "start");

        combinedEvents = new ArrayList<Event>();

        // At this point both tasks should be finished and arraylists populated
        if(facebookEvents != null) {
            combinedEvents.addAll(facebookEvents);
        }
        if(databaseEvents != null) {
            combinedEvents.addAll(databaseEvents);
        }
        
        // Who requested the data for their fragment?
        if(MY_EVENTS_REQUESTED){
            setUpMyEventsFragmentWithData();
            MY_EVENTS_REQUESTED = false;
        } else if(NEARBY_EVENTS_REQUESTED){
            setUpEventsNearMeFragmentWithData();
            NEARBY_EVENTS_REQUESTED = false;
        }

    }

    // Takes in name of fragment that requested fresh data
    public void refreshData() {
        // Only MyEvents can refresh data
        MY_EVENTS_REQUESTED = true;

        // Database automatically updates data into combinedEvents / databaseEvents
        // Therefore we don't need to call it here for refresh

        // We do need to get fresh fb data though
        // New task so increment counter
        workCounter.incrementAndGet();
        getData();
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

    private class GetEvents extends AsyncTask<Void, Void, Void> {
        // Generated data
        public ArrayList<Event> eventsList;

        private final AtomicInteger workCounter;
        private JSONObject unparsedData;
        private int whoRequested;

        public GetEvents(AtomicInteger workCounter, JSONObject unparsedData) {
            this.workCounter = workCounter;
            this.unparsedData = unparsedData;
            this.whoRequested = whoRequested;
        }

        @Override
        protected void onPreExecute() {
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

            if(unparsedData.toString() != null) {
                try {
                    JSONArray events = unparsedData.getJSONArray("data");
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
                            } else{
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
                            } else {
                                if(placeName.equals("")){
                                    placeName = "No location given";
                                }
                                country = "";
                                city = "";
                                latitude = "";
                                longitude = "";
                            }
                        } else {
                            placeName = "No location given";
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
//            setParsedEventsList(eventsList);
            facebookEvents = eventsList;

            // Call extra event details
            getExtraEventDetails();

            // Task is finished, decrement the counter
            int remainingTasks = this.workCounter.decrementAndGet();
            Log.d("Decrementing counter", "GetEvents");
            // If all tasks are completed
            if(remainingTasks == 0) {
                Log.d("No more tasks", "GetEVents");
                setCombinedEvents();
            }
        }
    }


}
