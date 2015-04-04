package edu.uci.ics.hyracks.api.util;

public class StopWatch {
    private long startTime = 0;
    private long stopTime = 0;
    private long elapsedTime = 0;

    public void start() {
        elapsedTime = 0;
        startTime = System.currentTimeMillis();
    }

    public void stop() {
        stopTime = System.currentTimeMillis();
        elapsedTime += stopTime - startTime;
    }

    public void resume() {
        startTime = System.currentTimeMillis();
    }

    //elaspsed time in milliseconds
    public long getElapsedTime() {
        return elapsedTime;
    }

    //elaspsed time in seconds
    public long getElapsedTimeSecs() {
        return elapsedTime / 1000;
    }
}