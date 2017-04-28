package com.example.peterstone.capstoneproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

/**
 * Created by Peter Stone on 23/04/2017.
 */

public class LocationDetails extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LocationDetails.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;

    private ImageView mLocationImageView;
    private TextView mLocationNameTextView;
    private TextView mLocationDetailsTextView;
    private TextView mAttributionBase;
    private TextView mAttributionName;
    private String placeId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_detail_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.location_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLocationImageView = (ImageView) findViewById(R.id.location_detail_image);
        mLocationNameTextView = (TextView) findViewById(R.id.location_detail_name);
        mLocationDetailsTextView = (TextView) findViewById(R.id.location_detail_desc);
        mAttributionBase = (TextView) findViewById(R.id.location_detail_attr_base);
        mAttributionName = (TextView) findViewById(R.id.location_detail_attr_name);

        Intent intent = getIntent();
        int originId = intent.getIntExtra("origin", 0);
        if (originId == 101) {
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.current_location_preferences), Context.MODE_PRIVATE);
            String locationName = sharedPreferences.getString(getString(R.string.last_known_current_location), null);
            String locationText = sharedPreferences.getString(getString(R.string.current_location_text), null);
            String encoded = sharedPreferences.getString(getString(R.string.current_location_image), null);
            byte[] imageAsBytes = Base64.decode(encoded, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
            mLocationImageView.setImageBitmap(bitmap);
            mLocationNameTextView.setText(locationName);
            mLocationDetailsTextView.setText(locationText);
            mAttributionBase.setText(R.string.attribution_title_photo);
            if (Build.VERSION.SDK_INT >= 24) {
                mAttributionName.setText(Html.fromHtml(sharedPreferences.getString(getString(R.string.current_location_image_attrib), null), Html.FROM_HTML_MODE_LEGACY));
            } else {
                mAttributionName.setText(Html.fromHtml(sharedPreferences.getString(getString(R.string.current_location_image_attrib), null)));
            }
        }if (originId == 102){
            buildGoogleApi();
            placeId = intent.getStringExtra("placeId");
            Log.v(TAG, placeId);
        }
    }

    protected synchronized void buildGoogleApi() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(this, this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        placePhotosTask(placeId);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    abstract class PhotoTask extends AsyncTask<String, Void, PhotoTask.AttributedPhoto> {

        private int mHeight;

        private int mWidth;

        public PhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

        /**
         * Loads the first photo for a place id from the Geo Data API.
         * The place id must be the first (and only) parameter.
         */
        @Override
        protected AttributedPhoto doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            AttributedPhoto attributedPhoto = null;

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    // Get the first bitmap and its attributions.
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    CharSequence attribution = photo.getAttributions();
                    // Load a scaled bitmap for this photo.
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
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
        new PhotoTask(mLocationImageView.getWidth(), mLocationImageView.getHeight()) {
            @Override
            protected void onPreExecute() {
                // Display a temporary image to show while bitmap is loading.
//                mImageView.setImageResource(R.drawable.empty_photo);
            }

            @Override
            protected void onPostExecute(AttributedPhoto attributedPhoto) {
                if (attributedPhoto != null) {
                    // Photo has been loaded, display it.
                    mLocationImageView.setImageBitmap(attributedPhoto.bitmap);

                    // Display the attribution as HTML content if set.
                    if (attributedPhoto.attribution == null) {
                        mAttributionName.setVisibility(View.GONE);
                    } else {
                        mAttributionName.setVisibility(View.VISIBLE);
                        mAttributionName.setText(Html.fromHtml(attributedPhoto.attribution.toString()));
                    }

                }
            }
        }.execute(placeId);
    }
}
