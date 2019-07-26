package com.tomk.android.stockapp;

/**
 * Created by Tom Kowszun
 *
 */

import android.content.Context;

import com.tomk.android.stockapp.models.Repository.DataRepository;
import com.tomk.android.stockapp.models.Repository.RepositoryItem;

import java.util.ArrayList;

/**
 * This Loader loads a list of stocks from the DB that is later displayed
 * in recycler view.
 */
public class ListOfStocksDbAsyncLoader extends android.support.v4.content.AsyncTaskLoader<ArrayList<StockListItem>> {


    private StockDbAdapter stockDbAdapter;


    public ListOfStocksDbAsyncLoader(Context context, StockDbAdapter adapter) {
        super(context);

        this.stockDbAdapter = adapter;

    }

    @Override
    public ArrayList<StockListItem> loadInBackground() {

        ArrayList<StockListItem> listOfStocks = stockDbAdapter.getStocksList();

        if(listOfStocks == null || listOfStocks.size() < 1)
        {

            DataRepository repository = new DataRepository();
            ArrayList<StockListItem> stocksListItems = new ArrayList<>();

            // Deposit the default list of stocks in the database
            for (RepositoryItem repositoryItem : repository.getRepository()) {
                StockListItem stockListItem = new StockListItem(repositoryItem.getStockSymbol(), repositoryItem.getStockName(), null, null, null);
                stocksListItems.add(stockListItem);
            }

            stockDbAdapter.insertStocksList(stocksListItems, true);

        }

        return stockDbAdapter.getStocksList();
    }
}
