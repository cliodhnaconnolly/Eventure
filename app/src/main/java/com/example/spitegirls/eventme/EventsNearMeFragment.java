package com.example.spitegirls.eventme;

import android.content.Context;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;


public class EventsNearMeFragment extends Fragment implements OnMapReadyCallback {

<<<<<<< HEAD
    public static EventsNearMeFragment newInstance() {
        return new EventsNearMeFragment();
    }

    public EventsNearMeFragment() {

    }
  private GoogleMap mMap;

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

=======
//    public static EventsNearMeFragment newInstance() {
//        return new EventsNearMeFragment();
//    }
//
//    public EventsNearMeFragment() {
//
//    }
    private GoogleMap mMap;
    private MapView mapView;
//
//   @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//    }
//
>>>>>>> fcc3de21921976f95e771d1b6f62db6b6d4a86d3
  @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

<<<<<<< HEAD
        //Add a marker in UCD and move the camera
=======
        // Add a marker in UCD and move the camera
>>>>>>> fcc3de21921976f95e771d1b6f62db6b6d4a86d3
        LatLng UCD = new LatLng(53.3053, -6.2207);
        LatLng UCDLibrary = new LatLng(53.3068, -6.2230);
        String snippet = "Event at 2pm, Law Soc Debate";
        Marker UCDLibraryMarker = mMap.addMarker(new MarkerOptions().position(UCDLibrary).title("UCD Library").snippet(snippet));
        mMap.addMarker(new MarkerOptions().position(UCD).title("Marker in UCD"));
        mMap.setMinZoomPreference(12);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UCD, 17));
<<<<<<< HEAD
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
}
=======
        //mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
    }
>>>>>>> fcc3de21921976f95e771d1b6f62db6b6d4a86d3


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);


        return view;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

//    @Override
//    public void onDestroyView() {
//
//        Fragment f = (Fragment) getFragmentManager().findFragmentById(R.id.map);
//        if (f != null) {
//            getFragmentManager().beginTransaction().remove(f).commit();
//        }
//
//        super.onDestroyView();
//    }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mapView.onDestroy();
        }
}


