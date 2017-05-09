package com.example.peterstone.capstoneproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import com.example.peterstone.capstoneproject.SQL.PlaceContract;
import com.example.peterstone.capstoneproject.SQL.SavedPlacesDBHelper;
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
    private LatLng latLng;
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
        final Intent intent = getIntent();
        int origin = intent.getIntExtra("origin", 0);
        if (origin == R.integer.ORIGIN_CURRENT_LOCATION) {
            placeName = intent.getStringExtra("place_name");
            placeNameTextView.setText(placeName);
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
            final SQLiteDatabase sqLiteDatabase = new SavedPlacesDBHelper(PointOfInterestDetails.this).getWritableDatabase();
            final ContentValues values = new ContentValues();
            //TODO AsyncTask for Wiki
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    values.put(PlaceContract.SavedPlaceEntry.COLUMN_PLACE_NAME, placeName);
                    values.put(PlaceContract.SavedPlaceEntry.COLUMN_PLACE_IMAGE_URL, intent.getStringExtra("place_photo"));
                    values.put(PlaceContract.SavedPlaceEntry.COLUMN_PLACE_LAT, placeLat);
                    values.put(PlaceContract.SavedPlaceEntry.COLUMN_PLACE_LONG, placeLong);
                    sqLiteDatabase.insert(PlaceContract.SavedPlaceEntry.TABLE_NAME, null, values);
                    sqLiteDatabase.close();
                    Log.i(TAG, "Item Saved: " + values);
                    Toast.makeText(PointOfInterestDetails.this, "Point Of Interest added to your saved places.", Toast.LENGTH_SHORT).show();
                    //TODO save to DB.
                }
            });
        } else if (origin == R.integer.ORIGIN_GOOGLE_SEARCH) {

            actionButton.setVisibility(View.GONE);
            placeNameTextView.setText(intent.getStringExtra("place_name"));
            buildGoogleApi();
            placePhotosAsync(intent.getStringExtra("place_id"));
            //TODO AsyncTask for Wiki
            //TODO Progress Bar
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
        Log.i(TAG, "onMapReady Camera: " + googleMap.getCameraPosition().toString());
        googleMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
    }
}
