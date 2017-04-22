package com.example.peterstone.capstoneproject.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.peterstone.capstoneproject.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomePageOneFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = HomePageOneFragment.class.getSimpleName();
    private GoogleApiClient mApiClient;
    private final static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private LocationRequest mLocationRequest;
    private int permissionRequests =0;
    private TextView mUserLocation;
    private ImageView mImageView;

    public HomePageOneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.current_location_fragment, container, false);
        SupportPlaceAutocompleteFragment supportPlaceAutocompleteFragment = new SupportPlaceAutocompleteFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.autocomplete_container, supportPlaceAutocompleteFragment);
        transaction.commit();
        supportPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                Log.i(TAG, "Place ID: " + place.getId());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mUserLocation = (TextView) getActivity().findViewById(R.id.current_place_name);
        mImageView = (ImageView) getActivity().findViewById(R.id.current_place_image);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mApiClient != null && mApiClient.isConnected()) {
                    getCurrentLocation();
                    swipeRefreshLayout.setRefreshing(false);

                } else {
                    buildGoogleApi();
                    mApiClient.connect();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        buildGoogleApi();
        mApiClient.connect();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);
        Log.i(TAG, "onResume Called");
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
    public void onPause() {
        super.onPause();
        if (mApiClient != null && mApiClient.isConnected()) {
            mApiClient.stopAutoManage(getActivity());
            mApiClient.disconnect();
            mApiClient = null;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "Connection Failed " + connectionResult.getErrorMessage());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "Connected");
        if (permissionRequests < 10) {
            getCurrentLocation();
        }
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

    public void getCurrentLocation() {
        String currentPlace = null;
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            permissionRequests ++;
            Log.i(TAG, "Permission Requested: " + permissionRequests);
        }
        else if (mApiClient.isConnected()) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
            Log.v(TAG, "The last location is: " + mLastLocation);
            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0) {
                        String locality = addresses.get(0).getLocality();
                        String country = addresses.get(0).getCountryName();
                        Log.v(TAG, "Address Data: " + locality + " + " + country);
                        currentPlace = locality + " - " + country;
                        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                        String savedLocation = sharedPreferences.getString(getString(R.string.last_known_current_location), null);
                        if (currentPlace.equals(savedLocation)) {
                            Log.i(TAG, "sharedPref and currentPlace Match");
                            updateLocationName(currentPlace);
                            return;
                        } else if (!currentPlace.equals(savedLocation)) {
                            getCurrentPlaceId(currentPlace);
                            addLocationToSharedPreferences(currentPlace);
                            updateLocationName(currentPlace);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mLastLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
            }
        }
        Log.v(TAG, "The current place is: " + currentPlace);
    }

    private void addLocationToSharedPreferences(String currentPlace) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.last_known_current_location), currentPlace);
        editor.apply();
        Log.v(TAG, "Current sharedPref is: " + currentPlace);
        updateLocationName(currentPlace);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "New Location " + location);
    }

    public void updateLocationName(String currentPlace) {
        //TODO update entire fragment UI based on current location AND the parsed JSON response.
        if (currentPlace == null) {
            currentPlace = getString(R.string.unknown_location);
        }
        mUserLocation.setText(currentPlace);
    }

    private void getCurrentPlaceId(String location) {
        if (location != null && !location.equals(getString(R.string.unknown_location))) {
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
            String url = getString(R.string.webservice_base_url)
                    + getString(R.string.webservice_api_key)
                    + getString(R.string.webservice_query)
                    + location.replaceAll(" ", "%20");
            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        JSONObject resultsObject = results.getJSONObject(0);
                        String placeId = resultsObject.getString("place_id");
                        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(getString(R.string.current_place_id), placeId);
                        editor.apply();
                        Log.v(TAG, placeId);
                        placePhotosTask(placeId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //TODO parse JSON response and pull out ID and image, update UI.
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v(TAG, "Volley Error is: " + error);
                }
            });
            queue.add(objectRequest);
        }
    }

    abstract class PhotoTask extends AsyncTask<String, Void, PhotoTask.AttributedPhoto> {

        private int mHeight;

        private int mWidth;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

        @Override
        protected AttributedPhoto doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            AttributedPhoto attributedPhoto = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    // Get the first bitmap and its attributions.
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    CharSequence attribution = photo.getAttributions();
                    // Load a scaled bitmap for this photo.
                    Bitmap image = photo.getScaledPhoto(mApiClient, mWidth, mHeight).await()
                            .getBitmap();

                    attributedPhoto = new AttributedPhoto(attribution, image);
                }
                // Release the PlacePhotoMetadataBuffer.
                photoMetadataBuffer.release();
            }
            return attributedPhoto;
        }

        /**
         * Holder for an image and its attribution.
         */
        class AttributedPhoto {

            public final CharSequence attribution;

            public final Bitmap bitmap;

            public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
                this.attribution = attribution;
                this.bitmap = bitmap;
            }
        }
    }

    private void placePhotosTask(String placeId) {
        // Create a new AsyncTask that displays the bitmap and attribution once loaded.
        new PhotoTask(mImageView.getWidth(), mImageView.getHeight()) {
            @Override
            protected void onPreExecute() {
                // Display a temporary image to show while bitmap is loading.
//                mImageView.setImageResource(R.drawable.empty_photo);
            }

            @Override
            protected void onPostExecute(AttributedPhoto attributedPhoto) {
                if (attributedPhoto != null) {
                    // Photo has been loaded, display it.
                    mImageView.setImageBitmap(attributedPhoto.bitmap);

//                    // Display the attribution as HTML content if set.
//                    if (attributedPhoto.attribution == null) {
//                        mText.setVisibility(View.GONE);
//                    } else {
//                        mText.setVisibility(View.VISIBLE);
//                        mText.setText(Html.fromHtml(attributedPhoto.attribution.toString()));
//                    }
                }
            }
        }.execute(placeId);
    }
}