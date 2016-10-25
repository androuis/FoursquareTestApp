package com.andreibacalu.android.foursquaretestapp.venues.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.andreibacalu.android.foursquaretestapp.categories.models.Category;
import com.andreibacalu.android.foursquaretestapp.venues.ui.VenuesFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private String latLong;
    private Category[] mCategories;
    private String accessToken;

    public SectionsPagerAdapter(FragmentManager fm, @NonNull Category... categories) {
        super(fm);
        mCategories = categories;
    }

    public void setLatLong(String latLong) {
        this.latLong = latLong;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setCategories(Category[] categories) {
        mCategories = categories;
    }

    @Override
    public Fragment getItem(int position) {
        return VenuesFragment.newInstance(latLong, mCategories[position].categoryId, accessToken);
    }

    @Override
    public int getCount() {
        return mCategories.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mCategories[position].categoryName;
    }
}
