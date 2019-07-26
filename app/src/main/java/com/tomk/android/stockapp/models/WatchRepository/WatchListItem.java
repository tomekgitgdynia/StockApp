package com.tomk.android.stockapp.models.WatchRepository;

/**
 * Created by Tom Kowszun.
 */

public class WatchListItem {

    // Watch List Table
    private String watchListName;
    private String watchListDescription;
    private String value;
    private String valueChange;
    private String valueChangePercent;
    private String valueDate;
    private String valueTime;

    public WatchListItem() {
        watchListName = "";
        watchListDescription = "";
    }

    public WatchListItem(String watchListName, String watchListDescription, String value, String valueChange, String valueChangePercent,
    String valueDate, String valueTime) {
        this.watchListName = watchListName;
        this.watchListDescription = watchListDescription;
        this.value = value;
        this.valueChange = valueChange;
        this.valueChangePercent = valueChangePercent;
        this.valueDate = valueDate;
        this.valueTime = valueTime;
    }


    public String getWatchListName() {
        return watchListName;
    }

    public void setWatchListName(String watchListName) {
        this.watchListName = watchListName;
    }

    public String getWatchListDescription() {
        return watchListDescription;
    }

    public void setWatchListDescription(String watchListDescription) {
        this.watchListDescription = watchListDescription;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueChange() {
        return valueChange;
    }

    public void setValueChange(String valueChange) {
        this.valueChange = valueChange;
    }

    public String getValueChangePercent() {
        return valueChangePercent;
    }

    public void setValueChangePercent(String valueChangePercent) {
        this.valueChangePercent = valueChangePercent;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    public String getValueTime() {
        return valueTime;
    }

    public void setValueTime(String valueTime) {
        this.valueTime = valueTime;
    }
}
