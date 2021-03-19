package com.mcubes.model;

import java.util.Arrays;

public class Instruction {

    private int line;
    private String keyword;
    private String[] param;
    private String condition;

    
    public Instruction(int line, String keyword, String... param) {
        this.line = line;
        this.keyword = keyword;
        this.param = param;
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

    @Override
    public String toString() {
        return "Instruction{" +
                "line=" + line +
                ", keyword='" + keyword + '\'' +
                ", param=" + Arrays.toString(param) +
                '}';
    }
}
