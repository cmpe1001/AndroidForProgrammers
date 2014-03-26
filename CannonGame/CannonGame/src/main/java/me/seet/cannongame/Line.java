package me.seet.cannongame;

import android.graphics.Point;

// Line.java
// Class Line represents a line with two endpoints
public class Line {
    private Point mStart = new Point();   // start Point--(0,0) by default
    private Point mEnd = new Point();   // start Point--(0,0) by default

    public Point getStart() {
        return mStart;
    }

    public void setStart(Point mStart) {
        this.mStart = mStart;
    }

    public Point getEnd() {
        return mEnd;
    }

    public void setEnd(Point mEnd) {
        this.mEnd = mEnd;
    }
}

