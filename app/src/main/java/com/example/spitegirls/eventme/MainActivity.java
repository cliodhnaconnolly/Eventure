package com.example.spitegirls.eventme;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.facebook.GraphRequest.TAG;

// This is the most useful thing I've found RE:fragments
// https://guides.codepath.com/android/Creating-and-Using-Fragments#fragment-lifecycle

public class MainActivity extends AppCompatActivity implements MyAccountFragment.OnItemSelectedListener {

    MyAccountFragment accountFragment;
    MyEventsFragment eventsFragment;
    HashMap<String, String> sources;
    private AccessToken accessToken;
    private JSONObject personalDetails;
    private JSONObject eventDetails;
//    public HashSet<HashMap<String, String>> eventList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());
        sources = new HashMap<String, String>();

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
                        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        if(isNetworkAvailable()) {
                            switch (item.getItemId()) {
                                case R.id.my_events:
                                    getEventDetails();

                                    // Think this if causes the loading problems
                                    if (eventDetails != null) {
                                        Bundle args = new Bundle();
                                        String eventDetailsString = eventDetails.toString();
                                        args.putString("allEvents", eventDetailsString);
                                        eventsFragment = MyEventsFragment.newInstance(args);
                                        transaction.replace(R.id.my_frame, eventsFragment);
                                        transaction.commit();
                                    } else {
                                        transaction.replace(R.id.my_frame, new MyEventsFragment());
                                        transaction.commit();
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
                                    Log.d("NAME IS ", "Name is " + name);
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
        accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.d("PERSONAL DETAILS", "<" + object.toString() + ">");
                        personalDetails = object;
                        try {
                            eventDetails = object.getJSONObject("events");
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,events");
        request.setParameters(parameters);
        request.executeAsync();
        Log.d("PARAMETERS HAS", "<" + parameters.toString() + ">");

    }

    // Doesn't work but keeping here for the moment
//    public void getCoverPhotos() {
//        try {
//            JSONArray events = eventDetails.getJSONArray("data");
//            for(int i=0; i<events.length(); i++){
//                JSONObject event = events.optJSONObject(i);
//                Bundle coverBundle = new Bundle();
//                coverBundle.putString("fields", "cover,id");
//                // Getting cover photo from event_id
//                new GraphRequest(
//                        AccessToken.getCurrentAccessToken(),
//                        "/" + event.getString("id"),
//                        coverBundle,
//                        HttpMethod.GET,
//                        new GraphRequest.Callback() {
//                            public void onCompleted(GraphResponse response) {
//                                JSONObject responseJSONObject = response.getJSONObject();
//                                Log.d("RESPONSE IS", "<" + responseJSONObject.toString() + ">");
//                                if (responseJSONObject != null && responseJSONObject.has("cover")) {
//                                    try {
//                                        sources.put(responseJSONObject.getString("id"), responseJSONObject.getString("source"));
//                                    } catch (JSONException e) { e.printStackTrace(); }
//                                } else {
//                                    Log.d("FALSE", "ALARM");
//                                }
//
//                            }
//                        }
//                ).executeAsync();
//            }
//        } catch (JSONException e) { e.printStackTrace(); }
//    }

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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
