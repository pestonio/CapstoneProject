package com.example.peterstone.capstoneproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.peterstone.capstoneproject.SQL.SavedPlaceContract;
import com.example.peterstone.capstoneproject.SQL.SavedPlacesProvider;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Peter Stone on 08/05/2017.
 */

public class SavedPlacesRecyclerAdapter extends RecyclerView.Adapter<SavedPlacesRecyclerAdapter.SavedPlaceViewHolder> {

    private List<PlaceClass> mPlaces;
    private Context mContext;

    public SavedPlacesRecyclerAdapter(Context context, List<PlaceClass> places) {
        this.mPlaces = places;
        this.mContext = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public SavedPlaceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.location_detail_list_item, viewGroup, false);
        return new SavedPlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SavedPlaceViewHolder placeViewHolder, final int i) {
        String rating = mContext.getString(R.string.rating);
        placeViewHolder.cardPlaceName.setMaxLines(1);
        placeViewHolder.cardPlaceName.setEllipsize(TextUtils.TruncateAt.END);
        placeViewHolder.cardPlaceName.setText(mPlaces.get(i).mPlaceName);
        placeViewHolder.cardPlaceName.setContentDescription(mPlaces.get(i).mPlaceName);
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
                intent.putExtra("place_address", mPlaces.get(i).mPlaceAddress);
                intent.putExtra("place_lat", mPlaces.get(i).mPlaceLat);
                intent.putExtra("place_long", mPlaces.get(i).mPlaceLong);
                mContext.startActivity(intent);
            }
        });
        placeViewHolder.deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.delete_dialog_title)
                        .setMessage(R.string.delete_dialog_confirm)
                        .setPositiveButton(R.string.confirm_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String placeName = mPlaces.get(i).mPlaceName;
                                String selection = SavedPlaceContract.SavedPlaceEntry.COLUMN_PLACE_NAME + " LIKE ? ";
                                String[] selectionArgs = {placeName};
                                int rowsDeleted = mContext.getContentResolver().delete(SavedPlacesProvider.CONTENT_URI, selection, selectionArgs);
                                mPlaces.remove(i);
                                notifyItemRemoved(i);
                                notifyItemRangeChanged(i, mPlaces.size());
                                Toast.makeText(mContext, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(R.string.deny_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing.
                    }
                }).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
    }

    public static class SavedPlaceViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView cardPlaceName;
        TextView cardPlaceRating;
        ImageView cardImageView;
        ImageView deleteImageView;


        SavedPlaceViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.rc_current_card_view);
            cardPlaceName = (TextView) view.findViewById(R.id.rc_current_place_name);
            cardPlaceRating = (TextView) view.findViewById(R.id.rating);
            cardImageView = (ImageView) view.findViewById(R.id.rc_current_place_image);
            deleteImageView = (ImageView) view.findViewById(R.id.delete_saved_item);

        }
    }
}
