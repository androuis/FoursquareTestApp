package com.andreibacalu.android.foursquaretestapp.venues.utils;

import com.andreibacalu.android.foursquaretestapp.categories.models.Category;

/**
 * Created by abacalu on 10/25/2016.
 */

public class Constants {
    public static final String CLIENT_ID = "FQEF3J3ZGEJIS4WSYRWAHO4M3I4K4MKD1NBWPV4MKPMWXWEZ";
    public static final String CLIENT_SECRET = "L500YBNBI1QT4TUBF0YZVEYPE1CBH0FIAZNGWQ4MEF3IUXHF";

    public static final Category firstCategory = new Category("4d4b7104d754a06370d81259", "Arts & Entertainment");
    public static final Category secondCategory = new Category("4d4b7105d754a06373d81259", "Event");

    public static class IntentExtras {
        public static final String EXTRA_VENUE = "extra_venue";
        public static final String EXTRA_CATEGORY_ID = "extra_category_id";
        public static final String EXTRA_POSITION = "extra_position";
    }
}
