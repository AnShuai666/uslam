package com.ubtrobot.uslam.utils;

public class Line {
    private Point mStartPoint;
    private Point mEndPoint;

    public Line(Point startPoint, Point endPoint) {
        this.mStartPoint = startPoint;
        this.mEndPoint = endPoint;
    }

    public Line(float startX, float startY, float endX, float endY) {
        mStartPoint = new Point(startX, startY);
        mEndPoint = new Point(endX, endY);
    }

    public float[] getPts() {
        return new float[]{mStartPoint.x, mStartPoint.y, mEndPoint.x, mEndPoint.y};
    }

    public Point getStartPoint() {
        return mStartPoint;
    }

    public Point getEndPoint() {
        return mEndPoint;
    }

    @Override
    public String toString() {
        return String.format("{start: %s, end: %s}", mStartPoint.toString(), mEndPoint.toString());
    }

    public boolean tooSmall(float distance) {
        return mStartPoint.simpleNear(mEndPoint, distance);
    }
}
