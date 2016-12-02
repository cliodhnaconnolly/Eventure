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

    private boolean myEventsRequested = false;
    private boolean nearbyEventsRequested = false;
    private static final int NUMBER_OF_TASKS = 2;

    private AtomicInteger workCounter;

    private byte[] photo;

    public boolean isPhotoSubmitted = false;

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
                myEventsRequested = true;
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
                                    if(combinedEvents == null && myEventsRequested) {
                                        transaction.replace(R.id.my_frame, new MyEventsFragment());
                                        transaction.commit();
                                    }
                                    // Data not ready, user previously requested it in NearbyEvents tab
                                    else if(combinedEvents == null && myEventsRequested) {
                                        // Update user requested tab
                                        myEventsRequested = true;
                                        nearbyEventsRequested = false;

                                        // Continue to load blank fragment until data is ready
                                        transaction.replace(R.id.my_frame, new MyEventsFragment());
                                        transaction.commit();
                                    }
                                    // Data not ready, not previously requested
                                    else if(combinedEvents == null && !myEventsRequested && !nearbyEventsRequested) {
                                        transaction.replace(R.id.my_frame, new MyEventsFragment());
                                        transaction.commit();
                                        getData();
                                        myEventsRequested = true;
                                    }
                                    else {
                                        setUpMyEventsFragmentWithData();
                                    }

                                    break;
                                case R.id.events_near_me:
                                    // Makes sure data is pulled in case they go to map first. Sets up events for events near me fragment.
                                    // Data not ready, user changed tab was previously requested from My Events
                                    if (combinedEvents == null && myEventsRequested) {
                                        // Update user requested tab
                                        nearbyEventsRequested = true;
                                        myEventsRequested = false;

                                        // Data is being requested so put up blank fragment and wait
                                        transaction.replace(R.id.my_frame, new EventsNearMeFragment());
                                        transaction.commit();

                                    }
                                    // Data not ready, user repeated request of current tab
                                    else if(combinedEvents == null && nearbyEventsRequested) {
                                        // Refresh blank fragment purely for them to feel better about waiting all the milliseconds
                                        transaction.replace(R.id.my_frame, new EventsNearMeFragment());
                                        transaction.commit();
                                    }
                                    // Data not ready, data not previously requested
                                    else if(combinedEvents == null && !myEventsRequested && !nearbyEventsRequested) {
                                        transaction.replace(R.id.my_frame, new EventsNearMeFragment());
                                        transaction.commit();
                                        getData();
                                        nearbyEventsRequested = true;
                                    }
                                    // Data is ready, load fragment with data
                                    else {
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
        // Database does its own thing so we only need to retrieve info from FB here

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

        // Gets current id value from DB
        ValueEventListener idListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get id and update values
                // dataSnapshot returns a hashmap, eg. {id=0}
                HashMap idResult  = (HashMap) dataSnapshot.getValue();

                if(idResult.get("id").getClass() == Long.class ){
                    Long id = (Long) idResult.get("id");
                    currentId = id.intValue();
                } else if (idResult.get("id").getClass() == String.class){
                    int id = Integer.parseInt((String) idResult.get("id"));
                    currentId = id;
                } else {
                    Log.d("ERROR", "failed to retrieve ID from database");
                }
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
                HashMap results = (HashMap) dataSnapshot.getValue();

                // Reset db events list
                databaseEvents = new ArrayList<Event>();

                // Since DB will update its data anytime there is change let it increment a task to be done
                if(workCounter.get() == 0) {
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
                    }
                }

                // Task of retrieving events from Database is complete
                int remainingTasks = workCounter.decrementAndGet();

                // If no more tasks remain
                if(remainingTasks == 0) {
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
                isPhotoSubmitted = true;

            } catch (FileNotFoundException e) { e.printStackTrace(); }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_LONG).show();

            // If permission denied disable EventsNearMeFragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.my_frame, new ErrorFragment());
            transaction.commit();
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

    public void writeNewEvent(Event event) {
        String eventId = getFreshId().toString();
        mEventReference.child("events").child(eventId).setValue(event);

        if(isPhotoSubmitted) {
            // Going to rewrite what is here each time
            StorageReference eventStorage = mStorageRef.child("photos/" + (eventId));
            eventStorage.putBytes(photo)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("PHOTO UPLOAD", "SUCCESS");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d("PHOTO UPLOAD", "FAILURE");
                        }
                    });
            isPhotoSubmitted = false;
        }
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

    // Gets details like Cover Photo from Events from Facebook
    private void getExtraEventDetails() {
        Bundle bundle = new Bundle();
        bundle.putString("fields", "cover");

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
                            if (responseJSONObject != null && responseJSONObject.has("cover")) {
                                try {
                                    JSONObject cover = responseJSONObject.getJSONObject("cover");
                                    event.coverURL = cover.getString("source");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            ).executeAsync();
        }

    }

    private void setUpMyEventsFragmentWithData() {
        Bundle args = new Bundle();
        args.putSerializable("arraylist", combinedEvents);

        eventsFragment = MyEventsFragment.newInstance(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.my_frame, eventsFragment);
        transaction.commit();
    }

    private void setUpEventsNearMeFragmentWithData(){
        Bundle args = new Bundle();
        args.putSerializable("arraylist", combinedEvents);

        eventsNearFragment = EventsNearMeFragment.newInstance(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.my_frame, eventsNearFragment);
        transaction.commit();
    }

    private void setCombinedEvents(){

        combinedEvents = new ArrayList<Event>();

        // At this point both tasks should be finished and arraylists populated
        if(facebookEvents != null) {
            combinedEvents.addAll(facebookEvents);
        }
        if(databaseEvents != null) {
            combinedEvents.addAll(databaseEvents);
        }
        
        // Set up fragment that requested data
        if(myEventsRequested){
            setUpMyEventsFragmentWithData();
            myEventsRequested = false;
        } else if(nearbyEventsRequested){
            setUpEventsNearMeFragmentWithData();
            nearbyEventsRequested = false;
        }

    }

    public void refreshData() {
        // Only MyEvents can refresh data
        myEventsRequested = true;

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

    private class GetEvents extends AsyncTask<Void, Void, Void> {
        // Generated data
        public ArrayList<Event> eventsList;

        private final AtomicInteger workCounter;
        private JSONObject unparsedData;

        public GetEvents(AtomicInteger workCounter, JSONObject unparsedData) {
            this.workCounter = workCounter;
            this.unparsedData = unparsedData;
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

                    for (int i = 0; i < events.length(); i++) {
                        JSONObject event = events.optJSONObject(i);

                        if (event.has("description")) {
                            description = event.getString("description");
                        } else {
                            description = "No description given";
                        }

                        if (event.has("name")) {
                            name = event.getString("name");
                        } else {
                            name = "No title given";
                        }

                        if (event.has("start_time")) {
                            startTime = event.getString("start_time");
                        } else {
                            startTime = "No time given";
                        }

                        // Can't be null
                        id = event.getString("id");

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
                    Log.e("GET EVENTS", "Json parsing error: " + e.getMessage());
                }
            }
            else {
                Log.e("GET EVENTS", "Couldn't get json from server.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            facebookEvents = eventsList;

            // Call extra event details
            getExtraEventDetails();

            // Task is finished, decrement the counter
            int remainingTasks = this.workCounter.decrementAndGet();

            // If all tasks are completed
            if(remainingTasks == 0) {
                setCombinedEvents();
            }
        }
    }

}
