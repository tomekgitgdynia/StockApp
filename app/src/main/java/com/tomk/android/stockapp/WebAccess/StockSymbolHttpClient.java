package com.tomk.android.stockapp.WebAccess;

import android.util.Log;

import com.tomk.android.stockapp.mainActivity.MainStocksActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;


/**
 * Created by Tom Kowszun on 11/10/2017.
 * <p>
 * Queries the WS to obtain weather data
 */
public class StockSymbolHttpClient {


    private static String KEY_OBTAINED = "UKY832CIXXPKWVJV";
    private static String SUFFIX_LOCATION = "&q=";
    private static String SUFFIX_UNITS_METRIC = "&units=metric";
    private static String SUFFIX_UNITS_IMPERIAL = "&units=imperial";

//    https://www.alphavantage.co/query?function=TRIMA&symbol=MSFT&interval=1min&time_period=10&series_type=open&apikey=UKY832CIXXPKWVJV

    // full size
//String urlWithSymbol = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=" +
//        stockSymbol + "&interval=1min&outputsize=full&apikey=" + KEY_OBTAINED;


    private static String ADDRESS_AND_FUNCTION = "https://www.alphavantage.co/query?function=";

    private static String SYMBOL = "&symbol=";
    private static String INTERVAL = "&interval=";
    private static String OUTPUT_SIZE = "&outputsize=";
    private static String TIME_PERIOD = "&time_period=";
    private static String SERIES_TYPE = "&series_type=";
    private static String API_KEY = "&apikey=";

    private static String FULL = "full";
    private static String COMPACT = "compact";


    public static final int GOOD_CONNECTION = 200;
    public static final int NOT_FOUND = 404;

    private static final String TAG = "StockSymbolHttpClient";

    public TreeMap<String, String> getStockData(String stockSymbol, String stockName, String interval, String timePeriod,
                                                String seriesType, String apiKey, ArrayList<String> requestedTypesList) {

//        stockSymbol = "BA"; // debug only!
//        timePeriod = null;


        TreeMap<String, String> resultsMap = new TreeMap<>();

        String queryString = null;
        if(timePeriod == null || seriesType == null || requestedTypesList == null)
        {
            // Used for watch list stock data daily calls.
            // https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=MSFT&outputsize=full&apikey=demo

            // https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=BA&interval=1min&apikey=UKY832CIXXPKWVJV

            queryString = ADDRESS_AND_FUNCTION + MainStocksActivity.TIME_SERIES_DAILY + SYMBOL + stockSymbol + OUTPUT_SIZE + COMPACT + API_KEY + apiKey;
            String resultRawString = getStockDataForQueryString(queryString);
//            String resultRawString = null;

            if(stockSymbol.equals("MSFT"))
            {
                resultRawString = testRawStringA;
            } else
            {
                resultRawString = testRawStringC;
            }
            resultsMap.put(MainStocksActivity.T_STANDARD, resultRawString);
        }
        else
        {
            int cnt = 0;
            for (String function : requestedTypesList) {

                if (function.equals(MainStocksActivity.T_STANDARD)) {
                    // Used for standard stock data calls.
                    // https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=1min&apikey=UKY832CIXXPKWVJV
                    queryString = ADDRESS_AND_FUNCTION + MainStocksActivity.TIME_SERIES_INTRADAY + SYMBOL + stockSymbol + INTERVAL + interval + API_KEY + apiKey;
                } else {
                    // Used for tech indicator type calls
                    // https://www.alphavantage.co/query?function=TRIMA&symbol=MSFT&interval=1min&time_period=10&series_type=open&apikey=UKY832CIXXPKWVJV
                    queryString = ADDRESS_AND_FUNCTION + function + SYMBOL + stockSymbol + INTERVAL + interval + TIME_PERIOD + timePeriod + SERIES_TYPE + seriesType + API_KEY + apiKey;
                }
                String resultRawString = getStockDataForQueryString(queryString);
                resultsMap.put(function, resultRawString);
            }
        }


        return resultsMap;
    }

