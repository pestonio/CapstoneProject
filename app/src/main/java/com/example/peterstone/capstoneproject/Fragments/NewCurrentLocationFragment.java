package com.example.peterstone.capstoneproject.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.peterstone.capstoneproject.CurrentLocationRecyclerAdapter;
import com.example.peterstone.capstoneproject.PlaceClass;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Peter Stone on 27/04/2017.
 */

public class NewCurrentLocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = NewCurrentLocationFragment.class.getSimpleName();
    private final static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private ImageView mCurrentLocationImage;
    private TextView mCurrentLocationName;
    private TextView mAttributionName;
    private TextView mAttributionBase;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mApiClient;
    private static boolean LOCATION_PERMISSION_GRANTED;
    private RecyclerView mRecyclerView;
    private List<PlaceClass> mPlaceData;
    private Bitmap bitmap;

    public NewCurrentLocationFragment() {
        //Empty constructor.
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.current_location_fragment, container, false);
        SupportPlaceAutocompleteFragment supportPlaceAutocompleteFragment = new SupportPlaceAutocompleteFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.autocomplete_container, supportPlaceAutocompleteFragment);
        transaction.commit();
        supportPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //TODO intent to open location detail activity
                Log.i(TAG, "Place Selected: " + place);
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "onPlaceSelected Error: " + status);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPlaceData = new ArrayList<>();
//        mCurrentLocationImage = (ImageView) getActivity().findViewById(R.id.current_place_image);
//        mCurrentLocationName = (TextView) getActivity().findViewById(R.id.current_place_name);
//        mAttributionBase = (TextView) getActivity().findViewById(R.id.photo_attr_title);
//        mAttributionName = (TextView) getActivity().findViewById(R.id.image_attributions);
//        mCurrentLocationName.setText("TEST");//TODO set placeholder text and image or pull from SP.
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.current_place_recycler_view);
        LinearLayoutManager linerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linerLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        //TODO data and adapter.
        buildGoogleApi();
        if (mApiClient != null) {
            mApiClient.connect();
        }
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mApiClient.stopAutoManage(getActivity());
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
            getUserLocation();
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
        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                List<Address> address = geocoder.getFromLocation(latitude, longitude, 1);
                String country = address.get(0).getCountryName();
                String adminArea = address.get(0).getAdminArea();
                String locality = address.get(0).getLocality();
                Log.i(TAG, "Current Location is: " + country + " " + locality + " " + adminArea);
                placeInfo = locality + " " + country;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (currentLocation == null && mApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
        }
        String url = urlBuilder("London United Kingdom");
        //TODO pass correct address to URL builder.
        getPlaceData(url);
    }

    private String urlBuilder(String url) {
        final String BASE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
        final String QUERY = "query";
        final String API_KEY = "key";
        Uri builtUri = Uri.parse(BASE_URL)
                .buildUpon()
                .appendQueryParameter(QUERY, url.concat(" attraction"))
                .appendQueryParameter(API_KEY, getString(R.string.webservice_api_key))
                .build();
        String builtUrl = builtUri.toString();
        Log.i(TAG, "URL: " + builtUrl);
        return builtUrl;
    }

    private void getPlaceData(String url) {
        final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        JsonObjectRequest jsonInitialRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray initialJsonArray = response.getJSONArray("results");
                    for (int i = 0; i < initialJsonArray.length(); i++) {
                        JSONObject placeObject = initialJsonArray.getJSONObject(i);
                        JSONArray photoArray = placeObject.getJSONArray("photos");
                        JSONObject photoRef = photoArray.getJSONObject(0);
                        String placeName = placeObject.getString("name");
                        String photoReference = photoRef.getString("photo_reference");
                        String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + getString(R.string.webservice_api_key);

//                        getPlaceImage(placeName, photoReference);
//                        Bitmap bitmap = getPlaceImage(photoRef.getString("photo_reference"));
                        mPlaceData.add(new PlaceClass(placeName, url));
                    }
                    CurrentLocationRecyclerAdapter adapter= new CurrentLocationRecyclerAdapter(getActivity(), mPlaceData);
                    mRecyclerView.setAdapter(adapter);

//                    mCurrentLocationName.setText(mPlaceData.get(4));
//                    placePhotosTask(mPlaceData.get(5));
                    Log.i(TAG, "Array Data = " + mPlaceData);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Volley Error: " + error);
            }
        });queue.add(jsonInitialRequest);
    }

//    public void getPlaceImage (final String placeName, String photoRef){
//
//        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
//        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
//            @Override
//            public void onResponse(Bitmap response) {
//                bitmap = response;
//                Log.i(TAG, "Photo Request Success " + response);
//                mPlaceData.add(new PlaceClass(placeName, bitmap));
//            }
//        }, 0, 0, ImageView.ScaleType.CENTER_CROP, null, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e(TAG, "Error in photo request");
//            }
//        });queue.add(imageRequest);
//    }


    private void placePhotosTask(String placeId) {
        // Create a new AsyncTask that displays the bitmap and attribution once loaded.
        new PhotoTask(mCurrentLocationImage.getWidth(), mCurrentLocationImage.getHeight()) {
            @Override
            protected void onPreExecute() {
//                mImageProgressBar.setVisibility(View.VISIBLE);
                // Display a temporary image to show while bitmap is loading.
//                mImageView.setImageResource(R.drawable.empty_photo);
            }

            @Override
            protected void onPostExecute(AttributedPhoto attributedPhoto) {
                if (attributedPhoto != null) {
                    // Photo has been loaded, display it.
//                    mCurrentLocationImage.setImageBitmap(attributedPhoto.bitmap);
                    // Display the attribution as HTML content if set.
                    if (attributedPhoto.attribution == null) {
//                        mAttributionBase.setVisibility(View.GONE);
//                        mAttributionName.setVisibility(View.GONE);
                    } else {
                        mAttributionName.setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= 24) {
//                            mAttributionBase.setText(R.string.attribution_title_photo);
//                            mAttributionName.setText(Html.fromHtml(attributedPhoto.attribution.toString(), Html.FROM_HTML_MODE_LEGACY));
                        } else {
//                            mAttributionBase.setText(R.string.attribution_title_photo);
//                            mAttributionName.setText(Html.fromHtml(attributedPhoto.attribution.toString()));
                        };
                    }
                    Log.v(TAG, attributedPhoto.attribution.toString());
                }
            }
        }.execute(placeId);
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

}
