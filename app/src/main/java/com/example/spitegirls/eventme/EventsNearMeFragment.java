package com.example.spitegirls.eventme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;


public class EventsNearMeFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private MapView mapView;
    private TextView loadingMessage;
    private ProgressBar spinner;

    private Context mContext;
    private LocationManager lm;

    private ArrayList<Event> eventList;

    private static final int LOCATION_IS_NOT_ON = 1;
    private static final int NO_LOCATION_PERMISSION = 2;
    private static final int MIN_ZOOM_AT_CITY_LEVEL = 10;
    private static final int ZOOM_TO_STREET_LEVEL = 17;


    public static EventsNearMeFragment newInstance(Bundle bundle) {
        EventsNearMeFragment fragment = new EventsNearMeFragment();
        if(bundle != null){
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        checkGPS(LOCATION_IS_NOT_ON);
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //checkGPS(LOCATION_IS_NOT_ON);

        View view = inflater.inflate(R.layout.fragment_events_near_me, container, false);

        spinner = (ProgressBar) view.findViewById(R.id.spinnerMap);
        loadingMessage = (TextView) view.findViewById(R.id.loading_message);

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.setVisibility(View.INVISIBLE);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient
                .Builder(mContext )
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        return view;
    }

//    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Bundle bundle = this.getArguments();
        // Got a null pointer exception here when I went straight to here from my account (no my events)
        if(bundle.getSerializable("arraylist") != null) {
            eventList = (ArrayList<Event>) bundle.getSerializable("arraylist");
        }

        // Add info to marker
        // https://developers.google.com/maps/documentation/android-api/marker
        // Info on infoWindowClickListener also on that page
        for(int i = 0; i < eventList.size(); i++){
            Event currEvent = eventList.get(i);
            if(eventCheck(currEvent)){
                Log.d("STARTING TO ADD MARKER", currEvent.name);
                try {
                    Double latitude = Double.parseDouble(currEvent.latitude);
                    Double longitude = Double.parseDouble(currEvent.longitude);
                    LatLng FirstEvent = new LatLng(latitude, longitude);
                    if (currEvent.placeName != null && currEvent.startTime != null) {
                        Marker marker = mMap.addMarker(new MarkerOptions().position(FirstEvent).title(currEvent.name).snippet("Place: " + currEvent.placeName + ". Time: " + currEvent.startTime));
                        marker.setTag(currEvent);
                    }
                    else if(currEvent.placeName == null && currEvent.startTime != null) {
                        Marker marker = mMap.addMarker(new MarkerOptions().position(FirstEvent).title(currEvent.name).snippet("Time: " + currEvent.startTime));
                        marker.setTag(currEvent);
                    }
                    else if(currEvent.placeName != null && currEvent.startTime == null){
                        Marker marker = mMap.addMarker(new MarkerOptions().position(FirstEvent).title(currEvent.name).snippet("Place: " + currEvent.placeName));
                        marker.setTag(currEvent);
                    }
                    else {
                        Marker marker = mMap.addMarker(new MarkerOptions().position(FirstEvent).title(currEvent.name));
                        marker.setTag(currEvent);
                    }

                    Log.d("ADDED MARKER", currEvent.name);
                }catch(NumberFormatException e){
                    e.printStackTrace();
                }
            }
            else {
                Log.d("NO MARKER, YEAR IS:", currEvent.startTime.substring(0,4));
            }
        }

        mMap.setOnInfoWindowClickListener(this);
        mMap.setMinZoomPreference(MIN_ZOOM_AT_CITY_LEVEL);

        // Set up Location Manager
        lm = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));

        // IF LOCATION MANAGER IS NULL LoCATION IS NOT ON CHECK HERE
        // IF LOCATION MANAGER THROWS SECURITY EXCEPTION SET CHECK HERE

        // If GPS Provider has a last known location use this
        if(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
            Log.d("IN IF", "YES");
            mapView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.INVISIBLE);
            loadingMessage.setVisibility(View.INVISIBLE);

            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_TO_STREET_LEVEL));
        }
        // If Network Provider has a last known location use this
        else if(lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null){
            Log.d("IN IF", "YEAH");
            mapView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.INVISIBLE);
            loadingMessage.setVisibility(View.INVISIBLE);

            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_TO_STREET_LEVEL));
        }
        // If none of the above have a last known location go about it the slow way
        else {
            // Look for Updates on Location
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.0f, this);
            Log.d("IN ELSE", "HELLO");
        }


