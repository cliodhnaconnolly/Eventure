package com.example.spitegirls.eventme;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class EventDetailsFragment extends Fragment{

    private Event details;
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
                TextView title = (TextView) view.findViewById(R.id.title);
                title.setText(details.name);
                TextView description = (TextView) view.findViewById(R.id.description);
                description.setText(details.description);

                TextView startTime = (TextView) view.findViewById(R.id.start_time_details);
                startTime.setText(details.startTime);

                TextView city = (TextView) view.findViewById(R.id.city_details);
                if(!details.city.isEmpty()){
                    city.setText(details.city);
                } else { city.setVisibility(View.GONE); }

                TextView country = (TextView) view.findViewById(R.id.country_details);
                if(!details.country.isEmpty()){
                    country.setText(details.country);
                } else { country.setVisibility(View.GONE); }

                TextView latitude = (TextView) view.findViewById(R.id.latitude_details);
                if(!details.latitude.isEmpty()){
                    latitude.setText(details.latitude);
                } else { latitude.setVisibility(View.GONE); }

                TextView longitude = (TextView) view.findViewById(R.id.longitude_details);
                if(!details.longitude.isEmpty()){
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
