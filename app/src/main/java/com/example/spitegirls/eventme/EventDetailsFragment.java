package com.example.spitegirls.eventme;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ShareActionProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventDetailsFragment extends Fragment{

    private Event details;
    private TextView title;
    private TextView description;
    private TextView startDate;
    private Button findOnMap;
    private ImageView cover;
    private View line;
    private TextView eventPhotos;

    private StorageReference mStorageRef;
    private StorageReference eventStorage;

    public static EventDetailsFragment newInstance(Bundle details) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        if(details != null){
            fragment.setArguments(details);
            Log.d("INPUT TO ", details.toString());
            Log.d("SET ARGS", fragment.getArguments().toString());

        }
        return fragment;
    }

    public EventDetailsFragment() {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(this.getArguments() != null){
            // Sets up storage reference
            mStorageRef = FirebaseStorage.getInstance().getReference();

            Bundle bundle = this.getArguments();

            if(bundle.getSerializable("event") != null){

                details = (Event) bundle.getSerializable("event");
//                String timeDetails = details.startTime;
//                String eventDate = getEventDate(timeDetails);
                // Set up info
                title = (TextView) view.findViewById(R.id.title);
                title.setText(details.name);
                description = (TextView) view.findViewById(R.id.description);
                description.setText(details.description);

                startDate = (TextView) view.findViewById(R.id.event_date);
                startDate.setText(details.getReadableDate());

                TextView placeName = (TextView) view.findViewById(R.id.place_Name);
                if(!(details.placeName == null) &&!(details.placeName.isEmpty())){
                    placeName.setText(details.placeName);
                } else {placeName.setVisibility(View.GONE); }

                TextView city = (TextView) view.findViewById(R.id.city_details);
                if(!(details.city == null) && !details.city.isEmpty()){
                    city.setText(details.city);
                } else { city.setVisibility(View.GONE); }

                TextView country = (TextView) view.findViewById(R.id.country_details);
                if(!(details.country == null) && !details.country.isEmpty()){
                    country.setText(details.country);
                } else { country.setVisibility(View.GONE); }

                cover = (ImageView) view.findViewById(R.id.cover_photo);
                if( details.coverURL != null) {
                    new DownloadImage(cover).execute(details.coverURL);
                } else {
                    Log.d("Source is ", "source is null");
                    view.findViewById(R.id.cover_photo).setVisibility(View.GONE);

                    line = (View) view.findViewById(R.id.viewLine2);
                    line.setVisibility(View.GONE);

                    eventPhotos = (TextView) view.findViewById(R.id.event_photos);
                    eventPhotos.setVisibility(View.GONE);
                    // Might or might not be able to return value
                    getFirebasePhotos();
                }

                if(details.longitude.equals("")){
                    findOnMap = (Button) view.findViewById(R.id.find_on_map);
                    findOnMap.setVisibility(View.GONE);
                }

                Log.d("CALENDAR IS", details.getCalendarDate().toString());
            }
        }

        // FB doesn't work because they're assholes
        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Resources resources = getResources();

                String shareBody = "I think you might be interested in this event!" +
                        "\n\nEvent Name: " + title.getText().toString() + "\n\nDescription: "
                        + description.getText().toString() + "\nStart Time: "
                        + startDate.getText().toString() + "\n\n"
                        + "Go to Eventure to find out more!";

                String shareSubject = "Event you might be interested in! - Eventure";


                PackageManager pm = getActivity().getPackageManager();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");

                Intent blankIntent = new Intent();
                blankIntent.setAction(Intent.ACTION_SEND);
                Intent openInChooser = Intent.createChooser(blankIntent, "Share via..");

                List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
                List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
                for (int i = 0; i < resInfo.size(); i++) {
                    // Making sure only what we want shows up
                    ResolveInfo resolveInfo = resInfo.get(i);
                    String packageName = resolveInfo.activityInfo.packageName;
                    // If you wish to add more options add them below to the if statement if I'm missing any
                    if(packageName.contains("twitter") || packageName.contains("sms") || packageName.contains("android.gm")
                            || packageName.contains("email") || packageName.contains("snapchat") || packageName.contains("android")
                            || packageName.contains("whatsapp") || packageName.contains("viber") || packageName.contains("skype")
                            || packageName.contains("outlook")) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
                        intent.putExtra(Intent.EXTRA_TEXT, shareBody);

                        intentList.add(new LabeledIntent(intent, packageName, resolveInfo.loadLabel(pm), resolveInfo.icon));
                    }
                }

                // Converts list with intents to array
                LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);

                openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
                startActivity(openInChooser);

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    private void getFirebasePhotos(){

        eventStorage = mStorageRef.child("photos/" + details.id);
        // Buffer limit for download from storage to optimise performance
        final long ONE_MEGABYTE = 1024 * 1024;
        eventStorage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d("GOT AN IMAGE", "" + bytes.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                cover.setImageBitmap(bitmap);
                cover.setVisibility(View.VISIBLE);
                line.setVisibility(View.VISIBLE);
                eventPhotos.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("FAILED", "to retrieve photo, id was " + details.id);
            }
        });

    }

}
