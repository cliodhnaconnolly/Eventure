package com.example.spitegirls.eventme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

public class MyAccountFragment extends Fragment {

    private String name;
    private String surname;
    private String imageUrl;

    private OnItemSelectedListener listener;

    public interface OnItemSelectedListener {
        public void onLogoutItemSelected(String info);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof  OnItemSelectedListener) {
            listener = (OnItemSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyAccountFragment.OnItemSelectedListener");
        }
    }

    public void onLogoutClick(View view) {
        listener.onLogoutItemSelected("Log me out!!");
    }

    public static MyAccountFragment newInstance(Bundle inBundle) {
        MyAccountFragment fragment =  new MyAccountFragment();
        fragment.setArguments(inBundle);
        return fragment;
    }

    public MyAccountFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Getting back arguments
        name = getArguments().getString("name");
        surname = getArguments().getString("surname");
        imageUrl = getArguments().getString("imageUrl");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);
        Button btn = (Button) view.findViewById(R.id.logout_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLogoutClick(view);
            }
        });

        final Switch toggleTheme = (Switch) view.findViewById(R.id.switch1);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean switchActive = preferences.getBoolean("switchActive", false);
        if(switchActive){
            toggleTheme.setChecked(true);
        }

        toggleTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Switch Theme Colours")
                        .setMessage("Are you sure you want to change the theme colour? App will restart if you do so.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                                SharedPreferences.Editor editor = preference.edit();

                                // First check existing state
                                boolean preexisting = preference.getBoolean("alternateTheme", false);
                                // Depending on result theme is set
                                if(preexisting){
                                    editor.putBoolean("alternateTheme", false);
                                } else {
                                    editor.putBoolean("alternateTheme", true);
                                }

                                // So that we can check should switch be active
                                preexisting = preference.getBoolean("switchActive", false);
                                if(preexisting){
                                    editor.putBoolean("switchActive", false);
                                } else {
                                    editor.putBoolean("switchActive", true);
                                }

                                editor.commit();

                                getActivity().finish();
                                final Intent intent = getActivity().getIntent();
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                                getActivity().startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Undo toggle
                                toggleTheme.setChecked(false);
                            }
                        })
                        .setIcon(R.mipmap.ic_alert)
                        .show();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextView nameView = (TextView) view.findViewById(R.id.nameAndSurname);
        nameView.setText("" + name + " " + surname);
        new DownloadImage((ImageView) view.findViewById(R.id.profileImage)).execute(imageUrl);
    }

    // When orientation changes we want to maintain the item in bottom nav
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        BottomNavigationView bottomNavigationView;

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(3).setChecked(true);
            Log.d("ORIENTATION LANDSCAPE", "tried");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(3).setChecked(true);
        }
    }
}