package com.example.peterstone.capstoneproject.Fragments;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.peterstone.capstoneproject.PlaceClass;
import com.example.peterstone.capstoneproject.R;
import com.example.peterstone.capstoneproject.SQL.SavedPlaceContract;
import com.example.peterstone.capstoneproject.SQL.SavedPlacesProvider;
import com.example.peterstone.capstoneproject.SavedPlacesRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SavedPlacesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

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
    }

    @Override
    public void onStart() {
        super.onStart();
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.saved_places_list);
        mPlaceData = new ArrayList<>();
        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.e("LOADER CREATED", "Loader is created");
        Uri placeUri = SavedPlacesProvider.CONTENT_URI;
        return new CursorLoader(getActivity(), placeUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i("LOADER FINISHED", "Cursor contains: " + cursor.getCount());
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                int placeNameIndex = cursor.getColumnIndexOrThrow(SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_NAME);
                String placeName = cursor.getString(placeNameIndex);
                int placePhotoUrlIndex = cursor.getColumnIndexOrThrow(SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_IMAGE_URL);
                String placePhoto = cursor.getString(placePhotoUrlIndex);
                int placeLatIndex = cursor.getColumnIndexOrThrow(SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_LAT);
                double placeLat = cursor.getDouble(placeLatIndex);
                int placeLongIndex = cursor.getColumnIndexOrThrow(SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_LONG);
                double placeLong = cursor.getDouble(placeLongIndex);
                mPlaceData.add(new PlaceClass(placeName, null, null, null, placePhoto, placeLat, placeLong));
            }
            while (cursor.moveToNext());
            LinearLayoutManager linerLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(linerLayoutManager);
            mRecyclerView.setHasFixedSize(true);
            SavedPlacesRecyclerAdapter adapter = new SavedPlacesRecyclerAdapter(getActivity(), mPlaceData);
            mRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
