package com.andreibacalu.android.foursquaretestapp.venues.utils;

import android.content.Context;
import android.location.*;
import android.support.annotation.NonNull;
import android.widget.Toast;

/**
 * Created by abacalu on 10/24/2016.
 */

public class FoursquareUtils {

    public static String getStringFromLocation(@NonNull android.location.Location location) {
        return location.getLatitude() + "," + location.getLongitude();
    }

    public static void toastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void toastError(Context context, Throwable t) {
        Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
