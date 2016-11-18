package com.example.spitegirls.eventme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class EventsNearMeFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private MapView mapView;
    private Context mContext;
    private ArrayList<Event> eventList;

    public static EventsNearMeFragment newInstance(Bundle bundle) {
        EventsNearMeFragment fragment = new EventsNearMeFragment();
        if(bundle != null){
            fragment.setArguments(bundle);
//            Log.d("INPUT TO ", bundle.toString());
//            Log.d("SET ARGS", fragment.getArguments().toString());

        }
        return fragment;
    }
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        eventList = new ArrayList<Event>();
//    }
//

//    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Bundle bundle = this.getArguments();
        if(bundle.getSerializable("arraylist") != null) {
            eventList = (ArrayList<Event>) bundle.getSerializable("arraylist");
        }
        for(int i = 0; i < eventList.size(); i++){
            Event currEvent = getEvent(i);
            if(eventCheck(currEvent) == true){
                Log.d("STARTING TO ADD MARKER", currEvent.name);
                LatLng FirstEvent = new LatLng(Double.parseDouble(currEvent.latitude), Double.parseDouble(currEvent.longitude));
                mMap.addMarker(new MarkerOptions().position(FirstEvent).title(currEvent.name).snippet(currEvent.startTime));
                Log.d("ADDED MARKER", currEvent.name);
            }
            else {
                Log.d("NO MARKER, YEAR IS:", currEvent.startTime.substring(0,4));
            }

        }

        mMap.setMinZoomPreference(10);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getCoords(), 17));

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    private boolean eventCheck(Event currEvent){
        String year = currEvent.startTime.substring(0, 4);
        Log.d("YEAR", year);
        int currYear = 2016;
        if(Integer.parseInt(year) != currYear){
            return false;
        }
        else {
            return true;
        }
    }

    private Event getEvent(int i){
        Event currEvent = eventList.get(i);
        return currEvent;
    }

    private LatLng getCoords() {
        LatLng coords = null;
        LocationManager lm = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return coords;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        coords = new LatLng(location.getLatitude(), location.getLongitude());
        return coords;
    }

    // Not meant to be using this method, deprecated = --marks
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        checkGPS();

        View view = inflater.inflate(R.layout.fragment_events_near_me, container, false);

        mapView = (MapView) view.findViewById(R.id.mapView);
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

    private void checkGPS(){
        LocationManager manager = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setMessage("GPS doesn't seem to be on. You can still view the map but your location will not be detected")
                    .setCancelable(false)
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            return; //Dialog box will disappear after user comes back for settings
                        }
                    })
                    .setNegativeButton("Continue", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
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

       // Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
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


}
