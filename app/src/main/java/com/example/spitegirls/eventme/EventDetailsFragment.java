package com.example.spitegirls.eventme;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ShareActionProvider;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsFragment extends Fragment{

    private Event details;
    private TextView title;
    private TextView description;
    private TextView startTime;
    private TextView latitude;
    private TextView longitude;
    String source;

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

    // http://stackoverflow.com/questions/32201453/hashmap-as-param-for-a-fragment
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();

            if(bundle.getSerializable("event") != null){
                details = (Event) bundle.getSerializable("event");

                // Set up info
                title = (TextView) view.findViewById(R.id.title);
                title.setText(details.name);
                description = (TextView) view.findViewById(R.id.description);
                description.setText(details.description);

                startTime = (TextView) view.findViewById(R.id.start_time_details);
                startTime.setText(details.startTime);

                TextView city = (TextView) view.findViewById(R.id.city_details);
                if(!(details.city == null) && !details.city.isEmpty()){
                    city.setText(details.city);
                } else { city.setVisibility(View.GONE); }

                TextView country = (TextView) view.findViewById(R.id.country_details);
                if(!(details.country == null) && !details.country.isEmpty()){
                    country.setText(details.country);
                } else { country.setVisibility(View.GONE); }

                latitude = (TextView) view.findViewById(R.id.latitude_details);
                if(!(details.latitude == null) && !details.latitude.isEmpty()){
                    latitude.setText(details.latitude);
                } else { latitude.setVisibility(View.GONE); }

                longitude = (TextView) view.findViewById(R.id.longitude_details);
                if(!(details.longitude == null) && !details.longitude.isEmpty()){
                    longitude.setText(details.longitude);
                } else { longitude.setVisibility(View.GONE); }

//                TextView sourceText = (TextView) view.findViewById(R.id.source_details);
//                if(!details.get("source").isEmpty()){
//                    sourceText.setText(details.get("source"));
//                } else { sourceText.setVisibility(View.GONE); }

                // Doesn't work yet but trying
//                String source = (String) bundle.getSerializable("source");
//                getCoverPhotoSource(details.get("id"));

                if( details.coverURL != null) {
                    new DownloadImage((ImageView) view.findViewById(R.id.coverPhoto)).execute(source);
                } else {
                    Log.d("Source is ", "source is null");
                    view.findViewById(R.id.coverPhoto).setVisibility(View.GONE); }
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
                        + startTime.getText().toString() + "\nLatitude: " +
                        latitude.getText().toString() + "\nLongitude: "
                        + longitude.getText().toString() + "\n\n"
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

    // Commenting out for the moment as it doesn't work but may be useful later
    // Makes Facebook Graph API call to get specific event data
//    private void getCoverPhotoSource(String id){
//
//        Bundle coverBundle = new Bundle();
//        coverBundle.putString("fields", "cover,id");
//        // Getting cover photo from event_id
//        GraphRequest request = new GraphRequest(
//                AccessToken.getCurrentAccessToken(),
//                "/" + id,
//                coverBundle,
//                HttpMethod.GET,
//                new GraphRequest.Callback() {
//                    public void onCompleted(GraphResponse response) {
//                        JSONObject responseJSONObject = response.getJSONObject();
//                        Log.d("RESPONSE IS", "<" + responseJSONObject.toString() + ">");
//                        if (responseJSONObject != null && responseJSONObject.has("cover")) {
//                            try {
////                                sources.put(responseJSONObject.getString("id"), responseJSONObject.getString("source"));
//                                source = responseJSONObject.getString("source");
//                                //Log.d("SORUCE IS NOW", "<" + source + ">");
//                                //setDone(true);
//
//                            } catch (JSONException e) { e.printStackTrace(); }
//                        } else {
//                            Log.d("FALSE", "ALARM");
//                        }
//
//                    }
//                }
//        );
//        request.executeAsync();
//    }

}
