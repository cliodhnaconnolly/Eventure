package com.example.spitegirls.eventme;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Clíodhna on 08/11/2016.
 */

public class EventsNearMeFragment extends Fragment {

    public static EventsNearMeFragment newInstance() {
        return new EventsNearMeFragment();
    }

    public EventsNearMeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events_near_me, container, false);
    }

}
