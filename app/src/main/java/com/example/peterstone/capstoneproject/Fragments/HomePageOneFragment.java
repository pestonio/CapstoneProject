package com.example.peterstone.capstoneproject.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.peterstone.capstoneproject.R;

public class HomePageOneFragment extends Fragment {
    private TextView currentPlace;

    public HomePageOneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.current_location_fragment, container, false);
        currentPlace = (TextView) view.findViewById(R.id.current_place_name);
        return view;
    }

}