    private String getStockDataForQueryString(String queryString) {
        HttpURLConnection connection = null;
        InputStream is = null;
        String returnErrorMessage = MainStocksActivity.NO_ERRORS;
        try {

            URL url = new URL(queryString);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
//            connection.setDoInput(true);
//            connection.setDoOutput(true);

            // The timeouts are arbitrary.  We should experiment with the shorter
            // times perhaps
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(8000);
            connection.connect();

            int responseCode = connection.getResponseCode();
//            responseCode = NOT_FOUND;
            Log.d(TAG, "Connection response code " + responseCode);
            if (responseCode == GOOD_CONNECTION) {
                StringBuilder buffer = new StringBuilder();
                is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = br.readLine()) != null) buffer.append(line).append("\r\n");

                is.close();
                connection.disconnect();

                if(verifyFrequencyNotExceeded(buffer))
                {
                    return buffer.toString();
                } else
                {
                    returnErrorMessage = MainStocksActivity.HIGH_USAGE;
                }

            } else if (responseCode == NOT_FOUND) {
                returnErrorMessage = MainStocksActivity.STOCK_NOT_FOUND;
            }

        } catch (Throwable t) {
            if (t.toString().contains("FileNotFoundException")) {
                returnErrorMessage = MainStocksActivity.STOCK_NOT_FOUND;
            } else {
                Log.d(TAG, t.toString());
                returnErrorMessage = MainStocksActivity.ERROR_CONNECTING;
            }

        } finally {
            try {
                if (is != null) {
                    is.close();
                } else {
                    int g = 5;
                    Log.d(TAG, " Input stream was null ");
                }

                if (connection != null) {
                    connection.disconnect();

                } else {
                    int g = 5;
                    Log.d(TAG, " Connection was null ");
                }

            } catch (IOException e) {
                returnErrorMessage = MainStocksActivity.ERROR_CONNECTING;

            }

        }

        return returnErrorMessage;
    }

    private boolean verifyFrequencyNotExceeded(StringBuilder buffer) {

        if (buffer.indexOf("Thank you for using Alpha Vantage!") != -1) {
            return false;
        } else {
            return true;
        }

//        {
//            "Note": "Thank you for using Alpha Vantage! Our standard API call frequency is 5 calls per minute and 500 calls per day. Please visit https://www.alphavantage.co/premium/ if you would like to target a higher API call frequency."
//        }
    }

    String testRawStringA = "\n" + " {\n" + "    \"Meta Data\": {\n" + "        \"1. Information\": \"Daily Prices (open, high, low, close) and Volumes\",\n" + "        \"2. Symbol\": \"MSFT\",\n" + "        \"3. Last Refreshed\": \"2019-07-01\",\n" + "        \"4. Output Size\": \"Compact\",\n" + "        \"5. Time Zone\": \"US/Eastern\"\n" + "    },\n" + "    \"Time Series (Daily)\": {\n" + "        \"2019-07-01\": {\n" + "            \"1. open\": \"130.6300\",\n" + "            \"2. high\": \"136.7000\",\n" + "            \"3. low\": \"134.9700\",\n" + "            \"4. close\": \"130.6800\",\n" + "            \"5. volume\": \"22606027\"\n" + "        },\n" + "        \"2019-06-28\": {\n" + "            \"1. open\": \"134.5700\",\n" + "            \"2. high\": \"134.6000\",\n" + "            \"3. low\": \"133.1558\",\n" + "            \"4. close\": \"2567.9600\",\n" + "            \"5. volume\": \"30042969\"\n" + "        },\n" + "        \"2019-06-27\": {\n" + "            \"1. open\": \"134.1400\",\n" + "            \"2. high\": \"134.7100\",\n" + "            \"3. low\": \"133.5100\",\n" + "            \"4. close\": \"134.1500\",\n" + "            \"5. volume\": \"16557482\"\n" + "        },\n" + "        \"2019-06-26\": {\n" + "            \"1. open\": \"134.3500\",\n" + "            \"2. high\": \"135.7400\",\n" + "            \"3. low\": \"133.6000\",\n" + "            \"4. close\": \"133.9300\",\n" + "            \"5. volume\": \"23657745\"\n" + "        },\n" + "        \"2019-06-25\": {\n" + "            \"1. open\": \"137.2500\",\n" + "            \"2. high\": \"137.5900\",\n" + "            \"3. low\": \"132.7300\",\n" + "            \"4. close\": \"133.4300\",\n" + "            \"5. volume\": \"33327420\"\n" + "        },\n" + "        \"2019-06-24\": {\n" + "            \"1. open\": \"137.0000\",\n" + "            \"2. high\": \"138.4000\",\n" + "            \"3. low\": \"137.0000\",\n" + "            \"4. close\": \"137.7800\",\n" + "            \"5. volume\": \"20628841\"\n" + "        },\n" + "        \"2019-06-21\": {\n" + "            \"1. open\": \"136.5800\",\n" + "            \"2. high\": \"137.7300\",\n" + "            \"3. low\": \"136.4600\",\n" + "            \"4. close\": \"136.9700\",\n" + "            \"5. volume\": \"36727892\"\n" + "        },\n" + "        \"2019-06-20\": {\n" + "            \"1. open\": \"137.4500\",\n" + "            \"2. high\": \"137.6600\",\n" + "            \"3. low\": \"135.7200\",\n" + "            \"4. close\": \"136.9500\",\n" + "            \"5. volume\": \"33042592\"\n" + "        },\n" + "        \"2019-06-19\": {\n" + "            \"1. open\": \"135.0000\",\n" + "            \"2. high\": \"135.9300\",\n" + "            \"3. low\": \"133.8100\",\n" + "            \"4. close\": \"135.6900\",\n" + "            \"5. volume\": \"23744441\"\n" + "        },\n" + "        \"2019-06-18\": {\n" + "            \"1. open\": \"134.1900\",\n" + "            \"2. high\": \"135.2400\",\n" + "            \"3. low\": \"133.5700\",\n" + "            \"4. close\": \"135.1600\",\n" + "            \"5. volume\": \"25934458\"\n" + "        },\n" + "        \"2019-06-17\": {\n" + "            \"1. open\": \"132.6300\",\n" + "            \"2. high\": \"133.7300\",\n" + "            \"3. low\": \"132.5300\",\n" + "            \"4. close\": \"132.8500\",\n" + "            \"5. volume\": \"14517785\"\n" + "        },\n" + "        \"2019-06-14\": {\n" + "            \"1. open\": \"132.2600\",\n" + "            \"2. high\": \"133.7900\",\n" + "            \"3. low\": \"131.6400\",\n" + "            \"4. close\": \"132.4500\",\n" + "            \"5. volume\": \"17821703\"\n" + "        },\n" + "        \"2019-06-13\": {\n" + "            \"1. open\": \"131.9800\",\n" + "            \"2. high\": \"132.6700\",\n" + "            \"3. low\": \"131.5600\",\n" + "            \"4. close\": \"132.3200\",\n" + "            \"5. volume\": \"17200848\"\n" + "        },\n" + "        \"2019-06-12\": {\n" + "            \"1. open\": \"131.4000\",\n" + "            \"2. high\": \"131.9700\",\n" + "            \"3. low\": \"130.7100\",\n" + "            \"4. close\": \"131.4900\",\n" + "            \"5. volume\": \"17092464\"\n" + "        },\n" + "        \"2019-06-11\": {\n" + "            \"1. open\": \"133.8800\",\n" + "            \"2. high\": \"134.2400\",\n" + "            \"3. low\": \"131.2757\",\n" + "            \"4. close\": \"132.1000\",\n" + "            \"5. volume\": \"23913731\"\n" + "        },\n" + "        \"2019-06-10\": {\n" + "            \"1. open\": \"132.4000\",\n" + "            \"2. high\": \"134.0800\",\n" + "            \"3. low\": \"132.0000\",\n" + "            \"4. close\": \"132.6000\",\n" + "            \"5. volume\": \"26477098\"\n" + "        },\n" + "        \"2019-06-07\": {\n" + "            \"1. open\": \"129.1900\",\n" + "            \"2. high\": \"132.2500\",\n" + "            \"3. low\": \"128.2600\",\n" + "            \"4. close\": \"131.4000\",\n" + "            \"5. volume\": \"33885588\"\n" + "        },\n" + "        \"2019-06-06\": {\n" + "            \"1. open\": \"126.4400\",\n" + "            \"2. high\": \"127.9700\",\n" + "            \"3. low\": \"125.6000\",\n" + "            \"4. close\": \"127.8200\",\n" + "            \"5. volume\": \"21458961\"\n" + "        },\n" + "        \"2019-06-05\": {\n" + "            \"1. open\": \"124.9500\",\n" + "            \"2. high\": \"125.8700\",\n" + "            \"3. low\": \"124.2100\",\n" + "            \"4. close\": \"125.8300\",\n" + "            \"5. volume\": \"24926140\"\n" + "        },\n" + "        \"2019-06-04\": {\n" + "            \"1. open\": \"121.2800\",\n" + "            \"2. high\": \"123.2800\",\n" + "            \"3. low\": \"120.6522\",\n" + "            \"4. close\": \"123.1600\",\n" + "            \"5. volume\": \"29382642\"\n" + "        },\n" + "        \"2019-06-03\": {\n" + "            \"1. open\": \"123.8500\",\n" + "            \"2. high\": \"124.3700\",\n" + "            \"3. low\": \"119.0100\",\n" + "            \"4. close\": \"119.8400\",\n" + "            \"5. volume\": \"37983637\"\n" + "        },\n" + "        \"2019-05-31\": {\n" + "            \"1. open\": \"124.2300\",\n" + "            \"2. high\": \"124.6150\",\n" + "            \"3. low\": \"123.3200\",\n" + "            \"4. close\": \"123.6800\",\n" + "            \"5. volume\": \"26646769\"\n" + "        },\n" + "        \"2019-05-30\": {\n" + "            \"1. open\": \"125.2600\",\n" + "            \"2. high\": \"125.7600\",\n" + "            \"3. low\": \"124.7800\",\n" + "            \"4. close\": \"125.7300\",\n" + "            \"5. volume\": \"16829613\"\n" + "        },\n" + "        \"2019-05-29\": {\n" + "            \"1. open\": \"125.3800\",\n" + "            \"2. high\": \"125.3900\",\n" + "            \"3. low\": \"124.0400\",\n" + "            \"4. close\": \"124.9400\",\n" + "            \"5. volume\": \"22763140\"\n" + "        },\n" + "        \"2019-05-28\": {\n" + "            \"1. open\": \"126.9800\",\n" + "            \"2. high\": \"128.0000\",\n" + "            \"3. low\": \"126.0500\",\n" + "            \"4. close\": \"126.1600\",\n" + "            \"5. volume\": \"23128359\"\n" + "        },\n" + "        \"2019-05-24\": {\n" + "            \"1. open\": \"126.9100\",\n" + "            \"2. high\": \"127.4150\",\n" + "            \"3. low\": \"125.9700\",\n" + "            \"4. close\": \"126.2400\",\n" + "            \"5. volume\": \"14123358\"\n" + "        },\n" + "        \"2019-05-23\": {\n" + "            \"1. open\": \"126.2000\",\n" + "            \"2. high\": \"126.2900\",\n" + "            \"3. low\": \"124.7400\",\n" + "            \"4. close\": \"126.1800\",\n" + "            \"5. volume\": \"23603810\"\n" + "        },\n" + "        \"2019-05-22\": {\n" + "            \"1. open\": \"126.6200\",\n" + "            \"2. high\": \"128.2400\",\n" + "            \"3. low\": \"126.5200\",\n" + "            \"4. close\": \"127.6700\",\n" + "            \"5. volume\": \"15396485\"\n" + "        },\n" + "        \"2019-05-21\": {\n" + "            \"1. open\": \"127.4300\",\n" + "            \"2. high\": \"127.5273\",\n" + "            \"3. low\": \"126.5800\",\n" + "            \"4. close\": \"126.9000\",\n" + "            \"5. volume\": \"15293260\"\n" + "        },\n" + "        \"2019-05-20\": {\n" + "            \"1. open\": \"126.5200\",\n" + "            \"2. high\": \"127.5894\",\n" + "            \"3. low\": \"125.7607\",\n" + "            \"4. close\": \"126.2200\",\n" + "            \"5. volume\": \"23706934\"\n" + "        },\n" + "        \"2019-05-17\": {\n" + "            \"1. open\": \"128.3050\",\n" + "            \"2. high\": \"130.4600\",\n" + "            \"3. low\": \"127.9200\",\n" + "            \"4. close\": \"128.0700\",\n" + "            \"5. volume\": \"25770539\"\n" + "        },\n" + "        \"2019-05-16\": {\n" + "            \"1. open\": \"126.7500\",\n" + "            \"2. high\": \"129.3800\",\n" + "            \"3. low\": \"126.4600\",\n" + "            \"4. close\": \"128.9300\",\n" + "            \"5. volume\": \"30112216\"\n" + "        },\n" + "        \"2019-05-15\": {\n" + "            \"1. open\": \"124.2600\",\n" + "            \"2. high\": \"126.7100\",\n" + "            \"3. low\": \"123.7000\",\n" + "            \"4. close\": \"126.0200\",\n" + "            \"5. volume\": \"24722708\"\n" + "        },\n" + "        \"2019-05-14\": {\n" + "            \"1. open\": \"123.8700\",\n" + "            \"2. high\": \"125.8800\",\n" + "            \"3. low\": \"123.7000\",\n" + "            \"4. close\": \"124.7300\",\n" + "            \"5. volume\": \"25266315\"\n" + "        },\n" + "        \"2019-05-13\": {\n" + "            \"1. open\": \"124.1100\",\n" + "            \"2. high\": \"125.5500\",\n" + "            \"3. low\": \"123.0400\",\n" + "            \"4. close\": \"123.3500\",\n" + "            \"5. volume\": \"33944923\"\n" + "        },\n" + "        \"2019-05-10\": {\n" + "            \"1. open\": \"124.9100\",\n" + "            \"2. high\": \"127.9300\",\n" + "            \"3. low\": \"123.8200\",\n" + "            \"4. close\": \"127.1300\",\n" + "            \"5. volume\": \"30915084\"\n" + "        },\n" + "        \"2019-05-09\": {\n" + "            \"1. open\": \"124.2900\",\n" + "            \"2. high\": \"125.7900\",\n" + "            \"3. low\": \"123.5700\",\n" + "            \"4. close\": \"125.5000\",\n" + "            \"5. volume\": \"27235835\"\n" + "        },\n" + "        \"2019-05-08\": {\n" + "            \"1. open\": \"125.4400\",\n" + "            \"2. high\": \"126.3700\",\n" + "            \"3. low\": \"124.7500\",\n" + "            \"4. close\": \"125.5100\",\n" + "            \"5. volume\": \"28418996\"\n" + "        },\n" + "        \"2019-05-07\": {\n" + "            \"1. open\": \"126.4600\",\n" + "            \"2. high\": \"127.1800\",\n" + "            \"3. low\": \"124.2200\",\n" + "            \"4. close\": \"125.5200\",\n" + "            \"5. volume\": \"36017661\"\n" + "        },\n" + "        \"2019-05-06\": {\n" + "            \"1. open\": \"126.3900\",\n" + "            \"2. high\": \"128.5600\",\n" + "            \"3. low\": \"126.1100\",\n" + "            \"4. close\": \"128.1500\",\n" + "            \"5. volume\": \"24239464\"\n" + "        },\n" + "        \"2019-05-03\": {\n" + "            \"1. open\": \"127.3600\",\n" + "            \"2. high\": \"129.4300\",\n" + "            \"3. low\": \"127.2500\",\n" + "            \"4. close\": \"128.9000\",\n" + "            \"5. volume\": \"24911126\"\n" + "        },\n" + "        \"2019-05-02\": {\n" + "            \"1. open\": \"127.9800\",\n" + "            \"2. high\": \"128.0000\",\n" + "            \"3. low\": \"125.5200\",\n" + "            \"4. close\": \"126.2100\",\n" + "            \"5. volume\": \"27350161\"\n" + "        },\n" + "        \"2019-05-01\": {\n" + "            \"1. open\": \"130.5300\",\n" + "            \"2. high\": \"130.6500\",\n" + "            \"3. low\": \"127.7000\",\n" + "            \"4. close\": \"127.8800\",\n" + "            \"5. volume\": \"26821692\"\n" + "        },\n" + "        \"2019-04-30\": {\n" + "            \"1. open\": \"129.8100\",\n" + "            \"2. high\": \"130.7000\",\n" + "            \"3. low\": \"129.3947\",\n" + "            \"4. close\": \"130.6000\",\n" + "            \"5. volume\": \"24166503\"\n" + "        },\n" + "        \"2019-04-29\": {\n" + "            \"1. open\": \"129.9000\",\n" + "            \"2. high\": \"130.1800\",\n" + "            \"3. low\": \"129.3500\",\n" + "            \"4. close\": \"129.7700\",\n" + "            \"5. volume\": \"16324183\"\n" + "        },\n" + "        \"2019-04-26\": {\n" + "            \"1. open\": \"129.7000\",\n" + "            \"2. high\": \"130.5152\",\n" + "            \"3. low\": \"129.0200\",\n" + "            \"4. close\": \"129.8900\",\n" + "            \"5. volume\": \"23654933\"\n" + "        },\n" + "        \"2019-04-25\": {\n" + "            \"1. open\": \"130.0600\",\n" + "            \"2. high\": \"131.3700\",\n" + "            \"3. low\": \"128.8300\",\n" + "            \"4. close\": \"129.1500\",\n" + "            \"5. volume\": \"38033892\"\n" + "        },\n" + "        \"2019-04-24\": {\n" + "            \"1. open\": \"125.7900\",\n" + "            \"2. high\": \"125.8500\",\n" + "            \"3. low\": \"124.5200\",\n" + "            \"4. close\": \"125.0100\",\n" + "            \"5. volume\": \"31256980\"\n" + "        },\n" + "        \"2019-04-23\": {\n" + "            \"1. open\": \"124.1000\",\n" + "            \"2. high\": \"125.5800\",\n" + "            \"3. low\": \"123.8300\",\n" + "            \"4. close\": \"125.4400\",\n" + "            \"5. volume\": \"24025521\"\n" + "        },\n" + "        \"2019-04-22\": {\n" + "            \"1. open\": \"122.6200\",\n" + "            \"2. high\": \"124.0000\",\n" + "            \"3. low\": \"122.5700\",\n" + "            \"4. close\": \"123.7600\",\n" + "            \"5. volume\": \"15648724\"\n" + "        },\n" + "        \"2019-04-18\": {\n" + "            \"1. open\": \"122.1900\",\n" + "            \"2. high\": \"123.5200\",\n" + "            \"3. low\": \"121.3018\",\n" + "            \"4. close\": \"123.3700\",\n" + "            \"5. volume\": \"27990998\"\n" + "        },\n" + "        \"2019-04-17\": {\n" + "            \"1. open\": \"121.2400\",\n" + "            \"2. high\": \"121.8500\",\n" + "            \"3. low\": \"120.5400\",\n" + "            \"4. close\": \"121.7700\",\n" + "            \"5. volume\": \"19300938\"\n" + "        },\n" + "        \"2019-04-16\": {\n" + "            \"1. open\": \"121.6400\",\n" + "            \"2. high\": \"121.6500\",\n" + "            \"3. low\": \"120.1000\",\n" + "            \"4. close\": \"120.7700\",\n" + "            \"5. volume\": \"14071787\"\n" + "        },\n" + "        \"2019-04-15\": {\n" + "            \"1. open\": \"120.9400\",\n" + "            \"2. high\": \"121.5800\",\n" + "            \"3. low\": \"120.5700\",\n" + "            \"4. close\": \"121.0500\",\n" + "            \"5. volume\": \"15792572\"\n" + "        },\n" + "        \"2019-04-12\": {\n" + "            \"1. open\": \"120.6400\",\n" + "            \"2. high\": \"120.9800\",\n" + "            \"3. low\": \"120.3700\",\n" + "            \"4. close\": \"120.9500\",\n" + "            \"5. volume\": \"19745143\"\n" + "        },\n" + "        \"2019-04-11\": {\n" + "            \"1. open\": \"120.5400\",\n" + "            \"2. high\": \"120.8500\",\n" + "            \"3. low\": \"119.9200\",\n" + "            \"4. close\": \"120.3300\",\n" + "            \"5. volume\": \"14209121\"\n" + "        },\n" + "        \"2019-04-10\": {\n" + "            \"1. open\": \"119.7600\",\n" + "            \"2. high\": \"120.3500\",\n" + "            \"3. low\": \"119.5400\",\n" + "            \"4. close\": \"120.1900\",\n" + "            \"5. volume\": \"16477169\"\n" + "        },\n" + "        \"2019-04-09\": {\n" + "            \"1. open\": \"118.6300\",\n" + "            \"2. high\": \"119.5400\",\n" + "            \"3. low\": \"118.5800\",\n" + "            \"4. close\": \"119.2800\",\n" + "            \"5. volume\": \"17611981\"\n" + "        },\n" + "        \"2019-04-08\": {\n" + "            \"1. open\": \"119.8100\",\n" + "            \"2. high\": \"120.0200\",\n" + "            \"3. low\": \"118.6400\",\n" + "            \"4. close\": \"119.9300\",\n" + "            \"5. volume\": \"15116186\"\n" + "        },\n" + "        \"2019-04-05\": {\n" + "            \"1. open\": \"119.3900\",\n" + "            \"2. high\": \"120.2300\",\n" + "            \"3. low\": \"119.3700\",\n" + "            \"4. close\": \"119.8900\",\n" + "            \"5. volume\": \"15826245\"\n" + "        },\n" + "        \"2019-04-04\": {\n" + "            \"1. open\": \"120.1000\",\n" + "            \"2. high\": \"120.2300\",\n" + "            \"3. low\": \"118.3800\",\n" + "            \"4. close\": \"119.3600\",\n" + "            \"5. volume\": \"20112848\"\n" + "        },\n" + "        \"2019-04-03\": {\n" + "            \"1. open\": \"119.8600\",\n" + "            \"2. high\": \"120.4300\",\n" + "            \"3. low\": \"119.1500\",\n" + "            \"4. close\": \"119.9700\",\n" + "            \"5. volume\": \"22860744\"\n" + "        },\n" + "        \"2019-04-02\": {\n" + "            \"1. open\": \"119.0600\",\n" + "            \"2. high\": \"119.4800\",\n" + "            \"3. low\": \"118.5200\",\n" + "            \"4. close\": \"119.1900\",\n" + "            \"5. volume\": \"18142297\"\n" + "        },\n" + "        \"2019-04-01\": {\n" + "            \"1. open\": \"118.9500\",\n" + "            \"2. high\": \"119.1085\",\n" + "            \"3. low\": \"118.1000\",\n" + "            \"4. close\": \"119.0200\",\n" + "            \"5. volume\": \"22789103\"\n" + "        },\n" + "        \"2019-03-29\": {\n" + "            \"1. open\": \"118.0700\",\n" + "            \"2. high\": \"118.3200\",\n" + "            \"3. low\": \"116.9600\",\n" + "            \"4. close\": \"117.9400\",\n" + "            \"5. volume\": \"25399752\"\n" + "        },\n" + "        \"2019-03-28\": {\n" + "            \"1. open\": \"117.4400\",\n" + "            \"2. high\": \"117.5800\",\n" + "            \"3. low\": \"116.1300\",\n" + "            \"4. close\": \"116.9300\",\n" + "            \"5. volume\": \"18334755\"\n" + "        },\n" + "        \"2019-03-27\": {\n" + "            \"1. open\": \"117.8750\",\n" + "            \"2. high\": \"118.2100\",\n" + "            \"3. low\": \"115.5215\",\n" + "            \"4. close\": \"116.7700\",\n" + "            \"5. volume\": \"22733427\"\n" + "        },\n" + "        \"2019-03-26\": {\n" + "            \"1. open\": \"118.6200\",\n" + "            \"2. high\": \"118.7050\",\n" + "            \"3. low\": \"116.8500\",\n" + "            \"4. close\": \"117.9100\",\n" + "            \"5. volume\": \"26097665\"\n" + "        },\n" + "        \"2019-03-25\": {\n" + "            \"1. open\": \"116.5600\",\n" + "            \"2. high\": \"118.0100\",\n" + "            \"3. low\": \"116.3224\",\n" + "            \"4. close\": \"117.6600\",\n" + "            \"5. volume\": \"27067117\"\n" + "        },\n" + "        \"2019-03-22\": {\n" + "            \"1. open\": \"119.5000\",\n" + "            \"2. high\": \"119.5900\",\n" + "            \"3. low\": \"117.0400\",\n" + "            \"4. close\": \"117.0500\",\n" + "            \"5. volume\": \"33624528\"\n" + "        },\n" + "        \"2019-03-21\": {\n" + "            \"1. open\": \"117.1350\",\n" + "            \"2. high\": \"120.8200\",\n" + "            \"3. low\": \"117.0900\",\n" + "            \"4. close\": \"120.2200\",\n" + "            \"5. volume\": \"29854446\"\n" + "        },\n" + "        \"2019-03-20\": {\n" + "            \"1. open\": \"117.3900\",\n" + "            \"2. high\": \"118.7500\",\n" + "            \"3. low\": \"116.7100\",\n" + "            \"4. close\": \"117.5200\",\n" + "            \"5. volume\": \"28113343\"\n" + "        },\n" + "        \"2019-03-19\": {\n" + "            \"1. open\": \"118.0900\",\n" + "            \"2. high\": \"118.4400\",\n" + "            \"3. low\": \"116.9900\",\n" + "            \"4. close\": \"117.6500\",\n" + "            \"5. volume\": \"37588697\"\n" + "        },\n" + "        \"2019-03-18\": {\n" + "            \"1. open\": \"116.1700\",\n" + "            \"2. high\": \"117.6100\",\n" + "            \"3. low\": \"116.0500\",\n" + "            \"4. close\": \"117.5700\",\n" + "            \"5. volume\": \"31207596\"\n" + "        },\n" + "        \"2019-03-15\": {\n" + "            \"1. open\": \"115.3400\",\n" + "            \"2. high\": \"117.2500\",\n" + "            \"3. low\": \"114.5900\",\n" + "            \"4. close\": \"115.9100\",\n" + "            \"5. volume\": \"54630661\"\n" + "        },\n" + "        \"2019-03-14\": {\n" + "            \"1. open\": \"114.5400\",\n" + "            \"2. high\": \"115.2000\",\n" + "            \"3. low\": \"114.3300\",\n" + "            \"4. close\": \"114.5900\",\n" + "            \"5. volume\": \"30763367\"\n" + "        },\n" + "        \"2019-03-13\": {\n" + "            \"1. open\": \"114.1300\",\n" + "            \"2. high\": \"115.0000\",\n" + "            \"3. low\": \"113.7800\",\n" + "            \"4. close\": \"114.5000\",\n" + "            \"5. volume\": \"35513771\"\n" + "        },\n" + "        \"2019-03-12\": {\n" + "            \"1. open\": \"112.8200\",\n" + "            \"2. high\": \"113.9900\",\n" + "            \"3. low\": \"112.6499\",\n" + "            \"4. close\": \"113.6200\",\n" + "            \"5. volume\": \"26132717\"\n" + "        },\n" + "        \"2019-03-11\": {\n" + "            \"1. open\": \"110.9900\",\n" + "            \"2. high\": \"112.9500\",\n" + "            \"3. low\": \"110.9800\",\n" + "            \"4. close\": \"112.8300\",\n" + "            \"5. volume\": \"26491618\"\n" + "        },\n" + "        \"2019-03-08\": {\n" + "            \"1. open\": \"109.1600\",\n" + "            \"2. high\": \"110.7100\",\n" + "            \"3. low\": \"108.8000\",\n" + "            \"4. close\": \"110.5100\",\n" + "            \"5. volume\": \"22818430\"\n" + "        },\n" + "        \"2019-03-07\": {\n" + "            \"1. open\": \"111.4000\",\n" + "            \"2. high\": \"111.5500\",\n" + "            \"3. low\": \"109.8650\",\n" + "            \"4. close\": \"110.3900\",\n" + "            \"5. volume\": \"25338954\"\n" + "        },\n" + "        \"2019-03-06\": {\n" + "            \"1. open\": \"111.8700\",\n" + "            \"2. high\": \"112.6600\",\n" + "            \"3. low\": \"111.4300\",\n" + "            \"4. close\": \"111.7500\",\n" + "            \"5. volume\": \"17686996\"\n" + "        },\n" + "        \"2019-03-05\": {\n" + "            \"1. open\": \"112.2500\",\n" + "            \"2. high\": \"112.3900\",\n" + "            \"3. low\": \"111.2300\",\n" + "            \"4. close\": \"111.7000\",\n" + "            \"5. volume\": \"19538318\"\n" + "        },\n" + "        \"2019-03-04\": {\n" + "            \"1. open\": \"113.0200\",\n" + "            \"2. high\": \"113.2500\",\n" + "            \"3. low\": \"110.8000\",\n" + "            \"4. close\": \"112.2600\",\n" + "            \"5. volume\": \"26608014\"\n" + "        },\n" + "        \"2019-03-01\": {\n" + "            \"1. open\": \"112.8900\",\n" + "            \"2. high\": \"113.0200\",\n" + "            \"3. low\": \"111.6650\",\n" + "            \"4. close\": \"112.5300\",\n" + "            \"5. volume\": \"23501169\"\n" + "        },\n" + "        \"2019-02-28\": {\n" + "            \"1. open\": \"112.0400\",\n" + "            \"2. high\": \"112.8800\",\n" + "            \"3. low\": \"111.7300\",\n" + "            \"4. close\": \"112.0300\",\n" + "            \"5. volume\": \"29083934\"\n" + "        },\n" + "        \"2019-02-27\": {\n" + "            \"1. open\": \"111.6900\",\n" + "            \"2. high\": \"112.3600\",\n" + "            \"3. low\": \"110.8800\",\n" + "            \"4. close\": \"112.1700\",\n" + "            \"5. volume\": \"21487062\"\n" + "        },\n" + "        \"2019-02-26\": {\n" + "            \"1. open\": \"111.2600\",\n" + "            \"2. high\": \"113.2400\",\n" + "            \"3. low\": \"111.1700\",\n" + "            \"4. close\": \"112.3600\",\n" + "            \"5. volume\": \"21536733\"\n" + "        },\n" + "        \"2019-02-25\": {\n" + "            \"1. open\": \"111.7600\",\n" + "            \"2. high\": \"112.1800\",\n" + "            \"3. low\": \"111.2600\",\n" + "            \"4. close\": \"111.5900\",\n" + "            \"5. volume\": \"23750599\"\n" + "        },\n" + "        \"2019-02-22\": {\n" + "            \"1. open\": \"110.0500\",\n" + "            \"2. high\": \"111.2000\",\n" + "            \"3. low\": \"109.8200\",\n" + "            \"4. close\": \"110.9700\",\n" + "            \"5. volume\": \"27763218\"\n" + "        },\n" + "        \"2019-02-21\": {\n" + "            \"1. open\": \"106.9000\",\n" + "            \"2. high\": \"109.4800\",\n" + "            \"3. low\": \"106.8700\",\n" + "            \"4. close\": \"109.4100\",\n" + "            \"5. volume\": \"29063231\"\n" + "        },\n" + "        \"2019-02-20\": {\n" + "            \"1. open\": \"107.8600\",\n" + "            \"2. high\": \"107.9400\",\n" + "            \"3. low\": \"106.2900\",\n" + "            \"4. close\": \"107.1500\",\n" + "            \"5. volume\": \"21607671\"\n" + "        },\n" + "        \"2019-02-19\": {\n" + "            \"1. open\": \"107.7900\",\n" + "            \"2. high\": \"108.6600\",\n" + "            \"3. low\": \"107.7800\",\n" + "            \"4. close\": \"108.1700\",\n" + "            \"5. volume\": \"18038460\"\n" + "        },\n" + "        \"2019-02-15\": {\n" + "            \"1. open\": \"107.9100\",\n" + "            \"2. high\": \"108.3000\",\n" + "            \"3. low\": \"107.3624\",\n" + "            \"4. close\": \"108.2200\",\n" + "            \"5. volume\": \"26606886\"\n" + "        },\n" + "        \"2019-02-14\": {\n" + "            \"1. open\": \"106.3100\",\n" + "            \"2. high\": \"107.2900\",\n" + "            \"3. low\": \"105.6600\",\n" + "            \"4. close\": \"106.9000\",\n" + "            \"5. volume\": \"21784703\"\n" + "        },\n" + "        \"2019-02-13\": {\n" + "            \"1. open\": \"107.5000\",\n" + "            \"2. high\": \"107.7800\",\n" + "            \"3. low\": \"106.7100\",\n" + "            \"4. close\": \"106.8100\",\n" + "            \"5. volume\": \"18394869\"\n" + "        },\n" + "        \"2019-02-12\": {\n" + "            \"1. open\": \"106.1400\",\n" + "            \"2. high\": \"107.1400\",\n" + "            \"3. low\": \"105.4800\",\n" + "            \"4. close\": \"106.8900\",\n" + "            \"5. volume\": \"25056595\"\n" + "        },\n" + "        \"2019-02-11\": {\n" + "            \"1. open\": \"106.2000\",\n" + "            \"2. high\": \"106.5800\",\n" + "            \"3. low\": \"104.9650\",\n" + "            \"4. close\": \"105.2500\",\n" + "            \"5. volume\": \"18914123\"\n" + "        },\n" + "        \"2019-02-08\": {\n" + "            \"1. open\": \"104.3900\",\n" + "            \"2. high\": \"105.7800\",\n" + "            \"3. low\": \"104.2603\",\n" + "            \"4. close\": \"105.6700\",\n" + "            \"5. volume\": \"21461093\"\n" + "        },\n" + "        \"2019-02-07\": {\n" + "            \"1. open\": \"105.1850\",\n" + "            \"2. high\": \"105.5900\",\n" + "            \"3. low\": \"104.2900\",\n" + "            \"4. close\": \"105.2700\",\n" + "            \"5. volume\": \"29760697\"\n" + "        }\n" + "    }\n" + "}";

    String testRawStringB = "\n" + " {\n" + "    \"Meta Data\": {\n" + "        \"1. Information\": \"Daily Prices (open, high, low, close) and Volumes\",\n" + "        \"2. Symbol\": \"AB\",\n" + "        \"3. Last Refreshed\": \"2019-07-01\",\n" + "        \"4. Output Size\": \"Compact\",\n" + "        \"5. Time Zone\": \"US/Eastern\"\n" + "    },\n" + "    \"Time Series (Daily)\": {\n" + "        \"2019-07-01\": {\n" + "            \"1. open\": \"131.6300\",\n" + "            \"2. high\": \"136.7000\",\n" + "            \"3. low\": \"134.9700\",\n" + "            \"4. close\": \"135.6800\",\n" + "            \"5. volume\": \"22606027\"\n" + "        },\n" + "        \"2019-06-28\": {\n" + "            \"1. open\": \"134.5700\",\n" + "            \"2. high\": \"134.6000\",\n" + "            \"3. low\": \"133.1558\",\n" + "            \"4. close\": \"133.9600\",\n" + "            \"5. volume\": \"30042969\"\n" + "        },\n" + "        \"2019-06-27\": {\n" + "            \"1. open\": \"134.1400\",\n" + "            \"2. high\": \"134.7100\",\n" + "            \"3. low\": \"133.5100\",\n" + "            \"4. close\": \"134.1500\",\n" + "            \"5. volume\": \"16557482\"\n" + "        },\n" + "        \"2019-06-26\": {\n" + "            \"1. open\": \"134.3500\",\n" + "            \"2. high\": \"135.7400\",\n" + "            \"3. low\": \"133.6000\",\n" + "            \"4. close\": \"133.9300\",\n" + "            \"5. volume\": \"23657745\"\n" + "        },\n" + "        \"2019-06-25\": {\n" + "            \"1. open\": \"137.2500\",\n" + "            \"2. high\": \"137.5900\",\n" + "            \"3. low\": \"132.7300\",\n" + "            \"4. close\": \"133.4300\",\n" + "            \"5. volume\": \"33327420\"\n" + "        },\n" + "        \"2019-06-24\": {\n" + "            \"1. open\": \"137.0000\",\n" + "            \"2. high\": \"138.4000\",\n" + "            \"3. low\": \"137.0000\",\n" + "            \"4. close\": \"137.7800\",\n" + "            \"5. volume\": \"20628841\"\n" + "        },\n" + "        \"2019-06-21\": {\n" + "            \"1. open\": \"136.5800\",\n" + "            \"2. high\": \"137.7300\",\n" + "            \"3. low\": \"136.4600\",\n" + "            \"4. close\": \"136.9700\",\n" + "            \"5. volume\": \"36727892\"\n" + "        },\n" + "        \"2019-06-20\": {\n" + "            \"1. open\": \"137.4500\",\n" + "            \"2. high\": \"137.6600\",\n" + "            \"3. low\": \"135.7200\",\n" + "            \"4. close\": \"136.9500\",\n" + "            \"5. volume\": \"33042592\"\n" + "        },\n" + "        \"2019-06-19\": {\n" + "            \"1. open\": \"135.0000\",\n" + "            \"2. high\": \"135.9300\",\n" + "            \"3. low\": \"133.8100\",\n" + "            \"4. close\": \"135.6900\",\n" + "            \"5. volume\": \"23744441\"\n" + "        },\n" + "        \"2019-06-18\": {\n" + "            \"1. open\": \"134.1900\",\n" + "            \"2. high\": \"135.2400\",\n" + "            \"3. low\": \"133.5700\",\n" + "            \"4. close\": \"135.1600\",\n" + "            \"5. volume\": \"25934458\"\n" + "        },\n" + "        \"2019-06-17\": {\n" + "            \"1. open\": \"132.6300\",\n" + "            \"2. high\": \"133.7300\",\n" + "            \"3. low\": \"132.5300\",\n" + "            \"4. close\": \"132.8500\",\n" + "            \"5. volume\": \"14517785\"\n" + "        },\n" + "        \"2019-06-14\": {\n" + "            \"1. open\": \"132.2600\",\n" + "            \"2. high\": \"133.7900\",\n" + "            \"3. low\": \"131.6400\",\n" + "            \"4. close\": \"132.4500\",\n" + "            \"5. volume\": \"17821703\"\n" + "        },\n" + "        \"2019-06-13\": {\n" + "            \"1. open\": \"131.9800\",\n" + "            \"2. high\": \"132.6700\",\n" + "            \"3. low\": \"131.5600\",\n" + "            \"4. close\": \"132.3200\",\n" + "            \"5. volume\": \"17200848\"\n" + "        },\n" + "        \"2019-06-12\": {\n" + "            \"1. open\": \"131.4000\",\n" + "            \"2. high\": \"131.9700\",\n" + "            \"3. low\": \"130.7100\",\n" + "            \"4. close\": \"131.4900\",\n" + "            \"5. volume\": \"17092464\"\n" + "        },\n" + "        \"2019-06-11\": {\n" + "            \"1. open\": \"133.8800\",\n" + "            \"2. high\": \"134.2400\",\n" + "            \"3. low\": \"131.2757\",\n" + "            \"4. close\": \"132.1000\",\n" + "            \"5. volume\": \"23913731\"\n" + "        },\n" + "        \"2019-06-10\": {\n" + "            \"1. open\": \"132.4000\",\n" + "            \"2. high\": \"134.0800\",\n" + "            \"3. low\": \"132.0000\",\n" + "            \"4. close\": \"132.6000\",\n" + "            \"5. volume\": \"26477098\"\n" + "        },\n" + "        \"2019-06-07\": {\n" + "            \"1. open\": \"129.1900\",\n" + "            \"2. high\": \"132.2500\",\n" + "            \"3. low\": \"128.2600\",\n" + "            \"4. close\": \"131.4000\",\n" + "            \"5. volume\": \"33885588\"\n" + "        },\n" + "        \"2019-06-06\": {\n" + "            \"1. open\": \"126.4400\",\n" + "            \"2. high\": \"127.9700\",\n" + "            \"3. low\": \"125.6000\",\n" + "            \"4. close\": \"127.8200\",\n" + "            \"5. volume\": \"21458961\"\n" + "        },\n" + "        \"2019-06-05\": {\n" + "            \"1. open\": \"124.9500\",\n" + "            \"2. high\": \"125.8700\",\n" + "            \"3. low\": \"124.2100\",\n" + "            \"4. close\": \"125.8300\",\n" + "            \"5. volume\": \"24926140\"\n" + "        },\n" + "        \"2019-06-04\": {\n" + "            \"1. open\": \"121.2800\",\n" + "            \"2. high\": \"123.2800\",\n" + "            \"3. low\": \"120.6522\",\n" + "            \"4. close\": \"123.1600\",\n" + "            \"5. volume\": \"29382642\"\n" + "        },\n" + "        \"2019-06-03\": {\n" + "            \"1. open\": \"123.8500\",\n" + "            \"2. high\": \"124.3700\",\n" + "            \"3. low\": \"119.0100\",\n" + "            \"4. close\": \"119.8400\",\n" + "            \"5. volume\": \"37983637\"\n" + "        },\n" + "        \"2019-05-31\": {\n" + "            \"1. open\": \"124.2300\",\n" + "            \"2. high\": \"124.6150\",\n" + "            \"3. low\": \"123.3200\",\n" + "            \"4. close\": \"123.6800\",\n" + "            \"5. volume\": \"26646769\"\n" + "        },\n" + "        \"2019-05-30\": {\n" + "            \"1. open\": \"125.2600\",\n" + "            \"2. high\": \"125.7600\",\n" + "            \"3. low\": \"124.7800\",\n" + "            \"4. close\": \"125.7300\",\n" + "            \"5. volume\": \"16829613\"\n" + "        },\n" + "        \"2019-05-29\": {\n" + "            \"1. open\": \"125.3800\",\n" + "            \"2. high\": \"125.3900\",\n" + "            \"3. low\": \"124.0400\",\n" + "            \"4. close\": \"124.9400\",\n" + "            \"5. volume\": \"22763140\"\n" + "        },\n" + "        \"2019-05-28\": {\n" + "            \"1. open\": \"126.9800\",\n" + "            \"2. high\": \"128.0000\",\n" + "            \"3. low\": \"126.0500\",\n" + "            \"4. close\": \"126.1600\",\n" + "            \"5. volume\": \"23128359\"\n" + "        },\n" + "        \"2019-05-24\": {\n" + "            \"1. open\": \"126.9100\",\n" + "            \"2. high\": \"127.4150\",\n" + "            \"3. low\": \"125.9700\",\n" + "            \"4. close\": \"126.2400\",\n" + "            \"5. volume\": \"14123358\"\n" + "        },\n" + "        \"2019-05-23\": {\n" + "            \"1. open\": \"126.2000\",\n" + "            \"2. high\": \"126.2900\",\n" + "            \"3. low\": \"124.7400\",\n" + "            \"4. close\": \"126.1800\",\n" + "            \"5. volume\": \"23603810\"\n" + "        },\n" + "        \"2019-05-22\": {\n" + "            \"1. open\": \"126.6200\",\n" + "            \"2. high\": \"128.2400\",\n" + "            \"3. low\": \"126.5200\",\n" + "            \"4. close\": \"127.6700\",\n" + "            \"5. volume\": \"15396485\"\n" + "        },\n" + "        \"2019-05-21\": {\n" + "            \"1. open\": \"127.4300\",\n" + "            \"2. high\": \"127.5273\",\n" + "            \"3. low\": \"126.5800\",\n" + "            \"4. close\": \"126.9000\",\n" + "            \"5. volume\": \"15293260\"\n" + "        },\n" + "        \"2019-05-20\": {\n" + "            \"1. open\": \"126.5200\",\n" + "            \"2. high\": \"127.5894\",\n" + "            \"3. low\": \"125.7607\",\n" + "            \"4. close\": \"126.2200\",\n" + "            \"5. volume\": \"23706934\"\n" + "        },\n" + "        \"2019-05-17\": {\n" + "            \"1. open\": \"128.3050\",\n" + "            \"2. high\": \"130.4600\",\n" + "            \"3. low\": \"127.9200\",\n" + "            \"4. close\": \"128.0700\",\n" + "            \"5. volume\": \"25770539\"\n" + "        },\n" + "        \"2019-05-16\": {\n" + "            \"1. open\": \"126.7500\",\n" + "            \"2. high\": \"129.3800\",\n" + "            \"3. low\": \"126.4600\",\n" + "            \"4. close\": \"128.9300\",\n" + "            \"5. volume\": \"30112216\"\n" + "        },\n" + "        \"2019-05-15\": {\n" + "            \"1. open\": \"124.2600\",\n" + "            \"2. high\": \"126.7100\",\n" + "            \"3. low\": \"123.7000\",\n" + "            \"4. close\": \"126.0200\",\n" + "            \"5. volume\": \"24722708\"\n" + "        },\n" + "        \"2019-05-14\": {\n" + "            \"1. open\": \"123.8700\",\n" + "            \"2. high\": \"125.8800\",\n" + "            \"3. low\": \"123.7000\",\n" + "            \"4. close\": \"124.7300\",\n" + "            \"5. volume\": \"25266315\"\n" + "        },\n" + "        \"2019-05-13\": {\n" + "            \"1. open\": \"124.1100\",\n" + "            \"2. high\": \"125.5500\",\n" + "            \"3. low\": \"123.0400\",\n" + "            \"4. close\": \"123.3500\",\n" + "            \"5. volume\": \"33944923\"\n" + "        },\n" + "        \"2019-05-10\": {\n" + "            \"1. open\": \"124.9100\",\n" + "            \"2. high\": \"127.9300\",\n" + "            \"3. low\": \"123.8200\",\n" + "            \"4. close\": \"127.1300\",\n" + "            \"5. volume\": \"30915084\"\n" + "        },\n" + "        \"2019-05-09\": {\n" + "            \"1. open\": \"124.2900\",\n" + "            \"2. high\": \"125.7900\",\n" + "            \"3. low\": \"123.5700\",\n" + "            \"4. close\": \"125.5000\",\n" + "            \"5. volume\": \"27235835\"\n" + "        },\n" + "        \"2019-05-08\": {\n" + "            \"1. open\": \"125.4400\",\n" + "            \"2. high\": \"126.3700\",\n" + "            \"3. low\": \"124.7500\",\n" + "            \"4. close\": \"125.5100\",\n" + "            \"5. volume\": \"28418996\"\n" + "        },\n" + "        \"2019-05-07\": {\n" + "            \"1. open\": \"126.4600\",\n" + "            \"2. high\": \"127.1800\",\n" + "            \"3. low\": \"124.2200\",\n" + "            \"4. close\": \"125.5200\",\n" + "            \"5. volume\": \"36017661\"\n" + "        },\n" + "        \"2019-05-06\": {\n" + "            \"1. open\": \"126.3900\",\n" + "            \"2. high\": \"128.5600\",\n" + "            \"3. low\": \"126.1100\",\n" + "            \"4. close\": \"128.1500\",\n" + "            \"5. volume\": \"24239464\"\n" + "        },\n" + "        \"2019-05-03\": {\n" + "            \"1. open\": \"127.3600\",\n" + "            \"2. high\": \"129.4300\",\n" + "            \"3. low\": \"127.2500\",\n" + "            \"4. close\": \"128.9000\",\n" + "            \"5. volume\": \"24911126\"\n" + "        },\n" + "        \"2019-05-02\": {\n" + "            \"1. open\": \"127.9800\",\n" + "            \"2. high\": \"128.0000\",\n" + "            \"3. low\": \"125.5200\",\n" + "            \"4. close\": \"126.2100\",\n" + "            \"5. volume\": \"27350161\"\n" + "        },\n" + "        \"2019-05-01\": {\n" + "            \"1. open\": \"130.5300\",\n" + "            \"2. high\": \"130.6500\",\n" + "            \"3. low\": \"127.7000\",\n" + "            \"4. close\": \"127.8800\",\n" + "            \"5. volume\": \"26821692\"\n" + "        },\n" + "        \"2019-04-30\": {\n" + "            \"1. open\": \"129.8100\",\n" + "            \"2. high\": \"130.7000\",\n" + "            \"3. low\": \"129.3947\",\n" + "            \"4. close\": \"130.6000\",\n" + "            \"5. volume\": \"24166503\"\n" + "        },\n" + "        \"2019-04-29\": {\n" + "            \"1. open\": \"129.9000\",\n" + "            \"2. high\": \"130.1800\",\n" + "            \"3. low\": \"129.3500\",\n" + "            \"4. close\": \"129.7700\",\n" + "            \"5. volume\": \"16324183\"\n" + "        },\n" + "        \"2019-04-26\": {\n" + "            \"1. open\": \"129.7000\",\n" + "            \"2. high\": \"130.5152\",\n" + "            \"3. low\": \"129.0200\",\n" + "            \"4. close\": \"129.8900\",\n" + "            \"5. volume\": \"23654933\"\n" + "        },\n" + "        \"2019-04-25\": {\n" + "            \"1. open\": \"130.0600\",\n" + "            \"2. high\": \"131.3700\",\n" + "            \"3. low\": \"128.8300\",\n" + "            \"4. close\": \"129.1500\",\n" + "            \"5. volume\": \"38033892\"\n" + "        },\n" + "        \"2019-04-24\": {\n" + "            \"1. open\": \"125.7900\",\n" + "            \"2. high\": \"125.8500\",\n" + "            \"3. low\": \"124.5200\",\n" + "            \"4. close\": \"125.0100\",\n" + "            \"5. volume\": \"31256980\"\n" + "        },\n" + "        \"2019-04-23\": {\n" + "            \"1. open\": \"124.1000\",\n" + "            \"2. high\": \"125.5800\",\n" + "            \"3. low\": \"123.8300\",\n" + "            \"4. close\": \"125.4400\",\n" + "            \"5. volume\": \"24025521\"\n" + "        },\n" + "        \"2019-04-22\": {\n" + "            \"1. open\": \"122.6200\",\n" + "            \"2. high\": \"124.0000\",\n" + "            \"3. low\": \"122.5700\",\n" + "            \"4. close\": \"123.7600\",\n" + "            \"5. volume\": \"15648724\"\n" + "        },\n" + "        \"2019-04-18\": {\n" + "            \"1. open\": \"122.1900\",\n" + "            \"2. high\": \"123.5200\",\n" + "            \"3. low\": \"121.3018\",\n" + "            \"4. close\": \"123.3700\",\n" + "            \"5. volume\": \"27990998\"\n" + "        },\n" + "        \"2019-04-17\": {\n" + "            \"1. open\": \"121.2400\",\n" + "            \"2. high\": \"121.8500\",\n" + "            \"3. low\": \"120.5400\",\n" + "            \"4. close\": \"121.7700\",\n" + "            \"5. volume\": \"19300938\"\n" + "        },\n" + "        \"2019-04-16\": {\n" + "            \"1. open\": \"121.6400\",\n" + "            \"2. high\": \"121.6500\",\n" + "            \"3. low\": \"120.1000\",\n" + "            \"4. close\": \"120.7700\",\n" + "            \"5. volume\": \"14071787\"\n" + "        },\n" + "        \"2019-04-15\": {\n" + "            \"1. open\": \"120.9400\",\n" + "            \"2. high\": \"121.5800\",\n" + "            \"3. low\": \"120.5700\",\n" + "            \"4. close\": \"121.0500\",\n" + "            \"5. volume\": \"15792572\"\n" + "        },\n" + "        \"2019-04-12\": {\n" + "            \"1. open\": \"120.6400\",\n" + "            \"2. high\": \"120.9800\",\n" + "            \"3. low\": \"120.3700\",\n" + "            \"4. close\": \"120.9500\",\n" + "            \"5. volume\": \"19745143\"\n" + "        },\n" + "        \"2019-04-11\": {\n" + "            \"1. open\": \"120.5400\",\n" + "            \"2. high\": \"120.8500\",\n" + "            \"3. low\": \"119.9200\",\n" + "            \"4. close\": \"120.3300\",\n" + "            \"5. volume\": \"14209121\"\n" + "        },\n" + "        \"2019-04-10\": {\n" + "            \"1. open\": \"119.7600\",\n" + "            \"2. high\": \"120.3500\",\n" + "            \"3. low\": \"119.5400\",\n" + "            \"4. close\": \"120.1900\",\n" + "            \"5. volume\": \"16477169\"\n" + "        },\n" + "        \"2019-04-09\": {\n" + "            \"1. open\": \"118.6300\",\n" + "            \"2. high\": \"119.5400\",\n" + "            \"3. low\": \"118.5800\",\n" + "            \"4. close\": \"119.2800\",\n" + "            \"5. volume\": \"17611981\"\n" + "        },\n" + "        \"2019-04-08\": {\n" + "            \"1. open\": \"119.8100\",\n" + "            \"2. high\": \"120.0200\",\n" + "            \"3. low\": \"118.6400\",\n" + "            \"4. close\": \"119.9300\",\n" + "            \"5. volume\": \"15116186\"\n" + "        },\n" + "        \"2019-04-05\": {\n" + "            \"1. open\": \"119.3900\",\n" + "            \"2. high\": \"120.2300\",\n" + "            \"3. low\": \"119.3700\",\n" + "            \"4. close\": \"119.8900\",\n" + "            \"5. volume\": \"15826245\"\n" + "        },\n" + "        \"2019-04-04\": {\n" + "            \"1. open\": \"120.1000\",\n" + "            \"2. high\": \"120.2300\",\n" + "            \"3. low\": \"118.3800\",\n" + "            \"4. close\": \"119.3600\",\n" + "            \"5. volume\": \"20112848\"\n" + "        },\n" + "        \"2019-04-03\": {\n" + "            \"1. open\": \"119.8600\",\n" + "            \"2. high\": \"120.4300\",\n" + "            \"3. low\": \"119.1500\",\n" + "            \"4. close\": \"119.9700\",\n" + "            \"5. volume\": \"22860744\"\n" + "        },\n" + "        \"2019-04-02\": {\n" + "            \"1. open\": \"119.0600\",\n" + "            \"2. high\": \"119.4800\",\n" + "            \"3. low\": \"118.5200\",\n" + "            \"4. close\": \"119.1900\",\n" + "            \"5. volume\": \"18142297\"\n" + "        },\n" + "        \"2019-04-01\": {\n" + "            \"1. open\": \"118.9500\",\n" + "            \"2. high\": \"119.1085\",\n" + "            \"3. low\": \"118.1000\",\n" + "            \"4. close\": \"119.0200\",\n" + "            \"5. volume\": \"22789103\"\n" + "        },\n" + "        \"2019-03-29\": {\n" + "            \"1. open\": \"118.0700\",\n" + "            \"2. high\": \"118.3200\",\n" + "            \"3. low\": \"116.9600\",\n" + "            \"4. close\": \"117.9400\",\n" + "            \"5. volume\": \"25399752\"\n" + "        },\n" + "        \"2019-03-28\": {\n" + "            \"1. open\": \"117.4400\",\n" + "            \"2. high\": \"117.5800\",\n" + "            \"3. low\": \"116.1300\",\n" + "            \"4. close\": \"116.9300\",\n" + "            \"5. volume\": \"18334755\"\n" + "        },\n" + "        \"2019-03-27\": {\n" + "            \"1. open\": \"117.8750\",\n" + "            \"2. high\": \"118.2100\",\n" + "            \"3. low\": \"115.5215\",\n" + "            \"4. close\": \"116.7700\",\n" + "            \"5. volume\": \"22733427\"\n" + "        },\n" + "        \"2019-03-26\": {\n" + "            \"1. open\": \"118.6200\",\n" + "            \"2. high\": \"118.7050\",\n" + "            \"3. low\": \"116.8500\",\n" + "            \"4. close\": \"117.9100\",\n" + "            \"5. volume\": \"26097665\"\n" + "        },\n" + "        \"2019-03-25\": {\n" + "            \"1. open\": \"116.5600\",\n" + "            \"2. high\": \"118.0100\",\n" + "            \"3. low\": \"116.3224\",\n" + "            \"4. close\": \"117.6600\",\n" + "            \"5. volume\": \"27067117\"\n" + "        },\n" + "        \"2019-03-22\": {\n" + "            \"1. open\": \"119.5000\",\n" + "            \"2. high\": \"119.5900\",\n" + "            \"3. low\": \"117.0400\",\n" + "            \"4. close\": \"117.0500\",\n" + "            \"5. volume\": \"33624528\"\n" + "        },\n" + "        \"2019-03-21\": {\n" + "            \"1. open\": \"117.1350\",\n" + "            \"2. high\": \"120.8200\",\n" + "            \"3. low\": \"117.0900\",\n" + "            \"4. close\": \"120.2200\",\n" + "            \"5. volume\": \"29854446\"\n" + "        },\n" + "        \"2019-03-20\": {\n" + "            \"1. open\": \"117.3900\",\n" + "            \"2. high\": \"118.7500\",\n" + "            \"3. low\": \"116.7100\",\n" + "            \"4. close\": \"117.5200\",\n" + "            \"5. volume\": \"28113343\"\n" + "        },\n" + "        \"2019-03-19\": {\n" + "            \"1. open\": \"118.0900\",\n" + "            \"2. high\": \"118.4400\",\n" + "            \"3. low\": \"116.9900\",\n" + "            \"4. close\": \"117.6500\",\n" + "            \"5. volume\": \"37588697\"\n" + "        },\n" + "        \"2019-03-18\": {\n" + "            \"1. open\": \"116.1700\",\n" + "            \"2. high\": \"117.6100\",\n" + "            \"3. low\": \"116.0500\",\n" + "            \"4. close\": \"117.5700\",\n" + "            \"5. volume\": \"31207596\"\n" + "        },\n" + "        \"2019-03-15\": {\n" + "            \"1. open\": \"115.3400\",\n" + "            \"2. high\": \"117.2500\",\n" + "            \"3. low\": \"114.5900\",\n" + "            \"4. close\": \"115.9100\",\n" + "            \"5. volume\": \"54630661\"\n" + "        },\n" + "        \"2019-03-14\": {\n" + "            \"1. open\": \"114.5400\",\n" + "            \"2. high\": \"115.2000\",\n" + "            \"3. low\": \"114.3300\",\n" + "            \"4. close\": \"114.5900\",\n" + "            \"5. volume\": \"30763367\"\n" + "        },\n" + "        \"2019-03-13\": {\n" + "            \"1. open\": \"114.1300\",\n" + "            \"2. high\": \"115.0000\",\n" + "            \"3. low\": \"113.7800\",\n" + "            \"4. close\": \"114.5000\",\n" + "            \"5. volume\": \"35513771\"\n" + "        },\n" + "        \"2019-03-12\": {\n" + "            \"1. open\": \"112.8200\",\n" + "            \"2. high\": \"113.9900\",\n" + "            \"3. low\": \"112.6499\",\n" + "            \"4. close\": \"113.6200\",\n" + "            \"5. volume\": \"26132717\"\n" + "        },\n" + "        \"2019-03-11\": {\n" + "            \"1. open\": \"110.9900\",\n" + "            \"2. high\": \"112.9500\",\n" + "            \"3. low\": \"110.9800\",\n" + "            \"4. close\": \"112.8300\",\n" + "            \"5. volume\": \"26491618\"\n" + "        },\n" + "        \"2019-03-08\": {\n" + "            \"1. open\": \"109.1600\",\n" + "            \"2. high\": \"110.7100\",\n" + "            \"3. low\": \"108.8000\",\n" + "            \"4. close\": \"110.5100\",\n" + "            \"5. volume\": \"22818430\"\n" + "        },\n" + "        \"2019-03-07\": {\n" + "            \"1. open\": \"111.4000\",\n" + "            \"2. high\": \"111.5500\",\n" + "            \"3. low\": \"109.8650\",\n" + "            \"4. close\": \"110.3900\",\n" + "            \"5. volume\": \"25338954\"\n" + "        },\n" + "        \"2019-03-06\": {\n" + "            \"1. open\": \"111.8700\",\n" + "            \"2. high\": \"112.6600\",\n" + "            \"3. low\": \"111.4300\",\n" + "            \"4. close\": \"111.7500\",\n" + "            \"5. volume\": \"17686996\"\n" + "        },\n" + "        \"2019-03-05\": {\n" + "            \"1. open\": \"112.2500\",\n" + "            \"2. high\": \"112.3900\",\n" + "            \"3. low\": \"111.2300\",\n" + "            \"4. close\": \"111.7000\",\n" + "            \"5. volume\": \"19538318\"\n" + "        },\n" + "        \"2019-03-04\": {\n" + "            \"1. open\": \"113.0200\",\n" + "            \"2. high\": \"113.2500\",\n" + "            \"3. low\": \"110.8000\",\n" + "            \"4. close\": \"112.2600\",\n" + "            \"5. volume\": \"26608014\"\n" + "        },\n" + "        \"2019-03-01\": {\n" + "            \"1. open\": \"112.8900\",\n" + "            \"2. high\": \"113.0200\",\n" + "            \"3. low\": \"111.6650\",\n" + "            \"4. close\": \"112.5300\",\n" + "            \"5. volume\": \"23501169\"\n" + "        },\n" + "        \"2019-02-28\": {\n" + "            \"1. open\": \"112.0400\",\n" + "            \"2. high\": \"112.8800\",\n" + "            \"3. low\": \"111.7300\",\n" + "            \"4. close\": \"112.0300\",\n" + "            \"5. volume\": \"29083934\"\n" + "        },\n" + "        \"2019-02-27\": {\n" + "            \"1. open\": \"111.6900\",\n" + "            \"2. high\": \"112.3600\",\n" + "            \"3. low\": \"110.8800\",\n" + "            \"4. close\": \"112.1700\",\n" + "            \"5. volume\": \"21487062\"\n" + "        },\n" + "        \"2019-02-26\": {\n" + "            \"1. open\": \"111.2600\",\n" + "            \"2. high\": \"113.2400\",\n" + "            \"3. low\": \"111.1700\",\n" + "            \"4. close\": \"112.3600\",\n" + "            \"5. volume\": \"21536733\"\n" + "        },\n" + "        \"2019-02-25\": {\n" + "            \"1. open\": \"111.7600\",\n" + "            \"2. high\": \"112.1800\",\n" + "            \"3. low\": \"111.2600\",\n" + "            \"4. close\": \"111.5900\",\n" + "            \"5. volume\": \"23750599\"\n" + "        },\n" + "        \"2019-02-22\": {\n" + "            \"1. open\": \"110.0500\",\n" + "            \"2. high\": \"111.2000\",\n" + "            \"3. low\": \"109.8200\",\n" + "            \"4. close\": \"110.9700\",\n" + "            \"5. volume\": \"27763218\"\n" + "        },\n" + "        \"2019-02-21\": {\n" + "            \"1. open\": \"106.9000\",\n" + "            \"2. high\": \"109.4800\",\n" + "            \"3. low\": \"106.8700\",\n" + "            \"4. close\": \"109.4100\",\n" + "            \"5. volume\": \"29063231\"\n" + "        },\n" + "        \"2019-02-20\": {\n" + "            \"1. open\": \"107.8600\",\n" + "            \"2. high\": \"107.9400\",\n" + "            \"3. low\": \"106.2900\",\n" + "            \"4. close\": \"107.1500\",\n" + "            \"5. volume\": \"21607671\"\n" + "        },\n" + "        \"2019-02-19\": {\n" + "            \"1. open\": \"107.7900\",\n" + "            \"2. high\": \"108.6600\",\n" + "            \"3. low\": \"107.7800\",\n" + "            \"4. close\": \"108.1700\",\n" + "            \"5. volume\": \"18038460\"\n" + "        },\n" + "        \"2019-02-15\": {\n" + "            \"1. open\": \"107.9100\",\n" + "            \"2. high\": \"108.3000\",\n" + "            \"3. low\": \"107.3624\",\n" + "            \"4. close\": \"108.2200\",\n" + "            \"5. volume\": \"26606886\"\n" + "        },\n" + "        \"2019-02-14\": {\n" + "            \"1. open\": \"106.3100\",\n" + "            \"2. high\": \"107.2900\",\n" + "            \"3. low\": \"105.6600\",\n" + "            \"4. close\": \"106.9000\",\n" + "            \"5. volume\": \"21784703\"\n" + "        },\n" + "        \"2019-02-13\": {\n" + "            \"1. open\": \"107.5000\",\n" + "            \"2. high\": \"107.7800\",\n" + "            \"3. low\": \"106.7100\",\n" + "            \"4. close\": \"106.8100\",\n" + "            \"5. volume\": \"18394869\"\n" + "        },\n" + "        \"2019-02-12\": {\n" + "            \"1. open\": \"106.1400\",\n" + "            \"2. high\": \"107.1400\",\n" + "            \"3. low\": \"105.4800\",\n" + "            \"4. close\": \"106.8900\",\n" + "            \"5. volume\": \"25056595\"\n" + "        },\n" + "        \"2019-02-11\": {\n" + "            \"1. open\": \"106.2000\",\n" + "            \"2. high\": \"106.5800\",\n" + "            \"3. low\": \"104.9650\",\n" + "            \"4. close\": \"105.2500\",\n" + "            \"5. volume\": \"18914123\"\n" + "        },\n" + "        \"2019-02-08\": {\n" + "            \"1. open\": \"104.3900\",\n" + "            \"2. high\": \"105.7800\",\n" + "            \"3. low\": \"104.2603\",\n" + "            \"4. close\": \"105.6700\",\n" + "            \"5. volume\": \"21461093\"\n" + "        },\n" + "        \"2019-02-07\": {\n" + "            \"1. open\": \"105.1850\",\n" + "            \"2. high\": \"105.5900\",\n" + "            \"3. low\": \"104.2900\",\n" + "            \"4. close\": \"105.2700\",\n" + "            \"5. volume\": \"29760697\"\n" + "        }\n" + "    }\n" + "}";

    String testRawStringC = "{\n" + "    \"Meta Data\": {\n" + "        \"1. Information\": \"Intraday (1min) open, high, low, close prices and volume\",\n" + "        \"2. Symbol\": \"BA\",\n" + "        \"3. Last Refreshed\": \"2019-07-19 16:00:00\",\n" + "        \"4. Interval\": \"1min\",\n" + "        \"5. Output Size\": \"Compact\",\n" + "        \"6. Time Zone\": \"US/Eastern\"\n" + "    },\n" + "    \"Time Series (1min)\": {\n" + "        \"2019-07-19 16:00:00\": {\n" + "            \"1. open\": \"378.0200\",\n" + "            \"2. high\": \"378.0915\",\n" + "            \"3. low\": \"376.9700\",\n" + "            \"4. close\": \"377.3200\",\n" + "            \"5. volume\": \"139368\"\n" + "        },\n" + "        \"2019-07-19 15:59:00\": {\n" + "            \"1. open\": \"378.0000\",\n" + "            \"2. high\": \"378.0700\",\n" + "            \"3. low\": \"377.9800\",\n" + "            \"4. close\": \"378.0700\",\n" + "            \"5. volume\": \"101576\"\n" + "        },\n" + "        \"2019-07-19 15:58:00\": {\n" + "            \"1. open\": \"378.0300\",\n" + "            \"2. high\": \"378.2500\",\n" + "            \"3. low\": \"378.0000\",\n" + "            \"4. close\": \"378.0000\",\n" + "            \"5. volume\": \"62225\"\n" + "        },\n" + "        \"2019-07-19 15:57:00\": {\n" + "            \"1. open\": \"377.7300\",\n" + "            \"2. high\": \"378.1800\",\n" + "            \"3. low\": \"377.5900\",\n" + "            \"4. close\": \"378.0100\",\n" + "            \"5. volume\": \"60371\"\n" + "        },\n" + "        \"2019-07-19 15:56:00\": {\n" + "            \"1. open\": \"378.3300\",\n" + "            \"2. high\": \"378.3300\",\n" + "            \"3. low\": \"377.6100\",\n" + "            \"4. close\": \"377.7800\",\n" + "            \"5. volume\": \"117872\"\n" + "        },\n" + "        \"2019-07-19 15:55:00\": {\n" + "            \"1. open\": \"377.3700\",\n" + "            \"2. high\": \"378.4000\",\n" + "            \"3. low\": \"377.3600\",\n" + "            \"4. close\": \"378.3300\",\n" + "            \"5. volume\": \"80716\"\n" + "        },\n" + "        \"2019-07-19 15:54:00\": {\n" + "            \"1. open\": \"377.0600\",\n" + "            \"2. high\": \"377.3700\",\n" + "            \"3. low\": \"377.0200\",\n" + "            \"4. close\": \"377.3300\",\n" + "            \"5. volume\": \"42103\"\n" + "        },\n" + "        \"2019-07-19 15:53:00\": {\n" + "            \"1. open\": \"376.8550\",\n" + "            \"2. high\": \"377.0600\",\n" + "            \"3. low\": \"376.8200\",\n" + "            \"4. close\": \"377.0600\",\n" + "            \"5. volume\": \"49136\"\n" + "        },\n" + "        \"2019-07-19 15:52:00\": {\n" + "            \"1. open\": \"376.6400\",\n" + "            \"2. high\": \"376.9000\",\n" + "            \"3. low\": \"376.6400\",\n" + "            \"4. close\": \"376.8400\",\n" + "            \"5. volume\": \"24951\"\n" + "        },\n" + "        \"2019-07-19 15:51:00\": {\n" + "            \"1. open\": \"376.6400\",\n" + "            \"2. high\": \"376.6700\",\n" + "            \"3. low\": \"376.4611\",\n" + "            \"4. close\": \"376.6200\",\n" + "            \"5. volume\": \"28834\"\n" + "        },\n" + "        \"2019-07-19 15:50:00\": {\n" + "            \"1. open\": \"376.5100\",\n" + "            \"2. high\": \"376.6800\",\n" + "            \"3. low\": \"376.4400\",\n" + "            \"4. close\": \"376.6150\",\n" + "            \"5. volume\": \"15934\"\n" + "        },\n" + "        \"2019-07-19 15:49:00\": {\n" + "            \"1. open\": \"376.3400\",\n" + "            \"2. high\": \"376.5595\",\n" + "            \"3. low\": \"376.3400\",\n" + "            \"4. close\": \"376.5200\",\n" + "            \"5. volume\": \"14877\"\n" + "        },\n" + "        \"2019-07-19 15:48:00\": {\n" + "            \"1. open\": \"376.5450\",\n" + "            \"2. high\": \"376.5500\",\n" + "            \"3. low\": \"376.3410\",\n" + "            \"4. close\": \"376.3700\",\n" + "            \"5. volume\": \"21589\"\n" + "        },\n" + "        \"2019-07-19 15:47:00\": {\n" + "            \"1. open\": \"376.6300\",\n" + "            \"2. high\": \"376.6700\",\n" + "            \"3. low\": \"376.5000\",\n" + "            \"4. close\": \"376.5400\",\n" + "            \"5. volume\": \"12071\"\n" + "        },\n" + "        \"2019-07-19 15:46:00\": {\n" + "            \"1. open\": \"376.6700\",\n" + "            \"2. high\": \"376.6700\",\n" + "            \"3. low\": \"376.4700\",\n" + "            \"4. close\": \"376.6500\",\n" + "            \"5. volume\": \"17294\"\n" + "        },\n" + "        \"2019-07-19 15:45:00\": {\n" + "            \"1. open\": \"376.6500\",\n" + "            \"2. high\": \"376.8800\",\n" + "            \"3. low\": \"376.6000\",\n" + "            \"4. close\": \"376.7100\",\n" + "            \"5. volume\": \"21258\"\n" + "        },\n" + "        \"2019-07-19 15:44:00\": {\n" + "            \"1. open\": \"376.2801\",\n" + "            \"2. high\": \"376.6500\",\n" + "            \"3. low\": \"376.2801\",\n" + "            \"4. close\": \"376.6500\",\n" + "            \"5. volume\": \"35605\"\n" + "        },\n" + "        \"2019-07-19 15:43:00\": {\n" + "            \"1. open\": \"376.3400\",\n" + "            \"2. high\": \"376.4200\",\n" + "            \"3. low\": \"376.2900\",\n" + "            \"4. close\": \"376.2900\",\n" + "            \"5. volume\": \"11389\"\n" + "        },\n" + "        \"2019-07-19 15:42:00\": {\n" + "            \"1. open\": \"376.1500\",\n" + "            \"2. high\": \"376.4200\",\n" + "            \"3. low\": \"376.1500\",\n" + "            \"4. close\": \"376.4200\",\n" + "            \"5. volume\": \"23616\"\n" + "        },\n" + "        \"2019-07-19 15:41:00\": {\n" + "            \"1. open\": \"376.3500\",\n" + "            \"2. high\": \"376.3527\",\n" + "            \"3. low\": \"375.9950\",\n" + "            \"4. close\": \"376.1500\",\n" + "            \"5. volume\": \"21125\"\n" + "        },\n" + "        \"2019-07-19 15:40:00\": {\n" + "            \"1. open\": \"376.3900\",\n" + "            \"2. high\": \"376.4400\",\n" + "            \"3. low\": \"376.3500\",\n" + "            \"4. close\": \"376.3629\",\n" + "            \"5. volume\": \"19067\"\n" + "        },\n" + "        \"2019-07-19 15:39:00\": {\n" + "            \"1. open\": \"376.4100\",\n" + "            \"2. high\": \"376.4200\",\n" + "            \"3. low\": \"376.3600\",\n" + "            \"4. close\": \"376.3949\",\n" + "            \"5. volume\": \"11409\"\n" + "        },\n" + "        \"2019-07-19 15:38:00\": {\n" + "            \"1. open\": \"376.3100\",\n" + "            \"2. high\": \"376.4600\",\n" + "            \"3. low\": \"376.2800\",\n" + "            \"4. close\": \"376.4200\",\n" + "            \"5. volume\": \"13534\"\n" + "        },\n" + "        \"2019-07-19 15:37:00\": {\n" + "            \"1. open\": \"376.1700\",\n" + "            \"2. high\": \"376.4300\",\n" + "            \"3. low\": \"376.1700\",\n" + "            \"4. close\": \"376.3100\",\n" + "            \"5. volume\": \"19822\"\n" + "        },\n" + "        \"2019-07-19 15:36:00\": {\n" + "            \"1. open\": \"376.0900\",\n" + "            \"2. high\": \"376.1500\",\n" + "            \"3. low\": \"376.0100\",\n" + "            \"4. close\": \"376.1500\",\n" + "            \"5. volume\": \"12831\"\n" + "        },\n" + "        \"2019-07-19 15:35:00\": {\n" + "            \"1. open\": \"376.1100\",\n" + "            \"2. high\": \"376.1100\",\n" + "            \"3. low\": \"375.8900\",\n" + "            \"4. close\": \"376.0400\",\n" + "            \"5. volume\": \"14311\"\n" + "        },\n" + "        \"2019-07-19 15:34:00\": {\n" + "            \"1. open\": \"375.9900\",\n" + "            \"2. high\": \"376.1600\",\n" + "            \"3. low\": \"375.9900\",\n" + "            \"4. close\": \"376.1300\",\n" + "            \"5. volume\": \"13657\"\n" + "        },\n" + "        \"2019-07-19 15:33:00\": {\n" + "            \"1. open\": \"375.9300\",\n" + "            \"2. high\": \"376.1000\",\n" + "            \"3. low\": \"375.8400\",\n" + "            \"4. close\": \"375.9900\",\n" + "            \"5. volume\": \"18953\"\n" + "        },\n" + "        \"2019-07-19 15:32:00\": {\n" + "            \"1. open\": \"376.0900\",\n" + "            \"2. high\": \"376.1200\",\n" + "            \"3. low\": \"375.8333\",\n" + "            \"4. close\": \"375.9450\",\n" + "            \"5. volume\": \"24388\"\n" + "        },\n" + "        \"2019-07-19 15:31:00\": {\n" + "            \"1. open\": \"376.1600\",\n" + "            \"2. high\": \"376.1900\",\n" + "            \"3. low\": \"376.0000\",\n" + "            \"4. close\": \"376.0900\",\n" + "            \"5. volume\": \"15446\"\n" + "        },\n" + "        \"2019-07-19 15:30:00\": {\n" + "            \"1. open\": \"376.2200\",\n" + "            \"2. high\": \"376.2800\",\n" + "            \"3. low\": \"376.0600\",\n" + "            \"4. close\": \"376.1610\",\n" + "            \"5. volume\": \"9982\"\n" + "        },\n" + "        \"2019-07-19 15:29:00\": {\n" + "            \"1. open\": \"376.1600\",\n" + "            \"2. high\": \"376.2200\",\n" + "            \"3. low\": \"376.1300\",\n" + "            \"4. close\": \"376.2000\",\n" + "            \"5. volume\": \"9238\"\n" + "        },\n" + "        \"2019-07-19 15:28:00\": {\n" + "            \"1. open\": \"376.2200\",\n" + "            \"2. high\": \"376.2200\",\n" + "            \"3. low\": \"376.1200\",\n" + "            \"4. close\": \"376.1900\",\n" + "            \"5. volume\": \"11765\"\n" + "        },\n" + "        \"2019-07-19 15:27:00\": {\n" + "            \"1. open\": \"376.3700\",\n" + "            \"2. high\": \"376.5700\",\n" + "            \"3. low\": \"376.2400\",\n" + "            \"4. close\": \"376.2400\",\n" + "            \"5. volume\": \"16019\"\n" + "        },\n" + "        \"2019-07-19 15:26:00\": {\n" + "            \"1. open\": \"376.3000\",\n" + "            \"2. high\": \"376.3900\",\n" + "            \"3. low\": \"376.2400\",\n" + "            \"4. close\": \"376.3700\",\n" + "            \"5. volume\": \"11001\"\n" + "        },\n" + "        \"2019-07-19 15:25:00\": {\n" + "            \"1. open\": \"376.4400\",\n" + "            \"2. high\": \"376.4500\",\n" + "            \"3. low\": \"376.2000\",\n" + "            \"4. close\": \"376.2500\",\n" + "            \"5. volume\": \"15269\"\n" + "        },\n" + "        \"2019-07-19 15:24:00\": {\n" + "            \"1. open\": \"376.3400\",\n" + "            \"2. high\": \"376.5200\",\n" + "            \"3. low\": \"376.2200\",\n" + "            \"4. close\": \"376.4400\",\n" + "            \"5. volume\": \"26734\"\n" + "        },\n" + "        \"2019-07-19 15:23:00\": {\n" + "            \"1. open\": \"376.0300\",\n" + "            \"2. high\": \"376.3500\",\n" + "            \"3. low\": \"375.8900\",\n" + "            \"4. close\": \"376.3500\",\n" + "            \"5. volume\": \"26658\"\n" + "        },\n" + "        \"2019-07-19 15:22:00\": {\n" + "            \"1. open\": \"376.0300\",\n" + "            \"2. high\": \"376.1000\",\n" + "            \"3. low\": \"375.7000\",\n" + "            \"4. close\": \"376.1000\",\n" + "            \"5. volume\": \"21597\"\n" + "        },\n" + "        \"2019-07-19 15:21:00\": {\n" + "            \"1. open\": \"376.1701\",\n" + "            \"2. high\": \"376.2108\",\n" + "            \"3. low\": \"375.7700\",\n" + "            \"4. close\": \"376.0300\",\n" + "            \"5. volume\": \"29292\"\n" + "        },\n" + "        \"2019-07-19 15:20:00\": {\n" + "            \"1. open\": \"376.3000\",\n" + "            \"2. high\": \"376.3000\",\n" + "            \"3. low\": \"376.0900\",\n" + "            \"4. close\": \"376.1575\",\n" + "            \"5. volume\": \"9776\"\n" + "        },\n" + "        \"2019-07-19 15:19:00\": {\n" + "            \"1. open\": \"376.3767\",\n" + "            \"2. high\": \"376.3767\",\n" + "            \"3. low\": \"376.1600\",\n" + "            \"4. close\": \"376.3100\",\n" + "            \"5. volume\": \"19973\"\n" + "        },\n" + "        \"2019-07-19 15:18:00\": {\n" + "            \"1. open\": \"376.3700\",\n" + "            \"2. high\": \"376.5500\",\n" + "            \"3. low\": \"376.3500\",\n" + "            \"4. close\": \"376.3545\",\n" + "            \"5. volume\": \"8231\"\n" + "        },\n" + "        \"2019-07-19 15:17:00\": {\n" + "            \"1. open\": \"376.5300\",\n" + "            \"2. high\": \"376.5800\",\n" + "            \"3. low\": \"376.3000\",\n" + "            \"4. close\": \"376.4200\",\n" + "            \"5. volume\": \"8597\"\n" + "        },\n" + "        \"2019-07-19 15:16:00\": {\n" + "            \"1. open\": \"376.2300\",\n" + "            \"2. high\": \"376.6400\",\n" + "            \"3. low\": \"376.2300\",\n" + "            \"4. close\": \"376.5800\",\n" + "            \"5. volume\": \"16940\"\n" + "        },\n" + "        \"2019-07-19 15:15:00\": {\n" + "            \"1. open\": \"376.2050\",\n" + "            \"2. high\": \"376.2700\",\n" + "            \"3. low\": \"376.1501\",\n" + "            \"4. close\": \"376.2500\",\n" + "            \"5. volume\": \"15017\"\n" + "        },\n" + "        \"2019-07-19 15:14:00\": {\n" + "            \"1. open\": \"376.4000\",\n" + "            \"2. high\": \"376.4400\",\n" + "            \"3. low\": \"376.1300\",\n" + "            \"4. close\": \"376.2400\",\n" + "            \"5. volume\": \"19558\"\n" + "        },\n" + "        \"2019-07-19 15:13:00\": {\n" + "            \"1. open\": \"376.4700\",\n" + "            \"2. high\": \"376.5200\",\n" + "            \"3. low\": \"376.3900\",\n" + "            \"4. close\": \"376.4100\",\n" + "            \"5. volume\": \"12073\"\n" + "        },\n" + "        \"2019-07-19 15:12:00\": {\n" + "            \"1. open\": \"376.4260\",\n" + "            \"2. high\": \"376.4900\",\n" + "            \"3. low\": \"376.3101\",\n" + "            \"4. close\": \"376.4000\",\n" + "            \"5. volume\": \"14285\"\n" + "        },\n" + "        \"2019-07-19 15:11:00\": {\n" + "            \"1. open\": \"376.5250\",\n" + "            \"2. high\": \"376.7100\",\n" + "            \"3. low\": \"376.4400\",\n" + "            \"4. close\": \"376.4462\",\n" + "            \"5. volume\": \"16607\"\n" + "        },\n" + "        \"2019-07-19 15:10:00\": {\n" + "            \"1. open\": \"376.6880\",\n" + "            \"2. high\": \"376.7000\",\n" + "            \"3. low\": \"376.5000\",\n" + "            \"4. close\": \"376.5100\",\n" + "            \"5. volume\": \"14722\"\n" + "        },\n" + "        \"2019-07-19 15:09:00\": {\n" + "            \"1. open\": \"376.8471\",\n" + "            \"2. high\": \"376.8500\",\n" + "            \"3. low\": \"376.7100\",\n" + "            \"4. close\": \"376.7100\",\n" + "            \"5. volume\": \"6759\"\n" + "        },\n" + "        \"2019-07-19 15:08:00\": {\n" + "            \"1. open\": \"376.7800\",\n" + "            \"2. high\": \"376.9500\",\n" + "            \"3. low\": \"376.7500\",\n" + "            \"4. close\": \"376.8600\",\n" + "            \"5. volume\": \"9271\"\n" + "        },\n" + "        \"2019-07-19 15:07:00\": {\n" + "            \"1. open\": \"377.1100\",\n" + "            \"2. high\": \"377.1200\",\n" + "            \"3. low\": \"376.7100\",\n" + "            \"4. close\": \"376.7100\",\n" + "            \"5. volume\": \"10526\"\n" + "        },\n" + "        \"2019-07-19 15:06:00\": {\n" + "            \"1. open\": \"377.1000\",\n" + "            \"2. high\": \"377.1520\",\n" + "            \"3. low\": \"376.8400\",\n" + "            \"4. close\": \"377.0200\",\n" + "            \"5. volume\": \"10861\"\n" + "        },\n" + "        \"2019-07-19 15:05:00\": {\n" + "            \"1. open\": \"377.3484\",\n" + "            \"2. high\": \"377.3484\",\n" + "            \"3. low\": \"377.1200\",\n" + "            \"4. close\": \"377.1500\",\n" + "            \"5. volume\": \"11804\"\n" + "        },\n" + "        \"2019-07-19 15:04:00\": {\n" + "            \"1. open\": \"377.1900\",\n" + "            \"2. high\": \"377.4100\",\n" + "            \"3. low\": \"377.1700\",\n" + "            \"4. close\": \"377.4000\",\n" + "            \"5. volume\": \"21662\"\n" + "        },\n" + "        \"2019-07-19 15:03:00\": {\n" + "            \"1. open\": \"377.3400\",\n" + "            \"2. high\": \"377.3400\",\n" + "            \"3. low\": \"377.0900\",\n" + "            \"4. close\": \"377.2000\",\n" + "            \"5. volume\": \"9596\"\n" + "        },\n" + "        \"2019-07-19 15:02:00\": {\n" + "            \"1. open\": \"377.2500\",\n" + "            \"2. high\": \"377.4400\",\n" + "            \"3. low\": \"377.2100\",\n" + "            \"4. close\": \"377.3400\",\n" + "            \"5. volume\": \"7170\"\n" + "        },\n" + "        \"2019-07-19 15:01:00\": {\n" + "            \"1. open\": \"377.0100\",\n" + "            \"2. high\": \"377.2400\",\n" + "            \"3. low\": \"376.9600\",\n" + "            \"4. close\": \"377.2400\",\n" + "            \"5. volume\": \"10278\"\n" + "        },\n" + "        \"2019-07-19 15:00:00\": {\n" + "            \"1. open\": \"376.9800\",\n" + "            \"2. high\": \"377.2100\",\n" + "            \"3. low\": \"376.9605\",\n" + "            \"4. close\": \"377.0500\",\n" + "            \"5. volume\": \"11757\"\n" + "        },\n" + "        \"2019-07-19 14:59:00\": {\n" + "            \"1. open\": \"377.0600\",\n" + "            \"2. high\": \"377.1200\",\n" + "            \"3. low\": \"377.0000\",\n" + "            \"4. close\": \"377.0000\",\n" + "            \"5. volume\": \"7731\"\n" + "        },\n" + "        \"2019-07-19 14:58:00\": {\n" + "            \"1. open\": \"377.1000\",\n" + "            \"2. high\": \"377.2400\",\n" + "            \"3. low\": \"377.0162\",\n" + "            \"4. close\": \"377.1100\",\n" + "            \"5. volume\": \"14134\"\n" + "        },\n" + "        \"2019-07-19 14:57:00\": {\n" + "            \"1. open\": \"376.9600\",\n" + "            \"2. high\": \"377.0300\",\n" + "            \"3. low\": \"376.8300\",\n" + "            \"4. close\": \"376.9800\",\n" + "            \"5. volume\": \"19125\"\n" + "        },\n" + "        \"2019-07-19 14:56:00\": {\n" + "            \"1. open\": \"376.9400\",\n" + "            \"2. high\": \"376.9600\",\n" + "            \"3. low\": \"376.8800\",\n" + "            \"4. close\": \"376.9000\",\n" + "            \"5. volume\": \"4061\"\n" + "        },\n" + "        \"2019-07-19 14:55:00\": {\n" + "            \"1. open\": \"377.0000\",\n" + "            \"2. high\": \"377.0000\",\n" + "            \"3. low\": \"376.9000\",\n" + "            \"4. close\": \"376.9794\",\n" + "            \"5. volume\": \"12092\"\n" + "        },\n" + "        \"2019-07-19 14:54:00\": {\n" + "            \"1. open\": \"377.0600\",\n" + "            \"2. high\": \"377.0800\",\n" + "            \"3. low\": \"376.9700\",\n" + "            \"4. close\": \"377.0000\",\n" + "            \"5. volume\": \"10472\"\n" + "        },\n" + "        \"2019-07-19 14:53:00\": {\n" + "            \"1. open\": \"377.2800\",\n" + "            \"2. high\": \"377.3100\",\n" + "            \"3. low\": \"377.0700\",\n" + "            \"4. close\": \"377.1300\",\n" + "            \"5. volume\": \"7028\"\n" + "        },\n" + "        \"2019-07-19 14:52:00\": {\n" + "            \"1. open\": \"377.4100\",\n" + "            \"2. high\": \"377.4100\",\n" + "            \"3. low\": \"377.2800\",\n" + "            \"4. close\": \"377.3166\",\n" + "            \"5. volume\": \"2980\"\n" + "        },\n" + "        \"2019-07-19 14:51:00\": {\n" + "            \"1. open\": \"377.4300\",\n" + "            \"2. high\": \"377.4300\",\n" + "            \"3. low\": \"377.1500\",\n" + "            \"4. close\": \"377.2610\",\n" + "            \"5. volume\": \"10228\"\n" + "        },\n" + "        \"2019-07-19 14:50:00\": {\n" + "            \"1. open\": \"377.5000\",\n" + "            \"2. high\": \"377.6400\",\n" + "            \"3. low\": \"377.3900\",\n" + "            \"4. close\": \"377.4691\",\n" + "            \"5. volume\": \"9635\"\n" + "        },\n" + "        \"2019-07-19 14:49:00\": {\n" + "            \"1. open\": \"377.2300\",\n" + "            \"2. high\": \"377.6200\",\n" + "            \"3. low\": \"377.2300\",\n" + "            \"4. close\": \"377.5631\",\n" + "            \"5. volume\": \"14376\"\n" + "        },\n" + "        \"2019-07-19 14:48:00\": {\n" + "            \"1. open\": \"377.0000\",\n" + "            \"2. high\": \"377.2299\",\n" + "            \"3. low\": \"377.0000\",\n" + "            \"4. close\": \"377.2299\",\n" + "            \"5. volume\": \"3262\"\n" + "        },\n" + "        \"2019-07-19 14:47:00\": {\n" + "            \"1. open\": \"377.2147\",\n" + "            \"2. high\": \"377.2250\",\n" + "            \"3. low\": \"376.9300\",\n" + "            \"4. close\": \"376.9586\",\n" + "            \"5. volume\": \"8884\"\n" + "        },\n" + "        \"2019-07-19 14:46:00\": {\n" + "            \"1. open\": \"377.2900\",\n" + "            \"2. high\": \"377.3020\",\n" + "            \"3. low\": \"377.0000\",\n" + "            \"4. close\": \"377.1700\",\n" + "            \"5. volume\": \"7538\"\n" + "        },\n" + "        \"2019-07-19 14:45:00\": {\n" + "            \"1. open\": \"377.3400\",\n" + "            \"2. high\": \"377.4100\",\n" + "            \"3. low\": \"377.2300\",\n" + "            \"4. close\": \"377.3700\",\n" + "            \"5. volume\": \"13640\"\n" + "        },\n" + "        \"2019-07-19 14:44:00\": {\n" + "            \"1. open\": \"377.2052\",\n" + "            \"2. high\": \"377.3200\",\n" + "            \"3. low\": \"377.2052\",\n" + "            \"4. close\": \"377.3100\",\n" + "            \"5. volume\": \"4617\"\n" + "        },\n" + "        \"2019-07-19 14:43:00\": {\n" + "            \"1. open\": \"377.2540\",\n" + "            \"2. high\": \"377.3200\",\n" + "            \"3. low\": \"377.1000\",\n" + "            \"4. close\": \"377.2000\",\n" + "            \"5. volume\": \"15096\"\n" + "        },\n" + "        \"2019-07-19 14:42:00\": {\n" + "            \"1. open\": \"377.2200\",\n" + "            \"2. high\": \"377.3100\",\n" + "            \"3. low\": \"377.0700\",\n" + "            \"4. close\": \"377.2400\",\n" + "            \"5. volume\": \"8000\"\n" + "        },\n" + "        \"2019-07-19 14:41:00\": {\n" + "            \"1. open\": \"377.0100\",\n" + "            \"2. high\": \"377.2900\",\n" + "            \"3. low\": \"376.8400\",\n" + "            \"4. close\": \"377.2900\",\n" + "            \"5. volume\": \"15612\"\n" + "        },\n" + "        \"2019-07-19 14:40:00\": {\n" + "            \"1. open\": \"376.7800\",\n" + "            \"2. high\": \"377.0000\",\n" + "            \"3. low\": \"376.7489\",\n" + "            \"4. close\": \"376.9650\",\n" + "            \"5. volume\": \"16534\"\n" + "        },\n" + "        \"2019-07-19 14:39:00\": {\n" + "            \"1. open\": \"376.8950\",\n" + "            \"2. high\": \"376.9600\",\n" + "            \"3. low\": \"376.7100\",\n" + "            \"4. close\": \"376.8550\",\n" + "            \"5. volume\": \"8459\"\n" + "        },\n" + "        \"2019-07-19 14:38:00\": {\n" + "            \"1. open\": \"376.8826\",\n" + "            \"2. high\": \"377.0400\",\n" + "            \"3. low\": \"376.7600\",\n" + "            \"4. close\": \"376.9300\",\n" + "            \"5. volume\": \"17629\"\n" + "        },\n" + "        \"2019-07-19 14:37:00\": {\n" + "            \"1. open\": \"376.6650\",\n" + "            \"2. high\": \"376.9000\",\n" + "            \"3. low\": \"376.6300\",\n" + "            \"4. close\": \"376.8767\",\n" + "            \"5. volume\": \"14644\"\n" + "        },\n" + "        \"2019-07-19 14:36:00\": {\n" + "            \"1. open\": \"376.5900\",\n" + "            \"2. high\": \"376.7800\",\n" + "            \"3. low\": \"376.5900\",\n" + "            \"4. close\": \"376.7800\",\n" + "            \"5. volume\": \"4806\"\n" + "        },\n" + "        \"2019-07-19 14:35:00\": {\n" + "            \"1. open\": \"376.7300\",\n" + "            \"2. high\": \"376.9600\",\n" + "            \"3. low\": \"376.6000\",\n" + "            \"4. close\": \"376.6100\",\n" + "            \"5. volume\": \"7925\"\n" + "        },\n" + "        \"2019-07-19 14:34:00\": {\n" + "            \"1. open\": \"376.9700\",\n" + "            \"2. high\": \"376.9800\",\n" + "            \"3. low\": \"376.7525\",\n" + "            \"4. close\": \"376.8300\",\n" + "            \"5. volume\": \"8908\"\n" + "        },\n" + "        \"2019-07-19 14:33:00\": {\n" + "            \"1. open\": \"377.1900\",\n" + "            \"2. high\": \"377.2199\",\n" + "            \"3. low\": \"376.9400\",\n" + "            \"4. close\": \"376.9800\",\n" + "            \"5. volume\": \"9438\"\n" + "        },\n" + "        \"2019-07-19 14:32:00\": {\n" + "            \"1. open\": \"377.1200\",\n" + "            \"2. high\": \"377.2800\",\n" + "            \"3. low\": \"377.1200\",\n" + "            \"4. close\": \"377.1900\",\n" + "            \"5. volume\": \"13545\"\n" + "        },\n" + "        \"2019-07-19 14:31:00\": {\n" + "            \"1. open\": \"377.0000\",\n" + "            \"2. high\": \"377.1300\",\n" + "            \"3. low\": \"377.0000\",\n" + "            \"4. close\": \"377.1200\",\n" + "            \"5. volume\": \"5151\"\n" + "        },\n" + "        \"2019-07-19 14:30:00\": {\n" + "            \"1. open\": \"376.7600\",\n" + "            \"2. high\": \"377.0700\",\n" + "            \"3. low\": \"376.7600\",\n" + "            \"4. close\": \"376.9900\",\n" + "            \"5. volume\": \"9551\"\n" + "        },\n" + "        \"2019-07-19 14:29:00\": {\n" + "            \"1. open\": \"376.7700\",\n" + "            \"2. high\": \"376.7700\",\n" + "            \"3. low\": \"376.6431\",\n" + "            \"4. close\": \"376.7600\",\n" + "            \"5. volume\": \"10285\"\n" + "        },\n" + "        \"2019-07-19 14:28:00\": {\n" + "            \"1. open\": \"376.7838\",\n" + "            \"2. high\": \"377.0000\",\n" + "            \"3. low\": \"376.7700\",\n" + "            \"4. close\": \"376.8300\",\n" + "            \"5. volume\": \"14876\"\n" + "        },\n" + "        \"2019-07-19 14:27:00\": {\n" + "            \"1. open\": \"376.3700\",\n" + "            \"2. high\": \"376.8700\",\n" + "            \"3. low\": \"376.3700\",\n" + "            \"4. close\": \"376.8000\",\n" + "            \"5. volume\": \"10582\"\n" + "        },\n" + "        \"2019-07-19 14:26:00\": {\n" + "            \"1. open\": \"376.3500\",\n" + "            \"2. high\": \"376.5700\",\n" + "            \"3. low\": \"376.3195\",\n" + "            \"4. close\": \"376.3195\",\n" + "            \"5. volume\": \"9846\"\n" + "        },\n" + "        \"2019-07-19 14:25:00\": {\n" + "            \"1. open\": \"376.4450\",\n" + "            \"2. high\": \"376.5900\",\n" + "            \"3. low\": \"376.3500\",\n" + "            \"4. close\": \"376.3500\",\n" + "            \"5. volume\": \"3075\"\n" + "        },\n" + "        \"2019-07-19 14:24:00\": {\n" + "            \"1. open\": \"376.7600\",\n" + "            \"2. high\": \"376.7600\",\n" + "            \"3. low\": \"376.4700\",\n" + "            \"4. close\": \"376.5900\",\n" + "            \"5. volume\": \"9001\"\n" + "        },\n" + "        \"2019-07-19 14:23:00\": {\n" + "            \"1. open\": \"376.5700\",\n" + "            \"2. high\": \"376.8100\",\n" + "            \"3. low\": \"376.4900\",\n" + "            \"4. close\": \"376.6800\",\n" + "            \"5. volume\": \"16066\"\n" + "        },\n" + "        \"2019-07-19 14:22:00\": {\n" + "            \"1. open\": \"376.7100\",\n" + "            \"2. high\": \"376.8200\",\n" + "            \"3. low\": \"376.5600\",\n" + "            \"4. close\": \"376.6438\",\n" + "            \"5. volume\": \"8441\"\n" + "        },\n" + "        \"2019-07-19 14:21:00\": {\n" + "            \"1. open\": \"376.5500\",\n" + "            \"2. high\": \"376.6900\",\n" + "            \"3. low\": \"376.4200\",\n" + "            \"4. close\": \"376.6900\",\n" + "            \"5. volume\": \"10819\"\n" + "        }\n" + "    }\n" + "}\n";
}
//
// {
//    "Meta Data": {
//        "1. Information": "Daily Prices (open, high, low, close) and Volumes",
//        "2. Symbol": "MSFT",
//        "3. Last Refreshed": "2019-07-01",
//        "4. Output Size": "Compact",
//        "5. Time Zone": "US/Eastern"
//    },
//    "Time Series (Daily)": {
//        "2019-07-01": {
//            "1. open": "136.6300",
//            "2. high": "136.7000",
//            "3. low": "134.9700",
//            "4. close": "135.6800",
//            "5. volume": "22606027"
//        },
//        "2019-06-28": {
//            "1. open": "134.5700",
//            "2. high": "134.6000",
//            "3. low": "133.1558",
//            "4. close": "133.9600",
//            "5. volume": "30042969"
//        },
//        "2019-06-27": {
//            "1. open": "134.1400",
//            "2. high": "134.7100",
//            "3. low": "133.5100",
//            "4. close": "134.1500",
//            "5. volume": "16557482"
//        },
//        "2019-06-26": {
//            "1. open": "134.3500",
//            "2. high": "135.7400",
//            "3. low": "133.6000",
//            "4. close": "133.9300",
//            "5. volume": "23657745"
//        },
//        "2019-06-25": {
//            "1. open": "137.2500",
//            "2. high": "137.5900",
//            "3. low": "132.7300",
//            "4. close": "133.4300",
//            "5. volume": "33327420"
//        },
//        "2019-06-24": {
//            "1. open": "137.0000",
//            "2. high": "138.4000",
//            "3. low": "137.0000",
//            "4. close": "137.7800",
//            "5. volume": "20628841"
//        },
//        "2019-06-21": {
//            "1. open": "136.5800",
//            "2. high": "137.7300",
//            "3. low": "136.4600",
//            "4. close": "136.9700",
//            "5. volume": "36727892"
//        },
//        "2019-06-20": {
//            "1. open": "137.4500",
//            "2. high": "137.6600",
//            "3. low": "135.7200",
//            "4. close": "136.9500",
//            "5. volume": "33042592"
//        },
//        "2019-06-19": {
//            "1. open": "135.0000",
//            "2. high": "135.9300",
//            "3. low": "133.8100",
//            "4. close": "135.6900",
//            "5. volume": "23744441"
//        },
//        "2019-06-18": {
//            "1. open": "134.1900",
//            "2. high": "135.2400",
//            "3. low": "133.5700",
//            "4. close": "135.1600",
//            "5. volume": "25934458"
//        },
//        "2019-06-17": {
//            "1. open": "132.6300",
//            "2. high": "133.7300",
//            "3. low": "132.5300",
//            "4. close": "132.8500",
//            "5. volume": "14517785"
//        },
//        "2019-06-14": {
//            "1. open": "132.2600",
//            "2. high": "133.7900",
//            "3. low": "131.6400",
//            "4. close": "132.4500",
//            "5. volume": "17821703"
//        },
//        "2019-06-13": {
//            "1. open": "131.9800",
//            "2. high": "132.6700",
//            "3. low": "131.5600",
//            "4. close": "132.3200",
//            "5. volume": "17200848"
//        },
//        "2019-06-12": {
//            "1. open": "131.4000",
//            "2. high": "131.9700",
//            "3. low": "130.7100",
//            "4. close": "131.4900",
//            "5. volume": "17092464"
//        },
//        "2019-06-11": {
//            "1. open": "133.8800",
//            "2. high": "134.2400",
//            "3. low": "131.2757",
//            "4. close": "132.1000",
//            "5. volume": "23913731"
//        },
//        "2019-06-10": {
//            "1. open": "132.4000",
//            "2. high": "134.0800",
//            "3. low": "132.0000",
//            "4. close": "132.6000",
//            "5. volume": "26477098"
//        },
//        "2019-06-07": {
//            "1. open": "129.1900",
//            "2. high": "132.2500",
//            "3. low": "128.2600",
//            "4. close": "131.4000",
//            "5. volume": "33885588"
//        },
//        "2019-06-06": {
//            "1. open": "126.4400",
//            "2. high": "127.9700",
//            "3. low": "125.6000",
//            "4. close": "127.8200",
//            "5. volume": "21458961"
//        },
//        "2019-06-05": {
//            "1. open": "124.9500",
//            "2. high": "125.8700",
//            "3. low": "124.2100",
//            "4. close": "125.8300",
//            "5. volume": "24926140"
//        },
//        "2019-06-04": {
//            "1. open": "121.2800",
//            "2. high": "123.2800",
//            "3. low": "120.6522",
//            "4. close": "123.1600",
//            "5. volume": "29382642"
//        },
//        "2019-06-03": {
//            "1. open": "123.8500",
//            "2. high": "124.3700",
//            "3. low": "119.0100",
//            "4. close": "119.8400",
//            "5. volume": "37983637"
//        },
//        "2019-05-31": {
//            "1. open": "124.2300",
//            "2. high": "124.6150",
//            "3. low": "123.3200",
//            "4. close": "123.6800",
//            "5. volume": "26646769"
//        },
//        "2019-05-30": {
//            "1. open": "125.2600",
//            "2. high": "125.7600",
//            "3. low": "124.7800",
//            "4. close": "125.7300",
//            "5. volume": "16829613"
//        },
//        "2019-05-29": {
//            "1. open": "125.3800",
//            "2. high": "125.3900",
//            "3. low": "124.0400",
//            "4. close": "124.9400",
//            "5. volume": "22763140"
//        },
//        "2019-05-28": {
//            "1. open": "126.9800",
//            "2. high": "128.0000",
//            "3. low": "126.0500",
//            "4. close": "126.1600",
//            "5. volume": "23128359"
//        },
//        "2019-05-24": {
//            "1. open": "126.9100",
//            "2. high": "127.4150",
//            "3. low": "125.9700",
//            "4. close": "126.2400",
//            "5. volume": "14123358"
//        },
//        "2019-05-23": {
//            "1. open": "126.2000",
//            "2. high": "126.2900",
//            "3. low": "124.7400",
//            "4. close": "126.1800",
//            "5. volume": "23603810"
//        },
//        "2019-05-22": {
//            "1. open": "126.6200",
//            "2. high": "128.2400",
//            "3. low": "126.5200",
//            "4. close": "127.6700",
//            "5. volume": "15396485"
//        },
//        "2019-05-21": {
//            "1. open": "127.4300",
//            "2. high": "127.5273",
//            "3. low": "126.5800",
//            "4. close": "126.9000",
//            "5. volume": "15293260"
//        },
//        "2019-05-20": {
//            "1. open": "126.5200",
//            "2. high": "127.5894",
//            "3. low": "125.7607",
//            "4. close": "126.2200",
//            "5. volume": "23706934"
//        },
//        "2019-05-17": {
//            "1. open": "128.3050",
//            "2. high": "130.4600",
//            "3. low": "127.9200",
//            "4. close": "128.0700",
//            "5. volume": "25770539"
//        },
//        "2019-05-16": {
//            "1. open": "126.7500",
//            "2. high": "129.3800",
//            "3. low": "126.4600",
//            "4. close": "128.9300",
//            "5. volume": "30112216"
//        },
//        "2019-05-15": {
//            "1. open": "124.2600",
//            "2. high": "126.7100",
//            "3. low": "123.7000",
//            "4. close": "126.0200",
//            "5. volume": "24722708"
//        },
//        "2019-05-14": {
//            "1. open": "123.8700",
//            "2. high": "125.8800",
//            "3. low": "123.7000",
//            "4. close": "124.7300",
//            "5. volume": "25266315"
//        },
//        "2019-05-13": {
//            "1. open": "124.1100",
//            "2. high": "125.5500",
//            "3. low": "123.0400",
//            "4. close": "123.3500",
//            "5. volume": "33944923"
//        },
//        "2019-05-10": {
//            "1. open": "124.9100",
//            "2. high": "127.9300",
//            "3. low": "123.8200",
//            "4. close": "127.1300",
//            "5. volume": "30915084"
//        },
//        "2019-05-09": {
//            "1. open": "124.2900",
//            "2. high": "125.7900",
//            "3. low": "123.5700",
//            "4. close": "125.5000",
//            "5. volume": "27235835"
//        },
//        "2019-05-08": {
//            "1. open": "125.4400",
//            "2. high": "126.3700",
//            "3. low": "124.7500",
//            "4. close": "125.5100",
//            "5. volume": "28418996"
//        },
//        "2019-05-07": {
//            "1. open": "126.4600",
//            "2. high": "127.1800",
//            "3. low": "124.2200",
//            "4. close": "125.5200",
//            "5. volume": "36017661"
//        },
//        "2019-05-06": {
//            "1. open": "126.3900",
//            "2. high": "128.5600",
//            "3. low": "126.1100",
//            "4. close": "128.1500",
//            "5. volume": "24239464"
//        },
//        "2019-05-03": {
//            "1. open": "127.3600",
//            "2. high": "129.4300",
//            "3. low": "127.2500",
//            "4. close": "128.9000",
//            "5. volume": "24911126"
//        },
//        "2019-05-02": {
//            "1. open": "127.9800",
//            "2. high": "128.0000",
//            "3. low": "125.5200",
//            "4. close": "126.2100",
//            "5. volume": "27350161"
//        },
//        "2019-05-01": {
//            "1. open": "130.5300",
//            "2. high": "130.6500",
//            "3. low": "127.7000",
//            "4. close": "127.8800",
//            "5. volume": "26821692"
//        },
//        "2019-04-30": {
//            "1. open": "129.8100",
//            "2. high": "130.7000",
//            "3. low": "129.3947",
//            "4. close": "130.6000",
//            "5. volume": "24166503"
//        },
//        "2019-04-29": {
//            "1. open": "129.9000",
//            "2. high": "130.1800",
//            "3. low": "129.3500",
//            "4. close": "129.7700",
//            "5. volume": "16324183"
//        },
//        "2019-04-26": {
//            "1. open": "129.7000",
//            "2. high": "130.5152",
//            "3. low": "129.0200",
//            "4. close": "129.8900",
//            "5. volume": "23654933"
//        },
//        "2019-04-25": {
//            "1. open": "130.0600",
//            "2. high": "131.3700",
//            "3. low": "128.8300",
//            "4. close": "129.1500",
//            "5. volume": "38033892"
//        },
//        "2019-04-24": {
//            "1. open": "125.7900",
//            "2. high": "125.8500",
//            "3. low": "124.5200",
//            "4. close": "125.0100",
//            "5. volume": "31256980"
//        },
//        "2019-04-23": {
//            "1. open": "124.1000",
//            "2. high": "125.5800",
//            "3. low": "123.8300",
//            "4. close": "125.4400",
//            "5. volume": "24025521"
//        },
//        "2019-04-22": {
//            "1. open": "122.6200",
//            "2. high": "124.0000",
//            "3. low": "122.5700",
//            "4. close": "123.7600",
//            "5. volume": "15648724"
//        },
//        "2019-04-18": {
//            "1. open": "122.1900",
//            "2. high": "123.5200",
//            "3. low": "121.3018",
//            "4. close": "123.3700",
//            "5. volume": "27990998"
//        },
//        "2019-04-17": {
//            "1. open": "121.2400",
//            "2. high": "121.8500",
//            "3. low": "120.5400",
//            "4. close": "121.7700",
//            "5. volume": "19300938"
//        },
//        "2019-04-16": {
//            "1. open": "121.6400",
//            "2. high": "121.6500",
//            "3. low": "120.1000",
//            "4. close": "120.7700",
//            "5. volume": "14071787"
//        },
//        "2019-04-15": {
//            "1. open": "120.9400",
//            "2. high": "121.5800",
//            "3. low": "120.5700",
//            "4. close": "121.0500",
//            "5. volume": "15792572"
//        },
//        "2019-04-12": {
//            "1. open": "120.6400",
//            "2. high": "120.9800",
//            "3. low": "120.3700",
//            "4. close": "120.9500",
//            "5. volume": "19745143"
//        },
//        "2019-04-11": {
//            "1. open": "120.5400",
//            "2. high": "120.8500",
//            "3. low": "119.9200",
//            "4. close": "120.3300",
//            "5. volume": "14209121"
//        },
//        "2019-04-10": {
//            "1. open": "119.7600",
//            "2. high": "120.3500",
//            "3. low": "119.5400",
//            "4. close": "120.1900",
//            "5. volume": "16477169"
//        },
//        "2019-04-09": {
//            "1. open": "118.6300",
//            "2. high": "119.5400",
//            "3. low": "118.5800",
//            "4. close": "119.2800",
//            "5. volume": "17611981"
//        },
//        "2019-04-08": {
//            "1. open": "119.8100",
//            "2. high": "120.0200",
//            "3. low": "118.6400",
//            "4. close": "119.9300",
//            "5. volume": "15116186"
//        },
//        "2019-04-05": {
//            "1. open": "119.3900",
//            "2. high": "120.2300",
//            "3. low": "119.3700",
//            "4. close": "119.8900",
//            "5. volume": "15826245"
//        },
//        "2019-04-04": {
//            "1. open": "120.1000",
//            "2. high": "120.2300",
//            "3. low": "118.3800",
//            "4. close": "119.3600",
//            "5. volume": "20112848"
//        },
//        "2019-04-03": {
//            "1. open": "119.8600",
//            "2. high": "120.4300",
//            "3. low": "119.1500",
//            "4. close": "119.9700",
//            "5. volume": "22860744"
//        },
//        "2019-04-02": {
//            "1. open": "119.0600",
//            "2. high": "119.4800",
//            "3. low": "118.5200",
//            "4. close": "119.1900",
//            "5. volume": "18142297"
//        },
//        "2019-04-01": {
//            "1. open": "118.9500",
//            "2. high": "119.1085",
//            "3. low": "118.1000",
//            "4. close": "119.0200",
//            "5. volume": "22789103"
//        },
//        "2019-03-29": {
//            "1. open": "118.0700",
//            "2. high": "118.3200",
//            "3. low": "116.9600",
//            "4. close": "117.9400",
//            "5. volume": "25399752"
//        },
//        "2019-03-28": {
//            "1. open": "117.4400",
//            "2. high": "117.5800",
//            "3. low": "116.1300",
//            "4. close": "116.9300",
//            "5. volume": "18334755"
//        },
//        "2019-03-27": {
//            "1. open": "117.8750",
//            "2. high": "118.2100",
//            "3. low": "115.5215",
//            "4. close": "116.7700",
//            "5. volume": "22733427"
//        },
//        "2019-03-26": {
//            "1. open": "118.6200",
//            "2. high": "118.7050",
//            "3. low": "116.8500",
//            "4. close": "117.9100",
//            "5. volume": "26097665"
//        },
//        "2019-03-25": {
//            "1. open": "116.5600",
//            "2. high": "118.0100",
//            "3. low": "116.3224",
//            "4. close": "117.6600",
//            "5. volume": "27067117"
//        },
//        "2019-03-22": {
//            "1. open": "119.5000",
//            "2. high": "119.5900",
//            "3. low": "117.0400",
//            "4. close": "117.0500",
//            "5. volume": "33624528"
//        },
//        "2019-03-21": {
//            "1. open": "117.1350",
//            "2. high": "120.8200",
//            "3. low": "117.0900",
//            "4. close": "120.2200",
//            "5. volume": "29854446"
//        },
//        "2019-03-20": {
//            "1. open": "117.3900",
//            "2. high": "118.7500",
//            "3. low": "116.7100",
//            "4. close": "117.5200",
//            "5. volume": "28113343"
//        },
//        "2019-03-19": {
//            "1. open": "118.0900",
//            "2. high": "118.4400",
//            "3. low": "116.9900",
//            "4. close": "117.6500",
//            "5. volume": "37588697"
//        },
//        "2019-03-18": {
//            "1. open": "116.1700",
//            "2. high": "117.6100",
//            "3. low": "116.0500",
//            "4. close": "117.5700",
//            "5. volume": "31207596"
//        },
//        "2019-03-15": {
//            "1. open": "115.3400",
//            "2. high": "117.2500",
//            "3. low": "114.5900",
//            "4. close": "115.9100",
//            "5. volume": "54630661"
//        },
//        "2019-03-14": {
//            "1. open": "114.5400",
//            "2. high": "115.2000",
//            "3. low": "114.3300",
//            "4. close": "114.5900",
//            "5. volume": "30763367"
//        },
//        "2019-03-13": {
//            "1. open": "114.1300",
//            "2. high": "115.0000",
//            "3. low": "113.7800",
//            "4. close": "114.5000",
//            "5. volume": "35513771"
//        },
//        "2019-03-12": {
//            "1. open": "112.8200",
//            "2. high": "113.9900",
//            "3. low": "112.6499",
//            "4. close": "113.6200",
//            "5. volume": "26132717"
//        },
//        "2019-03-11": {
//            "1. open": "110.9900",
//            "2. high": "112.9500",
//            "3. low": "110.9800",
//            "4. close": "112.8300",
//            "5. volume": "26491618"
//        },
//        "2019-03-08": {
//            "1. open": "109.1600",
//            "2. high": "110.7100",
//            "3. low": "108.8000",
//            "4. close": "110.5100",
//            "5. volume": "22818430"
//        },
//        "2019-03-07": {
//            "1. open": "111.4000",
//            "2. high": "111.5500",
//            "3. low": "109.8650",
//            "4. close": "110.3900",
//            "5. volume": "25338954"
//        },
//        "2019-03-06": {
//            "1. open": "111.8700",
//            "2. high": "112.6600",
//            "3. low": "111.4300",
//            "4. close": "111.7500",
//            "5. volume": "17686996"
//        },
//        "2019-03-05": {
//            "1. open": "112.2500",
//            "2. high": "112.3900",
//            "3. low": "111.2300",
//            "4. close": "111.7000",
//            "5. volume": "19538318"
//        },
//        "2019-03-04": {
//            "1. open": "113.0200",
//            "2. high": "113.2500",
//            "3. low": "110.8000",
//            "4. close": "112.2600",
//            "5. volume": "26608014"
//        },
//        "2019-03-01": {
//            "1. open": "112.8900",
//            "2. high": "113.0200",
//            "3. low": "111.6650",
//            "4. close": "112.5300",
//            "5. volume": "23501169"
//        },
//        "2019-02-28": {
//            "1. open": "112.0400",
//            "2. high": "112.8800",
//            "3. low": "111.7300",
//            "4. close": "112.0300",
//            "5. volume": "29083934"
//        },
//        "2019-02-27": {
//            "1. open": "111.6900",
//            "2. high": "112.3600",
//            "3. low": "110.8800",
//            "4. close": "112.1700",
//            "5. volume": "21487062"
//        },
//        "2019-02-26": {
//            "1. open": "111.2600",
//            "2. high": "113.2400",
//            "3. low": "111.1700",
//            "4. close": "112.3600",
//            "5. volume": "21536733"
//        },
//        "2019-02-25": {
//            "1. open": "111.7600",
//            "2. high": "112.1800",
//            "3. low": "111.2600",
//            "4. close": "111.5900",
//            "5. volume": "23750599"
//        },
//        "2019-02-22": {
//            "1. open": "110.0500",
//            "2. high": "111.2000",
//            "3. low": "109.8200",
//            "4. close": "110.9700",
//            "5. volume": "27763218"
//        },
//        "2019-02-21": {
//            "1. open": "106.9000",
//            "2. high": "109.4800",
//            "3. low": "106.8700",
//            "4. close": "109.4100",
//            "5. volume": "29063231"
//        },
//        "2019-02-20": {
//            "1. open": "107.8600",
//            "2. high": "107.9400",
//            "3. low": "106.2900",
//            "4. close": "107.1500",
//            "5. volume": "21607671"
//        },
//        "2019-02-19": {
//            "1. open": "107.7900",
//            "2. high": "108.6600",
//            "3. low": "107.7800",
//            "4. close": "108.1700",
//            "5. volume": "18038460"
//        },
//        "2019-02-15": {
//            "1. open": "107.9100",
//            "2. high": "108.3000",
//            "3. low": "107.3624",
//            "4. close": "108.2200",
//            "5. volume": "26606886"
//        },
//        "2019-02-14": {
//            "1. open": "106.3100",
//            "2. high": "107.2900",
//            "3. low": "105.6600",
//            "4. close": "106.9000",
//            "5. volume": "21784703"
//        },
//        "2019-02-13": {
//            "1. open": "107.5000",
//            "2. high": "107.7800",
//            "3. low": "106.7100",
//            "4. close": "106.8100",
//            "5. volume": "18394869"
//        },
//        "2019-02-12": {
//            "1. open": "106.1400",
//            "2. high": "107.1400",
//            "3. low": "105.4800",
//            "4. close": "106.8900",
//            "5. volume": "25056595"
//        },
//        "2019-02-11": {
//            "1. open": "106.2000",
//            "2. high": "106.5800",
//            "3. low": "104.9650",
//            "4. close": "105.2500",
//            "5. volume": "18914123"
//        },
//        "2019-02-08": {
//            "1. open": "104.3900",
//            "2. high": "105.7800",
//            "3. low": "104.2603",
//            "4. close": "105.6700",
//            "5. volume": "21461093"
//        },
//        "2019-02-07": {
//            "1. open": "105.1850",
//            "2. high": "105.5900",
//            "3. low": "104.2900",
//            "4. close": "105.2700",
//            "5. volume": "29760697"
//        }
//    }
//}