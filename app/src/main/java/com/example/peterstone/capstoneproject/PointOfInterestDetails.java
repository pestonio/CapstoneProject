package com.example.peterstone.capstoneproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.peterstone.capstoneproject.SQL.SavedPlaceContract;
import com.example.peterstone.capstoneproject.SQL.SavedPlacesProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Peter Stone on 23/04/2017.
 */

public class PointOfInterestDetails extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private GoogleApiClient mApiClient;
    private static final String TAG = PointOfInterestDetails.class.getSimpleName();
    private ImageView mPlaceImage;
    private RecyclerView mRecyclerView;
    private LatLng latLng;
    private TextView mPoiWikiText;
    private String placeName;
    private String mPlaceUrl;
    private String mSearchedPlaceUrl;
    private List<PlaceClass> mSearchedPlaceData;
    private ProgressBar mProgressBar;
    private Boolean isClicked = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_detail_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.location_detail_toolbar);
        setSupportActionBar(toolbar);
        mPlaceImage = (ImageView) findViewById(R.id.location_detail_image);
        mPoiWikiText = (TextView) findViewById(R.id.location_detail_desc);
        mSearchedPlaceData = new ArrayList<>();
        mProgressBar = (ProgressBar) findViewById(R.id.rc_progress_bar);
        TextView placeNameTextView = (TextView) findViewById(R.id.location_detail_name);
        TextView placeAddress = (TextView) findViewById(R.id.place_address);
        final FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.location_fab);
        mRecyclerView = (RecyclerView) findViewById(R.id.location_detail_recycler_view);
        final Intent intent = getIntent();
        int origin = intent.getIntExtra("origin", 0);
        if (origin == R.integer.ORIGIN_CURRENT_LOCATION) {
            placeName = intent.getStringExtra("place_name");
            boolean isSaved = checkIfSaved(placeName);
            if (isSaved) {
                actionButton.setVisibility(View.INVISIBLE);
            }
            getSupportActionBar().setTitle(placeName);
            placeNameTextView.setText(placeName);
            placeAddress.setText(intent.getStringExtra("place_address"));
            Picasso.with(this).load(intent.getStringExtra("place_photo")).into(mPlaceImage);
            final double placeLat = intent.getDoubleExtra("place_lat", 0);
            final double placeLong = intent.getDoubleExtra("place_long", 0);
            mRecyclerView.setVisibility(View.GONE);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            latLng = new LatLng(placeLat, placeLong);
            CameraPosition cameraPosition = new CameraPosition(latLng, 14, 0, 0);
            GoogleMapOptions options = new GoogleMapOptions().liteMode(true).camera(cameraPosition);
            SupportMapFragment supportMapFragment = SupportMapFragment.newInstance(options);
            supportMapFragment.getMapAsync(this);
            fragmentTransaction.replace(R.id.map_fragment, supportMapFragment);
            fragmentTransaction.commit();
            mProgressBar.setVisibility(View.INVISIBLE);
            final ContentValues values = new ContentValues();
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    values.put(SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_NAME, placeName);
                    values.put(SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_IMAGE_URL, intent.getStringExtra("place_photo"));
                    values.put(SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_ADDRESS, intent.getStringExtra("place_address"));
                    values.put(SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_LAT, placeLat);
                    values.put(SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_LONG, placeLong);
                    Uri uri = getContentResolver().insert(SavedPlacesProvider.CONTENT_URI, values);
                    actionButton.setVisibility(View.INVISIBLE);
                    //TODO SharedPreferences, check if in DB already, change FAB to delete.
                    Log.i(TAG, "Item Saved: " + values);
                    Toast.makeText(PointOfInterestDetails.this, R.string.saved_toast, Toast.LENGTH_SHORT).show();
                }
            });
            String baseUrl = "https://en.wikipedia.org/w/api.php?format=json&action=query&titles=";
            String queryParams = "&redirects&prop=extracts&exintro&explaintext";
            mPlaceUrl = baseUrl.concat(placeName).concat(queryParams).replaceAll(" ", "%20");
            new GetWikiInfoTask().execute();
        } else if (origin == R.integer.ORIGIN_GOOGLE_SEARCH) {
            actionButton.setVisibility(View.INVISIBLE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            FrameLayout mapFragment = (FrameLayout) findViewById(R.id.map_fragment);
            mapFragment.setVisibility(View.GONE);
            String searchedPlaceName = intent.getStringExtra("place_name");
            getSupportActionBar().setTitle(searchedPlaceName);
            placeNameTextView.setText(searchedPlaceName);
            buildGoogleApi();
            placePhotosAsync(intent.getStringExtra("place_id"));
            String url = urlBuilder(searchedPlaceName);
            getSearchedPlaceData(url);
            //TODO Progress Bar
        }
    }

    private boolean checkIfSaved(String placeName) {
        String selection = SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_NAME + " LIKE ? ";
        String[] selectionArgs = {placeName};
        Cursor cursor = this.getContentResolver().query(SavedPlacesProvider.CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return false;
        } else
            cursor.close();
        return true;
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


    private void getSearchedPlaceData(String url) {
        Log.i(TAG, "Passed URL: " + url);
        if (url != null) {
            final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
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
                                mSearchedPlaceUrl = getString(R.string.place_api_photo_url) + photoReference + getString(R.string.api_key_parameter) + getString(R.string.webservice_api_key);
                            } else {
                                mSearchedPlaceUrl = null;
                            }
                            mSearchedPlaceData.add(new PlaceClass(placeName, placeId, rating, address, mSearchedPlaceUrl, placeLat, placeLong));
                        }
                        LocationRecyclerAdapter adapter = new LocationRecyclerAdapter(getApplicationContext(), mSearchedPlaceData);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                        mRecyclerView.setLayoutManager(linearLayoutManager);
                        mRecyclerView.setHasFixedSize(true);
                        mRecyclerView.setNestedScrollingEnabled(false);
                        mRecyclerView.setAdapter(adapter);
                        mProgressBar.setVisibility(View.INVISIBLE);
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Server data currently unavailable. Please try again later.", Toast.LENGTH_SHORT).show();
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

    protected synchronized void buildGoogleApi() {
        mApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "API Connected");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "API Connection Suspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "API Connection Failed: " + connectionResult);

    }


    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
            = new ResultCallback<PlacePhotoResult>() {
        @Override
        public void onResult(PlacePhotoResult placePhotoResult) {
            if (!placePhotoResult.getStatus().isSuccess()) {
                return;
            }
            mPlaceImage.setImageBitmap(placePhotoResult.getBitmap());
        }
    };

    private void placePhotosAsync(String placeId) {
        Places.GeoDataApi.getPlacePhotos(mApiClient, placeId)
                .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {


                    @Override
                    public void onResult(PlacePhotoMetadataResult photos) {
                        if (!photos.getStatus().isSuccess()) {
                            return;
                        }

                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                        if (photoMetadataBuffer.getCount() > 0) {
                            photoMetadataBuffer.get(0)
                                    .getScaledPhoto(mApiClient, mPlaceImage.getWidth(),
                                            mPlaceImage.getHeight())
                                    .setResultCallback(mDisplayPhotoResultCallback);
                        }
                        photoMetadataBuffer.release();
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady Camera: " + googleMap.getCameraPosition().toString());
        googleMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
    }

    private class GetWikiInfoTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;
            JSONObject jsonObject = null;
            String extractString = null;
            try {
                URL url = new URL(mPlaceUrl);
                Log.i(TAG, "Wiki URL: " + url);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                if (builder.length() == 0) {
                    return null;
                }
                String response = builder.toString();
                try {
                    jsonObject = new JSONObject(response);
                    JSONObject queryObject = jsonObject.getJSONObject("query");
                    JSONObject pageObject = queryObject.getJSONObject("pages");
                    Iterator<?> iterator = pageObject.keys();
                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();
                        JSONObject extractObject = pageObject.getJSONObject(key);
                        if (extractObject.has("extract")) {
                            extractString = extractObject.getString("extract");
                        } else {
                            extractString = getString(R.string.wiki_data_unavailable);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return extractString;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String extract) {
            super.onPostExecute(extract);
            mPoiWikiText.setMaxLines(3);
            mPoiWikiText.setEllipsize(TextUtils.TruncateAt.END);
            mPoiWikiText.setText(extract);
            mPoiWikiText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isClicked) {
                        mPoiWikiText.setMaxLines(Integer.MAX_VALUE);
                        isClicked = true;
                    } else {
                        mPoiWikiText.setMaxLines(3);
                        isClicked = false;
                    }
                }
            });

            Log.i(TAG, "JSON Response: " + extract);
        }
    }
}
