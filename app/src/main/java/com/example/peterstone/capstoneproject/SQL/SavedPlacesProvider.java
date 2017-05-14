package com.example.peterstone.capstoneproject.SQL;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Peter Stone on 09/05/2017.
 */

public class SavedPlacesProvider extends ContentProvider {

    private SavedPlacesDBHelper placesDatabase;

    private static final int PLACES = 1;
    private static final int PLACE_ID = 2;

    private static final String AUTHORITY = "com.example.peterstone.capstoneproject.provider";

    private static final String BASE_PATH = "places";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

//    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/places";
//    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/place";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, PLACES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PLACE_ID);
    }

    @Override
    public boolean onCreate() {
        placesDatabase = new SavedPlacesDBHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        switch (sURIMatcher.match(uri)){
            case PLACES:
                cursor = placesDatabase.getReadableDatabase().query(SavedPlaceContract.SavedPlaceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase database = placesDatabase.getWritableDatabase();
        Uri returnUri = null;
        switch (uriType){
            case PLACES:
                long id = database.insert(SavedPlaceContract.SavedPlaceEntry.TABLE_NAME, null, values);
                if (id >0){
                    returnUri = ContentUris.withAppendedId(CONTENT_URI, id);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase database = placesDatabase.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType){
            case PLACES:
                rowsDeleted = database.delete(SavedPlaceContract.SavedPlaceEntry.TABLE_NAME, selection, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
