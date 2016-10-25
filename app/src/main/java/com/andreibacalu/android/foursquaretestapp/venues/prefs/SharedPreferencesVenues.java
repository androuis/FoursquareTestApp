package com.andreibacalu.android.foursquaretestapp.venues.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.andreibacalu.android.foursquaretestapp.venues.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by abacalu on 10/25/2016.
 */

public class SharedPreferencesVenues {
    private static final String PREFS_NAME = "venues";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static Set<String> getFavoriteVenueIds(String categoryId, Context context) {
        return getSharedPreferences(context).getStringSet(categoryId, new HashSet<String>());
    }

    public static void addFavoriteVenueIds(String categoryId, Context context, String...ids) {
        if (ids != null && ids.length > 0) {
            Set<String> idsSet = new HashSet<String>(Arrays.asList(ids));
            idsSet.addAll(getFavoriteVenueIds(categoryId, context));
            getSharedPreferences(context).edit().putStringSet(categoryId, idsSet).apply();
        }
    }

    public static void removeFavoriteVenueIds(String categoryId, Context context, String...ids) {
        if (ids != null && ids.length > 0) {
            Set<String> idsSet = getFavoriteVenueIds(categoryId, context);
            idsSet.removeAll(Arrays.asList(ids));
            getSharedPreferences(context).edit().putStringSet(categoryId, idsSet).apply();
        }
    }
}