//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getCoords(), ZOOM_TO_STREET_LEVEL));

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    // Called when user clicks a marker info window
    @Override
    public void onInfoWindowClick(final Marker marker) {
        Bundle detailsBundle = new Bundle();
        detailsBundle.putSerializable("event", (Event) marker.getTag());
        EventDetailsFragment eventFrag = EventDetailsFragment.newInstance(detailsBundle);
        if(marker.isVisible()) {    // if marker source is visible (i.e. marker created)
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.my_frame, eventFrag);

            //transaction.addToBackStack(null);   // Since we're in a tab we want to be able to go back internally
            // BUG ALERT
            // Ideally we want the user to be able to back to the map but Google has a bug where it counts
            // this as having multiple map fragments. Below is an article detailing workarounds
            // I've not implemented them yet but if anyone else wants to try go ahead
            // http://www.aphex.cx/the_google_maps_api_is_broken_on_android_5_here_s_a_workaround_for_multiple_map_fragments/

            transaction.commit();
        }
    }

    // Checks year of event to see should it show up on map
    private boolean eventCheck(Event currEvent){
        String year = currEvent.startTime.substring(0, 4);
        Log.d("YEAR", year);

        Calendar myCal = Calendar.getInstance();
        int currYear = myCal.get(Calendar.YEAR);

        if(Integer.parseInt(year) != currYear){
            return false;
        } else {
            return true;
        }
    }


    // Commenting out so not sued but Niamh can see for how she previously did checks
//    private LatLng getCoords() {
//        LatLng coords = new LatLng(53.3053, -6.2207); //Set default to UCD so the camera has somewhere to go/
//        try {
//            lm = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));
//
//            Log.d("ENABLED", "GPS IS " + lm.isProviderEnabled(LocationManager.GPS_PROVIDER));
//            Log.d("ENABLED", "NETWORK IS " + lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
//
//            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.0f, this);
//
//            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            coords = new LatLng(location.getLatitude(), location.getLongitude());
//
//        }catch (NullPointerException e){
//            checkGPS(LOCATION_IS_NOT_ON);
//        }catch(SecurityException s){
//            checkGPS(NO_LOCATION_PERMISSION);
//        }
//
//        return coords;
//    }

    // Location Listener Methods
    public void onLocationChanged(Location location) {
        // Set up map to have camera at user location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_TO_STREET_LEVEL));

        // Removing loading screen and have map viewable to user
        spinner.setVisibility(View.INVISIBLE);
        mapView.setVisibility(View.VISIBLE);
        loadingMessage.setVisibility(View.INVISIBLE);

        // Remove burden of checking location as it is no longer needed
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lm.removeUpdates(this);
        }
        lm.removeUpdates(this);
    }

    // Location Listener Methods
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    // Location Listener Methods
    public void onProviderEnabled(String provider) {
    }

    // Location Listener Methods
    public void onProviderDisabled(String provider) {
    }
    
    private void checkGPS(int check) {
        LocationManager manager = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));
        if (check == LOCATION_IS_NOT_ON){
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setMessage("GPS doesn't seem to be on. You can still view the map but your location will not be detected")
                        .setCancelable(false)
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                dialog.cancel();
                                return;
                            }
                        })
                        .setNegativeButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                                Bundle bundle = new Bundle();
                                bundle.putInt("appearance", ErrorFragment.NO_LOCATION_APPEARANCE);
                                ErrorFragment fragment = ErrorFragment.newInstance(bundle);
                                transaction.replace(R.id.my_frame, fragment);
                                transaction.commit();
                                dialog.cancel();
                            }
                        });
                final AlertDialog alertMessage = alertDialog.create();
                alertMessage.show();
            }
        }

        else if( check == NO_LOCATION_PERMISSION){
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setMessage("App needs permission before displaying Events Nearby")
                    .setCancelable(false)
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            if (mContext == null) {
                                return;
                            }
                            final Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            mContext.startActivity(intent);

                            dialog.cancel();
                        }
                    });

            final AlertDialog alertMessage = alertDialog.create();
            alertMessage.show();
        }

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // When orientation changes we want to maintain the item in bottom nav
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        BottomNavigationView bottomNavigationView;

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
        }
    }

}
