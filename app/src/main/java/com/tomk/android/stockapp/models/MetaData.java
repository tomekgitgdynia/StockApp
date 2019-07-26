package com.tomk.android.stockapp.models;

import java.util.Date;

public class MetaData {


    private String information = null;
    private String symbol = null;
    private String name = null;
    private Date lastRefreshedDate = null;
    private Integer interval = null;
    private String outputSize = null;
    private String timeZone = null;

    public MetaData()
    {

    }
    public MetaData(String information, String symbol, String name, Date lastRefreshedDate, Integer interval, String outputSize, String timeZone) {
        this.information = information;
        this.symbol = symbol;
        this.name = name;
        this.lastRefreshedDate = lastRefreshedDate;
        this.interval = interval;
        this.outputSize = outputSize;
        this.timeZone = timeZone;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Date getLastRefreshedDate() {
        return lastRefreshedDate;
    }

    public void setLastRefreshedDate(Date lastRefreshedDate) {
        this.lastRefreshedDate = lastRefreshedDate;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public String getOutputSize() {
        return outputSize;
    }

    public void setOutputSize(String outputSize) {
        this.outputSize = outputSize;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
