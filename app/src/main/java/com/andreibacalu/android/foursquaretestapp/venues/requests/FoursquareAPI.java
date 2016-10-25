package com.andreibacalu.android.foursquaretestapp.venues.requests;


import com.andreibacalu.android.foursquaretestapp.venues.models.VenuesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by abacalu on 10/21/2016.
 */

public interface FoursquareAPI {
    @GET("/v2/venues/search?v=20161025&m=foursquare")
    Call<VenuesResponse> getVenues(@Query("ll") String latLong, @Query("categoryId") String categoryId, @Query("oauth_token") String authToken);
}
