package com.example.peterstone.capstoneproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Peter Stone on 17/04/2017.
 */

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mApiClient;
    private final static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private static double LATITUDE = 0;
    private static double LONGITUDE = 0;
    private LocationRequest mLocationRequest;
    private static String currentPlace = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_title_current));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_title_saved));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.main_pager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        buildGoogleApi();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);
    }

    protected synchronized void buildGoogleApi() {
        mApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addConnectionCallbacks(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mApiClient.isConnected()) {
            mApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "Connection Failed " + connectionResult.getErrorMessage());

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
        Log.v(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "Connection Suspended");
        mApiClient.connect();

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                }
            }
        }
    }

    public String getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
            Log.v(TAG, "The last location is: " + mLastLocation);
            if (mLastLocation != null) {
                LATITUDE = mLastLocation.getLatitude();
                LONGITUDE = mLastLocation.getLongitude();
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
                    if (addresses.size() > 0) {
                        String locality = addresses.get(0).getLocality();
                        String country = addresses.get(0).getCountryName();
                        Log.v(TAG, "Address Data: " + locality + " + " + country);
                        currentPlace = locality + ", " + country;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mLastLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
                Log.v(TAG, "requestLocationUpdates called");
            }

        }
        Log.v(TAG, "The current place is: " + currentPlace);
        return currentPlace;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "New Location " + location);
    }
}
