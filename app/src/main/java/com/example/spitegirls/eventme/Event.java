package com.example.spitegirls.eventme;

// Class used to store event objects
public class Event implements java.io.Serializable{

    public Event(){
        // Used by Firebase
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
    // Can update this format to be more useful later
    public String latitude;
    public String longitude;

    // NON-STANDARD DETAILS
    public String coverURL;

    // For testing purposes only with Log.d
    public String toString(){
        return "{" + name + ", " + id + ", "+ placeName +" " + country + "," + city + "," + startTime + ","+ latitude + "," + longitude + "}" ;
    }
}


