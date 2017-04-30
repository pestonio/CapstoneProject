//package com.example.peterstone.capstoneproject.Fragments;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.text.Html;
//import android.text.TextUtils;
//import android.text.method.LinkMovementMethod;
//import android.util.Base64;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;
//import com.example.peterstone.capstoneproject.PointOfInterestDetails;
//import com.example.peterstone.capstoneproject.R;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.PlacePhotoMetadata;
//import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
//import com.google.android.gms.location.places.PlacePhotoMetadataResult;
//import com.google.android.gms.location.places.Places;
//import com.google.android.gms.location.places.ui.PlaceSelectionListener;
//import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.List;
//import java.util.Locale;
//
//public class CurrentLocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
//
//    private static final String TAG = CurrentLocationFragment.class.getSimpleName();
//    private final static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
//    private GoogleApiClient mApiClient;
//    private LocationRequest mLocationRequest;
//    private int permissionRequests = 0;
//    private TextView mUserLocation;
//    private ImageView mImageView;
//    private TextView mAttText;
//    private TextView mAttTextBase;
////    private ProgressBar mImageProgressBar;
//    private TextView mLocationText;
//    private boolean textIsClicked = false;
//
//
//    public CurrentLocationFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.current_location_fragment, container, false);
//        SupportPlaceAutocompleteFragment supportPlaceAutocompleteFragment = new SupportPlaceAutocompleteFragment();
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//        transaction.replace(R.id.autocomplete_container, supportPlaceAutocompleteFragment);
//        transaction.commit();
//        supportPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(Place place) {
//                Log.i(TAG, "Place: " + place.getName());
//                Log.i(TAG, "Place ID: " + place.getId());
//                Intent locationSelectedIntent = new Intent(getActivity(), PointOfInterestDetails.class);
//                locationSelectedIntent.putExtra("origin", 102);
//                startActivity(locationSelectedIntent);
//            }
//
//            @Override
//            public void onError(Status status) {
//                Log.i(TAG, "An error occurred: " + status);
//            }
//        });
//        return view;
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        textIsClicked = false;
//        initialiseViews();
//        mLocationText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (textIsClicked) {
//                    mLocationText.setMaxLines(5);
//                    textIsClicked = false;
//                }else {
//                    mLocationText.setMaxLines(Integer.MAX_VALUE);
//                    textIsClicked=true;
//                }
//            }
//        });
//        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);
//
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                if (mApiClient != null && mApiClient.isConnected()) {
//                    getCurrentLocation();
//                    swipeRefreshLayout.setRefreshing(false);
//
//                } else {
//                    buildGoogleApi();
//                    mApiClient.connect();
//                    swipeRefreshLayout.setRefreshing(false);
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        buildGoogleApi();
//        if (mApiClient != null && !mApiClient.isConnected()) {
//            mApiClient.connect();
//        }
//        mLocationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
//                .setInterval(10 * 1000)
//                .setFastestInterval(1000);
//        Log.i(TAG, "onResume Called");
//    }
//
//    protected synchronized void buildGoogleApi() {
//        mApiClient = new GoogleApiClient
//                .Builder(getContext())
//                .addConnectionCallbacks(this)
//                .addApi(Places.GEO_DATA_API)
//                .addApi(Places.PLACE_DETECTION_API)
//                .addApi(LocationServices.API)
//                .enableAutoManage(getActivity(), this)
//                .build();
//    }
//
//    private void initialiseViews(){
//        mUserLocation = (TextView) getActivity().findViewById(R.id.current_place_name);
//        mImageView = (ImageView) getActivity().findViewById(R.id.current_place_image);
//        mAttText = (TextView) getActivity().findViewById(R.id.image_attributions);
//        mAttTextBase = (TextView) getActivity().findViewById(R.id.photo_attr_title);
////        mImageProgressBar = (ProgressBar) getActivity().findViewById(R.id.image_load_progress);
////        mLocationText=(TextView) getActivity().findViewById(R.id.current_place_text);
//        mLocationText.setEllipsize(TextUtils.TruncateAt.END);
//        mLocationText.setMaxLines(5);
//        LinearLayout currentLayout = (LinearLayout) getActivity().findViewById(R.id.current_card_view);
//        currentLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent locationIntent = new Intent(getActivity(), PointOfInterestDetails.class);
//                locationIntent.putExtra("origin", 101);
//                startActivity(locationIntent);
//            }
//        });
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (mApiClient != null && mApiClient.isConnected()) {
//            mApiClient.stopAutoManage(getActivity());
//            mApiClient.disconnect();
//            mApiClient = null;
//        }
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.v(TAG, "Connection Failed " + connectionResult.getErrorMessage());
//    }
//
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        Log.v(TAG, "Connected");
//        if (permissionRequests < 10) {
//            getCurrentLocation();
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.v(TAG, "Connection Suspended");
//        mApiClient.disconnect();
//    }
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.v(TAG, "Permission Granted!");
//                    getCurrentLocation();
//                }
//            }
//        }
//    }
//
//    public void getCurrentLocation() {
//        String currentPlace = null;
//        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
//            permissionRequests++;
//            mLocationText.setText(R.string.permission_denied);
//            mUserLocation.setText(R.string.permission_denied);
////            mImageProgressBar.setVisibility(View.GONE);
//            Log.i(TAG, "Permission Requested: " + permissionRequests);
//        } else if (mApiClient.isConnected()) {
//            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
//            if (mLastLocation != null) {
//                double latitude = mLastLocation.getLatitude();
//                double longitude = mLastLocation.getLongitude();
//                Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
//                try {
//                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
//                    if (addresses.size() > 0) {
//                        String locality = addresses.get(0).getLocality();
//                        String country = addresses.get(0).getCountryName();
//                        String adminArea = addresses.get(0).getAdminArea();
//                        Log.v(TAG, "Address Data: " + locality + " + " + adminArea + country);
//                        currentPlace = locality + " " + country;
//                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("current_location_preferences", Context.MODE_PRIVATE);
//                        String savedLocation = sharedPreferences.getString(getString(R.string.last_known_current_location), null);
//                        if (currentPlace.equals(savedLocation)) {
//                            Log.i(TAG, "sharedPref and currentPlace Match");
//                            updateLocationName(currentPlace);
//                            updateLocationFromPref();
//                            return;
//                        } else if (!currentPlace.equals(savedLocation)) {
//                            getCurrentPlaceId(currentPlace);
//                            addLocationToSharedPreferences(currentPlace);
//                            updateLocationName(currentPlace);
////                            getLocationWikiIntro(currentPlace);
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (mLastLocation == null) {
//                LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
//            }
//        }
//        Log.v(TAG, "The current place is: " + currentPlace);
//    }
//
//    private void addLocationToSharedPreferences(String currentPlace) {
//        SharedPreferences sharedPref = getActivity().getSharedPreferences("current_location_preferences",Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString(getString(R.string.last_known_current_location), currentPlace);
//        editor.apply();
//        Log.v(TAG, "Current sharedPref is: " + currentPlace);
//        updateLocationName(currentPlace);
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        Log.v(TAG, "New Location " + location);
//    }
//
//    public void updateLocationName(String currentPlace) {
//        if (currentPlace == null) {
//            currentPlace = getString(R.string.unknown_location);
//        }
//        mUserLocation.setText(currentPlace);
//    }
//
//    public void updateLocationFromPref() {
////        mImageProgressBar.setVisibility(View.VISIBLE);
//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.current_location_preferences), Context.MODE_PRIVATE);
//        String encoded = sharedPreferences.getString(getString(R.string.current_location_image), null);
//        if (encoded == null){
//            return;
//        }
//        byte[] imageAsBytes = Base64.decode(encoded, Base64.DEFAULT);
//        Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
//        mImageView.setImageBitmap(bitmap);
//        if (Build.VERSION.SDK_INT >= 24) {
//            mAttTextBase.setText(R.string.attribution_title_photo);
//            mAttText.setText(Html.fromHtml(sharedPreferences.getString(getString(R.string.current_location_image_attrib), null), Html.FROM_HTML_MODE_LEGACY));
//        } else {
//            mAttTextBase.setText(R.string.attribution_title_photo);
//            mAttText.setText(Html.fromHtml(sharedPreferences.getString(getString(R.string.current_location_image_attrib), null)));
//        }
////        mImageProgressBar.setVisibility(View.GONE);
//        mAttText.setMovementMethod(LinkMovementMethod.getInstance());
//        String locationText = sharedPreferences.getString(getString(R.string.current_location_text), null);
//        mLocationText.setText(locationText);
//    }
//
//    private void getCurrentPlaceId(String location) {
//        if (location != null && !location.equals(getString(R.string.unknown_location))) {
//            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
//            String url = getString(R.string.webservice_base_url)
//                    + getString(R.string.webservice_api_key)
//                    + getString(R.string.webservice_query)
//                    + location.replaceAll(" ", "%20");
////                    + "%20attraction";
//            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    try {
//                        JSONArray results = response.getJSONArray("results");
//                        String placeId = null;
////                        StringBuilder places = new StringBuilder();
////                        for (int i=0; i<results.length(); i++) {
//                            JSONObject resultsObject = results.getJSONObject(0);
//                            placeId = resultsObject.getString("place_id");
//                            String placeName = resultsObject.getString("name");
////                            Log.v(TAG, "JSON ARRAY: " + placeName);
////                            places.append(placeName);
////                            places.append("\n");
////                        }
////                        mLocationText.setText(places.toString());
//                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("current_location_preferences", Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString(getString(R.string.current_place_id), placeId);
//                        editor.apply();
//                        Log.v(TAG, placeId);
//                        placePhotosTask(placeId);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.v(TAG, "Volley Error is: " + error);
//                }
//            });
//            queue.add(objectRequest);
//        }
//    }
//
//    private void placePhotosTask(String placeId) {
//        // Create a new AsyncTask that displays the bitmap and attribution once loaded.
//        new PhotoTask(mImageView.getWidth(), mImageView.getHeight()) {
//            @Override
//            protected void onPreExecute() {
////                mImageProgressBar.setVisibility(View.VISIBLE);
//                // Display a temporary image to show while bitmap is loading.
////                mImageView.setImageResource(R.drawable.empty_photo);
//            }
//
//            @Override
//            protected void onPostExecute(AttributedPhoto attributedPhoto) {
//                if (attributedPhoto != null) {
//                    // Photo has been loaded, display it.
//                    mImageView.setImageBitmap(attributedPhoto.bitmap);
////                    mImageProgressBar.setVisibility(View.GONE);
//                    Bitmap currentLocationImage = attributedPhoto.bitmap;
//                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                    currentLocationImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//                    byte[] bytes = byteArrayOutputStream.toByteArray();
//                    String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
//                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("current_location_preferences", Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString(getString(R.string.current_location_image), encodedImage);
//                    editor.apply();
//                    // Display the attribution as HTML content if set.
//                    if (attributedPhoto.attribution == null) {
//                        mAttText.setVisibility(View.GONE);
//                        mAttTextBase.setVisibility(View.GONE);
//                        editor.putString(getString(R.string.current_location_image_attrib), "");
//                        editor.apply();
//                    } else {
//                        mAttText.setVisibility(View.VISIBLE);
//                        if (Build.VERSION.SDK_INT >= 24) {
//                            mAttTextBase.setText(R.string.attribution_title_photo);
//                            mAttText.setText(Html.fromHtml(attributedPhoto.attribution.toString(), Html.FROM_HTML_MODE_LEGACY));
//                        } else {
//                            mAttTextBase.setText(R.string.attribution_title_photo);
//                            mAttText.setText(Html.fromHtml(attributedPhoto.attribution.toString()));
//                        }
//                        editor.putString(getString(R.string.current_location_image_attrib), attributedPhoto.attribution.toString());
//                        editor.apply();
//                    }
//                    Log.v(TAG, attributedPhoto.attribution.toString());
//                }
//            }
//        }.execute(placeId);
//    }
//
//    abstract class PhotoTask extends AsyncTask<String, Void, PhotoTask.AttributedPhoto> {
//
//        private int mHeight;
//
//        private int mWidth;
//
//        public PhotoTask(int width, int height) {
//            mHeight = height;
//            mWidth = width;
//        }
//
//        @Override
//        protected AttributedPhoto doInBackground(String... params) {
//            if (params.length != 1) {
//                return null;
//            }
//            final String placeId = params[0];
//            AttributedPhoto attributedPhoto = null;
//
//            PlacePhotoMetadataResult result = Places.GeoDataApi
//                    .getPlacePhotos(mApiClient, placeId).await();
//
//            if (result.getStatus().isSuccess()) {
//                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
//                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
//                    // Get the first bitmap and its attributions.
//                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
//                    CharSequence attribution = photo.getAttributions();
//                    // Load a scaled bitmap for this photo.
//                    Bitmap image = photo.getScaledPhoto(mApiClient, mWidth, mHeight).await()
//                            .getBitmap();
//
//                    attributedPhoto = new AttributedPhoto(attribution, image);
//                }
//                // Release the PlacePhotoMetadataBuffer.
//                photoMetadataBuffer.release();
//            }
//            return attributedPhoto;
//        }
//
//        /**
//         * Holder for an image and its attribution.
//         */
//        class AttributedPhoto {
//
//            public final CharSequence attribution;
//
//            public final Bitmap bitmap;
//
//            public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
//                this.attribution = attribution;
//                this.bitmap = bitmap;
//            }
//        }
//    }
//
////    public void getLocationWikiIntro(String currentPlace){
////        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
////        String formatting1 = currentPlace.replaceAll(" - ", ",_");
////        String formattedCurrentPlace = formatting1.replaceAll(" ", "%20");
////        String url = "https://en.wikipedia.org/w/api.php?format=json&action=query&titles=" + formattedCurrentPlace + "&redirects&prop=extracts&exintro&explaintext";
////        JsonObjectRequest wikiJsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
////            @Override
////            public void onResponse(JSONObject response) {
////                try {
////                    String key = null;
////                    JSONObject jsonObject = response.getJSONObject("query");
////                    JSONObject pages = jsonObject.getJSONObject("pages");
////                    Iterator<String> keys = pages.keys();
////                    if( keys.hasNext() ){
////                        key = keys.next();
////                        Log.v(TAG, key);
////                    }
////                    JSONObject extract = pages.getJSONObject(key);
////                    String extractText = extract.getString("extract").replaceAll("\n", "\n\n");
////                    Log.v(TAG, extractText);
////                    mLocationText.setText(extractText);
////                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("current_location_preferences", Context.MODE_PRIVATE);
////                    SharedPreferences.Editor editor = sharedPreferences.edit();
////                    editor.putString(getString(R.string.current_location_text), extractText);
////                    editor.apply();
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
////            }
////        }, new Response.ErrorListener() {
////            @Override
////            public void onErrorResponse(VolleyError error) {
////                Log.v(TAG, "Wiki Error");
////            }
////        });
////        queue.add(wikiJsonRequest);
////    }
//}