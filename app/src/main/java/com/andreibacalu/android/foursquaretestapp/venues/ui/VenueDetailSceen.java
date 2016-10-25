package com.andreibacalu.android.foursquaretestapp.venues.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.andreibacalu.android.foursquaretestapp.R;
import com.andreibacalu.android.foursquaretestapp.venues.models.Venue;
import com.andreibacalu.android.foursquaretestapp.venues.prefs.SharedPreferencesVenues;
import com.andreibacalu.android.foursquaretestapp.venues.utils.Constants;

public class VenueDetailSceen extends AppCompatActivity {

    private Venue mVenue;
    private String mCategoryId;
    private int mPosition;

    private TextView mVenueDetails;
    private CheckBox mVenueFavorite;

    private boolean isModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail_sceen);
        readData();
        setupUI();
        populateUI();
        setupListeners();
    }

    @Override
    protected void onPause() {
        if (isModified) {
            if (mVenue.isFavorite) {
                SharedPreferencesVenues.addFavoriteVenueIds(mCategoryId, getApplicationContext(), mVenue.id);
            } else {
                SharedPreferencesVenues.removeFavoriteVenueIds(mCategoryId, getApplicationContext(), mVenue.id);
            }
            isModified = false;
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        getIntent().putExtra(Constants.IntentExtras.EXTRA_VENUE, mVenue);
        setResult(RESULT_OK, getIntent());
        super.onBackPressed();
    }

    private void readData() {
        if (getIntent() != null &&
                getIntent().hasExtra(Constants.IntentExtras.EXTRA_VENUE) &&
                getIntent().hasExtra(Constants.IntentExtras.EXTRA_CATEGORY_ID) &&
                getIntent().hasExtra(Constants.IntentExtras.EXTRA_POSITION)) {
            mVenue = (Venue) getIntent().getSerializableExtra(Constants.IntentExtras.EXTRA_VENUE);
            mCategoryId = getIntent().getStringExtra(Constants.IntentExtras.EXTRA_CATEGORY_ID);
            mPosition = getIntent().getIntExtra(Constants.IntentExtras.EXTRA_POSITION, -1);
        } else {
            throw new IllegalStateException("Expected parameters for: venue, categoryId and position!");
        }
    }

    private void setupUI() {
        mVenueDetails = (TextView) findViewById(R.id.venue_details);
        mVenueFavorite = (CheckBox) findViewById(R.id.venue_favorite);
    }

    private void populateUI() {
        mVenueDetails.setText(mVenue.toString());
        mVenueFavorite.setChecked(mVenue.isFavorite);
    }

    private void setupListeners() {
        mVenueFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mVenue.isFavorite = isChecked;
                isModified = !isModified;
            }
        });
    }
}
