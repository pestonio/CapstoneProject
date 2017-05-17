package com.example.peterstone.capstoneproject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.RemoteViews;

import com.example.peterstone.capstoneproject.SQL.PlaceContract;
import com.example.peterstone.capstoneproject.SQL.PlacesDBHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class PlaceAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            PlacesDBHelper dbHelper = new PlacesDBHelper(context);
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            Cursor cursor = database.query(PlaceContract.PlaceEntry.TABLE_NAME, null, null, null, null, null, null);
            cursor.moveToFirst();
            ArrayList<CharSequence> places = new ArrayList<>();
            ArrayList<CharSequence> ratings = new ArrayList<>();
            ArrayList<String> imageUrls = new ArrayList<>();
            do {
                int placeNameIndex = cursor.getColumnIndexOrThrow(PlaceContract.PlaceEntry.COLUMN_PLACE_NAME);
                int placeRatingIndex = cursor.getColumnIndexOrThrow(PlaceContract.PlaceEntry.COLUMN_PLACE_RATING);
                int placeImageIndex = cursor.getColumnIndexOrThrow(PlaceContract.PlaceEntry.COLUMN_PLACE_IMAGE_URL);
                CharSequence placeName = cursor.getString(placeNameIndex);
                CharSequence placeRating = cursor.getString(placeRatingIndex);
                String placeImageUrl = cursor.getString(placeImageIndex);
                places.add(placeName);
                ratings.add(placeRating);
                imageUrls.add(placeImageUrl);
            } while (cursor.moveToNext());
            cursor.close();
            int arrayPosition = new Random().nextInt(cursor.getCount());
            CharSequence currentPlace = places.get(arrayPosition);
            CharSequence currentPlaceRating = "Rating: " + ratings.get(arrayPosition);
            String currentImageUrl = imageUrls.get(arrayPosition);
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.place_app_widget);
            views.setTextViewText(R.id.widget_place_name, currentPlace);
            views.setTextViewText(R.id.widget_rating, currentPlaceRating);
            if (currentImageUrl != null) {
                Picasso.with(context).load(currentImageUrl).into(views, R.id.widget_image, new int[]{appWidgetId});
            } else {
                Picasso.with(context).load(R.drawable.placeholder).into(views, R.id.widget_image, new int[]{appWidgetId});
            }
            Intent mainActivityIntent = new Intent(Intent.ACTION_VIEW);
            Uri placeUri = Uri.parse("geo:0,0?q=" + currentPlace);
            mainActivityIntent.setData(placeUri);
            PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(context,0,mainActivityIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_image, mainActivityPendingIntent);
            Intent refreshIntent = new Intent(context, PlaceAppWidget.class);
            refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_refresh_button, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

