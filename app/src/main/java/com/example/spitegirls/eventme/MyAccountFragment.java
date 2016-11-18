package com.example.spitegirls.eventme;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextView nameView = (TextView) view.findViewById(R.id.nameAndSurname);
        nameView.setText("" + name + " " + surname);
        new DownloadImage((ImageView) view.findViewById(R.id.profileImage)).execute(imageUrl);
    }

}