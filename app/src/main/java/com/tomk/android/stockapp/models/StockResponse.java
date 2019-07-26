package com.tomk.android.stockapp.models;


import java.util.ArrayList;

public class StockResponse
{
    private MetaData metaData;
    private ArrayList<TimeSeriesItem> timeSeriesItems;
    private ArrayList<ArrayList<Double>> indicators;
    private ArrayList<String> indicatorNameList;

    private String resultString;

    private double minOpen;
    private double maxOpen;

    private double minClose;
    private double maxClose;

    private double minLow;
    private double maxLow;

    private double minHigh;
    private double maxHigh;

    private int minVolume;
    private int maxVolume;

    private double startingOpen = Double.MIN_VALUE;
    private double endingClose = Double.MIN_VALUE;
    private int totalVolume = Integer.MIN_VALUE;

    public static final int GRAPH_LOW = 1;
    public static final int GRAPH_HIGH = 2;
    public static final int GRAPH_OPEN = 3;
    public static final int GRAPH_CLOSE = 4;
    public static final int GRAPH_VOLUME = 5;


    /**
     *
     */
    public StockResponse(MetaData metaData, ArrayList<TimeSeriesItem> timeSeriesItems) {
        super();
        this.metaData = metaData;
        this.timeSeriesItems = timeSeriesItems;
        indicators = new ArrayList<>();
        indicatorNameList = new ArrayList<>();
    }

    public StockResponse() {

    }


    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public ArrayList<TimeSeriesItem> getTimeSeriesItems() {
        return timeSeriesItems;
    }

    public void setTimeSeriesItems(ArrayList<TimeSeriesItem> timeSeriesItems) {
        this.timeSeriesItems = timeSeriesItems;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }

    public ArrayList<ArrayList<Double>> getIndicators() {
        return indicators;
    }

    public void setIndicators(ArrayList<ArrayList<Double>> indicators) {
        this.indicators = indicators;
    }

    public ArrayList<String> getIndicatorNameList() {
        return indicatorNameList;
    }

    public void setIndicatorNameList(ArrayList<String> indicatorNameList) {
        this.indicatorNameList = indicatorNameList;
    }

    public double getMinOpen() {
        return minOpen;
    }

    public void setMinOpen(double minOpen) {
        this.minOpen = minOpen;
    }

    public double getMaxOpen() {
        return maxOpen;
    }

    public void setMaxOpen(double maxOpen) {
        this.maxOpen = maxOpen;
    }

    public double getMinClose() {
        return minClose;
    }

    public void setMinClose(double minClose) {
        this.minClose = minClose;
    }

    public double getMaxClose() {
        return maxClose;
    }

    public void setMaxClose(double maxClose) {
        this.maxClose = maxClose;
    }

    public double getMinLow() {
        return minLow;
    }

    public void setMinLow(double minLow) {
        this.minLow = minLow;
    }

    public double getMaxLow() {
        return maxLow;
    }

    public void setMaxLow(double maxLow) {
        this.maxLow = maxLow;
    }

    public double getMinHigh() {
        return minHigh;
    }

    public void setMinHigh(double minHigh) {
        this.minHigh = minHigh;
    }

    public double getMaxHigh() {
        return maxHigh;
    }

    public void setMaxHigh(double maxHigh) {
        this.maxHigh = maxHigh;
    }

    public double getStartingOpen() {
        return startingOpen;
    }

    public void setStartingOpen(double startingOpen) {
        this.startingOpen = startingOpen;
    }

    public double getEndingClose() {
        return endingClose;
    }

    public void setEndingClose(double endingClose) {
        this.endingClose = endingClose;
    }

    public int getMinVolume() {
        return minVolume;
    }

    public void setMinVolume(int minVolume) {
        this.minVolume = minVolume;
    }

    public int getMaxVolume() {
        return maxVolume;
    }

    public void setMaxVolume(int maxVolume) {
        this.maxVolume = maxVolume;
    }

    public int getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(int totalVolume) {
        this.totalVolume = totalVolume;
    }
}