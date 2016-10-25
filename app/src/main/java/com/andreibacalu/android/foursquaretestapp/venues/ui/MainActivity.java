package com.andreibacalu.android.foursquaretestapp.venues.ui;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;

import com.andreibacalu.android.foursquaretestapp.categories.models.Category;
import com.andreibacalu.android.foursquaretestapp.venues.ui.adapters.SectionsPagerAdapter;
import com.andreibacalu.android.foursquaretestapp.venues.utils.Constants;
import com.andreibacalu.android.foursquaretestapp.venues.utils.FoursquareUtils;
import com.andreibacalu.android.foursquaretestapp.R;
import com.foursquare.android.nativeoauth.FoursquareCancelException;
import com.foursquare.android.nativeoauth.FoursquareDenyException;
import com.foursquare.android.nativeoauth.FoursquareInvalidRequestException;
import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.FoursquareOAuthException;
import com.foursquare.android.nativeoauth.FoursquareUnsupportedVersionException;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>, LocationListener {

    private static final String TAG_LOG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_FOURSQUARE_CONNECT = 100;
    private static final int REQUEST_CODE_FOURSQUARE_TOKEN_EXCHANGE = 101;
    private static final int REQUEST_CODE_LOCATION_PERMISSIONS = 102;
    private static final int REQUEST_CODE_SETTING_LOCATION = 103;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLastLocation == null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void setupUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FOURSQUARE_CONNECT:
                onCompleteConnect(resultCode, data);
                break;
            case REQUEST_CODE_FOURSQUARE_TOKEN_EXCHANGE:
                onCompleteTokenExchange(resultCode, data);
                break;
            case REQUEST_CODE_SETTING_LOCATION:
                getRealLocation(false);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onCompleteConnect(int resultCode, Intent data) {
        AuthCodeResponse codeResponse = FoursquareOAuth.getAuthCodeFromResult(resultCode, data);
        Exception exception = codeResponse.getException();
        if (exception == null) {
            // Success.
            String code = codeResponse.getCode();
            performTokenExchange(code);
        } else {
            Log.e(TAG_LOG, exception.getMessage());
            if (exception instanceof FoursquareCancelException) {
                // Cancel.
                FoursquareUtils.toastMessage(this, "Canceled");

            } else if (exception instanceof FoursquareDenyException) {
                // Deny.
                FoursquareUtils.toastMessage(this, "Denied");

            } else if (exception instanceof FoursquareOAuthException) {
                // OAuth error.
                String errorMessage = exception.getMessage();
                String errorCode = ((FoursquareOAuthException) exception).getErrorCode();
                FoursquareUtils.toastMessage(this, errorMessage + " [" + errorCode + "]");

            } else if (exception instanceof FoursquareUnsupportedVersionException) {
                // Unsupported Fourquare app version on the device.
                FoursquareUtils.toastError(this, exception);

            } else if (exception instanceof FoursquareInvalidRequestException) {
                // Invalid request.
                FoursquareUtils.toastError(this, exception);

            } else {
                // Error.
                FoursquareUtils.toastError(this, exception);
            }
        }
    }

    private void onCompleteTokenExchange(int resultCode, Intent data) {
        AccessTokenResponse tokenResponse = FoursquareOAuth.getTokenFromResult(resultCode, data);
        Exception exception = tokenResponse.getException();

        if (exception == null) {
            mSectionsPagerAdapter.setLatLong(FoursquareUtils.getStringFromLocation(mLastLocation));
            mSectionsPagerAdapter.setAccessToken(tokenResponse.getAccessToken());
            mSectionsPagerAdapter.setCategories(new Category[]{Constants.firstCategory, Constants.secondCategory});
            mSectionsPagerAdapter.notifyDataSetChanged();
        } else {
            if (exception instanceof FoursquareOAuthException) {
                // OAuth error.
                String errorMessage = exception.getMessage();
                String errorCode = ((FoursquareOAuthException) exception).getErrorCode();
                FoursquareUtils.toastMessage(this, errorMessage + " [" + errorCode + "]");
            } else {
                // Other exception type.
                FoursquareUtils.toastError(this, exception);
            }
        }
    }

    private void performTokenExchange(String code) {
        Intent intent = FoursquareOAuth.getTokenExchangeIntent(this, Constants.CLIENT_ID, Constants.CLIENT_SECRET, code);
        startActivityForResult(intent, REQUEST_CODE_FOURSQUARE_TOKEN_EXCHANGE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG_LOG, "GoogleApiClient, connection succeeded");
        getLastKnownLocation();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG_LOG, "GoogleApiClient, connection failed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG_LOG, "GoogleApiClient, connection suspended");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSIONS:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) ||
                        (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    getLastKnownLocation();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getLastKnownLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSIONS);
            FoursquareUtils.toastMessage(this, "Unfortunately the app needs a location permission to work properly.");
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            getRealLocation(true);
        } else {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
            Intent intent = FoursquareOAuth.getConnectIntent(this, Constants.CLIENT_ID);
            startActivityForResult(intent, REQUEST_CODE_FOURSQUARE_CONNECT);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getRealLocation(boolean askForSettings) {
        if (askForSettings) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            // Will try to enable the location settings
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(this);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        switch (locationSettingsResult.getStatus().getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    locationSettingsResult.getStatus().startResolutionForResult(this, REQUEST_CODE_SETTING_LOCATION);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG_LOG, e.getMessage());
                    // Nothing to do here
                }
                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Nothing to do here
                break;
            default:
                // Nothing to do here
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        getLastKnownLocation();
        if (mGoogleApiClient.isConnected()) {
            // Remove location updates after the first update
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

}
