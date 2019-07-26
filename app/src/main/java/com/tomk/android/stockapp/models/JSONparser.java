package com.tomk.android.stockapp.models;

import android.util.Log;

import com.tomk.android.stockapp.mainActivity.MainStocksActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * Copyright Tom Kowszun 2017.
 */

public class JSONparser {

    private static final String TAG = "JSONparser";

    public static boolean getStockResponse(String stockSymbol, String stockName, TreeMap<String, String> rawData, StockResponse stockResponse, ArrayList<String> typeList) throws ParseException {

        // Basic validation
        if (rawData == null || rawData.size() < 1 || !rawData.get(MainStocksActivity.T_STANDARD).contains("Meta Data")) {
            return false;
        }

        // Get metadata from the Standard data return
        JSONObject jObj = null;
        try {
            jObj = new JSONObject(rawData.get(MainStocksActivity.T_STANDARD));
            JSONObject metaDataJsonObj = getObject("Meta Data", jObj);
            MetaData md = getMetaData(stockSymbol, stockName, metaDataJsonObj);
            stockResponse.setMetaData(md);

            String interval = (String) metaDataJsonObj.get("4. Interval");
            String timeSeriesKey = "Time Series " + "(" + interval + ")";
            JSONObject timeSeries = getObject(timeSeriesKey, jObj);
            packTimeSeriesData(timeSeries, interval, stockResponse);
        } catch (JSONException e) {
            return false;
        }


        // This is the technical indicator data iteration loop
        Set mapKeys = rawData.keySet();
        for (Iterator rawKey = mapKeys.iterator(); rawKey.hasNext(); ) {
            String key = rawKey.next().toString();
            if (!key.equals(MainStocksActivity.T_STANDARD)) {
                String rawString = rawData.get(key);
                JSONObject techObj = null;
                try {
                    techObj = new JSONObject(rawString);
                } catch (JSONException e) {
                    return false;
                }
                if(! packTechData(techObj, "Technical Analysis: " + key, stockResponse))
                {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean getWatchListResponse(String stockSymbol, String stockName, TreeMap<String, String> rawData, WatchListResponse watchListResponse, ArrayList<String> typeList) throws ParseException {

        // Basic validation
        if (rawData == null || rawData.size() < 1 || !rawData.get(MainStocksActivity.T_STANDARD).contains("Meta Data")) {
            return false;
        }

        JSONObject jObj = null;
        try {
            jObj = new JSONObject(rawData.get(MainStocksActivity.T_STANDARD));
            JSONObject metaDataJsonObj = getObject("Meta Data", jObj);
            MetaData md = getMetaData(stockSymbol, stockName, metaDataJsonObj);
            watchListResponse.setRefreshDate(md.getLastRefreshedDate().toString());
            watchListResponse.setSymbol(md.getSymbol());
            String intervalString = null;
            if(metaDataJsonObj.getString("1. Information").contains("Daily Prices"))
            {
                intervalString = "Daily";
            } else
            {
                intervalString = metaDataJsonObj.getString("4. Interval").replaceAll("min", "");
            }


            String timeSeriesKey = "Time Series " + "(" + intervalString + ")";
            JSONObject timeSeries = getObject(timeSeriesKey, jObj);
            packWatchListData(timeSeries, intervalString, watchListResponse);
        } catch (JSONException e) {
            return false;
        }

        return true;
    }


    public static boolean packTechData(JSONObject techDataObj, String indicatorName, StockResponse stockResponse) {


        boolean parsingStatus = true;
        String indicator = null;
        ArrayList<Double> indicatorData = new ArrayList<>();

        JSONObject metaDataJsonObj = null;
        JSONObject technicalAnalysisJsonObj = null;
        SimpleDateFormat fmt = null;

        try {
            metaDataJsonObj = getObject("Meta Data", techDataObj);
            indicator = (String) metaDataJsonObj.get("2: Indicator");
            technicalAnalysisJsonObj = getObject(indicatorName, techDataObj);
        } catch (JSONException e) {
            parsingStatus = false;
        }
        if (!parsingStatus) return parsingStatus;

        try {
            Iterator<String> indicatorKeys = technicalAnalysisJsonObj.keys();
            int cnt = 0;
            for (Iterator<String> technicalndicatorIterator = indicatorKeys; technicalndicatorIterator.hasNext(); ) {
                cnt++;
                if (cnt > 100) break;
                String techIndicatorKey = technicalndicatorIterator.next();
                JSONObject technicalAnalysisJsonObjectItem = null;

                technicalAnalysisJsonObjectItem = new JSONObject(technicalAnalysisJsonObj.get(techIndicatorKey).toString());
                Iterator<String> dataKeys = technicalAnalysisJsonObjectItem.keys();
                for (Iterator<String> dataIterator = dataKeys; dataIterator.hasNext(); ) {

                    String dataKey = dataIterator.next();
                    Double data = null;

                    data = Double.parseDouble((String) technicalAnalysisJsonObjectItem.get(dataKey));
                    indicatorData.add(0, data);

//                Log.d(TAG, " ------------XXXXXXXXXXX-----> adding " +  data + " at " + techIndicatorKey);
                }
            }
        } catch (JSONException e) {
            parsingStatus = false;
        }
        if (!parsingStatus) return parsingStatus;

        if (indicatorData.size() > 1) {
            if (stockResponse.getIndicators() == null) {
                ArrayList<ArrayList<Double>> indicators = new ArrayList<>();
                indicators.add(indicatorData);
                stockResponse.setIndicators(indicators);
            } else {
                stockResponse.getIndicators().add(indicatorData);
            }
            if (stockResponse.getIndicatorNameList() != null) {
                stockResponse.getIndicatorNameList().add(indicator);
            } else {
                stockResponse.setIndicatorNameList(new ArrayList<String>());
                stockResponse.getIndicatorNameList().add(indicator);
            }

        }

        return parsingStatus;
    }

    private static MetaData getMetaData(String stockSymbol, String stockName, JSONObject metaDataJsonObj) throws JSONException, ParseException {

        // ------------------- Packing Meta Data from the standard
        MetaData md = null;
        if (metaDataJsonObj == null) {
            Log.d(TAG, "metaDataJsonObj is null");
            return null;
        } else {
            Date lastRefreshDate = null;

            String lastRefreshString = metaDataJsonObj.getString("3. Last Refreshed");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            lastRefreshDate = fmt.parse(lastRefreshString);
            String intervalString = null;
            Integer intervalInteger = null;
            if(metaDataJsonObj.getString("1. Information").contains("Daily Prices"))
            {
                intervalString = "0";
                intervalInteger = Integer.parseInt(intervalString);
                md = new MetaData(metaDataJsonObj.getString("1. Information"),
                        metaDataJsonObj.getString("2. Symbol"),
                        stockName,
                        lastRefreshDate, intervalInteger,
                        metaDataJsonObj.getString("4. Output Size"),
                        metaDataJsonObj.getString("5. Time Zone"));
            } else
            {
                intervalString = metaDataJsonObj.getString("4. Interval").replaceAll("min", "");
                intervalInteger = Integer.parseInt(intervalString);
                md = new MetaData(metaDataJsonObj.getString("1. Information"),
                        metaDataJsonObj.getString("2. Symbol"),
                        stockName,
                        lastRefreshDate, intervalInteger,
                        metaDataJsonObj.getString("5. Output Size"),
                        metaDataJsonObj.getString("6. Time Zone"));
            }
            // Make sure that the stock symbol in the returned meta data is the same as the requested stock symbol
            String returnedStockSymbol = md.getSymbol();
            if(!returnedStockSymbol.equals(stockSymbol))
            {
               return null;
            } else
            {
                md.setName(stockName);
            }

            // if the api returnes the stock name there would be the check here.  If not, the stock name requestd will be packed into meta data object


        }
        return md;
    }

    public static void packTimeSeriesData(JSONObject timeSeries, String intervalStringWithUnit, StockResponse stockResponse) throws JSONException, ParseException {

        Iterator<String> tsKeys = timeSeries.keys();
        ArrayList<TimeSeriesItem> timeSeriesItems = new ArrayList<>();
        Date intervalDate = null;
        int cnt = 0;

        String intervalString = intervalStringWithUnit.replaceAll("min", "");
        Integer intervalInteger = Integer.parseInt(intervalString);
        for (Iterator<String> tsIterator = tsKeys; tsIterator.hasNext(); ) {
            if (cnt > 100) break;
            String tsKey = tsIterator.next();
            JSONObject timeSeriesJsonObjectItem = new JSONObject(timeSeries.get(tsKey).toString());

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            intervalDate = fmt.parse(tsKey);
            String date = fmt.format(new Date());
            cnt++;
//            Log.d(TAG, " ------------XXXXXXXXXXX-----> Time Series intervalDate " + cnt + " " + tsKey);

            Double open = Double.valueOf(timeSeriesJsonObjectItem.get("1. open").toString());
            Double close = Double.valueOf(timeSeriesJsonObjectItem.get("4. close").toString());
            Double high = Double.valueOf(timeSeriesJsonObjectItem.get("2. high").toString());
            Double low = Double.valueOf(timeSeriesJsonObjectItem.get("3. low").toString());
            Integer volume = Integer.valueOf(timeSeriesJsonObjectItem.getString("5. volume").toString());

            TimeSeriesItem timeSeriesItem = new TimeSeriesItem(intervalDate, intervalInteger, open, close, high, low, volume);
            timeSeriesItems.add(0, timeSeriesItem);
        }

        stockResponse.setTimeSeriesItems(timeSeriesItems);

    }

    public static void packWatchListData(JSONObject timeSeries, String intervalStringWithUnit, WatchListResponse watchListResponse) throws JSONException, ParseException {

        Iterator<String> tsKeys = timeSeries.keys();
        ArrayList<TimeSeriesItem> timeSeriesItems = new ArrayList<>();
        Date intervalDate = null;
        int cnt = 0;

        // We need only one item
        for (Iterator<String> tsIterator = tsKeys; tsIterator.hasNext(); ) {
            if (cnt > 1)
            {
                String tsKey = tsIterator.next();
                Log.d(TAG, " ------------XXXXXXXXXXX-----> Time Series intervalDate - more than one ! " + cnt + " " + tsKey);
                break;
            }
            String tsKey = tsIterator.next();
            JSONObject timeSeriesJsonObjectItem = new JSONObject(timeSeries.get(tsKey).toString());

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            intervalDate = fmt.parse(tsKey);
            String date = fmt.format(new Date());
            cnt++;
//            Log.d(TAG, " ------------XXXXXXXXXXX-----> Time Series intervalDate " + cnt + " " + tsKey);

            Double open = Double.valueOf(timeSeriesJsonObjectItem.get("1. open").toString());
            Double close = Double.valueOf(timeSeriesJsonObjectItem.get("4. close").toString());
            Double high = Double.valueOf(timeSeriesJsonObjectItem.get("2. high").toString());
            Double low = Double.valueOf(timeSeriesJsonObjectItem.get("3. low").toString());
            Integer volume = Integer.valueOf(timeSeriesJsonObjectItem.getString("5. volume").toString());

            watchListResponse.setRefreshDate(date);
            watchListResponse.setOpen(open);
            watchListResponse.setClose(close);
            watchListResponse.setHigh(high);
            watchListResponse.setLow(low);
            watchListResponse.setVolume(volume);
        }
    }

    private static void findKey(JSONObject technicalAnalysisJsonObj, Date dateToFind) throws JSONException {
        Iterator<String> techKeys = technicalAnalysisJsonObj.keys();

        boolean foundDate = false;
        for (Iterator<String> techIterator = techKeys; techIterator.hasNext(); ) {
            if (foundDate) break;
            String techKey = techIterator.next();

            // dateToFind = Fri Jan 26 13:32:00 EST 2018
            String dateToFindString = dateToFind.toString();
            DateFormat techIndicatorFormat = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");

            // change to this format (seen in time series)
            DateFormat timeSeriesFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm");

            String convertedString = formatDateFromOnetoAnother(dateToFindString, "EEE MMM dd kk:mm:ss z yyyy", "yyyy-mm-dd HH:mm");


//            System.out.println(convertedString);

            //

//            Date techDate = Date.parse(techIterator.next());
            // techKey = 2018-11-26 15:12
            JSONObject techObjectItem = new JSONObject(technicalAnalysisJsonObj.get(techKey).toString());
            String item = techObjectItem.toString();
            if (convertedString.equals(techKey)) {
                Log.d(TAG, " ------------XXXXXXXXXXX-----> found date " + convertedString + " value " + item);
                foundDate = true;
            }
        }
    }

    public static String formatDateFromOnetoAnother(String date, String givenformat, String resultformat) {

        String result = "";
        SimpleDateFormat sdf;
        SimpleDateFormat sdf1;

        try {
            sdf = new SimpleDateFormat(givenformat);
            sdf1 = new SimpleDateFormat(resultformat);
            result = sdf1.format(sdf.parse(date));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            sdf = null;
            sdf1 = null;
        }
        return result;
    }

    private static JSONObject getObject(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getJSONObject(tagName);
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static float getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

    private static int getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }

}
