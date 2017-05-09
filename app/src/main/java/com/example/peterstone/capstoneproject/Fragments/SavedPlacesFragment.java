package com.example.peterstone.capstoneproject.Fragments;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.peterstone.capstoneproject.PlaceClass;
import com.example.peterstone.capstoneproject.R;
import com.example.peterstone.capstoneproject.SQL.PlaceContract;
import com.example.peterstone.capstoneproject.SQL.SavedPlacesDBHelper;
import com.example.peterstone.capstoneproject.SavedPlacesRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SavedPlacesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private List<PlaceClass> mPlaceData;



    public SavedPlacesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.saved_places_fragment,container,false);
        //TODO hide background icon + text, add delete functionality.
        //TODO Loader!
    }

    @Override
    public void onStart() {
        super.onStart();
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.saved_places_list);
        mPlaceData = new ArrayList<>();
        final SQLiteDatabase database = new SavedPlacesDBHelper(getActivity()).getReadableDatabase();
        String[] projection = {PlaceContract.SavedPlaceEntry.COLUMN_PLACE_NAME, PlaceContract.SavedPlaceEntry.COLUMN_PLACE_ID, PlaceContract.SavedPlaceEntry.COLUMN_PLACE_RATING, PlaceContract.SavedPlaceEntry.COLUMN_PLACE_ADDRESS, PlaceContract.SavedPlaceEntry.COLUMN_PLACE_IMAGE_URL, PlaceContract.SavedPlaceEntry.COLUMN_PLACE_LAT, PlaceContract.SavedPlaceEntry.COLUMN_PLACE_LONG};
        final Cursor cursor = database.query(PlaceContract.SavedPlaceEntry.TABLE_NAME, projection, null, null, null, null, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                cursor.moveToFirst();
                if (cursor.getCount() >0) {
                    do {
                        int placeNameIndex = cursor.getColumnIndexOrThrow(PlaceContract.SavedPlaceEntry.COLUMN_PLACE_NAME);
                        String placeName = cursor.getString(placeNameIndex);
                        int placePhotoUrlIndex = cursor.getColumnIndexOrThrow(PlaceContract.SavedPlaceEntry.COLUMN_PLACE_IMAGE_URL);
                        String placePhoto = cursor.getString(placePhotoUrlIndex);
                        int placeLatIndex = cursor.getColumnIndexOrThrow(PlaceContract.SavedPlaceEntry.COLUMN_PLACE_LAT);
                        double placeLat = cursor.getDouble(placeLatIndex);
                        int placeLongIndex = cursor.getColumnIndexOrThrow(PlaceContract.SavedPlaceEntry.COLUMN_PLACE_LONG);
                        double placeLong = cursor.getDouble(placeLongIndex);
                        mPlaceData.add(new PlaceClass(placeName, null, null, null, placePhoto, placeLat, placeLong));
                    }
                    while (cursor.moveToNext());
                    cursor.close();
                    database.close();
                }
            }
        }).start();

        LinearLayoutManager linerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linerLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        SavedPlacesRecyclerView adapter = new SavedPlacesRecyclerView(getActivity(), mPlaceData);
        mRecyclerView.setAdapter(adapter);
    }
}
