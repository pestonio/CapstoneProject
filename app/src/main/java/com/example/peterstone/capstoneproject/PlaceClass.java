package com.example.peterstone.capstoneproject;

import android.graphics.Bitmap;

/**
 * Created by Peter Stone on 28/04/2017.
 */

public class PlaceClass {
    String mPlaceName;
//  String mPlaceId;
//  String mPlaceRating;
//  String mPlaceAddress;
    Bitmap mPlaceImage;

//    String placeId, String placeRating, String placeAddress,
    //TODO add above details into view.

    public PlaceClass(String placename, Bitmap placeImage){
        this.mPlaceName = placename;
//        this.mPlaceId = placeId;
//        this.mPlaceRating = placeRating;
//        this.mPlaceAddress = placeAddress;
        this.mPlaceImage = placeImage;
    }
}
