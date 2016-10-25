package com.andreibacalu.android.foursquaretestapp.venues.models;

import java.io.Serializable;

/**
 * Created by abacalu on 10/21/2016.
 */
public class Location implements Serializable {
    public String address;
    public double latitude;
    public double longitude;
    public String countryCode;
    public String city;
    public String state;
    public String country;

    @Override
    public String toString() {
        return "Location: \n" +
                "\t\taddress: " + address + "\n" +
                "\t\tlatitude: " + latitude + "\n" +
                "\t\tlongitude: " + longitude + "\n" +
                "\t\tcountryCode: " + countryCode + "\n" +
                "\t\tcity: " + city + "\n" +
                "\t\tstate: " + state + "\n" +
                "\t\tcountry: " + country;
    }
}
