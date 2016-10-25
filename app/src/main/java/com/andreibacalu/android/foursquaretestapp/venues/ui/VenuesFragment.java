package com.andreibacalu.android.foursquaretestapp.venues.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreibacalu.android.foursquaretestapp.R;
import com.andreibacalu.android.foursquaretestapp.venues.models.Venue;
import com.andreibacalu.android.foursquaretestapp.venues.models.VenuesResponse;
import com.andreibacalu.android.foursquaretestapp.venues.prefs.SharedPreferencesVenues;
import com.andreibacalu.android.foursquaretestapp.venues.requests.FoursquareAPI;
import com.andreibacalu.android.foursquaretestapp.venues.utils.Constants;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A placeholder fragment containing a simple view.
 */
public class VenuesFragment extends Fragment implements Callback<VenuesResponse> {

    private static final String TAG_LOG = VenuesFragment.class.getSimpleName();

    private static final String BASE_URL_FOURSQUARE_API = "https://api.foursquare.com/";

    private static final String ARG_LAT_LONG = "lat_long";
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_ACCESS_TOKEN = "access_token";

    private static final int RESULT_CODE_MARK_AS_FAVORITE = 200;

    private String mLatLong;
    private String mCategoryId;
    private String mAccessToken;
    private ArrayList<String> mFavoriteIds = new ArrayList<>();

    private RecyclerViewAdapter mRecyclerViewAdapter;

    private Retrofit mRetrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL_FOURSQUARE_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private FoursquareAPI mFoursquareAPI = mRetrofit.create(FoursquareAPI.class);

    public VenuesFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static VenuesFragment newInstance(String latLong, String categoryId, String accessToken) {
        VenuesFragment fragment = new VenuesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LAT_LONG, latLong);
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_ACCESS_TOKEN, accessToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readData();
    }

    private void readData() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mLatLong = arguments.getString(ARG_LAT_LONG);
            mCategoryId = arguments.getString(ARG_CATEGORY_ID);
            mAccessToken = arguments.getString(ARG_ACCESS_TOKEN);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mFavoriteIds = new ArrayList<>(SharedPreferencesVenues.getFavoriteVenueIds(mCategoryId, getContext()
                .getApplicationContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerViewAdapter = new RecyclerViewAdapter(new Venue[0]);
        recyclerView.setAdapter(mRecyclerViewAdapter);
        if (validArguments()) {
            mFoursquareAPI.getVenues(mLatLong, mCategoryId, mAccessToken).enqueue(this);
        }
    }

    private boolean validArguments() {
        return !TextUtils.isEmpty(mLatLong) && !TextUtils.isEmpty(mCategoryId) && !TextUtils.isEmpty(mAccessToken);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_CODE_MARK_AS_FAVORITE:
                if (resultCode == Activity.RESULT_OK) {
                    int position = data.getIntExtra(Constants.IntentExtras.EXTRA_POSITION, -1);
                    Venue modifiedVenue = (Venue) data.getSerializableExtra(Constants.IntentExtras.EXTRA_VENUE);
                    if (position >= 0 && modifiedVenue != null && mRecyclerViewAdapter.getItem(position).isFavorite != modifiedVenue.isFavorite) {
                        mRecyclerViewAdapter.getItem(position).isFavorite = modifiedVenue.isFavorite;
                        mRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResponse(Call<VenuesResponse> call,
                           Response<VenuesResponse> response) {
        Log.d(TAG_LOG, response.message());
        if (response.body() != null && response.body().response.venues != null) {
            Venue[] venues = response.body().response.venues;
            for (Venue venue: venues) {
                if (mFavoriteIds.contains(venue.id)) {
                    venue.isFavorite = true;
                }
            }
            mRecyclerViewAdapter.setVenues(venues);
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFailure(Call<VenuesResponse> call, Throwable t) {
        Log.e(TAG_LOG, t.getMessage());
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.VenueViewHolder> {

        Venue[] mVenues;

        RecyclerViewAdapter(@NonNull Venue[] venues) {
            mVenues = venues;
        }

        public void setVenues(@NonNull Venue[] venues) {
            mVenues = venues;
        }

        @Override
        public RecyclerViewAdapter.VenueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_venue, parent, false);
            return new RecyclerViewAdapter.VenueViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.VenueViewHolder holder, int position) {
            final Venue venue = mVenues[position];
            holder.mVenueFavorite.setBackgroundResource(venue.isFavorite ? R.mipmap.ic_star_full : R.mipmap.ic_star_empty);
            holder.mVenueName.setText(venue.name);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(VenuesFragment.this.getContext(), VenueDetailSceen.class);
                    intent.putExtra(Constants.IntentExtras.EXTRA_VENUE, venue);
                    intent.putExtra(Constants.IntentExtras.EXTRA_CATEGORY_ID, mCategoryId);
                    intent.putExtra(Constants.IntentExtras.EXTRA_POSITION, holder.getAdapterPosition());
                    startActivityForResult(intent, RESULT_CODE_MARK_AS_FAVORITE);
                }
            });
        }

        Venue getItem(int position) {
            return mVenues[position];
        }

        @Override
        public int getItemCount() {
            return mVenues.length;
        }

        class VenueViewHolder extends RecyclerView.ViewHolder {
            ImageView mVenueFavorite;
            TextView mVenueName;
            VenueViewHolder(View itemView) {
                super(itemView);
                mVenueFavorite = (ImageView) itemView.findViewById(R.id.venue_favorite);
                mVenueName = (TextView) itemView.findViewById(R.id.venue_name);
            }
        }
    }
}
