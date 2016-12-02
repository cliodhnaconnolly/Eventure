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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

    private AlertDialog alertMessage;

    private Context mContext;
    private LocationManager lm;

    private ArrayList<Event> eventList;

    private static final int MIN_ZOOM_AT_CITY_LEVEL = 10;
    private static final int ZOOM_TO_STREET_LEVEL = 17;
    private static final int LOCATION_PERMISSION = 0;


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

        super.onStart();

        if(this.getArguments() != null) {

            checkGPS();
            mGoogleApiClient.connect();

            // Checks if locations permission given if not request it
            if (ContextCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, getString(R.string.permission_granted), Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(mContext, getString(R.string.permission_not_granted), Toast.LENGTH_LONG).show();

            // If permission denied disable EventsNearMeFragment
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.my_frame, new ErrorFragment());
            transaction.commit();
        }
        return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events_near_me, container, false);

        spinner = (ProgressBar) view.findViewById(R.id.spinnerMap);
        loadingMessage = (TextView) view.findViewById(R.id.loading_message);

        if(this.getArguments() != null) {
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
        }

        return view;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Bundle bundle = this.getArguments();
        if(bundle != null) {
            // Retrieves arraylist from bundle containing all user events
            if (bundle.getSerializable("arraylist") != null) {
                eventList = (ArrayList<Event>) bundle.getSerializable("arraylist");
            }

            // Add info to marker
            for(Event currEvent : eventList) {
                if (eventCheck(currEvent)) {
                    try {
                        Double latitude = Double.parseDouble(currEvent.latitude);
                        Double longitude = Double.parseDouble(currEvent.longitude);
                        LatLng FirstEvent = new LatLng(latitude, longitude);
                        if (currEvent.placeName != null && currEvent.startTime != null) {
                            Marker marker = mMap.addMarker(new MarkerOptions().position(FirstEvent).title(currEvent.name).snippet("Place: " + currEvent.placeName + ". Time: " + currEvent.getReadableDate()));
                            marker.setTag(currEvent);
                        } else if (currEvent.placeName == null && currEvent.startTime != null) {
                            Marker marker = mMap.addMarker(new MarkerOptions().position(FirstEvent).title(currEvent.name).snippet("Time: " + currEvent.getReadableDate()));
                            marker.setTag(currEvent);
                        } else if (currEvent.placeName != null && currEvent.startTime == null) {
                            Marker marker = mMap.addMarker(new MarkerOptions().position(FirstEvent).title(currEvent.name).snippet("Place: " + currEvent.placeName));
                            marker.setTag(currEvent);
                        } else {
                            Marker marker = mMap.addMarker(new MarkerOptions().position(FirstEvent).title(currEvent.name));
                            marker.setTag(currEvent);
                        }

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Sets camera to user location
            moveCameraUserLocation();

            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(true);

            mMap.setOnInfoWindowClickListener(this);
            mMap.setMinZoomPreference(MIN_ZOOM_AT_CITY_LEVEL);
        }
    }

    private void moveCameraUserLocation(){
        try {
            // Set up Location Manager
            lm = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));

            // Check GPS is enabled if lm indicates problem
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                checkGPS();
            }

            // If GPS Provider has a last known location use this
            if (lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                mapView.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.INVISIBLE);
                loadingMessage.setVisibility(View.INVISIBLE);

                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_TO_STREET_LEVEL));
            }

            // If Network Provider has a last known location use this
            else if (lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
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
            }



        }catch (SecurityException e){
            // Exception caught, dealt with by Requesting Permissions but brief moment in time
            // Before RequestingPermissions result shuts down fragment
        }
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

            transaction.commit();
        }
    }

    // Checks year of event to see should it show up on map
    private boolean eventCheck(Event currEvent){
        String year = currEvent.startTime.substring(0, 4);

        Calendar myCal = Calendar.getInstance();
        int currYear = myCal.get(Calendar.YEAR);

        if(Integer.parseInt(year) != currYear){
            return false;
        } else {
            return true;
        }
    }

    // Location Listener Methods
    public void onLocationChanged(Location location) {
        // Set up map to have camera at user location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_TO_STREET_LEVEL));

        // Removing loading screen and have map viewable to user
        spinner.setVisibility(View.INVISIBLE);
        mapView.setVisibility(View.VISIBLE);
        loadingMessage.setVisibility(View.INVISIBLE);

        // Remove burden of checking location as it is no longer needed
        if (ActivityCompat.checkSelfPermission((Activity) mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission((Activity) mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    
    private void checkGPS() {
        LocationManager manager = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Launch Alert dialog if user does not have location turned on
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setMessage(getString(R.string.error_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.settings_option), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            dialog.cancel();

                        }
                    })
                    .setNegativeButton(getString(R.string.continue_option), new DialogInterface.OnClickListener() {
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
            alertMessage = alertDialog.create();
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
        super.onResume();

        // Needed to close dialog when coming back from settings on older versions of Android
        if(alertMessage != null && alertMessage.isShowing()){
            alertMessage.cancel();
        }

        if(this.getArguments() != null) {
            mapView.onResume();
            if(mMap != null){
                moveCameraUserLocation();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(this.getArguments() != null){
            mapView.onDestroy();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(this.getArguments() != null){
            mapView.onPause();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(this.getArguments() != null){
            mapView.onLowMemory();
        }
    }

    // When orientation changes we want to maintain the item in bottom nav
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int eventsNearby = 1;

        BottomNavigationView bottomNavigationView;

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(eventsNearby).setChecked(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(eventsNearby).setChecked(true);
        }
    }

}
