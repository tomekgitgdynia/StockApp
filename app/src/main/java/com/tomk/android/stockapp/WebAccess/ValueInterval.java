package com.tomk.android.stockapp.WebAccess;

/**
 *
 */
public class ValueInterval {

    private long date;
    private long dateOffset;
    private double open;
    private double close;
    private double high;
    private double low;
    private int volume;

    public ValueInterval(long date, long dateOffset, double open, double close, double high, double low, int volume) {

        this.date = date;
        this.dateOffset = dateOffset;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
    }

    public void copy(ValueInterval original) {

        this.setDate(original.getDate());
        this.setDateOffset(original.getDateOffset());
        this.setOpen(original.getOpen());
        this.setClose(original.getClose());
        this.setHigh(original.getHigh());
        this.setLow(original.getLow());
        this.setVolume(original.getVolume());

    }

    /**
     * @return the date
     */
    public long getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(long date) {
        this.date = date;
    }

    /**
     * @return the open
     */
    public double getOpen() {
        return open;
    }

    /**
     * @param open the open to set
     */
    public void setOpen(double open) {
        this.open = open;
    }

    /**
     * @return the close
     */
    public double getClose() {
        return close;
    }

    /**
     * @param close the close to set
     */
    public void setClose(double close) {
        this.close = close;
    }

    /**
     * @return the high
     */
    public double getHigh() {
        return high;
    }

    /**
     * @param high the high to set
     */
    public void setHigh(double high) {
        this.high = high;
    }

    /**
     * @return the low
     */
    public double getLow() {
        return low;
    }

    /**
     * @param low the low to set
     */
    public void setLow(double low) {
        this.low = low;
    }

    /**
     * @return the volume
     */
    public int getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * @return the dateOffset
     */
    public long getDateOffset() {
        return dateOffset;
    }

    /**
     * @param dateOffset the dateOffset to set
     */
    public void setDateOffset(long dateOffset) {
        this.dateOffset = dateOffset;
    }

}
