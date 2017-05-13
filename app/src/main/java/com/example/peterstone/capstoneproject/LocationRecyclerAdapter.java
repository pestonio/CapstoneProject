package com.example.peterstone.capstoneproject;

import android.content.Context;
import android.content.Intent;
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

public class LocationRecyclerAdapter extends RecyclerView.Adapter<LocationRecyclerAdapter.PlaceViewHolder> {

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView cardPlaceName;
        TextView cardPlaceRating;
        ImageView cardImageView;
        ImageView deleteImageView;

        PlaceViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.rc_current_card_view);
            cardPlaceName = (TextView) view.findViewById(R.id.rc_current_place_name);
            cardPlaceRating = (TextView) view.findViewById(R.id.rating);
            cardImageView = (ImageView) view.findViewById(R.id.rc_current_place_image);
            deleteImageView = (ImageView) view.findViewById(R.id.delete_saved_item);
        }
    }

    List<PlaceClass> mPlaces;
    Context mContext;

    public LocationRecyclerAdapter(Context context, List<PlaceClass> places) {
        this.mPlaces = places;
        this.mContext = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.location_detail_list_item, viewGroup, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaceViewHolder placeViewHolder, final int i) {
        String rating = "Rating: ";
        placeViewHolder.deleteImageView.setVisibility(View.GONE);
        placeViewHolder.cardPlaceName.setText(mPlaces.get(i).mPlaceName);
        if (mPlaces.get(i).mPlaceRating != null) {
            placeViewHolder.cardPlaceRating.setText(rating.concat(mPlaces.get(i).mPlaceRating));
        }
        if (mPlaces.get(i).mPlaceImageUrl != null) {
            Picasso.with(mContext).load(mPlaces.get(i).mPlaceImageUrl).into(placeViewHolder.cardImageView);
        } else {
            Picasso.with(mContext).load(R.drawable.placeholder).into(placeViewHolder.cardImageView);
        }
        placeViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PointOfInterestDetails.class);
                intent.putExtra("origin", R.integer.ORIGIN_CURRENT_LOCATION);
                intent.putExtra("place_name", mPlaces.get(i).mPlaceName);
                intent.putExtra("place_photo", mPlaces.get(i).mPlaceImageUrl);
                intent.putExtra("place_lat", mPlaces.get(i).mPlaceLat);
                intent.putExtra("place_long", mPlaces.get(i).mPlaceLong);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
    }
}
