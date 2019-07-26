package com.tomk.android.stockapp;

/**
 * Created by Tom Kowszun
 *
 */

import android.content.Context;

import com.tomk.android.stockapp.models.WatchRepository.WatchListItem;
import com.tomk.android.stockapp.models.WatchRepository.WatchListRepository;

import java.util.ArrayList;

/**
 * This Loader loads default data from flat files into the DB
 */
public class DefaultDataAsyncLoader extends android.support.v4.content.AsyncTaskLoader<ArrayList<WatchListItem>> {


    private ApplicationDbAdapter aplicationDbAdapter;
    private Context context;


    public DefaultDataAsyncLoader(Context context, ApplicationDbAdapter adapter) {
        super(context);
        this.context = context;
        this.aplicationDbAdapter = adapter;

    }

    @Override
    public ArrayList<WatchListItem> loadInBackground() {

        saveDataInDB();
        return null;
    }

    // Call adapter methods to save data to db
    protected void saveDataInDB() {

        // Create database adapter and open the database
        if (aplicationDbAdapter == null) {
            aplicationDbAdapter = new ApplicationDbAdapter(context);
            aplicationDbAdapter.open();
        }

        WatchListRepository watchListRepository = new WatchListRepository();

        aplicationDbAdapter.insertWatchList(watchListRepository.getRepository(), true);

    }

}
