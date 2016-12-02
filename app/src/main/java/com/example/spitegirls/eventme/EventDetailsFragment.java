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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsFragment extends Fragment{

    private Event details;
    private TextView title;
    private TextView description;
    private TextView startDate;
    private ImageView cover;
    private View line;
    private TextView eventPhotos;

    private StorageReference mStorageRef;

    public static EventDetailsFragment newInstance(Bundle details) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        if(details != null){
            fragment.setArguments(details);

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

                // Set up info
                title = (TextView) view.findViewById(R.id.title);
                title.setText(details.name);

                description = (TextView) view.findViewById(R.id.description);
                if(details.description != null && !details.description.isEmpty()){
                    description.setText(details.description);
                } else {
                    description.setVisibility(View.GONE);
                }

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
                    Log.d("Image source is ", "source is null");
                    view.findViewById(R.id.cover_photo).setVisibility(View.GONE);

                    line = (View) view.findViewById(R.id.viewLine2);
                    line.setVisibility(View.GONE);

                    eventPhotos = (TextView) view.findViewById(R.id.event_photos);
                    eventPhotos.setVisibility(View.GONE);
                    getFirebasePhotos();
                }

                Log.d("CALENDAR IS", details.getCalendarDate().toString());
            }
        }

        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Resources resources = getResources();

                String shareBody = getString(R.string.share_interested) +
                        getString(R.string.share_event_name) + title.getText().toString()
                        + getString(R.string.text_description)
                        + description.getText().toString() + getString(R.string.share_start_time)
                        + startDate.getText().toString() + "\n\n"
                        + getString(R.string.share_find_out_more);

                String shareSubject = getString(R.string.share_subject);


                PackageManager pm = getActivity().getPackageManager();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");

                Intent blankIntent = new Intent();
                blankIntent.setAction(Intent.ACTION_SEND);
                Intent openInChooser = Intent.createChooser(blankIntent, getString(R.string.share_via));

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
                // Launch chooser for sharing
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

    // Retrieves images from Firebase database to display within event details
    private void getFirebasePhotos(){
        StorageReference eventStorage = mStorageRef.child("photos/" + details.id);

        // Buffer limit for download from storage to optimise performance
        final long ONE_MEGABYTE = 1024 * 1024;
        eventStorage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
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
