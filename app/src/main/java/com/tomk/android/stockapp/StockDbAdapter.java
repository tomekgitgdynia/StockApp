package com.tomk.android.stockapp;

/**
 * Created by Tom Kowszun.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tomk.android.stockapp.models.MetaData;
import com.tomk.android.stockapp.models.StockResponse;
import com.tomk.android.stockapp.models.TimeSeriesItem;

import java.util.ArrayList;
import java.util.Date;


public class StockDbAdapter {
    public static final String DATABASE_NAME = "STOCK_DATABASE.db";
    public static final int MAX_INDICATORS = 3;
    private static final String STOCK_METADATA_TABLE = "STOCK_METADATA_TABLE";
    private static final String STOCK_TIME_SERIES_TABLE = "STOCK_TIME_SERIES_TABLE";
    private static final String STOCKS_LIST_TABLE = "STOCKS_LIST_TABLE";

    private static final int DATABASE_VERSION = 201;

    private final Context mCtx;
    private static String TAG = StockDbAdapter.class.getSimpleName();

    private SQLiteDatabase stockDatabase;

    private static final String STOCK_SYMBOL = "stockSymbol";

    private static final String META_INFORMATION = "information";
    private static final String META_LAST_REFRESHED_DATE = "lastRefreshedDate";
    private static final String META_LAST_REFRESHED_TIME = "lastRefreshedTime";
    private static final String META_INTERVAL = "interval";
    private static final String META_OUTPUT_SIZE = "outputSize";
    private static final String META_TIME_ZONE = "timeZone";

    private static final String TIME_SERIES_DATE = "date";
    private static final String TIME_SERIES_INTERVAL = "interval";
    private static final String TIME_SERIES_OPEN = "open";
    private static final String TIME_SERIES_CLOSE = "close";
    private static final String TIME_SERIES_HIGH = "high";
    private static final String TIME_SERIES_LOW = "low";
    private static final String TIME_SERIES_VOLUME = "volume";

    private static final String TIME_SERIES_INDICATOR_1 = "indicator1";
    private static final String TIME_SERIES_INDICATOR_2 = "indicator2";
    private static final String TIME_SERIES_INDICATOR_3 = "indicator3";

    private static final String STOCK_NAME = "stockName";
    private static final String STOCK_DESCRIPTION = "stockDescription";
    private static final String MARKET_NAME = "marketName";
    private static final String MARKET_DESCRIPTION = "marketDescription";


    private static final String CREATE_TABLE_STOCK_METADATA = "create table " + STOCK_METADATA_TABLE + "(" + STOCK_SYMBOL + " STRING PRIMARY KEY not null UNIQUE," + STOCK_NAME + " TEXT," + META_INFORMATION + " TEXT," + META_LAST_REFRESHED_DATE + " TEXT," + META_LAST_REFRESHED_TIME + " TEXT," + META_INTERVAL + " TEXT," + META_OUTPUT_SIZE + " TEXT," + META_TIME_ZONE + " TEXT" + ");";

    private static final String CREATE_TABLE_STOCK_TIME_SERIES = "create table " + STOCK_TIME_SERIES_TABLE + "(" + STOCK_SYMBOL + " TEXT," + STOCK_NAME + " TEXT," + TIME_SERIES_DATE + " STRING PRIMARY KEY not null UNIQUE," + TIME_SERIES_INTERVAL + " TEXT," + TIME_SERIES_OPEN + " TEXT," + TIME_SERIES_CLOSE + " TEXT," + TIME_SERIES_HIGH + " TEXT," + TIME_SERIES_LOW + " TEXT," + TIME_SERIES_VOLUME + " TEXT," + TIME_SERIES_INDICATOR_1 + " TEXT," + TIME_SERIES_INDICATOR_2 + " TEXT," + TIME_SERIES_INDICATOR_3 + " TEXT" + ");";

    private static final String CREATE_STOCKS_LIST_TABLE = "create table " + STOCKS_LIST_TABLE + "(" + STOCK_SYMBOL + " STRING PRIMARY KEY not null UNIQUE," + STOCK_NAME + " TEXT," + STOCK_DESCRIPTION + " TEXT," + MARKET_NAME + " TEXT," + MARKET_DESCRIPTION + " TEXT" + ");";

    public StockDbAdapter(Context ctx) {
        this.mCtx = ctx;

    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(CREATE_STOCKS_LIST_TABLE);
            db.execSQL(CREATE_TABLE_STOCK_METADATA);
            db.execSQL(CREATE_TABLE_STOCK_TIME_SERIES);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + STOCK_METADATA_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + STOCK_TIME_SERIES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + STOCKS_LIST_TABLE);
            onCreate(db);
        }
    }

    public void open() throws SQLException {
        DatabaseHelper mDbHelper = new DatabaseHelper(mCtx);
        try {
            stockDatabase = mDbHelper.getWritableDatabase();
        } catch (Exception ex) {
            Log.d(TAG, "Exception opening database" + ex);
        }

        String dbPath = stockDatabase.getPath();

        final ArrayList<String> dirArray = new ArrayList<>();
        Cursor c = stockDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        while (c.moveToNext()) {
            String s = c.getString(0);
            if (!s.equals("android_metadata")) {
                dirArray.add(s);
            }
        }
        c.close();
    }

    public void insertStockResponse(StockResponse stockResponse) {
        deleteAllStockData();
        long resultInsertMD = insertMetaData(stockResponse.getMetaData(), true);
        long resultInsertTS = insertTimeSeries(stockResponse.getTimeSeriesItems(), stockResponse.getIndicators(),
                stockResponse.getMetaData().getSymbol(), stockResponse.getMetaData().getName(), true);
    }


    private long insertMetaData(MetaData metaData, boolean replace) {

        long result = (long) 0.0;
        if (replace) {

            try {
                ContentValues newValues = new ContentValues();
                String convertedDate = Util.dateToString(metaData.getLastRefreshedDate());
                newValues.put(StockDbAdapter.STOCK_SYMBOL, metaData.getSymbol());
                newValues.put(StockDbAdapter.STOCK_NAME, metaData.getName());
                newValues.put(StockDbAdapter.META_INFORMATION, metaData.getInformation());
                newValues.put(StockDbAdapter.META_LAST_REFRESHED_DATE, convertedDate);
                newValues.put(StockDbAdapter.META_INTERVAL, metaData.getInterval());
                newValues.put(StockDbAdapter.META_OUTPUT_SIZE, metaData.getOutputSize());
                newValues.put(StockDbAdapter.META_TIME_ZONE, metaData.getTimeZone());

                result = stockDatabase.insertWithOnConflict(STOCK_METADATA_TABLE, null, newValues, SQLiteDatabase.CONFLICT_IGNORE);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, " insertMetaData IllegalArgumentException " + e.toString());
            } catch (SQLException e) {
                Log.d(TAG, " insertMetaData SQLException " + e.toString());
            } catch (Exception e) {
                Log.d(TAG, " insertMetaData Exception " + e.toString());
            }

        } else {
            result = (long) 0.0;
        }

        return result;
    }

    private long insertTimeSeries(ArrayList<TimeSeriesItem> timeSeriesItems, ArrayList<ArrayList<Double>> indicators, String stockSymbol, String stockName, boolean replace) {

        long result = (long) 0.0;
        int dataIndex = 0;
        if (replace) {
            for (TimeSeriesItem item : timeSeriesItems) {

                try {
                    ContentValues newValues = new ContentValues();
                    String convertedDate = Util.dateToString(item.getDate());
                    newValues.put(StockDbAdapter.STOCK_SYMBOL, stockSymbol);
                    newValues.put(StockDbAdapter.STOCK_NAME, stockName);
                    newValues.put(StockDbAdapter.TIME_SERIES_DATE, convertedDate);
                    newValues.put(StockDbAdapter.TIME_SERIES_INTERVAL, item.getInterval());
                    newValues.put(StockDbAdapter.TIME_SERIES_OPEN, item.getOpen());
                    newValues.put(StockDbAdapter.TIME_SERIES_CLOSE, item.getClose());
                    newValues.put(StockDbAdapter.TIME_SERIES_HIGH, item.getHigh());
                    newValues.put(StockDbAdapter.TIME_SERIES_LOW, item.getLow());
                    newValues.put(StockDbAdapter.TIME_SERIES_VOLUME, item.getVolume());

                    if (indicators != null) {
                        for (int i = 0; i < indicators.size(); i++) {
                            switch (i) {
                                case 0:
                                    newValues.put(StockDbAdapter.TIME_SERIES_INDICATOR_1, indicators.get(i).get(dataIndex));
                                    break;
                                case 1:
                                    newValues.put(StockDbAdapter.TIME_SERIES_INDICATOR_2, indicators.get(i).get(dataIndex));
                                    break;
                                case 2:
                                    newValues.put(StockDbAdapter.TIME_SERIES_INDICATOR_3, indicators.get(i).get(dataIndex));
                                    break;
                            }
                        }
                    }

                    result = stockDatabase.insertWithOnConflict(STOCK_TIME_SERIES_TABLE, null, newValues, SQLiteDatabase.CONFLICT_IGNORE);
                    dataIndex++;
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, " insertTimeSeries IllegalArgumentException " + e.toString());
                } catch (SQLException e) {
                    Log.d(TAG, " insertTimeSeries SQLException " + e.toString());
                } catch (Exception e) {
                    Log.d(TAG, " insertTimeSeries Exception " + e.toString());
                }
            }
        } else {
            result = (long) 0.0;
        }

        return result;
    }

    public StockResponse getStockResponse() {

        StockResponse stockResponse = new StockResponse();

        // Get the metadata table
        Cursor metaDataCursor = stockDatabase.rawQuery("select * from " + STOCK_METADATA_TABLE, null);
        MetaData metadata = null;
        if (metaDataCursor != null) {
            if (metaDataCursor.moveToFirst()) {
                do {
                    metadata = getMetaDataFromCursor(metaDataCursor);
                } while (metaDataCursor.moveToNext());
            }
        }
        if (metaDataCursor != null) metaDataCursor.close();

        // Get the time series  table
        Cursor stockCursor = stockDatabase.rawQuery("select * from " + STOCK_TIME_SERIES_TABLE, null);
        ArrayList<TimeSeriesItem> timeSeriesList = new ArrayList<>();
        // Process the indicator data
        ArrayList<ArrayList<Double>> indicators = new ArrayList<>();

        ArrayList<Double> indicator_1 = new ArrayList<>();
        ArrayList<Double> indicator_2 = new ArrayList<>();
        ArrayList<Double> indicator_3 = new ArrayList<>();
        indicators.add(indicator_1);
        indicators.add(indicator_2);
        indicators.add(indicator_3);

        stockResponse.setIndicators(indicators);


        if (stockCursor != null) {
            if (stockCursor.moveToFirst()) {
                do {
                    TimeSeriesItem timeSeriesItem = getTimeSeriesItemFromCursor(stockCursor, stockResponse);
                    timeSeriesList.add(timeSeriesItem);
                } while (stockCursor.moveToNext());
            }
        }
        if (stockCursor != null) stockCursor.close();

        stockResponse.setMetaData(metadata);
        stockResponse.setTimeSeriesItems(timeSeriesList);

        return stockResponse;
    }

    private static TimeSeriesItem getTimeSeriesItemFromCursor(Cursor cursor, StockResponse stockResponse) {

        TimeSeriesItem timeSeriesItem = new TimeSeriesItem();

        Date date = Util.stringToDate(cursor.getString(cursor.getColumnIndex(TIME_SERIES_DATE)));
        timeSeriesItem.setDate(date);

        timeSeriesItem.setInterval(cursor.getInt(cursor.getColumnIndex(TIME_SERIES_INTERVAL)));
        timeSeriesItem.setOpen(cursor.getDouble(cursor.getColumnIndex(TIME_SERIES_OPEN)));
        timeSeriesItem.setClose(cursor.getDouble(cursor.getColumnIndex(TIME_SERIES_CLOSE)));
        timeSeriesItem.setHigh(cursor.getDouble(cursor.getColumnIndex(TIME_SERIES_HIGH)));
        timeSeriesItem.setLow(cursor.getDouble(cursor.getColumnIndex(TIME_SERIES_LOW)));
        timeSeriesItem.setVolume(cursor.getInt(cursor.getColumnIndex(TIME_SERIES_VOLUME)));


//        // Process the indicator data
        ArrayList<ArrayList<Double>> indicators = stockResponse.getIndicators();

//
        indicators.get(0).add(cursor.getDouble(cursor.getColumnIndex(TIME_SERIES_INDICATOR_1)));
        indicators.get(1).add(cursor.getDouble(cursor.getColumnIndex(TIME_SERIES_INDICATOR_2)));
        indicators.get(2).add(cursor.getDouble(cursor.getColumnIndex(TIME_SERIES_INDICATOR_3)));

        return (timeSeriesItem);
    }

    private static MetaData getMetaDataFromCursor(Cursor cursor) {
        MetaData metaData = new MetaData();

        metaData.setSymbol(cursor.getString(cursor.getColumnIndex(STOCK_SYMBOL)));
        metaData.setName(cursor.getString(cursor.getColumnIndex(STOCK_NAME)));
        metaData.setInformation(cursor.getString(cursor.getColumnIndex(META_INFORMATION)));
        Date date = Util.stringToDate(cursor.getString(cursor.getColumnIndex(META_LAST_REFRESHED_DATE)));
        metaData.setLastRefreshedDate(date);
        metaData.setOutputSize(cursor.getString(cursor.getColumnIndex(META_OUTPUT_SIZE)));
        metaData.setTimeZone(cursor.getString(cursor.getColumnIndex(META_TIME_ZONE)));

        return (metaData);
    }

    public long insertStocksList(ArrayList<StockListItem> stocksList, boolean replace) {

        long result = (long) 0.0;
        if (replace) {
            for (StockListItem item : stocksList) {

                try {
                    ContentValues newValues = new ContentValues();

                    newValues.put(StockDbAdapter.STOCK_SYMBOL, item.getStockSymbol());
                    newValues.put(StockDbAdapter.STOCK_NAME, item.getStockName());
                    newValues.put(StockDbAdapter.STOCK_DESCRIPTION, item.getStockDescription());
                    newValues.put(StockDbAdapter.MARKET_NAME, item.getMarketName());
                    newValues.put(StockDbAdapter.MARKET_DESCRIPTION, item.getMarketDescription());
                    result = stockDatabase.insertWithOnConflict(STOCKS_LIST_TABLE, null, newValues, SQLiteDatabase.CONFLICT_IGNORE);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, " insertStocksList IllegalArgumentException " + e.toString());
                } catch (SQLException e) {
                    Log.d(TAG, " insertStocksList SQLException " + e.toString());
                } catch (Exception e) {
                    Log.d(TAG, " insertStocksList Exception " + e.toString());
                }
            }
        } else {
            result = (long) 0.0;
        }

        return result;
    }

    public ArrayList<StockListItem> getStocksList() {

        ArrayList<StockListItem> stocksList = new ArrayList<>();

        // Get the the table
        Cursor stockCursor = stockDatabase.rawQuery("select * from " + STOCKS_LIST_TABLE, null);
        if (stockCursor != null) {
            if (stockCursor.moveToFirst()) {
                do {
                    StockListItem stockListItem = getStocksListItemFromCursor(stockCursor);
                    stocksList.add(stockListItem);
                } while (stockCursor.moveToNext());
            }
        }
        if (stockCursor != null) stockCursor.close();


        return stocksList;
    }

    private static StockListItem getStocksListItemFromCursor(Cursor cursor) {

        StockListItem stockListItem = new StockListItem();

        stockListItem.setStockSymbol(cursor.getString(cursor.getColumnIndex(STOCK_SYMBOL)));
        stockListItem.setStockName(cursor.getString(cursor.getColumnIndex(STOCK_NAME)));
        stockListItem.setStockDescription(cursor.getString(cursor.getColumnIndex(STOCK_DESCRIPTION)));
        stockListItem.setMarketName(cursor.getString(cursor.getColumnIndex(MARKET_NAME)));
        stockListItem.setMarketDescription(cursor.getString(cursor.getColumnIndex(MARKET_DESCRIPTION)));

        return (stockListItem);
    }

    public boolean deleteAllStockListData() {

        boolean result = false;

        try {
            result = stockDatabase.delete(STOCKS_LIST_TABLE, null, null) > 0;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, " deleteAllStockListData IllegalArgumentException " + e.toString());
        } catch (SQLException e) {
            Log.d(TAG, " deleteAllStockListData SQLException " + e.toString());
        } catch (Exception e) {
            Log.d(TAG, " deleteAllStockListData Exception " + e.toString());
        }

        return result;
    }

    public boolean deleteAllStockData() {

        boolean result = false;

        try {
            result = stockDatabase.delete(STOCK_METADATA_TABLE, null, null) > 0;
            result = stockDatabase.delete(STOCK_TIME_SERIES_TABLE, null, null) > 0;
            result = stockDatabase.delete(STOCKS_LIST_TABLE, null, null) > 0;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, " deleteAllStockData IllegalArgumentException " + e.toString());
        } catch (SQLException e) {
            Log.d(TAG, " deleteAllStockData IllegalArgumentException " + e.toString());
        } catch (Exception e) {
            Log.d(TAG, " deleteAllStockData IllegalArgumentException " + e.toString());
        }

        return result;
    }
}
