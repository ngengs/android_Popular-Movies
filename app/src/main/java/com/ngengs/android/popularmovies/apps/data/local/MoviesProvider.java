package com.ngengs.android.popularmovies.apps.data.local;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.ngengs.android.popularmovies.apps.globals.LocalDatabase;
import com.ngengs.android.popularmovies.apps.utils.DatabaseHelper;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by ngengs on 7/3/2017.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class MoviesProvider extends ContentProvider {
    public static final String TAG = "MoviesProvider";


    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private static final String FAILED_TO_INSERT_ROW_INTO = "Failed to insert row into ";

    // movies._id = ?
    private static final String MOVIE_ID_SELECTION = MoviesEntry.TABLE_NAME + "." + MoviesEntry._ID + " = ? ";

    private DatabaseHelper databaseHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = LocalDatabase.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, LocalDatabase.PATH_MOVIES, LocalDatabase.CODE_MOVIES);
        uriMatcher.addURI(authority, LocalDatabase.PATH_MOVIES + "/#", LocalDatabase.CODE_MOVIE_BY_ID);

        uriMatcher.addURI(authority, LocalDatabase.PATH_MOVIES + "/" + LocalDatabase.PATH_POPULAR, LocalDatabase.CODE_POPULAR_MOVIES);
        uriMatcher.addURI(authority, LocalDatabase.PATH_MOVIES + "/" + LocalDatabase.PATH_TOP_RATED, LocalDatabase.CODE_TOP_RATED_MOVIES);

        uriMatcher.addURI(authority, LocalDatabase.PATH_MOVIES + "/" + LocalDatabase.PATH_FAVORITES, LocalDatabase.CODE_FAVORITES);

        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = URI_MATCHER.match(uri);
        Cursor cursor;
        checkColumns(match, projection);
        switch (match) {
            case LocalDatabase.CODE_MOVIES:
                cursor = databaseHelper.getReadableDatabase().query(
                        MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case LocalDatabase.CODE_MOVIE_BY_ID:
                cursor = getMovieById(uri, projection, sortOrder);
                break;
            case LocalDatabase.CODE_POPULAR_MOVIES:
                cursor = getMoviesFromReferenceTable(MoviesPopular.TABLE_NAME,
                        projection, selection, selectionArgs, sortOrder);
                break;
            case LocalDatabase.CODE_TOP_RATED_MOVIES:
                cursor = getMoviesFromReferenceTable(MoviesTopRated.TABLE_NAME,
                        projection, selection, selectionArgs, sortOrder);
                break;
            case LocalDatabase.CODE_FAVORITES:
                cursor = getMoviesFromReferenceTable(MoviesFavorites.TABLE_NAME,
                        projection, selection, selectionArgs, sortOrder);
                break;
            default:
                return null;
        }
        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case LocalDatabase.CODE_MOVIES:
                return MoviesEntry.CONTENT_DIR_TYPE;
            case LocalDatabase.CODE_MOVIE_BY_ID:
                return MoviesEntry.CONTENT_ITEM_TYPE;
            case LocalDatabase.CODE_POPULAR_MOVIES:
                return MoviesPopular.CONTENT_DIR_TYPE;
            case LocalDatabase.CODE_TOP_RATED_MOVIES:
                return MoviesTopRated.CONTENT_DIR_TYPE;
            case LocalDatabase.CODE_FAVORITES:
                return MoviesFavorites.CONTENT_DIR_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        Uri returnUri;
        long id;
        switch (match) {
            case LocalDatabase.CODE_MOVIES:
                id = db.insertWithOnConflict(MoviesEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id > 0) {
                    returnUri = MoviesEntry.buildMovieUri(id);
                } else {
                    throw new android.database.SQLException(FAILED_TO_INSERT_ROW_INTO + uri);
                }
                break;
            case LocalDatabase.CODE_POPULAR_MOVIES:
                id = db.insert(MoviesPopular.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = MoviesPopular.CONTENT_URI;
                } else {
                    throw new android.database.SQLException(FAILED_TO_INSERT_ROW_INTO + uri);
                }
                break;
            case LocalDatabase.CODE_TOP_RATED_MOVIES:
                id = db.insert(MoviesTopRated.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = MoviesTopRated.CONTENT_URI;
                } else {
                    throw new android.database.SQLException(FAILED_TO_INSERT_ROW_INTO + uri);
                }
                break;
            case LocalDatabase.CODE_FAVORITES:
                id = db.insert(MoviesFavorites.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = MoviesFavorites.CONTENT_URI;
                } else {
                    throw new android.database.SQLException(FAILED_TO_INSERT_ROW_INTO + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int rowsDeleted;
        switch (match) {
            case LocalDatabase.CODE_MOVIES:
                rowsDeleted = db.delete(MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LocalDatabase.CODE_MOVIE_BY_ID:
                long id = MoviesEntry.getIdFromUri(uri);
                rowsDeleted = db.delete(MoviesEntry.TABLE_NAME, MOVIE_ID_SELECTION, new String[]{Long.toString(id)});
                break;
            case LocalDatabase.CODE_POPULAR_MOVIES:
                rowsDeleted = db.delete(MoviesPopular.TABLE_NAME, selection, selectionArgs);
                break;
            case LocalDatabase.CODE_TOP_RATED_MOVIES:
                rowsDeleted = db.delete(MoviesTopRated.TABLE_NAME, selection, selectionArgs);
                break;
            case LocalDatabase.CODE_FAVORITES:
                rowsDeleted = db.delete(MoviesFavorites.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int rowsUpdated;
        switch (match) {
            case LocalDatabase.CODE_MOVIES:
                rowsUpdated = db.update(MoviesEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public void shutdown() {
        databaseHelper.close();
        super.shutdown();
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int returnCount = 0;
        switch (match) {
            case LocalDatabase.CODE_MOVIES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = db.insertWithOnConflict(MoviesEntry.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                //noinspection ConstantConditions
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case LocalDatabase.CODE_POPULAR_MOVIES:
                db.beginTransaction();
                try {
                    db.setTransactionSuccessful();
                    for (ContentValues value : values) {
                        long id = db.insertWithOnConflict(MoviesPopular.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (id != -1) {
                            returnCount++;
                        }
                    }
                } finally {
                    db.endTransaction();
                }
                //noinspection ConstantConditions
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case LocalDatabase.CODE_TOP_RATED_MOVIES:
                db.beginTransaction();
                try {
                    db.setTransactionSuccessful();
                    for (ContentValues value : values) {
                        long id = db.insertWithOnConflict(MoviesTopRated.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (id != -1) {
                            returnCount++;
                        }
                    }
                } finally {
                    db.endTransaction();
                }
                //noinspection ConstantConditions
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        long id = MoviesEntry.getIdFromUri(uri);
        return databaseHelper.getReadableDatabase().query(
                MoviesEntry.TABLE_NAME,
                projection,
                MOVIE_ID_SELECTION,
                new String[]{Long.toString(id)},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMoviesFromReferenceTable(String tableName, String[] projection, String selection,
                                               String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        // tableName INNER JOIN movies ON tableName.movie_id = movies._id
        sqLiteQueryBuilder.setTables(
                tableName + " INNER JOIN " + MoviesEntry.TABLE_NAME +
                        " ON " + tableName + "." + LocalDatabase.COLUMN_MOVIE_ID_KEY +
                        " = " + MoviesEntry.TABLE_NAME + "." + MoviesEntry._ID
        );
        Log.d(TAG, "getMoviesFromReferenceTable: " + sqLiteQueryBuilder.toString());

        return sqLiteQueryBuilder.query(databaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private void checkColumns(int match, String[] projection) {
        if (projection != null) {
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(MoviesEntry.getColumnsWithTable()));
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection.");
            }
        }
    }
}
