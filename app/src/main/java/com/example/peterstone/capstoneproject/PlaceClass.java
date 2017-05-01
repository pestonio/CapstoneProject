package com.example.peterstone.capstoneproject;

/**
 * Created by Peter Stone on 28/04/2017.
 */

public class PlaceClass {
    String mPlaceName;
    String mPlaceId;
    String mPlaceRating;
    String mPlaceAddress;
    String mPlaceImageUrl;


    public PlaceClass(String placename, String placeId, String placeRating, String placeAddress, String placeImageUrl) {
        this.mPlaceName = placename;
        this.mPlaceId = placeId;
        this.mPlaceRating = placeRating;
        this.mPlaceAddress = placeAddress;
        this.mPlaceImageUrl = placeImageUrl;
    }
}
