package com.example.peterstone.capstoneproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by Peter Stone on 23/04/2017.
 */

public class PointOfInterestDetails extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private GoogleApiClient mApiClient;
    private static final String TAG = PointOfInterestDetails.class.getSimpleName();
    private ImageView mPlaceImage;
    private RecyclerView mRecyclerView;
    private LatLng test;
    private String placeName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_detail_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.location_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPlaceImage = (ImageView) findViewById(R.id.location_detail_image);
        TextView placeNameTextView = (TextView) findViewById(R.id.location_detail_name);
        FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.location_fab);
        mRecyclerView = (RecyclerView) findViewById(R.id.location_detail_recycler_view);
        Intent intent = getIntent();
        int origin = intent.getIntExtra("origin", 0);
        if (origin == R.integer.ORIGIN_CURRENT_LOCATION) {
            placeName = intent.getStringExtra("place_name");
            placeNameTextView.setText(placeName);
            Picasso.with(this).load(intent.getStringExtra("place_photo")).into(mPlaceImage);
            double placeLat = intent.getDoubleExtra("place_lat", 0);
            double placeLong = intent.getDoubleExtra("place_long", 0);
            mRecyclerView.setVisibility(View.GONE);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            test = new LatLng(placeLat, placeLong); //TODO pass LatLng from JSON object.
            CameraPosition cameraPosition = new CameraPosition(test, 14, 0, 0);
            GoogleMapOptions options = new GoogleMapOptions().liteMode(true).camera(cameraPosition);
            SupportMapFragment supportMapFragment = SupportMapFragment.newInstance(options);
            supportMapFragment.getMapAsync(this);
            fragmentTransaction.replace(R.id.map_fragment, supportMapFragment);
            fragmentTransaction.commit();
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(PointOfInterestDetails.this, "Point Of Interest added to your saved places.", Toast.LENGTH_SHORT).show();
                    //TODO save to DB.
                }
            });
            //TODO AsyncTask for Wiki
        } else if (origin == R.integer.ORIGIN_GOOGLE_SEARCH) {
            actionButton.setVisibility(View.GONE);
            placeNameTextView.setText(intent.getStringExtra("place_name"));
            buildGoogleApi();
            placePhotosAsync(intent.getStringExtra("place_id"));
            //TODO AsyncTask for Wiki
            //TODO JSON attractions, recycler view.
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
        //TODO pass LatLng from JSON object along with POI name.
        Log.i(TAG, "onMapReady Camera: " + googleMap.getCameraPosition().toString());
//        LatLng test = new LatLng(51.432, -0.9701);
        googleMap.addMarker(new MarkerOptions().position(test).title(placeName));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(test, 14));
    }
}
