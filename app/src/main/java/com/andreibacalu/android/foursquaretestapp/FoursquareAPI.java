package com.andreibacalu.android.foursquaretestapp;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by abacalu on 10/21/2016.
 */

public interface FoursquareAPI {
    @GET("/v2/venues/search")
    Call<Venues> getVenues(@Query("ll") String latLong, @Query("categoryId") String categoryId);
}
