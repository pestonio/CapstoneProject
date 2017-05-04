package com.example.peterstone.capstoneproject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.peterstone.capstoneproject.Fragments.CurrentLocationFragment;
import com.example.peterstone.capstoneproject.Fragments.SavedPlacesFragment;

/**
 * Created by Peter Stone on 17/04/2017.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {

    private int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new CurrentLocationFragment();
            case 1:
                return new SavedPlacesFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
