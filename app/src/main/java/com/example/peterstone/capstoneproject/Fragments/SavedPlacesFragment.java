package com.example.peterstone.capstoneproject.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.peterstone.capstoneproject.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SavedPlacesFragment extends Fragment {


    public SavedPlacesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.saved_places_fragment,container,false);
    }

}
