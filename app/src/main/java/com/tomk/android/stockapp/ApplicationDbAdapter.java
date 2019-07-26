package com.tomk.android.stockapp;

/**
 * Created by Tom Kowszun.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tomk.android.stockapp.models.WatchRepository.WatchListItem;

import java.util.ArrayList;


public class ApplicationDbAdapter {

    public static final String DATABASE_NAME = "APPLICATION_DATABASE.db";

    private static final String WATCH_LIST_TABLE = "WATCH_LIST_TABLE";
    private static final String TICKER_TAPE_TABLE = "TICKER_TAPE_TABLE";

    private static final int DATABASE_VERSION = 201;
    private final Context mCtx;
    private static String TAG = ApplicationDbAdapter.class.getSimpleName();

    private SQLiteDatabase applicationDatabase;

    // Watch List Table
    private static final String NAME = "watchName";
    private static final String DESCRIPTION = "watchDescription";
    private static final String VALUE = "value";
    private static final String VALUE_CHANGE = "valueChange";
    private static final String VALUE_CHANGE_PERCENT = "valueChangePercent";
    private static final String VALUE_DATE = "valueDate";
    private static final String VALUE_TIME = "valueTime";



    private static final String CREATE_WATCH_LIST_TABLE = "create table " + WATCH_LIST_TABLE + "(" +
            NAME + " STRING PRIMARY KEY not null UNIQUE," +
            DESCRIPTION + " TEXT," +
            VALUE + " TEXT," +
            VALUE_CHANGE + " TEXT," +
            VALUE_CHANGE_PERCENT + " TEXT," +
            VALUE_DATE + " TEXT," +
            VALUE_TIME + " TEXT" + ");";

    public ApplicationDbAdapter(Context ctx) {
        this.mCtx = ctx;

    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(CREATE_WATCH_LIST_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + WATCH_LIST_TABLE);
            onCreate(db);
        }
    }

    public void open() throws SQLException {
        DatabaseHelper mDbHelper = new DatabaseHelper(mCtx);

        // Try opening the database.  If no database found, a new one will be created
        try {
            applicationDatabase = mDbHelper.getWritableDatabase();
        } catch (Exception ex) {
            String outS = " ************ Exception opening database " + ex;
            Log.i(" *=* ", outS);
        }

        // Get the list of tables in the database
        String dbPath = applicationDatabase.getPath();
        final ArrayList<String> dirArray = new ArrayList<>();
        Cursor c = applicationDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        while (c.moveToNext()) {
            String s = c.getString(0);
            if (!s.equals("android_metadata")) {
                dirArray.add(s);
                String outS = " z================>  Table name " + s + " " + dbPath + " is DB open ? " + applicationDatabase.isOpen();
                Log.i(" *=* ", outS);
            }
        }
        c.close();
    }

    // Populates Watch List Table inside the DB from a list of items passed into it
    public long insertWatchList(ArrayList<WatchListItem> watchlist, boolean replace) {
        long result = (long) 0.0;
        if (replace) {
            for (WatchListItem item : watchlist) {
                try {
                    ContentValues newValues = new ContentValues();

                    newValues.put(ApplicationDbAdapter.NAME, item.getWatchListName());
                    newValues.put(ApplicationDbAdapter.DESCRIPTION, item.getWatchListDescription());
                    newValues.put(ApplicationDbAdapter.VALUE, item.getValue());
                    newValues.put(ApplicationDbAdapter.VALUE_CHANGE, item.getValueChange());
                    newValues.put(ApplicationDbAdapter.VALUE_CHANGE_PERCENT, item.getValueChangePercent());
                    newValues.put(ApplicationDbAdapter.VALUE_DATE, item.getValueDate());
                    newValues.put(ApplicationDbAdapter.VALUE_TIME, item.getValueTime());
                    result = applicationDatabase.insertWithOnConflict(WATCH_LIST_TABLE, null, newValues, SQLiteDatabase.CONFLICT_IGNORE);
                } catch (IllegalArgumentException e) {
                    System.out.println(" IllegalArgumentException " + e.toString());
                } catch (SQLException e) {
                    System.out.println(" SQLException " + e.toString());
                } catch (Exception e) {
                    System.out.println(" Exception " + e.toString());
                }
            }
        } else {
            result = (long) 0.0;
        }

        return result;
    }


    // Reads the Watch List from the DB table
    public ArrayList<WatchListItem> getWatchList() {
        ArrayList<WatchListItem> watchList = new ArrayList<>();

        // Get the the table
        Cursor watchCursor = null;

        try {
            watchCursor = applicationDatabase.rawQuery("select * from " + WATCH_LIST_TABLE, null);
        } catch (SQLiteException e)
        {
            System.out.println(" SQLiteException " + e.toString());
        }
        if (watchCursor != null) {
            if (watchCursor.moveToFirst()) {
                do {
                    WatchListItem watchListItem = getWatchListItemFromCursor(watchCursor);
                    watchList.add(watchListItem);
                } while (watchCursor.moveToNext());
            }
        }
        if (watchCursor != null) watchCursor.close();

        return watchList;
    }

    private static WatchListItem getWatchListItemFromCursor(Cursor cursor) {
        WatchListItem watchListItem = new WatchListItem();

        watchListItem.setWatchListName(cursor.getString(cursor.getColumnIndex(NAME)));
        watchListItem.setWatchListDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
        watchListItem.setValue(cursor.getString(cursor.getColumnIndex(VALUE)));
        watchListItem.setValueChange(cursor.getString(cursor.getColumnIndex(VALUE_CHANGE)));
        watchListItem.setValueChangePercent(cursor.getString(cursor.getColumnIndex(VALUE_CHANGE_PERCENT)));
        watchListItem.setValueDate(cursor.getString(cursor.getColumnIndex(VALUE_DATE)));
        watchListItem.setValueTime(cursor.getString(cursor.getColumnIndex(VALUE_TIME)));

        return (watchListItem);
    }

    public boolean deleteAllWatchListData() {
        boolean result = false;
        try {
            result = applicationDatabase.delete(WATCH_LIST_TABLE, null, null) > 0;
        } catch (IllegalArgumentException e) {
            System.out.println(" IllegalArgumentException " + e.toString());
        } catch (SQLException e) {
            System.out.println(" SQLException " + e.toString());
        } catch (Exception e) {
            System.out.println(" Exception " + e.toString());
        }

        return result;
    }


}
