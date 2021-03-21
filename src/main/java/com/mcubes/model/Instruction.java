package com.mcubes.model;

import java.util.Arrays;

public class Instruction {

    private int line;
    private String keyword;
    private String condition;
    //private int conditionNumber;
    private int conditionalBlockStart;
    private int nextCheckPoint;
    private int conditionalBlockEnd;
    private String[] param;


    public Instruction(int line, String keyword) {
        this.line = line;
        this.keyword = keyword;
    }

    public Instruction(int line, String keyword, String... param) {
        this(line, keyword);
        this.param = param;
    }

    public Instruction(int line, String keyword, String condition,
                       //int conditionNumber,
                       int conditionalBlockStart,
                       int nextCheckPoint,
                       int conditionalBlockEnd) {
        this(line, keyword);
        this.condition = condition;
        //this.conditionNumber = conditionNumber;
        this.conditionalBlockStart = conditionalBlockStart;
        this.nextCheckPoint = nextCheckPoint;
        this.conditionalBlockEnd = conditionalBlockEnd;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String[] getParam() {
        return param;
    }

    public void setParam(String[] param) {
        this.param = param;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

//    public int getConditionNumber() {
//        return conditionNumber;
//    }

//    public void setConditionNumber(int conditionNumber) {
//        this.conditionNumber = conditionNumber;
//    }

    public int getConditionalBlockStart() {
        return conditionalBlockStart;
    }

    public void setConditionalBlockStart(int conditionalBlockStart) {
        this.conditionalBlockStart = conditionalBlockStart;
    }

    public int getConditionalBlockEnd() {
        return conditionalBlockEnd;
    }

    public void setConditionalBlockEnd(int conditionalBlockEnd) {
        this.conditionalBlockEnd = conditionalBlockEnd;
    }

    public int getNextCheckPoint() {
        return nextCheckPoint;
    }

    public void setNextCheckPoint(int nextCheckPoint) {
        this.nextCheckPoint = nextCheckPoint;
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "line=" + line +
                ", keyword='" + keyword + '\'' +
                ", condition='" + condition + '\'' +
                ", conditionalBlockStart=" + conditionalBlockStart +
                ", nextCheckPoint=" + nextCheckPoint +
                ", conditionalBlockEnd=" + conditionalBlockEnd +
                ", param=" + Arrays.toString(param) +
                '}';
    }
}
