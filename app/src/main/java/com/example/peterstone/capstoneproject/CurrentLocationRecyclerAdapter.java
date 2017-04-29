package com.example.peterstone.capstoneproject;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Peter Stone on 28/04/2017.
 */

public class CurrentLocationRecyclerAdapter extends RecyclerView.Adapter<CurrentLocationRecyclerAdapter.PlaceViewHolder> {

    public static class PlaceViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        TextView cardPlaceName;
        ImageView cardImageView;

        PlaceViewHolder (View view){
            super(view);
            cardView = (CardView) view.findViewById(R.id.rc_current_card_view);
            cardPlaceName = (TextView) view.findViewById(R.id.rc_current_place_name);
            cardImageView = (ImageView) view.findViewById(R.id.rc_current_place_image);
        }
    }
    List<PlaceClass> mPlaces;
    Context mContext;

    public CurrentLocationRecyclerAdapter(Context context, List<PlaceClass> places){
        this.mPlaces = places;
        this.mContext = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.location_detail_list_item, viewGroup,false);
        PlaceViewHolder placeViewHolder = new PlaceViewHolder(view);
        return placeViewHolder;
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder placeViewHolder, int i) {
        placeViewHolder.cardPlaceName.setText(mPlaces.get(i).mPlaceName);
        Picasso.with(mContext).load(mPlaces.get(i).mPlaceImageUrl).into(placeViewHolder.cardImageView);
    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
    }
}
