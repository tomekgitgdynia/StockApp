package com.tomk.android.stockapp.WebAccess;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.tomk.android.stockapp.StockDbAdapter;
import com.tomk.android.stockapp.Util;
import com.tomk.android.stockapp.mainActivity.MainStocksActivity;
import com.tomk.android.stockapp.models.JSONparser;
import com.tomk.android.stockapp.models.StockResponse;

import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class GetStockIntentService extends IntentService {

    public static final String STOCK_DATA_ACTION = "com.tomk.android.stockapp.STOCK_ACTION";
    public static final String STOCK_SYMBOL = "com.tomk.android.stockapp.STOCK_SYMBOL";
    public static final String STOCK_ACTION_TYPE = "STOCK_ACTION_TYPE";
    public static final String NUMBER_OF_ITEMS = "0";
    public static final String RESULT_STRING = "";

    private static final int GOOD_CONNECTION = 200;
    private static final int NOT_FOUND = 404;
    private static final String TAG = "GetStockIntentService";
    private String stockData;
    private URL stockUrl = null;
    private StockDbAdapter stockDbAdapter = null;


    public GetStockIntentService() {
        super("GetStockIntentService");
    }

    //        public TreeMap<String, String> getStockData(String stockSymbol, String interval,
//                String timePeriod, String seriesType, String apiKey,
//                ArrayList<String> requestedTypesList)

    @Override
    protected void onHandleIntent(Intent intent) {

        int numberOfItems = 0;
        Intent broadcastIntent = new Intent();
        StockResponse stockResponse = null;
        if (intent != null) {
            ArrayList<String> listOfTypes = intent.getStringArrayListExtra("typeList");

            String stockSymbol = intent.getStringExtra(MainStocksActivity.STOCK_SYMBOL);
            String stockName = intent.getStringExtra(MainStocksActivity.STOCK_NAME);
            String interval = intent.getStringExtra(MainStocksActivity.INTERVAL);
            String timePeriod = intent.getStringExtra(MainStocksActivity.TIME_PERIOD);
            String seriesType = intent.getStringExtra(MainStocksActivity.SERIES_TYPE);
            String apiKeyObtained = intent.getStringExtra(MainStocksActivity.API_KEY_OBTAINED);

            stockResponse = decodeStockResponse(stockSymbol, stockName, interval, timePeriod, seriesType, apiKeyObtained, listOfTypes);

            // Save the stock response to database
            if (stockResponse != null && stockResponse.getTimeSeriesItems() != null && stockResponse.getTimeSeriesItems().size() != 0) {
                if (stockResponse.getResultString() != null && stockResponse.getResultString() == MainStocksActivity.NO_ERRORS) {
                    saveDataInDB(stockResponse);
                }
                numberOfItems = stockResponse.getTimeSeriesItems().size();
            }
        }

        // After the downloaded items have been saved in database, the event is broadcast to the Activity

        broadcastIntent.putExtra(NUMBER_OF_ITEMS, String.valueOf(numberOfItems));
        broadcastIntent.putExtra(RESULT_STRING, stockResponse.getResultString());
        if(stockResponse.getResultString().equals(MainStocksActivity.NO_ERRORS))
        {
            broadcastIntent.putExtra("stock_symbol", stockResponse.getMetaData().getSymbol());
        } else
        {
            broadcastIntent.putExtra("stock_symbol", "");
        }

        broadcastIntent.setAction(STOCK_DATA_ACTION);
        sendBroadcast(broadcastIntent);
    }

    // Call adapter methods to save data to db
    protected void saveDataInDB(StockResponse stockResponse) {

        // Create database adapter and open the database
        if (stockDbAdapter == null) {
            stockDbAdapter = new StockDbAdapter(this.getApplicationContext());
            stockDbAdapter.open();
        }

        stockDbAdapter.insertStockResponse(stockResponse);


        // Handle the list of stocks and save it in the database
//        DataRepository repository = new DataRepository();
//        ArrayList<StockListItem> stocksListItems = new ArrayList<>();
//        for (RepositoryItem repositoryItem : repository.getRepository()) {
//            StockListItem stockListItem = new StockListItem(repositoryItem.getStockSymbol(), repositoryItem.getStockName(), null, null, null);
//            stocksListItems.add(stockListItem);
//        }

//        stockDbAdapter.insertStocksList(stocksListItems, true);

    }

    /**
     * After the Stock List has been downloaded from the Web Services, it is then
     * parsed and packed into a list of items and returned.
     */
    private StockResponse decodeStockResponse(String stockSymbol, String stockName, String interval,
                                              String timePeriod, String seriesType,
                                              String apiKeyObtained, ArrayList<String> typeList) {
        StockResponse stockResponse = new StockResponse();

        TreeMap<String, String> rawData = null;
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Util.isWiFiAvailable(conMan)) {
            StockSymbolHttpClient stockSymbolHttpClient = new StockSymbolHttpClient();
            rawData = stockSymbolHttpClient.getStockData(stockSymbol, stockName, interval, timePeriod, seriesType, apiKeyObtained, typeList);
            if (rawData == null || rawData.size() < 1 || rawData.get(MainStocksActivity.T_STANDARD) == null || rawData.get(MainStocksActivity.T_STANDARD).length() < 1) {
                stockResponse.setResultString(MainStocksActivity.ERROR_CONNECTING);
                return stockResponse;
            } else if (rawData.get(MainStocksActivity.T_STANDARD).contains(MainStocksActivity.STOCK_NOT_FOUND)) {
                stockResponse.setResultString(MainStocksActivity.STOCK_NOT_FOUND);
                return stockResponse;
            } else if (rawData.get(MainStocksActivity.T_STANDARD).contains(MainStocksActivity.ERROR_CONNECTING)) {
                stockResponse.setResultString(MainStocksActivity.ERROR_CONNECTING);
                return stockResponse;
            }

            // No issues so far, perform the parsing from raw data to stockResponse
            try {
                if(!JSONparser.getStockResponse(stockSymbol, stockName, rawData, stockResponse, typeList))
                {
                    stockResponse.setResultString(MainStocksActivity.ERROR_CONNECTING);
                    return stockResponse;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            stockResponse.setResultString(MainStocksActivity.NO_ERRORS);
        } else {
            stockResponse.setResultString(MainStocksActivity.NO_WIFI);
            return stockResponse;
        }

        return stockResponse;
    }

}
