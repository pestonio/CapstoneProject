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
    double mPlaceLat;
    double mPlaceLong;


    public PlaceClass(String placename, String placeId, String placeRating, String placeAddress, String placeImageUrl, Double placeLat, Double placeLong) {
        this.mPlaceName = placename;
        this.mPlaceId = placeId;
        this.mPlaceRating = placeRating;
        this.mPlaceAddress = placeAddress;
        this.mPlaceImageUrl = placeImageUrl;
        this.mPlaceLat = placeLat;
        this.mPlaceLong = placeLong;
    }
}
