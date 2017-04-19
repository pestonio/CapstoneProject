package com.example.peterstone.capstoneproject.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.peterstone.capstoneproject.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomePageOneFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = HomePageOneFragment.class.getSimpleName();


    private GoogleApiClient mApiClient;
    private final static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private static double LATITUDE = 0;
    private static double LONGITUDE = 0;
    private LocationRequest mLocationRequest;
    private static String currentPlace = null;

    public HomePageOneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.current_location_fragment, container, false);
    }


    protected synchronized void buildGoogleApi() {
        mApiClient = new GoogleApiClient
                .Builder(getContext())
                .addConnectionCallbacks(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(getActivity(), this)
                .build();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mApiClient ==null) {
            buildGoogleApi();
        }
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);
    }

    @Override
    public void onPause() {
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
        Log.v(TAG, "Connected");
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "Connection Suspended");
        mApiClient.disconnect();

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
        if (mApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            } else {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
                Log.v(TAG, "The last location is: " + mLastLocation);
                if (mLastLocation != null) {
                    LATITUDE = mLastLocation.getLatitude();
                    LONGITUDE = mLastLocation.getLongitude();
                    Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
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
        Log.v(TAG, "API is null");
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "New Location " + location);
    }

}