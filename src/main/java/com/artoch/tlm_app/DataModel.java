package com.artoch.tlm_app;

import javafx.beans.property.*;

public class DataModel {

    private final LongProperty counter;
    private final StringProperty time;
    private final DoubleProperty sinus;
    private final IntegerProperty checksum;
    private final BooleanProperty checksumStatus;
    private final StringProperty highlightColour;

    public DataModel(long counter, String time, double sinus, int checksum, boolean checksumStatus,
                     String highlightColour) {
        this.counter = new SimpleLongProperty(counter);
        this.time = new SimpleStringProperty(time);
        this.sinus = new SimpleDoubleProperty(sinus);
        this.checksum = new SimpleIntegerProperty(checksum);
        this.checksumStatus = new SimpleBooleanProperty(checksumStatus);
        this.highlightColour = new SimpleStringProperty(highlightColour);
    }

    public Long getCounter() {
        return counter.get();
    }

    public void setCounter(long counter) {
        this.counter.set(counter);
    }

    public String getTime() {
        return time.get();
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public double getSinus() {
        return sinus.get();
    }

    public void setSinus(double sinus) {
        this.sinus.set(sinus);
    }

    public int getChecksum() {
        return checksum.get();
    }

    public void setChecksum(int checksum) {
        this.checksum.set(checksum);
    }

    public boolean getChecksumStatus() {
        return checksumStatus.get();
    }

    public void setCheckSumStatus(boolean checksumStatus) {
        this.checksumStatus.set(checksumStatus);
    }

    public String getHighlightColour() {
        return highlightColour.get();
    }

    public void setHighlightColour(String highlightColour) {
        this.highlightColour.set(highlightColour);
    }

}
