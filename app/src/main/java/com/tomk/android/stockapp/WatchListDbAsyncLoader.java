//package com.tomk.android.stockapp;
//
///**
// * Created by Tom Kowszun
// *
// */
//
//import android.content.Context;
//import android.net.ConnectivityManager;
//
//import com.tomk.android.stockapp.WebAccess.StockSymbolHttpClient;
//import com.tomk.android.stockapp.mainActivity.MainStocksActivity;
//import com.tomk.android.stockapp.models.JSONparser;
//import com.tomk.android.stockapp.models.WatchListResponse;
//import com.tomk.android.stockapp.models.WatchRepository.WatchListItem;
//import com.tomk.android.stockapp.models.WatchRepository.WatchListRepository;
//
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.TreeMap;
//
///**
// * This Loader loads the full data from the DB that is later displayed as a watch list
// */
//public class WatchListDbAsyncLoader extends android.support.v4.content.AsyncTaskLoader<ArrayList<WatchListItem>> {
//
//
//    private ApplicationDbAdapter aplicationDbAdapter;
//    private Context context;
//
//    String stockSymbol;
//    String interval;
//    String timePeriod;
//    String seriesType;
//    String apiKeyObtained;
//    ArrayList<String> typeList = new ArrayList();
//
//
//
//    public WatchListDbAsyncLoader(Context context, ApplicationDbAdapter adapter,
//                                  String interval,
//                                  String apiKeyObtained) {
//        super(context);
//
//        this.context = context;
//        this.aplicationDbAdapter = adapter;
//
//        this.stockSymbol = stockSymbol;
//        this.interval = interval;
//        this.apiKeyObtained = apiKeyObtained;
//
//    }
//
//    @Override
//    public ArrayList<WatchListItem> loadInBackground() {
//
//        // Create database adapter and open the database
//        // If there is no database, it will be created with an empty watch list table
//        if (aplicationDbAdapter == null) {
//            aplicationDbAdapter = new ApplicationDbAdapter(context);
//            aplicationDbAdapter.open();
//        }
//
//        // Get the watch list from db.  If the list is empty, insert the
//        // default list into db.
//        // Then after the list has been inserted into db, get it again.
//        ArrayList<WatchListItem> watchList = aplicationDbAdapter.getWatchList();
//
////        if(watchList == null || watchList.size() == 0)
//        if(true)
//        {
//            WatchListRepository watchListRepository = new WatchListRepository();
//            aplicationDbAdapter.insertWatchList(watchListRepository.getRepository(), true);
//            watchList =  aplicationDbAdapter.getWatchList();
//        }
//        // At this point there should be items in watch list.
//
//        // Loop through Watch List and fill in the detail values
//        ArrayList<WatchListResponse> listOfWatchListResponses = new ArrayList<WatchListResponse>();
//        boolean responseErrors = false;
//        for( WatchListItem  item : watchList )
//        {
//            // Access the web service
//            WatchListResponse watchListResponse = decodeWatchListResponse(item.getWatchListName(), interval, apiKeyObtained);
//            if(watchListResponse.getResultString().equals(MainStocksActivity.NO_ERRORS))
//            {
//                Double open = watchListResponse.getOpen();
//                Double close = watchListResponse.getClose();
//
//                item.setValue( String.valueOf(close));
//                item.setValueChange( String.valueOf(close - open));
//
//                String formattedString = String.format("%.3f", percentage( open, close));
//                item.setValueChangePercent( formattedString);
////                item.setValueDate( watchListResponse.getRefreshDate());
//                item.setValueDate( " ");
//                item.setWatchListName(watchListResponse.getSymbol());
//            }
//            else
//                responseErrors = true;
//        }
//
//        if(!responseErrors)
//        {
//            // Save the stock response to database
//            aplicationDbAdapter.insertWatchList(watchList, true);
//        }
//        return watchList;
//    }
//
//    private Double percentage(Double open, Double close)
//    {
//        Double percentage = 0.00;
//        Double difference = open - close;
//        if(open > close)
//        {
//            percentage = 100.00 - ((close * 100.00)/open);
//        } else if (close > open) {
//
//            percentage = 100.00 - ((open * 100.00)/close);
//        } else{
//
//            percentage = 0.00;
//        }
//
//        return percentage;
//
//    }
//
//    /**
//     * After the Watch List has been downloaded from the Web Services, it is then
//     * parsed and packed into a list of items and returned.
//     *
//     * for TIME_SERIES_INTRADAY
//     * https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=5min&apikey=demo
//     */
//    private WatchListResponse decodeWatchListResponse(String stockSymbol, String stockName, String interval,
//                                                    String apiKeyObtained) {
//        WatchListResponse watchListResponse = new WatchListResponse();
//
//        TreeMap<String, String> rawData = null;
//        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (Util.isWiFiAvailable(connectivityManager)) {
//
//            // Call to get raw data from Web Service
//            StockSymbolHttpClient stockSymbolHttpClient = new StockSymbolHttpClient();
//            rawData = stockSymbolHttpClient.getStockData(stockSymbol, null, interval, null, null, apiKeyObtained, null);
//
//
//            if (rawData == null || rawData.size() < 1 || rawData.get(MainStocksActivity.T_STANDARD) == null || rawData.get(MainStocksActivity.T_STANDARD).length() < 1) {
//                watchListResponse.setResultString(MainStocksActivity.ERROR_CONNECTING);
//                return watchListResponse;
//            } else if (rawData.get(MainStocksActivity.T_STANDARD).contains(MainStocksActivity.STOCK_NOT_FOUND)) {
//                watchListResponse.setResultString(MainStocksActivity.STOCK_NOT_FOUND);
//                return watchListResponse;
//            } else if (rawData.get(MainStocksActivity.T_STANDARD).contains(MainStocksActivity.ERROR_CONNECTING)) {
//                watchListResponse.setResultString(MainStocksActivity.ERROR_CONNECTING);
//                return watchListResponse;
//            }
//
//            try {
//                if(!JSONparser.getWatchListResponse(stockSymbol, stockName, rawData, watchListResponse, typeList))
//                {
//                    watchListResponse.setResultString(MainStocksActivity.ERROR_CONNECTING);
//                    return watchListResponse;
//                }
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            watchListResponse.setResultString(MainStocksActivity.NO_ERRORS);
//        } else {
//            watchListResponse.setResultString(MainStocksActivity.NO_WIFI);
//            return watchListResponse;
//        }
//
//        return watchListResponse;
//    }
//}
