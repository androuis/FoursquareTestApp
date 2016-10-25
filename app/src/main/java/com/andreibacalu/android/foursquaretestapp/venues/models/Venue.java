package com.andreibacalu.android.foursquaretestapp.venues.models;

import com.andreibacalu.android.foursquaretestapp.location.models.Location;

import java.io.Serializable;

/**
 * Created by abacalu on 10/21/2016.
 */

public class Venue implements Serializable {
    public String id;
    public String name;
    public Location location;
    public boolean isFavorite;

    @Override
    public String toString() {
        return "Venue: \n" +
                "\tid: " + id + "\n" +
                "\tname: " + name + "\n" +
                "\t" + location + "\n" +
                "\tisFavorite: " + isFavorite;
    }
}
