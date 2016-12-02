package com.example.spitegirls.eventme;

import java.util.Calendar;

// Class used to store event objects
public class Event implements java.io.Serializable{

    public Event(){
        // Empty constructor used by Firebase
    }

    public Event(String description, String name, String id, String placeName, String country, String city,
                 String startTime, String latitude, String longitude) {
        this.description = description;
        this.name = name;
        this.id = id;
        this.placeName = placeName;
        this.country = country;
        this.city = city;
        this.startTime = startTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // STANDARD DETAILS
    public String description;
    public String name;
    // ID used as string in most calls so most convenient format
    public String id;
    public String placeName;
    public String country;
    public String city;
    public String startTime;
    public String latitude;
    public String longitude;

    // NON-STANDARD DETAILS
    public String coverURL;

    public String toString(){
        return "{" + name + ", " + id + ", "+ placeName +" " + country + "," + city + "," + startTime + ","+ latitude + "," + longitude + "}" ;
    }

    public String getReadableDate() {
        String split[] = startTime.split("T");
        String data[] = split[0].split("-");

        int date = Integer.parseInt(data[2]);
        int month = Integer.parseInt(data[1]);

        // Format date
        if(date % 10 == 1 && date != 11) {
            data[2] = data[2] + "st";
        } else if(date % 10 == 2 && date != 12) {
            data[2] = data[2] + "nd";
        } else if(date % 10 == 3 && date != 13) {
            data[2] = data[2] + "rd";
        } else {
            data[2] = data[2] + "th";
        }

        // Format month
        switch(month) {
            case 1 : data[1] = "January"; break;
            case 2 : data[1] = "February"; break;
            case 3 : data[1] = "March"; break;
            case 4 : data[1] = "April"; break;
            case 5 : data[1] = "May"; break;
            case 6 : data[1] = "June"; break;
            case 7 : data[1] = "July"; break;
            case 8 : data[1] = "August"; break;
            case 9 : data[1] = "September"; break;
            case 10 : data[1] = "October"; break;
            case 11 : data[1] = "November"; break;
            case 12 : data[1] = "December"; break;
        }

        String formattedDate = data[2] + " " + data[1] + " " + data[0];

        String time[] = split[1].split(":");

        // Making format more understandable to people, 09:05 vs 9:5
        if(time[0].length() == 1){
            time[0] = "0" + time[0];
        }

        if(time[1].length() == 1){
            time[1] = "0" + time[1];
        }

        String formattedTime = " at " + time[0] + ":" + time[1];

        return formattedDate + formattedTime;
    }

    public Calendar getCalendarDate(){
        Calendar calendar = Calendar.getInstance();

        // Splits start time into 2 segments, date and time
        String[] segments = startTime.split("T");
        // Splits date into individual segments
        String[] date = segments[0].split("-");
        // Splits time into individual segments
        String[] time = segments[1].split(":");

        // Parsing inputted Date
        int day = Integer.parseInt(date[2]);
        // Calendar months start at 0 = January
        int month = Integer.parseInt(date[1]) - 1;
        int year = Integer.parseInt(date[0]);

        int hours = Integer.parseInt(time[0]);
        int minutes = Integer.parseInt(time[1]);

        // Setting input Calendar with parsed data
        calendar.set(year, month, day, hours, minutes);

        return calendar;
    }
}


