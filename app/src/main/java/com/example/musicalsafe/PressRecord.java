package com.example.musicalsafe;

public class PressRecord {

    private float touchX;
    private float touchY;
    private long startTime;
    private long elapsedTime;

    public PressRecord(float inTouchX, float inTouchY, long inStartTime) {
        touchX = inTouchX;
        touchY = inTouchY;
        startTime = inStartTime;
    }

    public void endTime(long inEndTime) {
        elapsedTime = System.currentTimeMillis() - startTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }
}
