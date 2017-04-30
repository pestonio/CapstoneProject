package com.example.peterstone.capstoneproject.SQL;

import android.provider.BaseColumns;

/**
 * Created by Peter Stone on 30/04/2017.
 */

public class PlaceContract {

    private PlaceContract(){}

    public static class PlaceEntry implements BaseColumns{
        public static final String TABLE_NAME = "places";
        public static final String COLUMN_PLACE_NAME = "place_name";
        public static final String COLUMN_PLACE_ID = "place_id";
        public static final String COLUMN_PLACE_RATING = "place_rating";
        public static final String COLUMN_PLACE_ADDRESS = "place_address";
        public static final String COLUMN_PLACE_IMAGE_URL = "place_image_url";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PLACE_NAME + " TEXT, " +
                COLUMN_PLACE_ID + " TEXT, " +
                COLUMN_PLACE_RATING + " TEXT, " +
                COLUMN_PLACE_ADDRESS + " TEXT, " +
                COLUMN_PLACE_IMAGE_URL + " TEXT" + ")";
    }
}
