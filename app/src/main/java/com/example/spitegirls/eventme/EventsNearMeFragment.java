package com.example.spitegirls.eventme;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


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
<<<<<<< HEAD

    @Override
=======
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
>>>>>>> 88e6a99bcb499f7c23fb333ef52ff4f3936f27f8
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
        View view = inflater.inflate(R.layout.fragment_events_near_me, container, false);

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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }
}


