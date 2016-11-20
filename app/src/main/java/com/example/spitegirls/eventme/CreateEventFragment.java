package com.example.spitegirls.eventme;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CreateEventFragment extends android.support.v4.app.Fragment {

    public static CreateEventFragment newInstance() {
        return new CreateEventFragment();
    }

    public CreateEventFragment() {

    }

//    private Integer year;
//    private Integer month;
//    private Integer day;
    public Calendar date;
    public Calendar dateAndTime;
    public static Button dateButton;
    public static Button timeButton;

    private RelativeLayout innerLayout;
    private RelativeLayout outerLayout;
    private LinearLayout buttonLayout;

    public static int EVENT_NAME_CHAR_LIMIT = 72;

    private EditText name;
    private EditText description;
    private EditText location;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        date = Calendar.getInstance();
        dateAndTime = Calendar.getInstance();
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        outerLayout = (RelativeLayout) view.findViewById(R.id.outerRelative);
        outerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Log.d("Relative Layouut", "IVE BEEN TOUCHED");
                hideSoftKeyboard(view);
                return false;
            }
        });

        innerLayout = (RelativeLayout) view.findViewById(R.id.innerRelative);
        innerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Log.d("Relative Layouut", "IVE BEEN TOUCHED");
                hideSoftKeyboard(view);
                return false;
            }
        });

        buttonLayout = (LinearLayout) view.findViewById(R.id.buttonLayout);
        buttonLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Log.d("ButtonLayout", "IVE BEEN TOUCHED");
                hideSoftKeyboard(view);
                return false;
            }
        });

        dateButton = (Button) view.findViewById(R.id.buttonDate);
        timeButton = (Button) view.findViewById(R.id.buttonTime);
        name = (EditText) view.findViewById(R.id.editTextEventName);
        description = (EditText) view.findViewById(R.id.editTextDescription);
        location = (EditText) view.findViewById(R.id.editTextLocation);

        view.findViewById(R.id.buttonSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String eventName = name.getText().toString();
                if(!isValidName(eventName)) {
                    name.setError(getString(R.string.error_name));
                }

                // Can't think of any validators for description
//                final String eventDescription = description.getText().toString();
//                if(!isValidDescription(eventDescription)) {
//                    description.setError("Invalid Description");
//                }

                final String date = dateButton.getText().toString();
                if(!isValidDate(date)) {
                    dateButton.requestFocus();
                    dateButton.setError(getString(R.string.error_date));
                    // Doesn't seem to pop up so...
                    Toast.makeText(getContext(), getString(R.string.error_date), Toast.LENGTH_LONG).show();
                }

                final String time = timeButton.getText().toString();
                if(!isValidTime(time)) {
                    timeButton.setError(getString(R.string.error_time));
                    // Doesn't seem to pop up so...
                    Toast.makeText(getContext(), getString(R.string.error_time), Toast.LENGTH_LONG).show();
                }

                // Don't want to overload with errors
                if(!isInFuture(date, time) && isValidTime(time) && isValidDate(date)) {
                    dateButton.requestFocus();
                    dateButton.setError(getString(R.string.error_date_time));
                    // Doesn't seem to pop up so...
                    Toast.makeText(getContext(), getString(R.string.error_date_time),
                            Toast.LENGTH_LONG).show();
                }

                if(isValidName(eventName) && isValidDate(date) && isValidTime(time) &&
                        isInFuture(date, time)) {
                    Event event = createEvent(eventName, location.getText().toString(), description.getText().toString(),
                            parseDateTime(date, time));
                    ((MainActivity) getActivity()).writeNewEvent(event);

                    // Resets fields
                    name.setText(getString(R.string.text_event_name));
                    location.setText(getString(R.string.text_location));
                    dateButton.setText(getString(R.string.text_date));
                    timeButton.setText(getString(R.string.text_time));
                    description.setText(getString(R.string.text_description));

                }
            }
        });
    }

    private Event createEvent(String name, String location, String description, Calendar date){
        // Must deal with Location later but for now setting it to be UCD Library
        String country = "Ireland";
        String city = "Dublin";
        String latitude = "53.307040";
        String longitude = "-6.223026";

        // Making the calendar format look more familiar
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        String startTime = simpleDate.format(date.getTime());

        return new Event(description, name, ((MainActivity) getActivity()).getFreshId().toString(), country,
                city, startTime, latitude, longitude);
    }

    // Hides keyboard from view
    protected void hideSoftKeyboard(View view){
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(description.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    // Validates Event Name
    private boolean isValidName(String name){
        if(name.length() > EVENT_NAME_CHAR_LIMIT) {
            return false;
        } else if(name == null || name.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    // Validates Event Time
    private boolean isValidTime(String time){
        if(time.equals("Pick a Time")){
            // User did not pick a time
            return false;
        } else {
            return true;
        }
    }

    // Validates Event Date
    private boolean isValidDate(String date){
        if(date.equals("Pick a Date")){
            // User did not pick a date
            return false;
        }

        return true;
    }

    // Checks date and time is in future
    private boolean isInFuture(String date, String time){
        // Today's date
        Calendar today = Calendar.getInstance();

        // Inputted date
        Calendar input = Calendar.getInstance();
        String[] splitDate = date.split("-");
        String[] splitTime = time.split(":");

        input = parseDateTime(date, time);



        if(today.after(input)) {
            return false;
        } else {
            return true;
        }
    }

    private Calendar parseDateTime(String date, String time){
        // Inputted date
        Calendar input = Calendar.getInstance();
        String[] splitDate = date.split("-");
        String[] splitTime = time.split(":");

        // Parsing inputted Date
        int day = Integer.parseInt(splitDate[0]);
        int month = Integer.parseInt(splitDate[1]);
        int year = Integer.parseInt(splitDate[2]);

        int hours = Integer.parseInt(splitTime[0]);
        int minutes = Integer.parseInt(splitTime[1]);

        // Setting input Calendar with parsed data
        input.set(year, month, day, hours, minutes);

        return input;
    }


    // Will use this later to get location
    // https://developers.google.com/places/android-api/autocomplete

    // Pickers used for Date and Time
    // https://developer.android.com/guide/topics/ui/controls/pickers.html

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            dateButton.setText(day + "-" + month + "-" + year);
        }

    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            //setTime(hourOfDay, minute);
            if(hourOfDay > 9 && minute > 9) {
                timeButton.setText(hourOfDay + ":" + minute);
            } else if(hourOfDay > 9 && minute < 9) {
                timeButton.setText(hourOfDay + ":0" + minute);
            } else if(hourOfDay < 9 && minute > 9) {
                timeButton.setText("0" + hourOfDay + ":" + minute);
            } else {
                timeButton.setText("0" + hourOfDay + ":0" + minute);
            }
        }
    }

    // When orientation changes we want to maintain the item in bottom nav
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        BottomNavigationView bottomNavigationView;

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(2).setChecked(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.getMenu().getItem(2).setChecked(true);
        }
    }
}
