package com.example.peterstone.capstoneproject.Fragments;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.peterstone.capstoneproject.LocationRecyclerAdapter;
import com.example.peterstone.capstoneproject.PlaceClass;
import com.example.peterstone.capstoneproject.PointOfInterestDetails;
import com.example.peterstone.capstoneproject.R;
import com.example.peterstone.capstoneproject.SQL.PlaceContract;
import com.example.peterstone.capstoneproject.SQL.PlacesDBHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Peter Stone on 27/04/2017.
 */

public class CurrentLocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = CurrentLocationFragment.class.getSimpleName();
    private final static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mApiClient;
    private static boolean LOCATION_PERMISSION_GRANTED;
    private RecyclerView mRecyclerView;
    private List<PlaceClass> mPlaceData;
    private String mPlaceUrl;
    private boolean isConnected;
    private TextView currentTownCity;
    private TextView currentCountry;
    private SQLiteDatabase mDatabase;
    private ContentValues mValues;
    private static final int PLACE_AUTOCOMPLETE_REQUEST = 1;
    private Parcelable mListState;
    private LinearLayoutManager mLinearLayoutManager;
    private static final String LIST_STATE_KEY = "list_key";

    public CurrentLocationFragment() {
        //Empty constructor.
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.current_location_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mDatabase = new PlacesDBHelper(getActivity()).getWritableDatabase();
        mValues = new ContentValues();
        currentTownCity = (TextView) getActivity().findViewById(R.id.current_town_city);
        currentCountry = (TextView) getActivity().findViewById(R.id.current_country);
        currentTownCity.setText("PlaceHolder Town");
        currentCountry.setText("PlaceHolder Country");
        LinearLayout search = (LinearLayout) getActivity().findViewById(R.id.search_view);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES).build();
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setFilter(typeFilter).build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        mPlaceData = new ArrayList<>();
        //TODO set placeholder text and image or pull from SP.
        ConnectivityManager cMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cMan.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.current_place_recycler_view);
        if (mListState != null){
            mLinearLayoutManager.onRestoreInstanceState(mListState);
            Log.i(TAG, "State Restored");
        }else {
            mLinearLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
            mRecyclerView.setHasFixedSize(true);
            if (isConnected) {
                ImageView connectionIcon = (ImageView) getActivity().findViewById(R.id.no_connection);
                TextView connectionText = (TextView) getActivity().findViewById(R.id.no_connection_text);
                connectionIcon.setVisibility(View.GONE);
                connectionText.setVisibility(View.GONE);
                buildGoogleApi();
                if (mApiClient != null) {
                    mApiClient.connect();
                }
            }
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setInterval(10 * 1000)
                    .setFastestInterval(1000);
            if (!isConnected) {
                mRecyclerView.setVisibility(View.GONE);
                currentCountry.setVisibility(View.GONE);
                currentTownCity.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                Log.i(TAG, "Searched for place: " + place.getId() + " " + place.getAddress());
                Intent intent = new Intent(getActivity(), PointOfInterestDetails.class);
                intent.putExtra("origin", R.integer.ORIGIN_GOOGLE_SEARCH);
                intent.putExtra("place_name", place.getAddress());
                intent.putExtra("place_id", place.getId());
                startActivity(intent);
            } else if (requestCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Log.e(TAG, "Searched for place error: " + status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "Search cancelled");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mApiClient != null) {
            mApiClient.stopAutoManage(getActivity());
        }
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
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Google API Client Connected");
        getUserLocationPermission();
        Log.i(TAG, "Permission Granted? " + LOCATION_PERMISSION_GRANTED);
        if (LOCATION_PERMISSION_GRANTED) {
            getUserLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Google API Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Google API Client Connection Failed: " + connectionResult);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location Changed: " + location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LOCATION_PERMISSION_GRANTED = true;
            if (isConnected && mApiClient != null) {
                mApiClient.connect();
            }
            Log.i(TAG, "Permission Granted!");
        } else {
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
            Toast.makeText(getActivity(), "Without location permission, a large portion of functionality will be unavailable.", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Permission Denied");
            LOCATION_PERMISSION_GRANTED = false;
            //TODO adjust view to display information related to lack of permission.
        }
    }

    private void getUserLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            Log.i(TAG, "Location Permission Requested");
        } else LOCATION_PERMISSION_GRANTED = true;
    }

    @SuppressWarnings("MissingPermission")
    private void getUserLocation() {
        String placeInfo = null;
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
        Log.i(TAG, "Location Service: " + currentLocation);
        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                List<Address> address = geocoder.getFromLocation(latitude, longitude, 1);
                String country = address.get(0).getCountryName();
                String locality = address.get(0).getLocality();
                Log.i(TAG, "Current Location is: " + country + " " + locality);
                placeInfo = locality + " " + country;
                currentTownCity.setText(locality);
                currentCountry.setText(country);
                SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                String lastKnownLocation = sharedPreferences.getString("lastLocation", null);

                if (lastKnownLocation == null || !lastKnownLocation.equals(placeInfo)) {
                    mDatabase.delete(PlaceContract.PlaceEntry.TABLE_NAME, null, null);
                    Log.i(TAG, "TABLE CLEARED!");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("lastLocation", placeInfo);
                    editor.apply();
                    String url = urlBuilder(placeInfo);
                    getPlaceData(url);
                } else {
                    Log.i(TAG, "Loading from DB");
                    loadFromDatabase();
                }
            } catch (IOException e) {
                Toast.makeText(getActivity(), "Location Services currently unavailable. Check connection and settings.", Toast.LENGTH_SHORT).show();
            }
        }
        if (currentLocation == null && mApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
        }
    }

    private String urlBuilder(String place) {
        final String BASE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
        final String QUERY = "query";
        final String API_KEY = "key";
        Uri builtUri = Uri.parse(BASE_URL)
                .buildUpon()
                .appendQueryParameter(QUERY, place.concat(" attraction"))
                .appendQueryParameter(API_KEY, getString(R.string.webservice_api_key))
                .build();
        return builtUri.toString();
    }

    private void getPlaceData(String url) {
        Log.i(TAG, "Passed URL: " + url);
        if (url != null) {
            final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
            JsonObjectRequest jsonInitialRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray initialJsonArray = response.getJSONArray("results");
                        for (int i = 0; i < initialJsonArray.length(); i++) {
                            JSONObject placeObject = initialJsonArray.getJSONObject(i);
                            String placeName = placeObject.getString("name");
                            String placeId = placeObject.getString("place_id");
                            JSONObject placeGeometry = placeObject.getJSONObject("geometry");
                            JSONObject placeLocation = placeGeometry.getJSONObject("location");
                            double placeLat = placeLocation.getDouble("lat");
                            double placeLong = placeLocation.getDouble("lng");
                            String rating = null;
                            if (placeObject.has("rating")) {
                                rating = placeObject.getString("rating");
                            }
                            String address = placeObject.getString("formatted_address");
                            Log.i(TAG, "place name = " + placeName);
                            if (placeObject.has("photos")) {
                                JSONArray photoArray = placeObject.getJSONArray("photos");
                                JSONObject photoRef = photoArray.getJSONObject(0);
                                String photoReference = photoRef.getString("photo_reference");
                                mPlaceUrl = getString(R.string.place_api_photo_url) + photoReference + getString(R.string.api_key_parameter) + getString(R.string.webservice_api_key);
                            } else {
                                mPlaceUrl = null;
                            }
                            mPlaceData.add(new PlaceClass(placeName, placeId, rating, address, mPlaceUrl, placeLat, placeLong));

                            mValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_NAME, placeName);
                            mValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeId);
                            mValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_RATING, rating);
                            mValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ADDRESS, address);
                            mValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_IMAGE_URL, mPlaceUrl);
                            mValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_LAT, placeLat);
                            mValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_LONG, placeLong);
                            mDatabase.insert(PlaceContract.PlaceEntry.TABLE_NAME, null, mValues);
                            Log.i(TAG, "SQL Entry is: " + mValues);
                        }
                        mDatabase.close();
                        LocationRecyclerAdapter adapter = new LocationRecyclerAdapter(getActivity(), mPlaceData);
                        mRecyclerView.setAdapter(adapter);
                        Log.i(TAG, "onResponse Array Data = " + mPlaceData);
                        if (mPlaceData.isEmpty()) {
                            Log.e(TAG, "Place Data is empty");
                        }

                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Server data currently unavailable. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Volley Error: " + error);
                }
            });
            queue.add(jsonInitialRequest);
        } else {
            Log.e(TAG, "Get Location Data Failed - URL is Null");
        }
    }

    private void loadFromDatabase() {
        final SQLiteDatabase sqlDatabase = new PlacesDBHelper(getActivity()).getReadableDatabase();
        String[] projection = {PlaceContract.PlaceEntry.COLUMN_PLACE_NAME, PlaceContract.PlaceEntry.COLUMN_PLACE_ID, PlaceContract.PlaceEntry.COLUMN_PLACE_RATING, PlaceContract.PlaceEntry.COLUMN_PLACE_ADDRESS, PlaceContract.PlaceEntry.COLUMN_PLACE_IMAGE_URL, PlaceContract.PlaceEntry.COLUMN_PLACE_LAT, PlaceContract.PlaceEntry.COLUMN_PLACE_LONG};
        final Cursor cursor = sqlDatabase.query(PlaceContract.PlaceEntry.TABLE_NAME, projection, null, null, null, null, null, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                cursor.moveToFirst();
                do {
                    int placeNameColumn = cursor.getColumnIndexOrThrow(PlaceContract.PlaceEntry.COLUMN_PLACE_NAME);
                    String placeName = cursor.getString(placeNameColumn);
                    int placeIdColumn = cursor.getColumnIndexOrThrow(PlaceContract.PlaceEntry.COLUMN_PLACE_ID);
                    String placeId = cursor.getString(placeIdColumn);
                    int placeRatingColumn = cursor.getColumnIndexOrThrow(PlaceContract.PlaceEntry.COLUMN_PLACE_RATING);
                    String placeRating = cursor.getString(placeRatingColumn);
                    int placeAddressColumn = cursor.getColumnIndexOrThrow(PlaceContract.PlaceEntry.COLUMN_PLACE_ADDRESS);
                    String placeAddress = cursor.getString(placeAddressColumn);
                    int placePhotoColumn = cursor.getColumnIndexOrThrow(PlaceContract.PlaceEntry.COLUMN_PLACE_IMAGE_URL);
                    String placePhotoUrl = cursor.getString(placePhotoColumn);
                    int placeLatColumn = cursor.getColumnIndexOrThrow(PlaceContract.PlaceEntry.COLUMN_PLACE_LAT);
                    double placeLat = cursor.getDouble(placeLatColumn);
                    int placeLongColumn = cursor.getColumnIndexOrThrow(PlaceContract.PlaceEntry.COLUMN_PLACE_LONG);
                    double placeLng = cursor.getDouble(placeLongColumn);
                    mPlaceData.add(new PlaceClass(placeName, placeId, placeRating, placeAddress, placePhotoUrl, placeLat, placeLng));
                    Log.i(TAG, "SQL saved place is: " + mPlaceData);
                }
                while (cursor.moveToNext());
                cursor.close();
                sqlDatabase.close();
            }
        }).start();
        LocationRecyclerAdapter adapter = new LocationRecyclerAdapter(getActivity(), mPlaceData);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mListState = mLinearLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, mListState);
    }

}
