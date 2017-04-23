package com.example.peterstone.capstoneproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Peter Stone on 23/04/2017.
 */

public class LocationDetails extends AppCompatActivity {

    private static final String TAG = LocationDetails.class.getSimpleName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_detail_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.location_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView locationImageView = (ImageView) findViewById(R.id.location_detail_image);
        TextView locationNameTextView = (TextView) findViewById(R.id.location_detail_name);
        TextView locationDetailsTextView = (TextView) findViewById(R.id.location_detail_desc);
        TextView attributionBase = (TextView) findViewById(R.id.location_detail_attr_base);
        TextView attributionName = (TextView) findViewById(R.id.location_detail_attr_name);

        Intent intent = getIntent();
        int originId = intent.getIntExtra("origin", 0);
        if (originId == 101) {
            SharedPreferences sharedPreferences = getSharedPreferences("current_location_preferences", Context.MODE_PRIVATE);
            String locationName = sharedPreferences.getString(getString(R.string.last_known_current_location), null);
            String locationText = sharedPreferences.getString(getString(R.string.current_location_text), null);
            String attributions = sharedPreferences.getString(getString(R.string.current_location_image_attrib), null);
            String encoded = sharedPreferences.getString(getString(R.string.current_location_image), null);
            byte[] imageAsBytes = Base64.decode(encoded, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
            locationImageView.setImageBitmap(bitmap);
            locationNameTextView.setText(locationName);
            locationDetailsTextView.setText(locationText);
            attributionBase.setText(R.string.attribution_title_photo);
            if (Build.VERSION.SDK_INT >= 24) {
                attributionName.setText(Html.fromHtml(sharedPreferences.getString(getString(R.string.current_location_image_attrib), null), Html.FROM_HTML_MODE_LEGACY));
            } else {
                attributionName.setText(Html.fromHtml(sharedPreferences.getString(getString(R.string.current_location_image_attrib), null)));
            }
        }
    }
}
