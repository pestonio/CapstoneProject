package com.example.peterstone.capstoneproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Peter Stone on 23/04/2017.
 */

public class PointOfInterestDetails extends AppCompatActivity {

    private static final String TAG = PointOfInterestDetails.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_detail_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.location_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView image = (ImageView) findViewById(R.id.location_detail_image);
        TextView placeName = (TextView) findViewById(R.id.location_detail_name);
        Intent intent = getIntent();
        placeName.setText(intent.getStringExtra("place_name"));
        intent.getStringExtra("place_photo");
        Picasso.with(this).load(intent.getStringExtra("place_photo")).into(image);

    }
}
