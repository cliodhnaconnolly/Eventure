package com.example.spitegirls.eventme;

import android.content.Intent;
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
    private AccessToken accessToken;
    private JSONObject personalDetails;
    private JSONObject eventDetails;
//    public HashSet<HashMap<String, String>> eventList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());

//        eventList = new HashSet<HashMap<String, String>>();

        Bundle inBundle = getIntent().getExtras();
        final String name = inBundle.get("name").toString();
        final String surname = inBundle.get("surname").toString();
        final String imageUrl = inBundle.get("imageUrl").toString();

        BottomNavigationView bottomBar = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomBar.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        switch (item.getItemId()) {
                            case R.id.my_events:
                                getEventDetails();
                               // decipherEvents();
                                //new GetEvents().execute(eventDetails.toString());
                                if(eventDetails != null){
                                    Bundle args = new Bundle();
                                    String eventDetailsString = eventDetails.toString();
                                    args.putString("allEvents", eventDetailsString);
                                    eventsFragment = MyEventsFragment.newInstance(args);
                                    transaction.replace(R.id.my_frame, eventsFragment);
                                    transaction.addToBackStack(null);
                                    transaction.commit();
                                } else {
                                    transaction.replace(R.id.my_frame, new MyEventsFragment());
                                    transaction.addToBackStack(null);
                                    transaction.commit();
                                }
                                break;
                            case R.id.events_near_me:
                                transaction.replace(R.id.my_frame, new EventsNearMeFragment());
                                transaction.addToBackStack(null);
                                transaction.commit();
                                break;
                            case R.id.create_event:
                                transaction.replace(R.id.my_frame, new CreateEventFragment());
                                transaction.addToBackStack(null);
                                transaction.commit();
                                break;
                            case R.id.my_account:
                                Log.d("NAME IS ", "Name is " + name);
                                accountFragment = MyAccountFragment.newInstance(name, surname, imageUrl);
                                transaction.replace(R.id.my_frame, accountFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                                break;
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

        new GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/{event-id}",
            null,
            HttpMethod.GET,
            new GraphRequest.Callback() {
                public void onCompleted(GraphResponse response) {
                    /* handle the result */
                    Log.d("RESPONSE", "<" + response.toString() + ">");
                }
            }
                ).executeAsync();
//        Bundle parameters2 = new Bundle();
//        parameters2.putString("fields", "id,name,picture");
//        eventRequest.setParameters(parameters2);
//        eventRequest.executeAsync();
//        Log.d("PARAMETERS 2 HAS", "<" + parameters2.toString() + ">");


    }

    public void decipherEvents(){
        try {
            JSONObject events = personalDetails.getJSONObject("events");
            Log.d("HOW MANY?", "This many " + events.length());
        } catch (JSONException e) { e.printStackTrace(); }


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

    // Used help from on how to parse
    // http://www.androidhive.info/2012/01/android-json-parsing-tutorial/

    // Currently in MyEventsFragment

//    private class GetEvents extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected String doInBackground(String... params){
//            if(params.toString() != null) {
//                try {
//                    JSONObject jsonObj = new JSONObject(params.toString());
//
//                    Log.d("INITIAL PARAMS", params.toString());
//
//                    JSONArray events = jsonObj.getJSONArray("events");
//
//                    for (int i = 0; i < events.length(); i++) {
//                        JSONObject event = events.optJSONObject(i);
//
//                        String description = event.getString("description");
//                        String name = event.getString("name");
//                        String startTime = event.getString("start_time");
//                        String id = event.getString("id");
//
//                        JSONObject location = event.getJSONObject("location");
//                        String country = location.getString("country");
//                        String city = location.getString("city");
//                        String latitude = location.getString("latitude");
//                        String longitude = location.getString("longitude");
//
//                        HashMap<String, String> eventMap = new HashMap<>();
//                        eventMap.put("description", description);
//                        eventMap.put("name", name);
//                        eventMap.put("startTime", startTime);
//                        eventMap.put("id", id);
//                        eventMap.put("country", country);
//                        eventMap.put("city", city);
//                        eventMap.put("latitude", latitude);
//                        eventMap.put("longitude", longitude);
//
//                        eventList.add(eventMap);
//                    }
//                } catch (final JSONException e) {
//                    Log.e(TAG, "Json parsing error: " + e.getMessage());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(),
//                                    "Json parsing error: " + e.getMessage(),
//                                    Toast.LENGTH_LONG)
//                                    .show();
//                        }
//                    });
//                }
//            }
//            else {
//                Log.e(TAG, "Couldn't get json from server.");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),
//                                "Couldn't get json from server. Check LogCat for possible errors!",
//                                Toast.LENGTH_LONG)
//                                .show();
//                    }
//                });
//
//            }
//
//            return null;
//        }
//
//
//
//    }
}
