package com.mcubes.model;

public class LineShiftThreshold {
    private int shiftWhenLine;
    private int shiftToLine;

    public LineShiftThreshold(int shiftWhenLine, int shiftToLine) {
        this.shiftWhenLine = shiftWhenLine;
        this.shiftToLine = shiftToLine;
    }

    public int getShiftWhenLine() {
        return shiftWhenLine;
    }

    public void setShiftWhenLine(int shiftWhenLine) {
        this.shiftWhenLine = shiftWhenLine;
    }

    public int getShiftToLine() {
        return shiftToLine;
    }

    public void setShiftToLine(int shiftToLine) {
        this.shiftToLine = shiftToLine;
    }
}
